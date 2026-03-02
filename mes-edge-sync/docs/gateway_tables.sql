-- 网关设备管理相关表结构（MySQL）
-- 执行前请确认 mes-edge 使用的数据库与字符集

-- 1. 网关表
CREATE TABLE IF NOT EXISTS t_gateway (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(128),
    code VARCHAR(64),
    tenant_id VARCHAR(32),
    remark VARCHAR(512),
    created_by VARCHAR(32),
    created_id VARCHAR(32),
    created_time DATETIME,
    updated_by VARCHAR(32),
    updated_id VARCHAR(32),
    updated_time DATETIME
) COMMENT '网关设备';

-- 2. 连接表 t_connect 增加网关与定时采集字段（若已有表则执行以下 ALTER）
ALTER TABLE t_connect ADD COLUMN gateway_id VARCHAR(32) COMMENT '所属网关ID';
ALTER TABLE t_connect ADD COLUMN collect_cron VARCHAR(64) COMMENT '采集cron表达式';
ALTER TABLE t_connect ADD COLUMN collect_interval_sec INT COMMENT '采集间隔秒数';
ALTER TABLE t_connect ADD COLUMN collect_enabled TINYINT(1) DEFAULT 0 COMMENT '是否启用定时采集';
ALTER TABLE t_connect ADD COLUMN last_collect_time DATETIME COMMENT '上次采集时间';
-- 若某列已存在可跳过对应 ALTER 或改为 MODIFY

-- 3. 连接采集配置表（按协议配置 MQTT topic / OPC 节点 / Modbus 寄存器等）
CREATE TABLE IF NOT EXISTS t_connect_collect_config (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    connect_id VARCHAR(32) NOT NULL,
    protocol VARCHAR(32),
    config_json TEXT COMMENT 'JSON: MQTT topics/qos, OPC nodeIds, Modbus slaveId/registerType/startAddr/count',
    created_by VARCHAR(32),
    created_id VARCHAR(32),
    created_time DATETIME,
    updated_by VARCHAR(32),
    updated_id VARCHAR(32),
    updated_time DATETIME
) COMMENT '连接采集配置';

-- 4. 设备采集绑定表（数据源与设备SN绑定及解析规则）
CREATE TABLE IF NOT EXISTS t_device_collect_binding (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    connect_id VARCHAR(32) NOT NULL,
    equip_sn VARCHAR(64) NOT NULL COMMENT '设备SN',
    source_key VARCHAR(256) NOT NULL COMMENT '数据源: MQTT topic / OPC nodeId / Modbus key',
    parse_rule_json TEXT COMMENT '解析规则 JSONPath 等',
    tenant_id VARCHAR(32),
    created_by VARCHAR(32),
    created_id VARCHAR(32),
    created_time DATETIME,
    updated_by VARCHAR(32),
    updated_id VARCHAR(32),
    updated_time DATETIME
) COMMENT '设备采集绑定';

CREATE INDEX idx_binding_connect_source ON t_device_collect_binding(connect_id, source_key);
CREATE INDEX idx_config_connect ON t_connect_collect_config(connect_id);

-- 5. 连接 Node-RED 规则流表（每个连接对应一组 Node-RED flow）
CREATE TABLE IF NOT EXISTS t_connect_flow (
    id VARCHAR(32) NOT NULL PRIMARY KEY,
    connect_id VARCHAR(32) NOT NULL UNIQUE COMMENT '连接ID，每个连接唯一',
    flow_json LONGTEXT COMMENT 'Node-RED flow 配置 JSON',
    tenant_id VARCHAR(32),
    created_by VARCHAR(32),
    created_id VARCHAR(32),
    created_time DATETIME,
    updated_by VARCHAR(32),
    updated_id VARCHAR(32),
    updated_time DATETIME
) COMMENT '连接Node-RED规则流';

CREATE INDEX idx_connect_flow_connect ON t_connect_flow(connect_id);
