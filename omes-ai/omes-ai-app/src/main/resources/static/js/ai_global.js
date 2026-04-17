var store = {
    token_header: 'mes-token',
    language: 'mes-lang'
};

var client = {
    tenantId: 0,
    platform: 'mes-edge'
};

var router = {
    inspect_ai_report_generate: "/inspection/ai/report",
    inspect_ai_knowledge_reindex: "/inspection/ai/knowledge/reindex",
    inspect_ai_knowledge_ask: "/inspection/ai/knowledge/ask",
    inspect_ai_knowledge_ingest: "/inspection/ai/knowledge/ingest",
    inspect_ai_agent_run: "/inspection/ai/agent/run",
    inspect_ai_agent_multi_chat: "/inspection/ai/agent/multi-chat",
    inspect_ai_agent_multi_chat_config: "/inspection/ai/agent/multi-chat/config",
    inspect_ai_agent_multi_chat_session_create: "/inspection/ai/agent/multi-chat/session/create",
    inspect_ai_agent_multi_chat_session_list: "/inspection/ai/agent/multi-chat/session/list",
    inspect_ai_agent_multi_chat_session_messages: "/inspection/ai/agent/multi-chat/session/messages"
};

function getCommonHeader() {
    return {
        'Authorization': localStorage.getItem(store.token_header),
        'x-era-platform': client.platform,
        'x-route-tenant': client.tenantId
    };
}

function initRequestUrl(url) {
    var lang = localStorage.getItem(store.language) || 'zh';
    return url + '?lang=' + encodeURIComponent(lang);
}

function handleResponseData(data, successFunc, failFuc) {
    if (data && data.code === 200) {
        if (successFunc) {
            successFunc(data.data);
        }
        return;
    }
    if (failFuc) {
        failFuc(data ? data.code : 500, data ? data.msg : 'unknown error');
    } else {
        alert((data && data.msg) || '请求失败');
    }
}

function post(url, param, successFunc, failFuc, urlParam) {
    var reqUrl = initRequestUrl(url);
    if (urlParam) {
        Object.keys(urlParam).forEach(function (key) {
            reqUrl += '&' + encodeURIComponent(key) + '=' + encodeURIComponent(urlParam[key]);
        });
    }
    $.ajax({
        url: reqUrl,
        data: JSON.stringify(param || {}),
        type: "POST",
        dataType: "json",
        headers: getCommonHeader(),
        contentType: "application/json",
        success: function (data) {
            handleResponseData(data, successFunc, failFuc);
        },
        error: function (e, msg) {
            if (failFuc) {
                failFuc(e.status || 500, msg || 'request error');
            } else {
                alert('请求异常: ' + (msg || 'unknown error'));
            }
        }
    });
}

function get(url, param, successFunc, failFuc) {
    $.ajax({
        url: initRequestUrl(url),
        data: param || {},
        dataType: "json",
        headers: getCommonHeader(),
        success: function (data) {
            handleResponseData(data, successFunc, failFuc);
        },
        error: function (e, msg) {
            if (failFuc) {
                failFuc(e.status || 500, msg || 'request error');
            } else {
                alert('请求异常: ' + (msg || 'unknown error'));
            }
        }
    });
}

function openMidWindow(name, url, area) {
    var width = (area && area[0]) || '90%';
    var height = (area && area[1]) || '95%';
    layer.open({
        title: name,
        type: 2,
        area: [width, height],
        shadeClose: true,
        content: url,
        scrollbar: false
    });
}
