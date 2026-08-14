-- =============================================================================
-- 工艺卡片模块 · 卸载脚本（PostgreSQL）
-- 适用：已有 OMES 库曾执行过工艺卡片增量脚本，现仅保留产线工艺
-- 全新安装可忽略本文件（data.sql 已不再创建工艺卡片表）
-- =============================================================================

-- 1. 业务表
DROP TABLE IF EXISTS "public"."m_biz_process_step_tooling";
DROP TABLE IF EXISTS "public"."m_biz_process_step_equipment";
DROP TABLE IF EXISTS "public"."r_biz_process_step_wip";
DROP TABLE IF EXISTS "public"."r_biz_process_step";
DROP TABLE IF EXISTS "public"."r_biz_process_mold";
DROP TABLE IF EXISTS "public"."t_biz_process";

-- 2. 菜单权限及租户/角色绑定
DELETE FROM "public"."r_ucenter_role_permission"
WHERE "permission_id" IN (
  SELECT "id" FROM "public"."p_ucenter_permission"
  WHERE "code" IN ('process_card', 'process_card_mg')
);

DELETE FROM "public"."r_ucenter_tenant_permission"
WHERE "permission_id" IN (
  SELECT "id" FROM "public"."p_ucenter_permission"
  WHERE "code" IN ('process_card', 'process_card_mg')
);

DELETE FROM "public"."p_ucenter_permission"
WHERE "code" IN ('process_card', 'process_card_mg');
