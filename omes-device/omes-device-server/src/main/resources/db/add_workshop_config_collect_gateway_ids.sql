-- 场景采集配置表：增加关联网关ID列表，用于按网关快速查询 tags
-- 执行前请确认表名与当前环境一致（默认 r_workshop_config_collect）
ALTER TABLE r_workshop_config_collect ADD COLUMN gateway_ids JSON NULL COMMENT '关联的网关ID列表，从 config.attrs 解析';

-- 可选：为已有数据回填 gateway_ids（从 config 的 attrs 中提取 gwId）
-- 若使用 MySQL 且 config 为 JSON，可执行（按实际 config 结构调整）：
-- UPDATE r_workshop_config_collect c
-- SET c.gateway_ids = (
--   SELECT JSON_ARRAYAGG(DISTINCT a.gwId) FROM JSON_TABLE(c.config, '$.attrs[*]' COLUMNS (gwId VARCHAR(255) PATH '$.gwId')) AS a WHERE a.gwId IS NOT NULL AND a.gwId != ''
-- )
-- WHERE c.config IS NOT NULL AND JSON_LENGTH(c.config, '$.attrs') > 0;
