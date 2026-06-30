-- =============================================================================
-- 工艺卡片模块 · 增量迁移脚本（PostgreSQL）
-- 适用：已有 OMES 库，仅新增工艺卡片表与菜单权限
-- 全新安装可忽略本文件，直接使用 db/data.sql 全量建库
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. 业务表（IF NOT EXISTS，可重复执行）
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS "public"."t_biz_process" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "process_code" varchar(64) NOT NULL,
  "process_image_url" varchar(512),
  "process_name" varchar(128) NOT NULL,
  "product_code" varchar(64),
  "product_name" varchar(128),
  "component_code" varchar(64),
  "component_name" varchar(128),
  "material_code" varchar(64),
  "material_name" varchar(128),
  "tech_condition" varchar(64),
  "material_preheat" varchar(256),
  "press_pressure" numeric(12,4),
  "blank_weight" numeric(12,4),
  "blank_weight_upper_offset" numeric(12,4),
  "blank_weight_lower_offset" numeric(12,4),
  "press_temperature" numeric(12,4),
  "press_temperature_upper_offset" numeric(12,4),
  "press_temperature_lower_offset" numeric(12,4),
  "hold_time_seconds" int4,
  PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_t_biz_process_code ON "public"."t_biz_process" ("process_code");
CREATE INDEX IF NOT EXISTS idx_t_biz_process_name ON "public"."t_biz_process" ("process_name");
COMMENT ON TABLE "public"."t_biz_process" IS '工艺卡片';

CREATE TABLE IF NOT EXISTS "public"."r_biz_process_mold" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "process_id" varchar(20) NOT NULL,
  "mold_drawing_no" varchar(128) NOT NULL,
  "slot_count" int4 NOT NULL,
  "sort_order" int4 DEFAULT 0 NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS idx_r_biz_process_mold_process ON "public"."r_biz_process_mold" ("process_id");
COMMENT ON TABLE "public"."r_biz_process_mold" IS '工艺压模图号';

CREATE TABLE IF NOT EXISTS "public"."r_biz_process_step" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "process_id" varchar(20) NOT NULL,
  "step_no" int4,
  "step_code" varchar(64),
  "step_name" varchar(128) NOT NULL,
  "step_content" varchar(2000),
  "step_script" text,
  "step_engine_config" text,
  "params" text,
  "sort_order" int4 DEFAULT 0 NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS idx_r_biz_process_step_process ON "public"."r_biz_process_step" ("process_id");
CREATE INDEX IF NOT EXISTS idx_r_biz_process_step_code ON "public"."r_biz_process_step" ("process_id", "step_code");
COMMENT ON TABLE "public"."r_biz_process_step" IS '工艺工序';

CREATE TABLE IF NOT EXISTS "public"."r_biz_process_step_wip" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "step_name" varchar(128) NOT NULL,
  "produce_wip_flag" int2 DEFAULT 0 NOT NULL,
  "direct_transfer_flag" int2 DEFAULT 0 NOT NULL,
  "wip_type" varchar(64),
  "wip_hold_time_hours" numeric(10,2),
  "schedule_device_code" varchar(64),
  "wip_trigger_target_step_name" varchar(128),
  PRIMARY KEY ("id")
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_r_biz_process_step_wip_name ON "public"."r_biz_process_step_wip" ("step_name");
CREATE INDEX IF NOT EXISTS idx_r_biz_process_step_wip_type ON "public"."r_biz_process_step_wip" ("wip_type");
COMMENT ON TABLE "public"."r_biz_process_step_wip" IS '工序WIP排产配置';

CREATE TABLE IF NOT EXISTS "public"."m_biz_process_step_equipment" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "step_id" varchar(20) NOT NULL,
  "equipment_code" varchar(64) NOT NULL,
  "equipment_name" varchar(128) NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS idx_m_biz_process_step_equip_step ON "public"."m_biz_process_step_equipment" ("step_id");
COMMENT ON TABLE "public"."m_biz_process_step_equipment" IS '工序设备关联';

CREATE TABLE IF NOT EXISTS "public"."m_biz_process_step_tooling" (
  "id" varchar(20) NOT NULL,
  "revision" int4,
  "created_by" varchar(64),
  "created_id" varchar(20),
  "created_time" timestamp(6),
  "updated_by" varchar(64),
  "updated_id" varchar(20),
  "updated_time" timestamp(6),
  "tenant_id" varchar(20),
  "step_id" varchar(20) NOT NULL,
  "tooling_code" varchar(64) NOT NULL,
  "tooling_name" varchar(128) NOT NULL,
  PRIMARY KEY ("id")
);

CREATE INDEX IF NOT EXISTS idx_m_biz_process_step_tool_step ON "public"."m_biz_process_step_tooling" ("step_id");
COMMENT ON TABLE "public"."m_biz_process_step_tooling" IS '工序工装关联';

-- -----------------------------------------------------------------------------
-- 2. 菜单权限（platform 默认 mes-edge，与 omes-web-admin 一致）
--    执行后请在「系统权限」中为租户/角色授权，或取消下方注释按 id 绑定
-- -----------------------------------------------------------------------------

INSERT INTO "public"."p_ucenter_permission" (
  "id", "name", "i18n", "code", "pcode", "strategy", "icon", "url", "sort_no", "type", "platform", "keep_alive", "created_time", "updated_time"
) VALUES
  ('pcard000000000001', '工艺卡片', 'processCardPage.title', 'process_card', NULL, 0, 'SettingOutlined', NULL, 45, 0, 'mes-edge', false, NOW(), NOW()),
  ('pcard000000000002', '工艺管理', 'processCardPage.title', 'process_card_mg', 'process_card', 0, NULL, '/view/process_manage_tables', 1, 0, 'mes-edge', true, NOW(), NOW())
ON CONFLICT ("code") DO NOTHING;

-- 绑定租户（将 YOUR_TENANT_ID 换成实际租户 id，如框架默认租户）
-- INSERT INTO "public"."r_ucenter_tenant_permission" ("tenant_id", "permission_id")
-- SELECT 'YOUR_TENANT_ID', "id" FROM "public"."p_ucenter_permission" WHERE "code" IN ('process_card', 'process_card_mg')
-- ON CONFLICT DO NOTHING;

-- 绑定角色（将 YOUR_ROLE_ID 换成管理员角色 id）
-- INSERT INTO "public"."r_ucenter_role_permission" ("role_id", "permission_id")
-- SELECT 'YOUR_ROLE_ID', "id" FROM "public"."p_ucenter_permission" WHERE "code" IN ('process_card', 'process_card_mg')
-- ON CONFLICT DO NOTHING;
