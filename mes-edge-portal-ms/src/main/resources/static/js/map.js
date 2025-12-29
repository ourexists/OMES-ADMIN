layui.use([], function () {

    // 1. 初始化地图
    var map = L.map('map').setView([32.060255, 118.796877], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    }).addTo(map);

    // 2. 模拟设备数据（实际可换成 Ajax 接口）
    var devices = [
        {
            id: "DEV-001",
            name: "1号泵站",
            lat: 31.4609636,
            lng: 121.0954240,
            status: "online"
        }
    ];

    // 3. 不同状态的图标
    var onlineIcon = L.icon({
        iconUrl: 'https://cdn-icons-png.flaticon.com/512/190/190411.png',
        iconSize: [32, 32]
    });

    var offlineIcon = L.icon({
        iconUrl: 'https://cdn-icons-png.flaticon.com/512/190/190406.png',
        iconSize: [32, 32]
    });

    // 4. 渲染设备点位
    devices.forEach(function (d) {
        var marker = L.marker([d.lat, d.lng], {
            icon: d.status === 'online' ? onlineIcon : offlineIcon
        }).addTo(map);

        marker.bindPopup(`
      <b>${d.name}</b><br/>
      设备编号：${d.id}<br/>
      状态：${d.status === 'online' ? '在线' : '离线'}
    `);
    });


});