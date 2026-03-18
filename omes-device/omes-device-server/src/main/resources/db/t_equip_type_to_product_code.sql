-- 将 t_equip.type 从整型改为产品编号（关联 t_product.code），并移除 product_id
-- 执行前请确保 t_product 中已有与旧 type 对应的产品（如 code='0','1','2'），否则先执行下方插入

-- 可选：若尚未有与旧枚举对应的产品，先插入（按需调整 id）
-- INSERT INTO t_product (id, name, code, created_time, updated_time) VALUES
--   (REPLACE(UUID(),'-',''), '通用设备', '0', NOW(), NOW()),
--   (REPLACE(UUID(),'-',''), '水泵', '1', NOW(), NOW()),
--   (REPLACE(UUID(),'-',''), '液位计', '2', NOW(), NOW())
-- ON DUPLICATE KEY UPDATE name=VALUES(name);
-- 若表无唯一键，可逐条 INSERT IGNORE 或先查再插；

-- 1. 将 type 改为 VARCHAR，存产品编号（原 0/1/2 会变为 '0'/'1'/'2'）
ALTER TABLE t_equip MODIFY COLUMN type VARCHAR(64) DEFAULT NULL COMMENT '所属产品编号(关联t_product.code)';

-- 2. 删除原“所属产品ID”字段（若之前未加过 product_id 可跳过本句）
ALTER TABLE t_equip DROP COLUMN product_id;
