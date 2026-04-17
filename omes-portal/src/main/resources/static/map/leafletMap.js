function SelectMap(options = {}) {
    let container = options.container || "map";
    let map;
    let marker;

    const onSetPointComplete = options.onSetPointComplete || function () {
    };

    const onReverseAddressComplete = options.onReverseAddressComplete || function () {
    };

    const searchControl = function SearchControl() {
        let control = L.control({position: 'topright'});
        control.onAdd = function () {
            let div = L.DomUtil.create('div', 'map-legend');
            div.innerHTML = `<div class="layui-form-item">
                        <div class="layui-input-inline">
                            <input type="text" class="layui-input searchText">
                        </div>
                        <button type="button" class="layui-btn layui-btn-radius layui-btn-normal btnSearch">
                            <i class="layui-icon layui-icon-search"></i>
                        </button>
                    </div>`;

            L.DomEvent.disableClickPropagation(div);
            L.DomEvent.disableScrollPropagation(div);

            const input = div.querySelector('.searchText');
            let btn = div.querySelector('.btnSearch');
            btn.onclick = function () {
                let keyword = input.value.trim();
                if (!keyword) {
                    layui.layer.msg('请输入搜索内容');
                    return;
                }
                searchLocation(keyword);
            };
            return div;
        };
        return control;
    }

    function init() {
        map = L.map(container).setView([32.060255, 118.796877], 6);
        searchControl().addTo(map);
        L.tileLayer('https://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
            attribution: '地图数据 © OpenStreetMap'
        }).addTo(map);

        map.on('click', function (e) {
            setPoint(e.latlng.lat, e.latlng.lng);
            reverseAddress(e.latlng.lat, e.latlng.lng)
        });
    }

    function setPoint(lat, lng) {
        lat = Number(lat).toFixed(6);
        lng = Number(lng).toFixed(6);
        map.setView([lat, lng], 15);
        if (marker) {
            map.removeLayer(marker);
        }
        marker = L.marker([lat, lng]).addTo(map);
        onSetPointComplete(lat, lng);
    }

    function reverseAddress(lat, lng) {
        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
            .then(res => res.json())
            .then(data => {
                if (!data || !data.display_name) {
                    return;
                }
                onReverseAddressComplete(data.display_name);
            })
            .catch(() => {
                layer.msg('地址解析失败');
            });
    }

    function searchLocation(keyword) {
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(keyword)}`)
            .then(res => res.json())
            .then(list => {
                if (!list || list.length === 0) {
                    layer.msg('未找到位置');
                    return;
                }
                let p = list[0];
                setPoint(p.lat, p.lon);
                onReverseAddressComplete(p.display_name);
            });
    }

    return {
        init, setPoint, getMap: () => map
    };
}

function GISMap(options = {}) {
    let container = options.container || "map";
    let map;
    let markerMap = {};
    let iconSize = 32;
    let clusterGroup;
    /** 无聚合时用于承载 marker 的图层 */
    let markerLayerGroup;
    /** 聚合组或非聚合的 layerGroup，供 addLayer/removeLayer */
    let markerParent;
    let userInteracting = false;
    const useMarkerCluster = options.useMarkerCluster !== false;
    /** true 时使用 L.circleMarker 矢量圆点，替代图片 Marker */
    const useCircleMarkers = !!options.useCircleMarkers;
    const getCircleMarkerStyle = typeof options.getCircleMarkerStyle === 'function'
        ? options.getCircleMarkerStyle
        : null;
    /** 与 useCircleMarkers 同时开启：DivIcon + 水波纹动画（类似 ECharts effectScatter） */
    const circleMarkerRipple = !!options.circleMarkerRipple && useCircleMarkers;
    const tileUrl = options.tileUrl || 'https://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png';
    const tileAttribution = options.tileAttribution || '地图数据 © OpenStreetMap';
    const tileSubdomains = options.tileSubdomains != null ? options.tileSubdomains : 'abc';
    const tileMaxZoom = options.maxZoom != null ? options.maxZoom : 19;
    const initialCenter = options.initialCenter || [32.060255, 118.796877];
    const initialZoom = options.initialZoom != null ? options.initialZoom : 6;
    const showLegend = options.showLegend !== false;
    const showWorkshopControl = options.showWorkshopControl !== false;

    const createPopupHtml = options.createPopupHtml || function () {
    };
    const createIconUri = options.createIconUri || function () {
    };

    const legendControl = function LegendControl() {
        let control = L.control({
            position: 'topleft'
        })
        control.onAdd = function () {
            let div = L.DomUtil.create('div', 'map-legend');
            div.innerHTML = `
                <div class="legend-item">
                    <span class="status-dot status-online"></span> 在线
                </div>
                  <div class="legend-item">
                    <span class="status-dot status-offline"></span> 离线
                </div>
                <div class="legend-item">
                    <span class="status-dot status-running"></span> 运行
                </div>
                <div class="legend-item">
                    <span class="status-dot status-stopped"></span> 停止
                </div>
                <div class="legend-item">
                    <span class="status-dot status-alarm"></span> 报警
                </div>
            `;
            return div;
        }
        return control
    }

    const workshopControl = function WorkshopControl() {
        let control = L.control({
            position: 'topright'
        });
        control.onAdd = function () {
            let div = L.DomUtil.create('div', 'map-legend');
            div.innerHTML = `
                <div class="workshopName" style="font-size: 16px; color: #0078d7"></div>
            `;
            return div;
        };
        return control;
    }

    function init() {
        map = L.map(container).setView(initialCenter, initialZoom);
        map.on('dragstart zoomstart', function () {
            userInteracting = true;
        });

        map.on('dragend zoomend', function () {
            userInteracting = false;
        });
        if (showLegend) {
            legendControl().addTo(map);
        }
        if (showWorkshopControl) {
            workshopControl().addTo(map);
        }

        L.tileLayer(tileUrl, {
            attribution: tileAttribution,
            subdomains: tileSubdomains,
            maxZoom: tileMaxZoom
        }).addTo(map);

        if (useMarkerCluster) {
            clusterGroup = L.markerClusterGroup();
            markerParent = clusterGroup;
            map.addLayer(clusterGroup);
        } else {
            markerLayerGroup = L.layerGroup();
            markerParent = markerLayerGroup;
            map.addLayer(markerLayerGroup);
        }
    }

    function adjustMapView(devices) {
        if (!devices || devices.length === 0) return;

        if (devices.length === 1) {
            map.setView([devices[0].lat, devices[0].lng], 15);
            return;
        }

        const allSame = devices.every(d => d.lat === devices[0].lat && d.lng === devices[0].lng);

        if (allSame) {
            map.setView([devices[0].lat, devices[0].lng], 15);
            return;
        }

        const bounds = L.latLngBounds(devices.map(d => [d.lat, d.lng]));

        map.fitBounds(bounds, {
            padding: [40, 40], maxZoom: 17
        });
    }

    function defaultCircleStyle() {
        return {
            radius: 8,
            fillColor: '#22d3ee',
            color: 'rgba(226, 232, 240, 0.55)',
            weight: 2,
            fillOpacity: 0.92,
            opacity: 1
        };
    }

    function circleStyleFromDevice(d) {
        return getCircleMarkerStyle ? getCircleMarkerStyle(d) : defaultCircleStyle();
    }

    function rippleDivIconFromDevice(d) {
        const st = circleStyleFromDevice(d);
        const r = Number(st.radius) || 8;
        const fill = String(st.fillColor || '#38bdf8');
        const stroke = String(st.color || 'rgba(15,23,42,0.9)');
        const dPx = Math.max(10, Math.round(r * 2));
        const box = Math.max(56, Math.round(dPx * 4.2));
        const anchor = Math.round(box / 2);
        const glow = '0 0 14px ' + fill;
        const waveStyle = 'width:' + dPx + 'px;height:' + dPx + 'px;border:2px solid ' + fill + ';';
        const core = '<span class="map-ripple-dot-core" style="width:' + dPx + 'px;height:' + dPx + 'px;background:' + fill + ';border:2px solid ' + stroke + ';box-shadow:' + glow + '"></span>';
        const w1 = '<span class="map-ripple-dot-wave" style="' + waveStyle + '"></span>';
        const w2 = '<span class="map-ripple-dot-wave map-ripple-dot-wave-d2" style="' + waveStyle + '"></span>';
        const html = '<div class="map-ripple-dot-root" style="width:' + box + 'px;height:' + box + 'px">' + w1 + w2 + core + '</div>';
        return L.divIcon({
            className: 'map-ripple-div-icon',
            html: html,
            iconSize: [box, box],
            iconAnchor: [anchor, anchor],
            popupAnchor: [0, -Math.max(12, anchor - Math.round(dPx * 0.35))]
        });
    }

    function createPointLayer(d) {
        if (useCircleMarkers && circleMarkerRipple) {
            const marker = L.marker([d.lat, d.lng], {
                icon: rippleDivIconFromDevice(d),
                zIndexOffset: 350
            });
            marker.bindPopup(generateContext(d));
            return marker;
        }
        if (useCircleMarkers) {
            const marker = L.circleMarker([d.lat, d.lng], circleStyleFromDevice(d));
            marker.bindPopup(generateContext(d));
            return marker;
        }
        const marker = L.marker([d.lat, d.lng], {icon: iconByStatus(d)});
        marker.bindPopup(generateContext(d));
        return marker;
    }

    function refreshMarkers(devices) {
        const newKeys = new Set();
        devices.forEach(function (d) {
            const key = d.id; // 或经纬度组合: `${d.lat},${d.lng}`
            newKeys.add(key);

            if (markerMap[key]) {
                // 已有 Marker → 更新状态 / Popup
                const marker = markerMap[key];
                updateMarker(marker, d);
            } else {
                const marker = createPointLayer(d);
                markerParent.addLayer(marker);
                markerMap[key] = marker;
            }
        })

        // 移除已不存在的 Marker
        Object.keys(markerMap).forEach(key => {
            if (!newKeys.has(key)) {
                markerParent.removeLayer(markerMap[key]);
                delete markerMap[key];
            }
        });
    }

    function generateContext(device) {
        let r = createPopupHtml(device);
        let context = r.body;
        if (r.title) {
            context = r.title + "</br>" + r.body;
        }
        return context;
    }

    function updateMarker(marker, device) {
        if (useCircleMarkers && circleMarkerRipple) {
            marker.setLatLng([device.lat, device.lng]);
            marker.setIcon(rippleDivIconFromDevice(device));
        } else if (useCircleMarkers) {
            marker.setLatLng([device.lat, device.lng]);
            marker.setStyle(circleStyleFromDevice(device));
        } else {
            marker.setIcon(iconByStatus(device));
        }
        marker.setPopupContent(generateContext(device));
    }

    function iconByStatus(device) {
        return L.icon({
            iconUrl: createIconUri(device),
            iconSize: [iconSize, iconSize],
            iconAnchor: [16, 32]
        });
    }

    return {
        init,
        adjustMapView,
        refreshMarkers,
        getMap: () => map
    };
}