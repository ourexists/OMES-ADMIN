-- 将设备定位迁移到场景，并删除 t_equip 上已废弃的经纬度/地址字段。
ALTER TABLE "public"."t_workshop"
    ADD COLUMN IF NOT EXISTS "lng" numeric(12,8);
ALTER TABLE "public"."t_workshop"
    ADD COLUMN IF NOT EXISTS "lat" numeric(12,8);
ALTER TABLE "public"."t_workshop"
    ADD COLUMN IF NOT EXISTS "address" varchar(255);

COMMENT ON COLUMN "public"."t_workshop"."lng" IS '经度（建议标注在最后一级场景）';
COMMENT ON COLUMN "public"."t_workshop"."lat" IS '纬度（建议标注在最后一级场景）';
COMMENT ON COLUMN "public"."t_workshop"."address" IS '地址';

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 't_equip'
          AND column_name = 'lng'
    ) THEN
        UPDATE "public"."t_workshop" w
        SET "lng" = src."lng",
            "lat" = src."lat",
            "address" = COALESCE(w."address", src."address")
        FROM (
            SELECT DISTINCT ON ("workshop_code")
                "workshop_code",
                "lng",
                "lat",
                "address"
            FROM "public"."t_equip"
            WHERE "lng" IS NOT NULL
              AND "lat" IS NOT NULL
              AND NOT ("lng" = 0 AND "lat" = 0)
              AND "workshop_code" IS NOT NULL
              AND "workshop_code" <> ''
            ORDER BY "workshop_code", "updated_time" DESC NULLS LAST
        ) src
        WHERE w."self_code" = src."workshop_code"
          AND w."lng" IS NULL
          AND w."lat" IS NULL;

        ALTER TABLE "public"."t_equip" DROP COLUMN IF EXISTS "lng";
        ALTER TABLE "public"."t_equip" DROP COLUMN IF EXISTS "lat";
        ALTER TABLE "public"."t_equip" DROP COLUMN IF EXISTS "address";
    END IF;
END $$;
