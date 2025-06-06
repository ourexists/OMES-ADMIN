/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

layui.define(["jquery", "i18n"], function (exports) {
    var $ = layui.jquery,
        i18n = layui.i18n;

    var i18np = {};

    i18np.load = function (lang) {
        i18n.properties({
            name: "message", // 资源文件名称
            path: "/i18n", // 资源文件所在目录路径
            mode: "map", // 模式：变量或 Map
            language: lang, // 对应的语言
            cache: false,
            callback: function () {
                //这里是我通过对标签添加选择器来统一管理需要配置的地方
                $("[lang-i18n]").each(function (e) {
                    $(this).attr(
                        "placeholder",
                        i18n.prop($(this).attr("lang-i18n"))
                    );
                    $(this).attr(
                        "title",
                        i18n.prop($(this).attr("lang-i18n"))
                    );
                    $(this).attr(
                        "data-title",
                        i18n.prop($(this).attr("lang-i18n"))
                    );
                    $(this).html(i18n.prop($(this).attr("lang-i18n")));
                    if ($(this).is("button")) {
                        $(this).text(i18n.prop($(this).attr("lang-i18n")))
                    }
                });

                $('[lay-reqtext]').each(function (e) {
                    $(this).attr('lay-reqtext', i18n.prop($(this).attr("lay-reqtext")))
                })
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