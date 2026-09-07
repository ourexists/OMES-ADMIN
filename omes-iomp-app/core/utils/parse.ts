import {isArray, isObject} from "@/uni_modules/cool-unix";

/**
 * App：单对象用 JSON.parseObject 转成宿主类型（如 Equip）。
 * 顶层数组请用 parseDataArray：isObject 排除数组，直接 as 会导致元素仍是 UTSJSONObject 并在运行时 ClassCastException。
 */
export function parseData<T>(data: any | null): T | null {
    if (data == null) {
        return null;
    }
    // #ifdef APP
    if (isObject(data)) {
        // @ts-ignore
        return JSON.parseObject<T>(JSON.stringify(data));
    }
    return data as T;
    // #endif

    // #ifndef APP
    return data as T;
    // #endif
}

/**
 * App（Android/iOS）：对象数组逐项 JSON.parseObject（元素类型为泛型 E），避免 UTSJSONObject 无法 cast 为 E。
 */
export function parseDataArray<E>(data: any | null): E[] | null {
    if (data == null) {
        return null;
    }
    // #ifdef APP
    /** uni.request 在部分 App 环境下 data 为原生列表，Array.isArray 为 false；stringify 再 parse 成真正的 JS 数组 */
    let payload: any = data;
    if (!isArray(payload)) {
        try {
            const s = JSON.stringify(data);
            if (s == null) {
                return null;
            }
            const str = s as string;
            if (str.length >= 2 && str.charAt(0) === "[") {
                // @ts-ignore
                const parsed = JSON.parseObject<any[]>(str);
                if (parsed != null && isArray(parsed)) {
                    payload = parsed;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (_e: any) {
            return null;
        }
    }
    if (!isArray(payload)) {
        return null;
    }
    /** 勿对整表一次 parseObject：嵌套 Map（如 EquipCollect.data）在批量解析后 App 上常读不出，表格/折线图无数据 */
    const arr = payload as any[];
    const out: E[] = [];
    for (let i = 0; i < arr.length; i++) {
        const el = arr[i];
        if (el == null) {
            continue;
        }
        if (isObject(el)) {
            // @ts-ignore
            const parsed = JSON.parseObject<E>(JSON.stringify(el));
            if (parsed != null) {
                out.push(parsed);
            }
        } else {
            out.push(el as E);
        }
    }
    return out;
    // #endif

    // #ifndef APP
    return data as E[];
    // #endif
}

export function formatDate(date: Date | null): string {
    if (date == null) {
        return '';
    }
    const year = date.getFullYear();  // 获取年份
    const month = (date.getMonth() + 1).toString().padStart(2, '0');  // 获取月份，+1 因为月份从 0 开始
    const day = date.getDate().toString().padStart(2, '0');  // 获取日期
    const hours = date.getHours().toString().padStart(2, '0');  // 获取小时
    const minutes = date.getMinutes().toString().padStart(2, '0');  // 获取分钟
    const seconds = date.getSeconds().toString().padStart(2, '0');  // 获取秒

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;  // 格式化为 yyyy-MM-dd HH:mm:ss
}

export function parseDate(dateformat: string): Date {
    return new Date(dateformat.replace(' ', 'T'));
}