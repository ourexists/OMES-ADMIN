/**
 * 科技大屏主题：从 document 上已加载的 equip_screen_tech_theme.css 变量读取色值，
 * 供 ECharts、地图点位等与 CSS 保持单一数据源。
 */
(function (global) {
    'use strict';

    function rootEl() {
        return document && document.documentElement;
    }

    /**
     * 读取单个 CSS 变量（trim），需在 theme.css 生效后调用（页面脚本放 body 底部即可）。
     * @param {string} name 如 '--chart-axis'
     * @returns {string}
     */
    function getTechScreenCssVar(name) {
        var el = rootEl();
        if (!el) return '';
        var v = getComputedStyle(el).getPropertyValue(name);
        return v ? String(v).trim() : '';
    }

    /**
     * 图表/地图用的状态色与面积色（名与 equip_screen_tech_theme.css 中变量对应）。
     * @returns {typeof DEFAULT_TECH_COLORS}
     */
    function getTechScreenColors() {
        return {
            online: getTechScreenCssVar('--color-status-online'),
            offline: getTechScreenCssVar('--color-status-offline'),
            alarm: getTechScreenCssVar('--color-status-alarm'),
            running: getTechScreenCssVar('--color-status-running'),
            stopped: getTechScreenCssVar('--color-status-stopped'),
            runningArea: getTechScreenCssVar('--color-area-running'),
            onlineArea: getTechScreenCssVar('--color-area-online'),
            alarmArea: getTechScreenCssVar('--color-area-alarm'),
        };
    }

    var DEFAULT_TECH_COLORS = {
        online: '#00d8ff',
        offline: '#8a93a1',
        alarm: '#ff5a5a',
        running: '#41d57a',
        stopped: '#ffcc00',
        runningArea: 'rgba(65,213,122,.2)',
        onlineArea: 'rgba(0,216,255,.22)',
        alarmArea: 'rgba(255,90,90,.22)',
    };

    /**
     * 与 getTechScreenColors 相同，但若某变量未定义则回退默认，避免图表无色。
     */
    function getTechScreenColorsSafe() {
        var c = getTechScreenColors();
        var out = {};
        var k;
        for (k in DEFAULT_TECH_COLORS) {
            if (Object.prototype.hasOwnProperty.call(DEFAULT_TECH_COLORS, k)) {
                out[k] = c[k] || DEFAULT_TECH_COLORS[k];
            }
        }
        return out;
    }

    global.getTechScreenCssVar = getTechScreenCssVar;
    global.getTechScreenColors = getTechScreenColors;
    global.getTechScreenColorsSafe = getTechScreenColorsSafe;
})(typeof window !== 'undefined' ? window : this);
