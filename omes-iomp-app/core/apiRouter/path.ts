/** 走 SAS 网关本地路径：令牌、验证码 */
export const AUTH_API_PREFIXES = [
    "/oauth2/",
    "/open/captcha",
] as const;

/** 走 SAS 网关（转发 Admin）：账户注册等 */
export const ADMIN_AUTH_API_PREFIXES = [
    "/authentication/",
] as const;

export function isAuthApiPath(path: string): boolean {
    const p = path.split("?")[0] ?? path;
    for (let i = 0; i < AUTH_API_PREFIXES.length; i++) {
        const prefix = AUTH_API_PREFIXES[i];
        if (p.startsWith(prefix)) {
            return true;
        }
    }
    return false;
}

export const apiPath = {
    captcha: '/open/captchaBase' as string,
    auth_token: '/oauth2/token' as string,
    /** 以下在 OMES-ADMIN 或经 SAS 代理，请求时使用 baseUrl / authBaseUrl */
    auth_register: '/authentication/register' as string,
    auth_channelRegister: '/authentication/channelRegister' as string,
    auth_channelRegisterAndReturn: '/authentication/channelRegisterAndReturn' as string,
    auth_changePass: '/authentication/changePass' as string,
    current_user: '/acc/currentUser' as string,
    message_listen: '/message/listen' as string,
    message_page: '/message/selectByPage' as string,
    message_detail: '/message/selectById' as string,
    message_read: '/message/read' as string,
    message_unread_count: '/message/countUnread' as string,
    workshop_tree: '/workshop/selectUserAssignTree' as string,
    equip_page: '/equip/selectByPage' as string,
    equip_count: '/equip/countRealtime' as string,
    equip_realtime: '/equip/selectRealtimeById' as string,
    equip_id: '/equip/selectById' as string,
    equip_run_history: '/equipRecordRun/selectByPage' as string,
    equip_online_history: '/equipRecordOnline/selectByPage' as string,
    equip_alarm_history: '/equipRecordAlarm/selectByPage' as string,
    equip_config: '/equip/queryEquipConfigBySn' as string,
    equip_write_control: '/equip/writeControl' as string,
    equip_collect_page: '/equip/collect/selectByPage' as string,
    equip_run_count: '/equipRecordRun/countMerging' as string,
    equip_online_count: '/equipRecordOnline/countMerging' as string,
    equip_alarm_count: '/equipRecordAlarm/countMerging' as string,
    workshop_scada: "/workshop/getScadaUrlByWorkshopCode" as string,
    workshop_realtime: "/workshop/getWorkshopRealtimeCollect" as string,
    /** 巡检项目：分页查询 */
    inspection_project_page: "/inspection/project/selectByPage" as string,
    /** 巡检任务：分页查询（可传 executorId 查当前账户名下任务） */
    inspect_task_page: "/inspection/task/selectByPage" as string,
    /** 巡检任务：根据ID查询 */
    inspect_task_by_id: "/inspection/task/selectById" as string,
    /** 巡检任务：新增或更新（用于确认执行等状态变更） */
    inspect_task_addOrUpdate: "/inspection/task/addOrUpdate" as string,
    /** 巡检模板：根据ID查询模板及其巡检项列表 */
    inspect_template_with_items: "/inspection/template/selectWithItems" as string,
    /** 设备巡检记录：提交保存（新增） */
    inspect_record_save: "/inspection/record/save" as string,
    /** 设备巡检记录：新增或更新（已检时更新） */
    inspect_record_addOrUpdate: "/inspection/record/addOrUpdate" as string,
    /** 设备巡检记录：分页查询（可传 taskId、equipId 等） */
    inspect_record_page: "/inspection/record/selectByPage" as string,
};

export const authParam = {
    client_id: 'mes' as string,
}
