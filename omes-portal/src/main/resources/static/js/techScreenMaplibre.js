/**
 * 科技大屏 MapLibre GL 封装（equip_screen_tech 专用）
 * 依赖全局 maplibregl（CDN 或本地 dist）
 */
function TechScreenMaplibre(options) {
    options = options || {};
    const containerId = options.container || 'map';
    const styleUrl = options.styleUrl || 'https://demotiles.maplibre.org/style.json';
    const initialCenter = options.initialCenter || [121.13, 31.45];
    const initialZoom = options.initialZoom != null ? options.initialZoom : 11;
    const getPointStyle = typeof options.getPointStyle === 'function' ? options.getPointStyle : function () {
        return {radius: 6, color: '#1f6feb'};
    };
    const createPopupHtml = options.createPopupHtml || function () {
        return {title: '', body: ''};
    };
    /** 为 'zh' 时拉取 style.json 并改写注记字段，优先 OSM 中文 name:zh / name */
    const mapLanguage = options.mapLanguage || null;
    /** 使用矢量瓦片 building 层做 fill-extrusion + 相机 pitch（CARTO basemap 等含 carto/building 时有效） */
    const enable25dBuildings = !!options.enable25dBuildings;
    const buildingExtrusionColor = typeof options.buildingExtrusionColor === 'string'
        ? options.buildingExtrusionColor
        : '#1a3354';
    const buildingsSourceId = options.buildingsSourceId || 'carto';
    const buildingsSourceLayer = options.buildingsSourceLayer || 'building';
    const initialPitch = enable25dBuildings ? (Number(options.initialPitch) || 52) : 0;
    const initialBearing = enable25dBuildings ? (Number(options.initialBearing) || 22) : 0;
    const maxPitch = enable25dBuildings ? (Number(options.maxPitch) || 60) : 0;
    /** 设备点 ECharts effectScatter 风格扩散波纹 + 中心轻微闪烁 */
    const enableDeviceRipple = options.enableDeviceRipple !== false;
    const ripplePeriodMs = Number(options.ripplePeriodMs) > 0 ? Number(options.ripplePeriodMs) : 2200;
    const ripplePhaseOffset = typeof options.ripplePhaseOffset === 'number' ? options.ripplePhaseOffset : 0.52;
    /** 将 Carto Dark Matter 等做二次调色：'deepBlue' 深蓝大屏；'charcoal' 炭灰午夜（参考监控大屏浅对比路网） */
    const basemapTint = options.basemapTint || (options.deepBlueBasemap ? 'deepBlue' : null);

    let map = null;
    let deviceById = {};
    let mapLoaded = false;
    let pendingDevices = null;
    let activePopup = null;
    let rippleRafId = null;

    function closePopup() {
        if (activePopup) {
            activePopup.remove();
            activePopup = null;
        }
    }

    function buildPopupHtml(d) {
        const r = createPopupHtml(d);
        const body = (r.body != null ? r.body : '');
        const title = (r.title != null ? r.title : '');
        return '<div class="map-tooltip-wrap"><div class="map-tooltip-title">' + title + '</div><div class="map-tooltip-body">' + body + '</div></div>';
    }

    function onDeviceLayerClick(e) {
        const f = e.features && e.features[0];
        if (!f || !f.properties || f.properties.id == null) return;
        const d = deviceById[f.properties.id];
        if (!d) return;
        closePopup();
        activePopup = new maplibregl.Popup({
            offset: 14,
            closeButton: true,
            maxWidth: '320px',
            className: 'maplibre-tech-popup'
        })
            .setLngLat(e.lngLat)
            .setHTML(buildPopupHtml(d))
            .addTo(map);
    }

    function stopDeviceRipple() {
        if (rippleRafId != null) {
            cancelAnimationFrame(rippleRafId);
            rippleRafId = null;
        }
    }

    function radiusPlusSpread(spreadPx) {
        return ['+', ['get', 'radius'], spreadPx];
    }

    function tickDeviceRipple() {
        if (!map || !mapLoaded || !enableDeviceRipple) {
            stopDeviceRipple();
            return;
        }
        if (!map.getLayer('devices-ripple-a')) {
            stopDeviceRipple();
            return;
        }
        if (typeof document !== 'undefined' && document.hidden) {
            rippleRafId = requestAnimationFrame(tickDeviceRipple);
            return;
        }
        const now = performance.now();
        const period = ripplePeriodMs;

        function rippleAt(phaseShift) {
            const t = ((now % period) / period + phaseShift) % 1;
            const ease = 1 - Math.pow(1 - t, 2.15);
            const spread = 3 + ease * 32;
            const op = 0.58 * Math.pow(1 - t, 1.3);
            const blur = 0.1 + t * 0.85;
            return {spread: spread, op: op, blur: blur};
        }

        const a = rippleAt(0);
        const b = rippleAt(ripplePhaseOffset);
        map.setPaintProperty('devices-ripple-a', 'circle-radius', radiusPlusSpread(a.spread));
        map.setPaintProperty('devices-ripple-a', 'circle-opacity', a.op);
        map.setPaintProperty('devices-ripple-a', 'circle-blur', a.blur);
        map.setPaintProperty('devices-ripple-b', 'circle-radius', radiusPlusSpread(b.spread));
        map.setPaintProperty('devices-ripple-b', 'circle-opacity', b.op);
        map.setPaintProperty('devices-ripple-b', 'circle-blur', b.blur);

        rippleRafId = requestAnimationFrame(tickDeviceRipple);
    }

    function startDeviceRipple() {
        stopDeviceRipple();
        if (!enableDeviceRipple || !map || !map.getLayer('devices-ripple-a')) {
            return;
        }
        rippleRafId = requestAnimationFrame(tickDeviceRipple);
    }

    function wireDeviceLayers() {
        map.addSource('devices-geo', {
            type: 'geojson',
            data: {type: 'FeatureCollection', features: []}
        });
        if (enableDeviceRipple) {
            map.addLayer({
                id: 'devices-ripple-b',
                type: 'circle',
                source: 'devices-geo',
                filter: ['==', ['get', 'ripple'], 1],
                paint: {
                    'circle-radius': radiusPlusSpread(4),
                    'circle-color': ['get', 'color'],
                    'circle-opacity': 0,
                    'circle-blur': 0.75
                }
            });
            map.addLayer({
                id: 'devices-ripple-a',
                type: 'circle',
                source: 'devices-geo',
                filter: ['==', ['get', 'ripple'], 1],
                paint: {
                    'circle-radius': radiusPlusSpread(4),
                    'circle-color': ['get', 'color'],
                    'circle-opacity': 0,
                    'circle-blur': 0.75
                }
            });
        }
        map.addLayer({
            id: 'devices-halo',
            type: 'circle',
            source: 'devices-geo',
            paint: {
                'circle-radius': ['+', ['get', 'radius'], 6],
                'circle-color': ['get', 'color'],
                'circle-opacity': 0.52,
                'circle-blur': 0.9
            }
        });
        map.addLayer({
            id: 'devices',
            type: 'circle',
            source: 'devices-geo',
            paint: {
                'circle-radius': ['get', 'radius'],
                'circle-color': ['get', 'color'],
                'circle-stroke-width': 1.5,
                /* Dark Matter 深底上用细黑描边，贴近 CARTO 大屏点位效果 */
                'circle-stroke-color': 'rgba(0, 0, 0, 0.55)',
                'circle-opacity': 1
            }
        });
        map.on('click', 'devices', onDeviceLayerClick);
        map.on('mouseenter', 'devices', function () {
            map.getCanvas().style.cursor = 'pointer';
        });
        map.on('mouseleave', 'devices', function () {
            map.getCanvas().style.cursor = '';
        });
        map.on('click', function (e) {
            const feats = map.queryRenderedFeatures(e.point, {layers: ['devices']});
            if (!feats || feats.length === 0) {
                closePopup();
            }
        });
    }

    /** 在第一个 symbol 图层之下插入建筑挤出层，避免盖住地名；设备层后加，保证在最上 */
    function findFirstSymbolLayerId() {
        const layers = map.getStyle().layers || [];
        for (let i = 0; i < layers.length; i++) {
            if (layers[i].type === 'symbol') {
                return layers[i].id;
            }
        }
        return undefined;
    }

    function apply25dBuildings() {
        if (!map.getSource(buildingsSourceId)) {
            return;
        }
        ['building', 'building-top'].forEach(function (lid) {
            if (map.getLayer(lid)) {
                map.setLayoutProperty(lid, 'visibility', 'none');
            }
        });
        if (map.getLayer('building-extrusion')) {
            return;
        }
        const beforeId = findFirstSymbolLayerId();
        map.addLayer({
            id: 'building-extrusion',
            type: 'fill-extrusion',
            source: buildingsSourceId,
            'source-layer': buildingsSourceLayer,
            minzoom: 12,
            filter: ['all',
                ['any', ['!', ['has', 'hide_3d']], ['!=', ['get', 'hide_3d'], true]]
            ],
            paint: {
                'fill-extrusion-color': buildingExtrusionColor,
                'fill-extrusion-opacity': 0.9,
                'fill-extrusion-base': 0,
                'fill-extrusion-height': ['max', ['coalesce', ['get', 'render_height'], 0], 3]
            }
        }, beforeId);
    }

    function clamp01(x) {
        return Math.max(0, Math.min(1, x));
    }

    function parseCssColor(str) {
        if (typeof str !== 'string') return null;
        var s = str.trim();
        if (/^transparent$/i.test(s)) return {r: 0, g: 0, b: 0, a: 0};
        var m = /^#([0-9a-f]{3})$/i.exec(s);
        if (m) {
            var v = m[1];
            return {
                r: parseInt(v.charAt(0) + v.charAt(0), 16),
                g: parseInt(v.charAt(1) + v.charAt(1), 16),
                b: parseInt(v.charAt(2) + v.charAt(2), 16),
                a: 1
            };
        }
        m = /^#([0-9a-f]{6})$/i.exec(s);
        if (m) {
            var h = m[1];
            return {
                r: parseInt(h.slice(0, 2), 16),
                g: parseInt(h.slice(2, 4), 16),
                b: parseInt(h.slice(4, 6), 16),
                a: 1
            };
        }
        m = /^rgba?\(\s*([0-9.]+)\s*,\s*([0-9.]+)\s*,\s*([0-9.]+)\s*(?:,\s*([0-9.]+)\s*)?\)$/i.exec(s);
        if (m) {
            return {r: +m[1], g: +m[2], b: +m[3], a: m[4] != null ? +m[4] : 1};
        }
        return null;
    }

    function rgbToHsl(r, g, b) {
        r /= 255;
        g /= 255;
        b /= 255;
        var max = Math.max(r, g, b);
        var min = Math.min(r, g, b);
        var l = (max + min) / 2;
        var h = 0;
        var s = 0;
        if (max !== min) {
            var d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
            switch (max) {
                case r:
                    h = (g - b) / d + (g < b ? 6 : 0);
                    break;
                case g:
                    h = (b - r) / d + 2;
                    break;
                default:
                    h = (r - g) / d + 4;
            }
            h /= 6;
        }
        return {h: h * 360, s: s, l: l};
    }

    function hslToRgb(h, s, l) {
        h = (h % 360) / 360;
        var r;
        var g;
        var b;
        if (s === 0) {
            r = g = b = l;
        } else {
            var hue2rgb = function (p, q, t) {
                if (t < 0) t += 1;
                if (t > 1) t -= 1;
                if (t < 1 / 6) return p + (q - p) * 6 * t;
                if (t < 1 / 2) return q;
                if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
                return p;
            };
            var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            var p = 2 * l - q;
            r = hue2rgb(p, q, h + 1 / 3);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 1 / 3);
        }
        return {r: Math.round(r * 255), g: Math.round(g * 255), b: Math.round(b * 255)};
    }

    function formatCssColor(rgb, a) {
        if (a != null && a < 1) {
            var aa = Math.round(a * 1000) / 1000;
            return 'rgba(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ',' + aa + ')';
        }
        function hx(n) {
            var x = Math.max(0, Math.min(255, n));
            return ('0' + x.toString(16)).slice(-2);
        }
        return '#' + hx(rgb.r) + hx(rgb.g) + hx(rgb.b);
    }

    function isColorLikeString(s) {
        if (typeof s !== 'string') return false;
        var t = s.trim();
        if (/^transparent$/i.test(t)) return true;
        if (/^#([0-9a-f]{3}|[0-9a-f]{6})$/i.test(t)) return true;
        return /^rgba?\(/i.test(t);
    }

    function deepBlueTintString(colorStr, layerId) {
        var c = parseCssColor(colorStr);
        if (!c) return colorStr;
        if (/^transparent$/i.test(colorStr.trim())) return colorStr;
        if (c.a === 0) return colorStr;
        var hsl = rgbToHsl(c.r, c.g, c.b);
        var id = layerId || '';
        var nh;
        var ns;
        var nl;
        if (id === 'background') {
            return '#020b14';
        }
        if (/^water|^waterway/i.test(id)) {
            nh = 207;
            ns = Math.min(0.58, Math.max(0.28, 0.33 + hsl.s * 0.35));
            nl = clamp01(hsl.l * 0.86 + 0.025);
            nl = Math.min(0.46, Math.max(0.06, nl));
        } else if (hsl.l > 0.52) {
            nh = 210;
            ns = Math.min(0.2, Math.max(0.06, hsl.s + 0.1));
            nl = clamp01(hsl.l * 0.98);
            nl = Math.min(0.9, Math.max(0.52, nl));
        } else {
            nh = 218;
            if (hsl.s < 0.14) {
                ns = Math.min(0.42, 0.13 + (1 - hsl.l) * 0.38);
            } else {
                ns = Math.min(0.52, hsl.s * 1.1 + 0.05);
            }
            if (hsl.l < 0.045) {
                nl = Math.min(0.09, Math.max(0.035, hsl.l * 1.15 + 0.018));
            } else {
                nl = clamp01(hsl.l * 0.97 + 0.012);
                nl = Math.min(0.92, Math.max(0.04, nl));
            }
        }
        var out = hslToRgb(nh, ns, nl);
        return formatCssColor(out, c.a);
    }

    /** 炭灰 + 近黑路网 + 微蓝水体 + 浅灰注记（偏高德/大屏午夜风，非纯黑底） */
    function charcoalMidnightTintString(colorStr, layerId) {
        var c = parseCssColor(colorStr);
        if (!c) return colorStr;
        if (/^transparent$/i.test(colorStr.trim())) return colorStr;
        if (c.a === 0) return colorStr;
        var hsl = rgbToHsl(c.r, c.g, c.b);
        var id = layerId || '';
        var nh;
        var ns;
        var nl;
        if (id === 'background') {
            return '#1b1c21';
        }
        if (/halo|shield/i.test(id) && hsl.l < 0.42) {
            return formatCssColor(hslToRgb(220, 0.03, Math.min(0.07, Math.max(0.03, hsl.l * 0.85))), c.a);
        }
        if (/^water|^waterway|^ocean/i.test(id)) {
            nh = 215;
            ns = Math.min(0.14, Math.max(0.06, hsl.s * 0.45 + 0.05));
            nl = clamp01(0.1 + hsl.l * 0.22);
            nl = Math.min(0.2, Math.max(0.09, nl));
        } else if (hsl.l > 0.52 || /label|place|poi|housenumber|annotation|continent|country|state|city|town|village/i.test(id)) {
            nh = 218;
            ns = Math.min(0.05, Math.max(0.02, hsl.s * 0.12));
            nl = clamp01(0.7 + (hsl.l - 0.52) * 0.42);
            nl = Math.min(0.86, Math.max(0.72, nl));
        } else if (/motorway|trunk|primary|secondary|tertiary|road|street|path|rail|bridge|tunnel|transport|aeroway/i.test(id)) {
            nh = 228;
            ns = Math.min(0.08, hsl.s * 0.35 + 0.02);
            if (hsl.l < 0.22) {
                nl = Math.min(0.035, Math.max(0.012, hsl.l * 0.42));
            } else {
                nl = clamp01(hsl.l * 0.55);
                nl = Math.min(0.18, Math.max(0.04, nl));
            }
        } else {
            nh = 222;
            ns = Math.min(0.07, hsl.s * 0.3 + 0.02);
            nl = clamp01(hsl.l * 0.9 + 0.03);
            nl = Math.min(0.22, Math.max(0.075, nl));
        }
        var out2 = hslToRgb(nh, ns, nl);
        return formatCssColor(out2, c.a);
    }

    function mapPaintColorStrings(val, layerId, fn) {
        if (typeof val === 'string') {
            return isColorLikeString(val) ? fn(val, layerId) : val;
        }
        if (!val || typeof val !== 'object') return val;
        if (Array.isArray(val.stops)) {
            var o = {};
            for (var k in val) {
                if (Object.prototype.hasOwnProperty.call(val, k)) {
                    o[k] = val[k];
                }
            }
            o.stops = val.stops.map(function (stop) {
                return [stop[0], mapPaintColorStrings(stop[1], layerId, fn)];
            });
            return o;
        }
        if (Array.isArray(val)) {
            return val.map(function (item) {
                return mapPaintColorStrings(item, layerId, fn);
            });
        }
        return val;
    }

    /** 遍历 paint 中带 color 的字段，把灰中性色改为深蓝系（针对 Carto GL） */
    function patchStyleDeepBlueBasemap(style) {
        var s = JSON.parse(JSON.stringify(style));
        if (!s.layers) return s;
        s.layers.forEach(function (layer) {
            if (!layer.paint) return;
            Object.keys(layer.paint).forEach(function (pk) {
                if (pk === 'line-gradient' || pk.indexOf('pattern') >= 0) return;
                if (pk.indexOf('color') < 0 && pk !== 'background-color') return;
                layer.paint[pk] = mapPaintColorStrings(layer.paint[pk], layer.id, deepBlueTintString);
            });
        });
        return s;
    }

    function patchStyleCharcoalBasemap(style) {
        var s = JSON.parse(JSON.stringify(style));
        if (!s.layers) return s;
        s.layers.forEach(function (layer) {
            if (!layer.paint) return;
            Object.keys(layer.paint).forEach(function (pk) {
                if (pk === 'line-gradient' || pk.indexOf('pattern') >= 0) return;
                if (pk.indexOf('color') < 0 && pk !== 'background-color') return;
                layer.paint[pk] = mapPaintColorStrings(layer.paint[pk], layer.id, charcoalMidnightTintString);
            });
        });
        return s;
    }

    /** 将 CARTO/Mapbox 类 style 中的 {name_en}、{name} 等改为优先中文的表达式 */
    function patchStyleLayersForChinese(style) {
        var s = JSON.parse(JSON.stringify(style));
        var zhName = ['coalesce',
            ['get', 'name:zh'],
            ['get', 'name:zh-Hans'],
            ['get', 'name:zh-Hant'],
            ['get', 'name'],
            ['get', 'name:latin'],
            ['get', 'name_en'],
            ''];

        function cloneExpr(e) {
            return JSON.parse(JSON.stringify(e));
        }

        function patchTextField(tf) {
            if (typeof tf === 'string') {
                if (tf === '{name_en}' || tf === '{name}') {
                    return cloneExpr(zhName);
                }
                return tf;
            }
            /* zoom 函数的 stops 内不能嵌套表达式，只能整体换成 coalesce */
            if (tf && typeof tf === 'object' && Array.isArray(tf.stops)) {
                return cloneExpr(zhName);
            }
            if (Array.isArray(tf)) {
                return tf;
            }
            return tf;
        }

        if (!s.layers) return s;
        s.layers.forEach(function (layer) {
            if (!layer.layout || layer.layout['text-field'] === undefined) return;
            layer.layout['text-field'] = patchTextField(layer.layout['text-field']);
        });
        return s;
    }

    function createMapWithStyle(resolvedStyle) {
        stopDeviceRipple();
        if (map) {
            try {
                map.remove();
            } catch (e) { /* ignore */ }
            map = null;
        }
        map = new maplibregl.Map({
            container: containerId,
            style: resolvedStyle,
            center: initialCenter,
            zoom: initialZoom,
            pitch: initialPitch,
            bearing: initialBearing,
            maxPitch: maxPitch,
            attributionControl: true
        });

        map.on('load', function () {
            if (enable25dBuildings) {
                apply25dBuildings();
            }
            wireDeviceLayers();
            mapLoaded = true;
            if (enableDeviceRipple) {
                startDeviceRipple();
            }
            if (pendingDevices) {
                applyDevices(pendingDevices.list, pendingDevices.fit);
                pendingDevices = null;
            }
        });
    }

    function applyDevices(devices, needViewReset) {
        if (!map || !mapLoaded) return;
        deviceById = {};
        const list = devices || [];
        const features = list.map(function (d) {
            deviceById[d.id] = d;
            const st = getPointStyle(d);
            const r = Number(st.radius) || 9;
            const color = String(st.color || '#1f6feb');
            const ripple = st.ripple === true ? 1 : 0;
            return {
                type: 'Feature',
                geometry: {
                    type: 'Point',
                    coordinates: [Number(d.lng), Number(d.lat)]
                },
                properties: {
                    id: d.id,
                    radius: r,
                    color: color,
                    ripple: ripple
                }
            };
        });
        const fc = {type: 'FeatureCollection', features: features};
        map.getSource('devices-geo').setData(fc);

        if (needViewReset && list.length > 0) {
            if (list.length === 1) {
                map.jumpTo({
                    center: [Number(list[0].lng), Number(list[0].lat)],
                    zoom: 14,
                    duration: 0
                });
            } else {
                const bounds = new maplibregl.LngLatBounds();
                list.forEach(function (d) {
                    bounds.extend([Number(d.lng), Number(d.lat)]);
                });
                try {
                    map.fitBounds(bounds, {padding: 56, maxZoom: 16, duration: 0});
                } catch (e) { /* ignore */ }
            }
        }
    }

    function init() {
        if (typeof maplibregl === 'undefined') {
            return;
        }
        const el = document.getElementById(containerId);
        if (!el) return;

        mapLoaded = false;
        pendingDevices = null;

        var shouldFetchStyle = typeof styleUrl === 'string' && styleUrl.charAt(0) !== '{' &&
            (mapLanguage === 'zh' || basemapTint === 'deepBlue' || basemapTint === 'charcoal');
        if (shouldFetchStyle) {
            fetch(styleUrl)
                .then(function (r) {
                    return r.json();
                })
                .then(function (styleJson) {
                    var s = styleJson;
                    if (basemapTint === 'deepBlue') {
                        s = patchStyleDeepBlueBasemap(s);
                    } else if (basemapTint === 'charcoal') {
                        s = patchStyleCharcoalBasemap(s);
                    }
                    if (mapLanguage === 'zh') {
                        s = patchStyleLayersForChinese(s);
                    }
                    createMapWithStyle(s);
                })
                .catch(function () {
                    createMapWithStyle(styleUrl);
                });
        } else {
            createMapWithStyle(styleUrl);
        }
    }

    function setDevices(devices, needViewReset) {
        if (!mapLoaded) {
            pendingDevices = {list: devices || [], fit: !!needViewReset};
            return;
        }
        applyDevices(devices || [], !!needViewReset);
    }

    function resize() {
        if (map) {
            map.resize();
        }
    }

    function getMap() {
        return map;
    }

    return {
        init: init,
        setDevices: setDevices,
        resize: resize,
        getMap: getMap
    };
}
