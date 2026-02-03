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
        map = L.map(container).setView([32.060255, 118.796877], 6);
        map.on('dragstart zoomstart', function () {
            userInteracting = true;
        });

        map.on('dragend zoomend', function () {
            userInteracting = false;
        });
        legendControl().addTo(map);
        workshopControl().addTo(map);

        L.tileLayer('https://{s}.tile.openstreetmap.fr/hot/{z}/{x}/{y}.png', {
            attribution: '地图数据 © OpenStreetMap'
        }).addTo(map);

        clusterGroup = L.markerClusterGroup();
        map.addLayer(clusterGroup);
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
                // 新 Marker
                const marker = L.marker([d.lat, d.lng], {icon: iconByStatus(d)});
                marker.bindPopup(generateContext(d));
                clusterGroup.addLayer(marker);
                markerMap[key] = marker;
            }
        })

        // 移除已不存在的 Marker
        Object.keys(markerMap).forEach(key => {
            if (!newKeys.has(key)) {
                clusterGroup.removeLayer(markerMap[key]);
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
        // 更新 icon
        marker.setIcon(iconByStatus(device));


        // 更新 Popup
        marker.setPopupContent(generateContext(context));
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