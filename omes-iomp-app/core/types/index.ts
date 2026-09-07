export type UserInfo = {
    unionId?: string,
    id: string,
    username?: string,
    accName: string,
    nickName?: string,
    avatarUrl?: string,
    sex: number,
    city?: string; // 城市
    country?: string,
    province?: string,
    idCard?: string,
    email?: string,
    mobile?: string,
    settledTime: string,
    expireTime: string,
    loginType?: number,
    perfection?: number,
    language?: string,
    source?: string,
    sourceId?: string
};

export type Message = {
    id: string;
    title: string;
    context: string;
    type: number;
    platform: string;
    notifyId?: string;
    sourceId?: string;
    readStatus: number;
    readTime?: string;
    createdTime?: string;
    source?: string;
}

export type Error = {
    code: string,
    msg: string
}

export type EquipRecord = {
    id: string,
    sn: string,
    startTime?: string,
    endTime?: string,
    state: number
}

export type EquipConfigAttr = {
    name: string;

    map: string;

    value?: string;

    needCollect?: boolean;
}

export type EquipConfigDetail = {
    runMap?: string;

    alarmMa?: string;

    attrs?: EquipConfigAttr[];
}

export type EquipConfig = {
    equipId: string;
    config?: EquipConfigDetail;
}

export type EquipCollect = {
    sn: string;
    /** 接口偶发缺省时为 null */
    data?: Record<string, string> | null;
    time: string;
    tenantId?: string;
}

export type Equip = {
    id: string;
    selfCode: string;
    name: string;
    /** 产品编号（设备类型），后端可能为 number 或 string */
    type: number | string;
    /** 设备关联的产品图片地址，由分页/详情接口统一返回 */
    productImage?: string;
    runState: number;
    alarmState: number;
    onlineState: number;
    workshop?: WorkshopTreeNode;
    attrs?: EquipAttr[];
    /** 控制属性（可下发控制的点） */
    controls?: EquipControl[];
    alarmTexts?: string[];
    onlineChangeTime?: string;
    runChangeTime?: string;
    alarmChangeTime?: string;
}

export type EquipAttr = {
    name: string;
    /** 采集点位标识，与后端 EquipAttr.map 一致 */
    map?: string;
    value?: string;
    unit?: string;
    equipId?: string;
}

/** 设备控制点：name、map、type、value、unit、min、max */
export type EquipControl = {
    name: string;
    map: string;
    type: number;
    value?: string;
    unit?: string | null;
    min?: string | null;
    max?: string | null;
}

export type EquipCount = {
    total: number;
    alarm: number;
    online: number;
    offline: number;
    run: number;
    stopped: number;
}

export type WorkshopTreeNode = {
    id: string;
    selfCode: string;
    name: string;
    code: string;
    pcode?: string;
    lng?: number | string | null;
    lat?: number | string | null;
    address?: string | null;
    children?: WorkshopTreeNode[];
}

export type WorkshopScada = {
    url?: string;
    interval?: number;
}

export type WorkshopRealtimeCollect = {
    name: string;
    map?: string;
    value?: string;
    unit?: string;
}

/** 巡检项目 */
export type InspectionProject = {
    id: string;
    name: string;
    code?: string;
    /** 状态：0-停用 1-启用 */
    status?: number;
    /** 巡检周期类型：day-日 week-周 month-月 */
    cycleType?: string;
    cycleValue?: number;
    description?: string;
    createdTime?: string;
    updatedTime?: string;
}

/** 巡检任务：0待执行 1执行中 2已完成 3已逾期 */
export type InspectTask = {
    id: string;
    planId?: string;
    /** 任务计划名称（展示用） */
    planName?: string;
    /** 关联巡检模板ID */
    templateId?: string;
    /** 关联巡检模板名称（展示用） */
    templateName?: string;
    scheduledTime?: string;
    workshopCode?: string;
    workshopName?: string;
    status?: number;
    executorPersonId?: string;
    executorId?: string;
    executorName?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    remark?: string;
    createTime?: string;
    updateTime?: string;
    tenantId?: string;
}

/** 巡检项类型：1选择 2数值 3是否 */
export type InspectItemType = 1 | 2 | 3;

/** 巡检项（模板下的一条：名称、类型、单位/选项） */
export type InspectItem = {
    id: string;
    templateId?: string;
    productId?: string;
    productName?: string;
    /** 产品编码，与设备的 type 对应，用于按设备产品筛选检测项 */
    productCode?: string;
    itemName?: string;
    /** 类型：1选择 2数值 3是否 */
    itemType?: InspectItemType;
    /** 单位或选项：数值填 A/MPa/°C 等；选择可填 正常/不足；是否可空 */
    unit?: string;
    sortOrder?: number;
}

/** 巡检模板（含巡检项列表，用于详情展示与员工巡检操作） */
export type InspectTemplate = {
    id: string;
    name?: string;
    remark?: string;
    items?: InspectItem[];
}

/** 单条巡检结果（提交记录时用） */
export type InspectRecordItemResult = {
    itemId: string;
    value?: string;
};

/** 设备巡检记录提交体 */
export type InspectRecordSaveBody = {
    taskId: string;
    equipId: string;
    results: InspectRecordItemResult[];
};

/** 巡检记录明细（一条检测项的填写结果） */
export type InspectRecordItemDto = {
    id?: string;
    recordId?: string;
    itemId?: string;
    itemName?: string;
    /** 填写值/结果文本 */
    content?: string;
    result?: number;
    remark?: string;
};

/** 巡检记录（主表：一次设备提交对应一条） */
export type InspectRecordDto = {
    id?: string;
    taskId?: string;
    equipId?: string;
    equipName?: string;
    score?: number;
    recordTime?: string;
    items?: InspectRecordItemDto[];
};

