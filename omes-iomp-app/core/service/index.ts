import {config, ignoreTokens, isDev} from "@/config";
import {isAuthApiPath} from "@/core/apiRouter/path";
import {locale, t} from "@/locale";
import {isArray, isObject, parse} from "@/uni_modules/cool-unix";
import {useStore} from "../store";

const HTTP_LOG_TAG = "[OMES-HTTP]";

/**
 * 请求/错误日志用：避免整包采集等大对象把控制台刷爆
 */
function summarizeForLog(payload: any | null, maxLen: number): string {
    if (payload == null) {
        return "";
    }
    try {
        const s = typeof payload === "string" ? payload : JSON.stringify(payload);
        if (s.length <= maxLen) {
            return s;
        }
        return `${s.substring(0, maxLen)}...(totalLen=${s.length})`;
    } catch (_e: any) {
        return "[unserializable]";
    }
}

function logHttp(message: string): void {
    console.log(`${HTTP_LOG_TAG} ${message}`);
}

// 请求参数类型定义
export type RequestOptions = {
    url: string; // 请求地址
    method?: RequestMethod; // 请求方法
    data?: any; // 请求体数据
    params?: any; // URL参数
    header?: any; // 请求头
    timeout?: number; // 超时时间
    withCredentials?: boolean; // 是否携带凭证
    firstIpv4?: boolean; // 是否优先使用IPv4
    enableChunked?: boolean; // 是否启用分块传输
};

// 响应数据类型定义
export type Response = {
    code?: number;
    msg?: string;
    data?: any;
};

export type LoginTokenResponse = {
    token_type: string
    access_token: string
    expires_in: number
}

// 请求队列（用于等待token刷新后继续请求）
let requests: ((token: string) => void)[] = [];

// 标记token是否正在刷新
let isRefreshing = false;

// 判断当前url是否忽略token校验
const isIgnoreToken = (url: string) => {
    return ignoreTokens.some((e) => {
        const pattern = e.replace(/\*/g, ".*");
        return new RegExp(pattern).test(url);
    });
};

/**
 * 通用请求方法
 * @param options 请求参数
 * @returns Promise<T>
 */
export function request(options: RequestOptions): Promise<any | null> {
    let {url, method = "GET", data = {}, params = {}, header = {}, timeout = 60000} = options;
    const methodUpper = (method != null ? method : "GET").toUpperCase();
    const useJsonBody =
        (methodUpper === "POST" || methodUpper === "PUT" || methodUpper === "PATCH") &&
        data != null &&
        typeof data === "object";

    const {user} = useStore();

    // OAuth2/验证码走 authBaseUrl；其余（含 /authentication）走 baseUrl；统一网关下两者相同
    if (!url.startsWith("http")) {
        const pathOnly = options.url.split("?")[0] ?? options.url;
        const root = isAuthApiPath(pathOnly) ? config.authBaseUrl : config.baseUrl;
        url = root + url;
    }

    // 获取当前token
    let Authorization: string | null = user.token;

    // 如果是忽略token的接口，则不携带token
    if (isIgnoreToken(url)) {
        Authorization = null;
    }

    return new Promise((resolve, reject) => {
        if (url.includes("?")) {
            url = url + "&locale=" + locale.value;
        } else {
            url = url + "?locale=" + locale.value;
        }
        const pathForLog = options.url;
        const bodyMax = isDev ? 2000 : 500;
        // 发起请求的实际函数
        const next = () => {
            const h = header as UTSJSONObject | null;
            const hasContentType =
                h != null &&
                (h["Content-Type"] != null ||
                    h["content-type"] != null);
            uni.request({
                url,
                method,
                data: data,
                header: {
                    ...(useJsonBody && !hasContentType ? {"Content-Type": "application/json; charset=utf-8"} : {}),
                    Authorization,
                    language: locale.value,
                    "x-era-platform": config.platform,
                    "x-route-tenant": 0,
                    ...(header as UTSJSONObject)
                },
                timeout,
                success(res) {
                    // 401/403 未授权或禁止访问，统一退出登录
                    if (res.statusCode == 401 || res.statusCode == 403) {
                        user.logout();
                        reject({msg: res.statusCode == 403 ? t("无访问权限") : t("无权限")});
                        return;
                    }

                    // 502 服务异常
                    else if (res.statusCode == 502) {
                        reject({
                            msg: t("服务异常")
                        } as Response);
                    }

                    // 404 未找到
                    else if (res.statusCode == 404) {
                        return reject({
                            msg: `[404] ${url}`
                        } as Response);
                    }

                    // 200 正常响应
                    else if (res.statusCode == 200) {
                        if (res.data == null) {
                            resolve(null);
                        } else if (!isObject(res.data as any)) {
                            resolve(res.data);
                        } else {
                            // 解析响应数据
                            const raw = res.data; // 临时保存
                            const parsed: Response = parse<Response>(raw)!;
                            const code = parsed.code;
                            const msg = parsed.msg;
                            const data = parsed.data;
                            switch (code) {
                                case 200: {
                                    let hint = "null";
                                    if (data == null) {
                                        hint = "null";
                                    } else if (isArray(data)) {
                                        hint = `array(len=${(data as any[]).length})`;
                                    } else if (isObject(data)) {
                                        hint = "object";
                                    } else {
                                        hint = typeof data;
                                    }
                                    resolve(data);
                                    break;
                                }
                                case 500:
                                    reject({msg, code});
                                    break;
                                case 401:
                                case 403:
                                    user.logout();
                                    reject({msg: code == 403 ? t("无访问权限") : t("无权限")});
                                    break;
                                default:
                                    if (options.url.includes("/oauth2/token")) {
                                        resolve(res.data);
                                    } else {
                                        reject({msg, code});
                                    }
                            }
                        }
                    } else {
                        reject({msg: t("服务异常")});
                    }
                },

                // 网络请求失败
                fail(err) {
                    logHttp(
                        `<-- NET_FAIL path=${pathForLog} err=${summarizeForLog(err, isDev ? 800 : 400)}`
                    );
                    reject({msg: err.errMsg});
                }
            });
        };

        // 非刷新认证接口才进行token有效性校验
        // if (!options.url.includes("/oauth2/token")) {
        //     if (!isNull(Authorization)) {
        //         // 判断token是否过期
        //         if (storage.isExpired("token")) {
        //             // 判断refreshToken是否过期
        //             if (storage.isExpired("refreshToken")) {
        //                 // 刷新token也过期，直接退出登录
        //                 user.logout();
        //                 return;
        //             }
        //
        //             // 如果当前没有在刷新token，则发起刷新
        //             if (!isRefreshing) {
        //                 isRefreshing = true;
        //                 user.refreshToken()
        //                     .then((token) => {
        //                         // 刷新成功后，执行队列中的请求
        //                         requests.forEach((cb) => cb(token));
        //                         requests = [];
        //                         isRefreshing = false;
        //                     })
        //                     .catch((err) => {
        //                         reject(err);
        //                         user.logout();
        //                     });
        //             }
        //
        //             // 将当前请求加入队列，等待token刷新后再执行
        //             new Promise((resolve) => {
        //                 requests.push((token: string) => {
        //                     // 重新设置token
        //                     Authorization = token;
        //                     next();
        //                     resolve(true);
        //                 });
        //             });
        //             // 此处return，等待token刷新
        //             return;
        //         }
        //     }
        // }

        // token有效，直接发起请求
        next();
    });
}