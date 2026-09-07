import type {Equip} from '@/core/types';
import {config} from "@/config";
import {t} from "@/locale";

/** 相对路径（如 /files/product/xxx.png）拼上 baseUrl 为完整可请求地址 */
function fullImageUrl(path: string): string {
    if (path.length == 0 || path.startsWith("http://") || path.startsWith("https://")) {
        return path;
    }
    const base = config.baseUrl ?? "";
    const trimBase = base.replace(/\/+$/, "");
    const trimPath = path.replace(/^\/+/, "");
    return trimBase.length > 0 ? `${trimBase}/${trimPath}` : path;
}

export type Badge = {
    type: 'offline' | 'alarm' | 'run' | 'stopped',
    text: string,
}

export function parseType(e: Equip | null): Badge {
    if (e != null) {
        if (e.onlineState == 0) {
            return {type: 'offline', text: t('离线')} as Badge
        }
        if (e.onlineState == 1 && e.alarmState == 1) {
            return {type: 'alarm', text: t('报警')} as Badge
        }
        if (e.onlineState == 1 && e.alarmState == 0 && e.runState == 1) {
            return {type: 'run', text: t('运行')} as Badge
        }
        if (e.onlineState == 1 && e.alarmState == 0 && e.runState == 0) {
            return {type: 'stopped', text: t('停止')} as Badge
        }
    }
    return {type: 'offline', text: t('离线')} as Badge
}


export function equipImage(equip: Equip): string | null {
    if (equip.productImage != null && equip.productImage !== "") {
        return fullImageUrl(equip.productImage);
    }
    return null;
}