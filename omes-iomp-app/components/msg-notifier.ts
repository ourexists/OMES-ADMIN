import {config, isDev} from "@/config";
import {apiPath} from "@/core/apiRouter/path";
import {userInfo} from "@/core/store";
import {ref} from "vue";
import {storage} from "@/uni_modules/cool-unix";
import {request} from "@/core/service";
import {parseDataArray} from "@/core/utils/parse";
import type {Message} from "@/core/types";


// #ifdef H5
let controller: any | null = null;
// #endif

let isStop: boolean = false;
let isConnected = false;
let isConnecting = false;

export const notifyQueue = ref<Message[]>([]);
export const notify_message = ref<string>("");
export const notify_visible = ref<boolean>(false);
export const notify_enable = ref<boolean>(true);
export const unread_count = ref<number>(0);

export function initNotifyEnable() {
    const saved = storage.get("notify_enable");

    if (saved == null) {
        notify_enable.value = true;
    } else if (typeof saved === "boolean") {
        notify_enable.value = saved;
    } else if (typeof saved === "number") {
        notify_enable.value = saved != 0;
    } else if (typeof saved === "string") {
        notify_enable.value = saved === "true" || saved === "1";
    } else {
        notify_enable.value = true; // 兜底
    }
}

initNotifyEnable();

export function formatDateTime(ts: number): string {
    const d = new Date(ts);
    const p = (n: number) => n < 10 ? '0' + n : '' + n;

    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

// UTS：setTimeout 回调须引用模块级具名函数，不能引用 connectMessage 内的局部 poll
async function messageNotifierPollTick(): Promise<void> {
    if (isStop) {
        isConnecting = false;
        return;
    }
    const now = Date.now();
    try {
        const pageRes = await request({
            url: apiPath.message_page as string,
            method: "POST",
            data: {
                page: 1,
                pageSize: 10,
                accId: userInfo.value?.id,
                platform: config.platform,
                createdTimeStart: formatDateTime(now - 3000),
                createdTimeEnd: formatDateTime(now)
            }
        });
        if (pageRes !== null) {
            const r = parseDataArray<Message>(pageRes);
            if (r != null) {
                r.forEach((msg: Message) => pushNotifyQueue(msg));
            }
        }
    } catch (err) {
        if (isDev) {
            console.warn("[msg-notifier] message_page", err);
        }
    }
    try {
        const countRes = await request({
            url: apiPath.message_unread_count as string,
            method: "GET",
        });
        if (countRes !== null) {
            unread_count.value = countRes as number;
        }
    } catch (err) {
        if (isDev) {
            console.warn("[msg-notifier] countUnread", err);
        }
    }
    if (!isStop) {
        setTimeout((): void => {
            void messageNotifierPollTick();
        }, 2000);
    } else {
        isConnecting = false;
    }
}

export async function connectMessage() {
    console.log("sse connect start")
    if (isConnected || isConnecting) {
        console.log("sse connect is connecting");
        return;
    }
    isStop = false;
    isConnecting = true;
    void messageNotifierPollTick();
}

export function disconnectMessage() {
    console.log("sse client disconnect=======");
    isStop = true;
    // #ifdef H5
    if (controller) {
        controller.abort();
        controller = null;
    }
    // #endif
    isConnected = false;
    isConnecting = false;
}

export function pushNotifyQueue(msg: Message) {
    notifyQueue.value.push(msg);
    //保持数据容量不超过10条,防止内存问题
    if (notifyQueue.value.length > 10) {
        notifyQueue.value.shift();
    }
}

export function onNotify() {

    if (notifyQueue.value.length <= 0) {
        notify_visible.value = false;
    } else {
        const msg = notifyQueue.value.shift();
        if (msg == null) {
            return
        }
        notify_visible.value = notify_enable.value ?? true;
        if (isDev) {
            console.log(JSON.stringify(msg) + "-" + notify_visible.value + "-" + notify_enable.value)
        }
        notify_message.value = msg.context;
    }
}

setInterval(() => {
    onNotify();
}, 8000);

function clear() {
    notifyQueue.value = [];
    notify_message.value = '';
    notify_visible.value = false;
}

export function changeNotify(val: boolean) {
    storage.set("notify_enable", val, 0);
    notify_enable.value = val;
}