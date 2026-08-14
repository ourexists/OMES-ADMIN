-- =============================================================================
-- 设备能力：绑定设备，容量按「能力方案 × 设备 × 原料」配置，不写入设备档案。
-- =============================================================================

ALTER TABLE "public"."t_equip" DROP COLUMN IF EXISTS "max_capacity";
ALTER TABLE "public"."r_dg_equip" DROP COLUMN IF EXISTS "max_capacity";
DROP TABLE IF EXISTS "public"."t_device";

DROP TABLE IF EXISTS "public"."r_equip_mat";
DROP TABLE IF EXISTS "public"."r_dg_equip_mat";

CREATE TABLE IF NOT EXISTS "public"."r_dg_equip" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "dg_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  CONSTRAINT "r_dg_equip_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "uk_r_dg_equip" UNIQUE ("dg_id", "equip_id")
);
COMMENT ON COLUMN "public"."r_dg_equip"."dg_id" IS '设备能力id';
COMMENT ON COLUMN "public"."r_dg_equip"."equip_id" IS '设备id（t_equip）';
COMMENT ON TABLE "public"."r_dg_equip" IS '设备能力绑定设备';

CREATE TABLE "public"."r_dg_equip_mat" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "dg_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "max_capacity" numeric(18,4),
  CONSTRAINT "r_dg_equip_mat_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "uk_r_dg_equip_mat" UNIQUE ("dg_id", "equip_id", "mat_code")
);
COMMENT ON COLUMN "public"."r_dg_equip_mat"."dg_id" IS '设备能力id';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."equip_id" IS '设备id';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."mat_code" IS '原料编号（t_mat.self_code）';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."max_capacity" IS '该原料在本能力方案下的容量';
COMMENT ON TABLE "public"."r_dg_equip_mat" IS '设备能力加工原料及容量';

COMMENT ON TABLE "public"."t_dg" IS '设备能力';
COMMENT ON COLUMN "public"."r_mo_d"."dg_code" IS '设备能力code';
COMMENT ON COLUMN "public"."r_mo_d"."dg_name" IS '设备能力名';
COMMENT ON COLUMN "public"."r_mps_d"."dg_code" IS '设备能力code';
COMMENT ON COLUMN "public"."r_mps_d"."dg_name" IS '设备能力名';
