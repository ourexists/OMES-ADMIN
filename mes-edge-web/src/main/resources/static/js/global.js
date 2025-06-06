/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

layui.config({
    base: '/static/js/'
}).extend({
    i18np: 'i18np',
    soulTable: 'soulTable',
    flowNode: "flowNode",
});

var i18np;
var $ = layui.jquery;

var store = {
    token_header: 'mes-token',
    language: 'mes-lang'
}

layui.use(['i18np'], function () {
    i18np = layui.i18np;
    var language = localStorage.getItem(store.language);
    if (language === null) {
        language = "zh";
    }
    window.getprop = function (lan) {
        return i18np.prop(lan);
    }
    window.changeLanguage = function (lan, elem) {
        i18np.changeLanguage(lan);
    }
    i18np.load(language);
})

var client = {
    id: 'mes',
    sc: 'admin123',
    grant_type: 'captcha'
}

var router = {
    "captcha": "/open/captcha",
    "auth_token": "/oauth/token",
    "mat_page": "/mat/selectByPage",
    "mat_edit": "/mat/addOrUpdate",
    "mat_del": "/mat/delete",
    "mc_page": "/mc/selectByPage",
    "mc_edit": "/mc/addOrUpdate",
    "mc_del": "/mc/delete",
    "mps_page": "/mps/selectByPage",
    "mps_edit": "/mps/addOrUpdate",
    "mps_del": "/mps/delete",
    "mps_id": "/mps/selectById",
    "mps_addBatch": "/mps/addBatch",
    "mps_join_que": "/mps/joinQueue",
    "mps_join_que_batch": "/mps/joinQueueBatch",
    "mps_remove_que": "/mps/removeQueue",
    "mps_jump_que": "/mps/jumpQueue",
    "mps_status": "/mps/status",
    "mps_changePriority": "/mps/changePriority",
    "mps_detail_page": "/mps_detail/selectByPage",
    "mps_detail_edit": "/mps_detail/addOrUpdate",
    "mps_detail_del": "/mps_detail/delete",
    "mps_flow": "/flow/mps",
    "mps_startTF": "/mps/startTf",
    "flow_mps_complete": '/flow/mpsFlowComplete',
    "bomc_tree": "/BOMC/treeClassify",
    "bomc_edit": "/BOMC/addOrUpdate",
    "bomc_del": "/BOMC/delete",
    "bomd_attribute": "/BOMD/attribute",
    "bom_page": "/BOM/selectByPage",
    "bom_edit": "/BOM/addOrUpdate",
    "bom_del": "/BOM/delete",
    "bom_id": "/BOM/selectById",
    "bom_type": "/BOM/type",
    "ingredient_attribute": "/mps_detail/ingredientAttribute",
    "mo_page": "/mo/selectByPage",
    "mo_id": "/mo/selectById",
    "mo_edit": "/mo/addOrUpdate",
    "mo_del": "/mo/delete",
    "mo_status": "/mo/status",
    "line_page": "/line/selectByPage",
    "line_edit": "/line/addOrUpdate",
    "line_del": "/line/delete",
    "line_id": "/line/selectById",
    "line_code": "/line/selectByCode",
    "line_type": "/line/type",
    "line_downS7": "/line/downloadS7",
    "tf_lineId": "/tf/selectByLineId",
    "tf_del": "/tf/delete",
    "tf_edit": "/tf/addOrUpdate",
    "tf_id": "/tf/selectById",
    "tf_type": "/tf/type",
    "tf_changePriority": "/tf/changePriority",
    "sync_page": "/sync/selectByPage",
    "sync_id": "/sync/selectById",
    "sync_breakpoint": "/sync/breakpointProcess",
    "sync_tx": "/sync/syncTx",
    "sync_status": "/sync/status",
    "qa_page": "/qa/selectByPage",
    "qa_status": "/qa/status",
    "qa_edit": "/qa/addOrUpdate",
    "qa_id": "/qa/selectById",
    "qa_check": "/qa/check",
    "s7_all": "/plc/getAll",
    "task_page": "/task/selectByPage",
    "task_id": "/task/selectById",
    "task_del": "/task/delete",
    "task_edit": "/task/addOrUpdate",
    "task_type": "/task/timerTask",
    "task_stop": "/task/stop",
    "task_start": "/task/start",
}

function getCommonHeader() {
    return {
        'Authorization': localStorage.getItem(store.token_header),
        'x-era-platform': 'mse-edge',
        'x-route-tenant': 0
    };
}

function initRequestUrl(url) {
    return url + "?lang=" + localStorage.getItem(store.language) + "&client_id=" + client.id + "&client_secret=" + client.sc + "&grant_type=" + client.grant_type;
}

function auth(url, param, successFunc, failFuc) {
    let req_url = initRequestUrl(url);
    if (param != null) {
        layui.each(param, function (index, item) {
            req_url += "&" + index + "=" + item;
        })
    }
    $.ajax({
        url: req_url,
        type: "POST",
        dataType: "json",
        headers: getCommonHeader(),
        async: false,
        contentType: "application/json",
        success: function (data) {
            if (data.code == null) {
                if (successFunc != null) {
                    successFunc(data);
                }
            } else {
                if (failFuc != null) {
                    failFuc(data.code, data.msg);
                }
            }
        }
    });
}

function handleResponseData(data, successFunc, failFuc) {
    if (data.code === 200) {
        if (successFunc != null) {
            successFunc(data.data);
        }
    } else if (data.code >= 401 && data.code < 500 && data.code !== 404 && data.code !== 406) {
        layer.confirm(i18np.prop('common.msg.lock_token'), null, function () {
            localStorage.removeItem(store.token_header);
            window.parent.location.href = '/view/login';
        })
    } else {
        console.info(failFuc);
        if (failFuc != null) {
            failFuc(data.code, data.msg);
        } else {
            layer.msg(data.msg);
        }
    }
}

function post(url, param, successFunc, failFuc, urlParam) {
    let req_url = initRequestUrl(url);
    if (urlParam != null) {
        urlParam.forEach((value, key) => {
            req_url += "&" + key + "=" + value;
        })
    }
    $.ajax({
        url: req_url,
        data: JSON.stringify(param),
        type: "POST",
        dataType: "json",
        headers: getCommonHeader(),
        async: false,
        contentType: "application/json",
        success: function (data) {
            handleResponseData(data, successFunc, failFuc);
        },
        error: function (e, msg) {
            if (e.status >= 401 && e.status < 500) {
                layer.confirm(i18np.prop('common.msg.lock_token'), null, function () {
                    localStorage.removeItem(store.token_header);
                    window.parent.location.href = '/view/login';
                })
            } else {
                layer.alert('请求异常，错误提示：' + msg);
            }
        }
    });
}

function get(url, param, successFunc, failFuc) {
    $.ajax({
        url: initRequestUrl(url),
        data: param,
        dataType: 'json',
        headers: getCommonHeader(),
        success: function (data) {
            handleResponseData(data, successFunc, failFuc);
        },
        error: function (e, msg) {
            if (e.status >= 401 && e.status < 500) {
                layer.confirm(i18np.prop('common.msg.lock_token'), null, function () {
                    localStorage.removeItem(store.token_header);
                    window.parent.location.href = '/view/login';
                })
            } else {
                layer.alert('请求异常，错误提示：' + msg);
            }
        }
    });
}

function openWindow(name, url, area, successFunc) {
    if (area == null) {
        area = ['80%', '60%'];
    }
    layer.open({
        title: i18np.prop(name),
        type: 2,
        area: area,
        offset: '5%',
        content: url,
        loading: true,
        scrollbar: false,
        success: function (layero, index) {
            if (successFunc != null) {
                successFunc();
            }
        }
    });

}

function delWindow(url, ids, successFuc, failFuc) {
    layer.confirm(i18np.prop('common.msg.delete.sure'),
        {
            btn: [i18np.prop('common.msg.confirm'), i18np.prop('common.msg.cancel')]
        }, function (index, layero) {
            layer.close(index);
            var param = {
                "ids": ids
            }
            post(url, param, successFuc, function () {
                layer.alert(data.msg, {
                    title: 'error'
                });
                failFuc();
            })
        }, function () {
            layer.close(index);
        }
    );
}

function closeWindow() {
    var index = parent.layer.getFrameIndex(window.name);
    parent.layer.close(index);
}

function delTreeNode(url, ids, successFuc, failFuc) {
    var param = {
        "ids": ids
    }
    post(url, param, successFuc, function () {
        layer.alert(data.msg, {
            title: 'error'
        });
        failFuc();
    })
}

//定义函数 绑定增加tab，删除tab，切换tab几项事件
var tabFunction = {
    //新增tab url 页面地址 id 对应data-id name标题
    tabAdd: function (url, id, name, filter) {
        layui.element.tabAdd(filter, {
            title: name,
            content: "<iframe id='loadTable' src='" + url + "' width='100%' height='1000' allowfullscreen frameborder=\"0\" scrolling='false' ></iframe>",
            id: id,
            change: true,
            type: 2
        });
        var iframe = document.getElementById('loadTable')
        var iframeWin = iframe.contentWindow || iframe.contentDocument.parentWindow;//兼容
        if (iframeWin.document.body) {
            iframe.height = iframeWin.document.documentElement.scrollHeight || iframeWin.document.body.scrollHeight;//滚动条高度
        }
    },
    //根据id切换tab
    tabChange: function (id, filter) {
        layui.element.tabChange(filter, id)
    },
    //关闭指定的tab
    tabDelete: function (id, filter) {
        layui.element.tabDelete(filter, id)
    },
    tabDeleteAll: function (ids, filter) {
        $.each(ids,
            function (i, item) {
                layui.element.tabDelete(filter, item);
            });
    }
}

function openTab(url, id, name, filter, element) {
    //判断右侧是否有tab
    if (element.length <= 0) {
        tabFunction.tabAdd(url, id, name, filter);
    } else {
        //判断tab是否已经存在
        var isExist = false;
        $.each(element, function () {
            //筛选id是否存在
            if ($(this).attr('lay-id') === id) {
                isExist = true;
            }
        });
        //不存在，增加tab
        if (isExist === false) {
            tabFunction.tabAdd(url, id, name, filter);
        }
    }
    //转到要打开的tab
    tabFunction.tabChange(id, filter);
}

function input_limit_float(value) {
    return value.replace(/^\D*(\d*(?:\.\d{0,3})?).*$/g, '$1');
}

function input_limit_int(value) {
    return value.replace(/[^\.\d]/g, '').replace('.', '');
}

