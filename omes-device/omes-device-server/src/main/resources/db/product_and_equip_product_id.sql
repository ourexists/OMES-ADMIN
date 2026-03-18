-- 产品管理表（原设备类型独立为产品）
CREATE TABLE IF NOT EXISTS t_product (
    id VARCHAR(64) NOT NULL PRIMARY KEY,
    name VARCHAR(128) DEFAULT NULL COMMENT '产品名称',
    code VARCHAR(64) DEFAULT NULL COMMENT '产品编号',
    image_url VARCHAR(512) DEFAULT NULL COMMENT '产品图片地址',
    tenant_id VARCHAR(64) DEFAULT NULL,
    created_by VARCHAR(64) DEFAULT NULL,
    created_id VARCHAR(64) DEFAULT NULL,
    created_time DATETIME DEFAULT NULL,
    updated_by VARCHAR(64) DEFAULT NULL,
    updated_id VARCHAR(64) DEFAULT NULL,
    updated_time DATETIME DEFAULT NULL
) COMMENT '产品（设备所属产品）';

-- 设备表增加所属产品ID
ALTER TABLE t_equip ADD COLUMN product_id VARCHAR(64) DEFAULT NULL COMMENT '所属产品ID' AFTER type;

-- 已有 t_product 表时，增加产品图片地址字段（若表为新建可忽略）
-- ALTER TABLE t_product ADD COLUMN image_url VARCHAR(512) DEFAULT NULL COMMENT '产品图片地址' AFTER code;
