-- 系统配置表 + 菜单（系统中心下）
-- 执行后需重启 Admin / SAS 使接口生效

CREATE TABLE IF NOT EXISTS "public"."t_system_config" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "config_key" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "config" text COLLATE "pg_catalog"."default",
  CONSTRAINT "t_system_config_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "uk_t_system_config_key" UNIQUE ("config_key")
);

COMMENT ON COLUMN "public"."t_system_config"."config_key" IS 'config key, e.g. app';
COMMENT ON COLUMN "public"."t_system_config"."config" IS 'config JSON';
COMMENT ON TABLE "public"."t_system_config" IS 'system config';

-- seed baidu map AK (from former VITE_BAIDU_MAP_AK)
INSERT INTO "public"."t_system_config" (
  "id", "revision", "created_time", "updated_time", "config_key", "config"
) VALUES (
  '2069000000000000001', 0, NOW(), NOW(), 'app',
  '{"baiduMapAk":"bqz5hIv33XPsCBJKQkDcJurUpMovTrIZ"}'
) ON CONFLICT ("config_key") DO NOTHING;

-- menu: System Center > System Config
INSERT INTO "public"."p_ucenter_permission" (
  "id", "revision", "created_time", "updated_time",
  "name", "i18n", "code", "pcode", "ppcode",
  "strategy", "icon", "component", "url", "sort_no", "type",
  "description", "internal_or_external", "platform", "keep_alive"
) VALUES (
  '2069000000000000002', 0, NOW(), NOW(),
  E'\u7CFB\u7EDF\u914D\u7F6E', 'menu.systemConfig', '005006', '005', ';005;',
  0, 'ToolOutlined', NULL, '/view/system_config', 10, 0,
  E'\u7CFB\u7EDF\u53C2\u6570\u914D\u7F6E', 0, 'mes-edge', false
) ON CONFLICT ("id") DO NOTHING;

-- grant to roles that already have System Center
INSERT INTO "public"."r_ucenter_role_permission" ("role_id", "permission_id")
SELECT rp.role_id, '2069000000000000002'
FROM "public"."r_ucenter_role_permission" rp
WHERE rp.permission_id = '1995408785283203074'
  AND NOT EXISTS (
    SELECT 1 FROM "public"."r_ucenter_role_permission" x
    WHERE x.role_id = rp.role_id AND x.permission_id = '2069000000000000002'
  );

-- fix garbled name if earlier insert used wrong encoding
UPDATE "public"."p_ucenter_permission"
SET "name" = E'\u7CFB\u7EDF\u914D\u7F6E',
    "description" = E'\u7CFB\u7EDF\u53C2\u6570\u914D\u7F6E'
WHERE "id" = '2069000000000000002';
