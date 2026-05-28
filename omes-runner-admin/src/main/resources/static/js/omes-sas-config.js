/*
 * OAuth2、验证码请求前缀（OMES-SAS，默认与 Admin 同主机、端口 10012）。
 * 部署时可先设置：window.OMES_SAS_BASE = 'http://192.168.x.x:10012'
 */
(function () {
    if (typeof window !== 'undefined' && window.OMES_SAS_BASE) {
        return;
    }
    var loc = typeof window !== 'undefined' ? window.location : null;
    if (!loc || !loc.hostname) {
        window.OMES_SAS_BASE = 'http://127.0.0.1:10012';
        return;
    }
    window.OMES_SAS_BASE = loc.protocol + '//' + loc.hostname + ':10012';
})();
