/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

layui.define(["jquery", "i18n"], function (exports) {
    var $ = layui.jquery,
        i18n = layui.i18n;

    var i18np = {};

    function processLangI18n($scope) {
        $scope = $scope || $(document);
        $scope.find("[lang-i18n]").addBack("[lang-i18n]").each(function () {
            var $el = $(this);
            var key = $el.attr("lang-i18n");
            if (!key) return;
            var text = i18n.prop(key);
            $el.attr("placeholder", text);
            $el.attr("title", text);
            $el.attr("data-title", text);
            var tag = $el.prop("tagName") ? $el.prop("tagName").toLowerCase() : "";
            if (tag === "input" || tag === "textarea" || tag === "select") {
                // 表单控件只设置 placeholder/title，不修改 value/options
            } else {
                $el.text(text);
            }
        });
    }

    i18np.apply = function (scope) {
        var $scope = scope ? (typeof scope === "string" ? $(scope) : $(scope)) : $(document);
        processLangI18n($scope);
    };

    i18np.load = function (lang) {
        i18n.properties({
            name: "i18n",
            path: "/static/i18n",
            mode: "map",
            language: lang,
            cache: false,
            callback: function () {
                processLangI18n($(document));
                $('[lay-reqtext]').each(function () {
                    $(this).attr('lay-reqtext', i18n.prop($(this).attr("lay-reqtext")));
                });
            },
        });
    };
    i18np.changeLanguage = function (language) {
        if (language === null) {
            language = "zh";
        }
        localStorage.setItem(store.language, language);
        location.reload();

    }
    i18np.prop = function (key /* Add parameters as function arguments as necessary  */) {
        return i18n.prop(key);
    }

    exports("i18np", i18np);
});