function SelectMap(options = {}) {
    let container = options.container || "map";
    let map;
    let marker = null;

    const onSetPointComplete = options.onSetPointComplete || function () {
    };

    const onReverseAddressComplete = options.onReverseAddressComplete || function () {
    };

    function SearchControl(options) {
        this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT; // 右上角
        this.defaultOffset = new BMapGL.Size(10, 30);
        this.onSelect = options?.onSelect;
    }

    SearchControl.prototype = new BMapGL.Control();

    SearchControl.prototype.initialize = function (map) {
        let div = document.createElement("div");
        div.className = 'map-search';
        div.innerHTML =
            `<div class="layui-form-item">
            <div class="layui-input-inline">
                <input type="text" class="layui-input searchText">
            </div>
            <button type="button" class="layui-btn layui-btn-radius layui-btn-normal btnSearch">
            <i class="layui-icon layui-icon-search"></i>
            </button>
        </div>`;

        // 搜索对象
        let localSearch = new BMapGL.LocalSearch(map, {
            onSearchComplete: function (results) {
                if (!results || results.getCurrentNumPois() === 0) {
                    layui.layer.msg('未找到位置');
                    return;
                }
                let poi = results.getPoi(0);
                handleSelect(poi);
            }
        });

        let input = div.querySelector('.searchText');
        let btn = div.querySelector('.btnSearch');

        let ac = new BMapGL.Autocomplete({
            input: input,
            location: map
        });

        ac.addEventListener("onconfirm", function (e) {
            let v = e.item.value;
            let keyword = v.province + v.city + v.district + v.street + v.business;
            input.value = keyword;
            localSearch.search(keyword);
        });

        btn.onclick = doSearch;

        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                doSearch();
            }
        });

        function doSearch() {
            let keyword = input.value.trim();
            if (!keyword) {
                layui.layer.msg('请输入搜索内容');
                return;
            }
            localSearch.search(keyword);
        }

        function handleSelect(poi) {
            if (typeof this.onSelect === 'function') {
                this.onSelect(poi);
            }
        }

        // 绑定 this
        handleSelect = handleSelect.bind(this);

        map.getContainer().append(div);
        return div;
    };


    function init() {
        let ln = [32.060255, 118.796877];
        map = new BMapGL.Map(container);
        map.centerAndZoom(new BMapGL.Point(ln[1], ln[0]), 6);
        map.enableScrollWheelZoom(true);

        map.setMapStyleV2({
            styleId: '68689e65b1c93202c41642c4fafae1e9'
        });

        // 点击地图选点
        map.addEventListener('click', function (e) {
            setPoint(e.latlng.lat, e.latlng.lng);
            reverseAddress(e.latlng.lat, e.latlng.lng);
        });

        // 搜索控件
        let searchControl = new SearchControl({
            onSelect: function (poi) {
                setPoint(poi.point.lat, poi.point.lng);
                layui.form.val('common-edit-form', {address: poi.title});
            }
        });
        map.addControl(searchControl);
    }

    function setPoint(lat, lng) {
        lat = Number(lat).toFixed(6);
        lng = Number(lng).toFixed(6);

        var point = new BMapGL.Point(lng, lat);

        map.centerAndZoom(point, 15);

        if (marker) {
            map.removeOverlay(marker);
        }
        marker = new BMapGL.Marker(point);
        map.addOverlay(marker);
        onSetPointComplete(lat, lng)
    }

    function reverseAddress(lat, lng) {
        let point = new BMapGL.Point(lng, lat);
        let geoc = new BMapGL.Geocoder();

        geoc.getLocation(point, function (rs) {
            if (!rs || !rs.address) {
                return;
            }
            onReverseAddressComplete(rs.address)
        });
    }

    return {
        init,
        setPoint,
        getMap: () => map
    };
}

function GISMap(options = {}) {
    let container = options.container || "map";
    let map;
    let markerMap = {};
    let iconSize = 32;

    const createPopupHtml = options.createPopupHtml || function () {
    };
    const createIconUri = options.createIconUri || function () {
    };

    function LegendControl() {
        this.defaultAnchor = BMAP_ANCHOR_TOP_LEFT; // 左上角
        this.defaultOffset = new BMapGL.Size(10, 10); // 偏移量
    }

    LegendControl.prototype = new BMapGL.Control();

    LegendControl.prototype.initialize = function (map) {
        const div = document.createElement('div');
        div.className = 'map-legend';
        div.innerHTML = `
        <div class="legend-item"><span class="status-dot status-online"></span> 在线</div>
        <div class="legend-item"><span class="status-dot status-offline"></span> 离线</div>
        <div class="legend-item"><span class="status-dot status-running"></span> 运行</div>
        <div class="legend-item"><span class="status-dot status-stopped"></span> 停止</div>
        <div class="legend-item"><span class="status-dot status-alarm"></span> 报警</div>
    `;

        map.getContainer().appendChild(div);
        return div;
    };

    function WorkshopControl() {
        this.defaultAnchor = BMAP_ANCHOR_TOP_RIGHT; // 右上角
        this.defaultOffset = new BMapGL.Size(10, 10); // 偏移量
    }

    WorkshopControl.prototype = new BMapGL.Control();

    WorkshopControl.prototype.initialize = function (map) {
        const div = document.createElement('div');
        div.className = 'map-legend';
        div.innerHTML = `<div class="workshopName" style="font-size: 16px; color: #0078d7"></div>`;
        map.getContainer().appendChild(div);
        return div;
    };

    function init() {
        map = new BMapGL.Map(container);
        map.enableScrollWheelZoom(true);
        const center = new BMapGL.Point(118.796877, 32.060255);
        map.centerAndZoom(center, 6);
        map.setMapStyleV2({
            styleId: '68689e65b1c93202c41642c4fafae1e9'
        });

        map.addControl(new BMapGL.ScaleControl());
        map.addControl(new BMapGL.ZoomControl());
        map.addControl(new BMapGL.CopyrightControl());


        const legend = new LegendControl();
        const workshop = new WorkshopControl();

        map.addControl(legend);
        map.addControl(workshop);

        map.addEventListener("zoomend", () => {
            const zoom = map.getZoom();
            if (zoom < 8) {
                hideDeviceMarkers();
            } else {
                showDeviceMarkers();
            }
        });
    }

    function hideDeviceMarkers() {
        Object.values(markerMap).forEach(marker => {
            marker.hide();
        });
    }

    function showDeviceMarkers() {
        Object.values(markerMap).forEach(marker => {
            marker.show();
        });
    }

    function adjustMapView(devices) {
        if (!devices || devices.length === 0) return;

        const points = devices.map(d => {
            return new BMapGL.Point(d.lng, d.lat);
        });

        map.setViewport(points);
    }

    function offsetPoints(lng, lat, index, total) {
        if (total === 1) {
            return {lng, lat};
        }

        const angle = (2 * Math.PI / total) * index;
        const offset = 0.00005; // 偏移半径（约 15m，可调）

        return {
            lng: lng + offset * Math.cos(angle),
            lat: lat + offset * Math.sin(angle)
        };
    }

    function groupByLngLat(devices) {
        const map = {};
        devices.forEach(d => {
            const key = `${d.lng}_${d.lat}`;
            if (!map[key]) map[key] = [];
            map[key].push(d);
        });
        return map;
    }

    function buildIcon(d) {
        return new BMapGL.Icon(
            createIconUri(d),
            // `/static/img/map/map_${status}_${type}.svg`,
            new BMapGL.Size(iconSize, iconSize),
            {
                // 图标中心点（底部居中）
                anchor: new BMapGL.Size(16, 32),
            }
        );
    }

    function refreshMarkers(devices) {
        const groups = groupByLngLat(devices);
        const newKeys = new Set();
        Object.values(groups).forEach(group => {
            group.forEach(function (d, index) {
                const key = d.id; // 或经纬度组合: `${d.lat},${d.lng}`
                newKeys.add(key);

                const p = offsetPoints(d.lng, d.lat, index, group.length);
                const point = new BMapGL.Point(p.lng, p.lat);

                if (markerMap[key]) {
                    // 已有 Marker → 更新状态 / Popup
                    markerMap[key].setPosition(point);
                    markerMap[key].setIcon(buildIcon(d));
                } else {
                    marker = new BMapGL.Marker(point, {
                        icon: buildIcon(d)
                    });

                    let r = createPopupHtml(d);
                    var opts = {
                        width: 250,     // 信息窗口宽度
                        height: 100,    // 信息窗口高度
                        title: r.title  // 信息窗口标题
                    }
                    let infoWindow = new BMapGL.InfoWindow(r.body, opts);
                    marker.addEventListener("click", () => {
                        map.openInfoWindow(
                            infoWindow,
                            point
                        );
                    });
                    map.addOverlay(marker);
                    markerMap[key] = marker;
                }
            })
        });
        // 移除已不存在的 Marker
        Object.keys(markerMap).forEach(key => {
            if (!newKeys.has(key)) {
                map.removeOverlay(markerMap[key]);
                delete markerMap[key];
            }
        });
    }

    return {
        init,
        adjustMapView,
        refreshMarkers,
        getMap: () => map
    };
}




