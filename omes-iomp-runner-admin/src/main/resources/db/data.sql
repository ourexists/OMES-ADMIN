/*
 Navicat Premium Dump SQL

 Source Server         : localhost_5432
 Source Server Type    : PostgreSQL
 Source Server Version : 160013 (160013)
 Source Host           : localhost:5432
 Source Catalog        : omes
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 160013 (160013)
 File Encoding         : 65001

 Date: 04/06/2026 10:51:24
*/


-- ----------------------------
-- Table structure for ai_agent_audit_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_agent_audit_log";
CREATE TABLE "public"."ai_agent_audit_log" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "session_id" varchar(64) COLLATE "pg_catalog"."default",
  "operator_id" varchar(128) COLLATE "pg_catalog"."default",
  "action" varchar(64) COLLATE "pg_catalog"."default",
  "success_flag" int2 NOT NULL,
  "message" text COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL
)
;

-- ----------------------------
-- Table structure for ai_agent_chat_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_agent_chat_message";
CREATE TABLE "public"."ai_agent_chat_message" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "session_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "role" varchar(64) COLLATE "pg_catalog"."default",
  "content" text COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL
)
;

-- ----------------------------
-- Table structure for ai_agent_chat_session
-- ----------------------------
DROP TABLE IF EXISTS "public"."ai_agent_chat_session";
CREATE TABLE "public"."ai_agent_chat_session" (
  "session_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "title" varchar(255) COLLATE "pg_catalog"."default",
  "operator_id" varchar(128) COLLATE "pg_catalog"."default",
  "created_at" timestamp(6) NOT NULL,
  "updated_at" timestamp(6) NOT NULL
)
;

-- ----------------------------
-- Table structure for oauth2_authorization
-- ----------------------------
DROP TABLE IF EXISTS "public"."oauth2_authorization";
CREATE TABLE "public"."oauth2_authorization" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "registered_client_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "principal_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "authorization_grant_type" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "authorized_scopes" varchar(1000) COLLATE "pg_catalog"."default",
  "attributes" bytea,
  "state" varchar(500) COLLATE "pg_catalog"."default",
  "authorization_code_value" bytea,
  "authorization_code_issued_at" timestamp(6),
  "authorization_code_expires_at" timestamp(6),
  "authorization_code_metadata" bytea,
  "access_token_value" bytea,
  "access_token_issued_at" timestamp(6),
  "access_token_expires_at" timestamp(6),
  "access_token_metadata" bytea,
  "access_token_type" varchar(100) COLLATE "pg_catalog"."default",
  "access_token_scopes" varchar(1000) COLLATE "pg_catalog"."default",
  "oidc_id_token_value" bytea,
  "oidc_id_token_issued_at" timestamp(6),
  "oidc_id_token_expires_at" timestamp(6),
  "oidc_id_token_metadata" bytea,
  "refresh_token_value" bytea,
  "refresh_token_issued_at" timestamp(6),
  "refresh_token_expires_at" timestamp(6),
  "refresh_token_metadata" bytea,
  "user_code_value" bytea,
  "user_code_issued_at" timestamp(6),
  "user_code_expires_at" timestamp(6),
  "user_code_metadata" bytea,
  "device_code_value" bytea,
  "device_code_issued_at" timestamp(6),
  "device_code_expires_at" timestamp(6),
  "device_code_metadata" bytea
)
;

-- ----------------------------
-- Table structure for oauth2_authorization_consent
-- ----------------------------
DROP TABLE IF EXISTS "public"."oauth2_authorization_consent";
CREATE TABLE "public"."oauth2_authorization_consent" (
  "registered_client_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "principal_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "authorities" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for oauth2_registered_client
-- ----------------------------
DROP TABLE IF EXISTS "public"."oauth2_registered_client";
CREATE TABLE "public"."oauth2_registered_client" (
  "id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "client_id" varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  "client_id_issued_at" timestamp(6) NOT NULL,
  "client_secret" varchar(200) COLLATE "pg_catalog"."default",
  "client_secret_expires_at" timestamp(6),
  "client_name" varchar(200) COLLATE "pg_catalog"."default" NOT NULL,
  "client_authentication_methods" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "authorization_grant_types" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "redirect_uris" varchar(1000) COLLATE "pg_catalog"."default",
  "post_logout_redirect_uris" varchar(1000) COLLATE "pg_catalog"."default",
  "scopes" varchar(1000) COLLATE "pg_catalog"."default" NOT NULL,
  "client_settings" varchar(2000) COLLATE "pg_catalog"."default" NOT NULL,
  "token_settings" varchar(2000) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for oauth_client_details
-- ----------------------------
DROP TABLE IF EXISTS "public"."oauth_client_details";
CREATE TABLE "public"."oauth_client_details" (
  "client_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "resource_ids" varchar(256) COLLATE "pg_catalog"."default",
  "client_secret" varchar(256) COLLATE "pg_catalog"."default",
  "scope" varchar(256) COLLATE "pg_catalog"."default",
  "authorized_grant_types" varchar(256) COLLATE "pg_catalog"."default",
  "web_server_redirect_uri" varchar(256) COLLATE "pg_catalog"."default",
  "authorities" varchar(256) COLLATE "pg_catalog"."default",
  "access_token_validity" int4,
  "refresh_token_validity" int4,
  "additional_information" varchar(4096) COLLATE "pg_catalog"."default",
  "autoapprove" varchar(256) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for p_ucenter_acc
-- ----------------------------
DROP TABLE IF EXISTS "public"."p_ucenter_acc";
CREATE TABLE "public"."p_ucenter_acc" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "acc_name" varchar(60) COLLATE "pg_catalog"."default" NOT NULL,
  "password" varchar(60) COLLATE "pg_catalog"."default" NOT NULL,
  "nick_name" varchar(60) COLLATE "pg_catalog"."default" NOT NULL,
  "user_name" varchar(60) COLLATE "pg_catalog"."default" NOT NULL,
  "id_card" varchar(50) COLLATE "pg_catalog"."default",
  "mobile" varchar(50) COLLATE "pg_catalog"."default",
  "email" varchar(70) COLLATE "pg_catalog"."default",
  "sex" int2,
  "init" int2,
  "status" int2,
  "settled_time" timestamp(6),
  "expire_time" timestamp(6),
  "birth_day" timestamp(6),
  "del_flag" int2 DEFAULT 0,
  "source" varchar(20) COLLATE "pg_catalog"."default",
  "source_id" varchar(64) COLLATE "pg_catalog"."default",
  "platform" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "union_id" varchar(64) COLLATE "pg_catalog"."default",
  "avatar_url" varchar(255) COLLATE "pg_catalog"."default",
  "country" varchar(15) COLLATE "pg_catalog"."default",
  "province" varchar(15) COLLATE "pg_catalog"."default",
  "city" varchar(25) COLLATE "pg_catalog"."default",
  "language" varchar(10) COLLATE "pg_catalog"."default",
  "perfection" int2
)
;
COMMENT ON COLUMN "public"."p_ucenter_acc"."acc_name" IS 'è´¦æ·å';
COMMENT ON COLUMN "public"."p_ucenter_acc"."password" IS 'è´¦æ·å¯ç ';
COMMENT ON COLUMN "public"."p_ucenter_acc"."nick_name" IS 'è´¦æ·æµç§°';
COMMENT ON COLUMN "public"."p_ucenter_acc"."user_name" IS 'ç¨æ·å';
COMMENT ON COLUMN "public"."p_ucenter_acc"."id_card" IS 'èº«ä»½è¯å·';
COMMENT ON COLUMN "public"."p_ucenter_acc"."mobile" IS 'è´¦æ·ææºå·';
COMMENT ON COLUMN "public"."p_ucenter_acc"."email" IS 'è´¦æ·é®ç®±';
COMMENT ON COLUMN "public"."p_ucenter_acc"."sex" IS 'ç¨æ·æ§å«';
COMMENT ON COLUMN "public"."p_ucenter_acc"."init" IS 'åå§ç¶æ(1:æ¯åå§åè´¦æ·)';
COMMENT ON COLUMN "public"."p_ucenter_acc"."status" IS 'è´¦æ·ç¶æ';
COMMENT ON COLUMN "public"."p_ucenter_acc"."settled_time" IS 'å¥é©»æ¶é´';
COMMENT ON COLUMN "public"."p_ucenter_acc"."expire_time" IS 'è¿ææ¶é´';
COMMENT ON COLUMN "public"."p_ucenter_acc"."birth_day" IS 'åºçå¹´æ';
COMMENT ON COLUMN "public"."p_ucenter_acc"."del_flag" IS 'é»è¾å é¤';
COMMENT ON COLUMN "public"."p_ucenter_acc"."source" IS 'æ¥æº';
COMMENT ON COLUMN "public"."p_ucenter_acc"."source_id" IS 'æ¥æºid';
COMMENT ON COLUMN "public"."p_ucenter_acc"."platform" IS 'å¹³å°';
COMMENT ON COLUMN "public"."p_ucenter_acc"."union_id" IS 'æ¥æºæ¹äºéid';
COMMENT ON COLUMN "public"."p_ucenter_acc"."avatar_url" IS 'å¤´åè·¯å¾';
COMMENT ON COLUMN "public"."p_ucenter_acc"."country" IS 'å½å®¶';
COMMENT ON COLUMN "public"."p_ucenter_acc"."province" IS 'ç';
COMMENT ON COLUMN "public"."p_ucenter_acc"."city" IS 'å¸';
COMMENT ON COLUMN "public"."p_ucenter_acc"."language" IS 'è¯­è¨';
COMMENT ON COLUMN "public"."p_ucenter_acc"."perfection" IS 'è´¦æ·å®ååº¦ï¼0: æªå®åï¼1:å·²å®åï¼';
COMMENT ON TABLE "public"."p_ucenter_acc" IS 'è´¦æ·è¡¨';

-- ----------------------------
-- Table structure for p_ucenter_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."p_ucenter_permission";
CREATE TABLE "public"."p_ucenter_permission" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "name" varchar(40) COLLATE "pg_catalog"."default" NOT NULL,
  "i18n" varchar(40) COLLATE "pg_catalog"."default",
  "code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "pcode" varchar(30) COLLATE "pg_catalog"."default",
  "ppcode" varchar(150) COLLATE "pg_catalog"."default",
  "strategy" int2,
  "icon" varchar(50) COLLATE "pg_catalog"."default",
  "component" varchar(150) COLLATE "pg_catalog"."default",
  "url" varchar(150) COLLATE "pg_catalog"."default",
  "sort_no" float8,
  "type" int2,
  "description" varchar(250) COLLATE "pg_catalog"."default",
  "internal_or_external" int2,
  "platform" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "keep_alive" bool DEFAULT false
)
;
COMMENT ON COLUMN "public"."p_ucenter_permission"."name" IS 'åç§°';
COMMENT ON COLUMN "public"."p_ucenter_permission"."code" IS 'ç¼å·';
COMMENT ON COLUMN "public"."p_ucenter_permission"."pcode" IS 'ç¶ç¼å·';
COMMENT ON COLUMN "public"."p_ucenter_permission"."ppcode" IS 'ç¥å®ç¼å·';
COMMENT ON COLUMN "public"."p_ucenter_permission"."strategy" IS 'æéç­ç¥';
COMMENT ON COLUMN "public"."p_ucenter_permission"."icon" IS 'å¾æ ';
COMMENT ON COLUMN "public"."p_ucenter_permission"."component" IS 'ç»ä»¶';
COMMENT ON COLUMN "public"."p_ucenter_permission"."url" IS 'è·³è½¬ç½é¡µé¾æ¥';
COMMENT ON COLUMN "public"."p_ucenter_permission"."sort_no" IS 'èåæåº';
COMMENT ON COLUMN "public"."p_ucenter_permission"."type" IS 'æéç±»å(0:èåæé  1:æé®æé  2:ç©ºæè¿°æé)';
COMMENT ON COLUMN "public"."p_ucenter_permission"."description" IS 'æè¿°';
COMMENT ON COLUMN "public"."p_ucenter_permission"."internal_or_external" IS 'å¤é¾èåæå¼æ¹å¼(0:åé¨æå¼ 1:å¤é¨æå¼)';
COMMENT ON COLUMN "public"."p_ucenter_permission"."platform" IS 'æå±å¹³å°';
COMMENT ON TABLE "public"."p_ucenter_permission" IS 'æéè¡¨';

-- ----------------------------
-- Table structure for p_ucenter_platform
-- ----------------------------
DROP TABLE IF EXISTS "public"."p_ucenter_platform";
CREATE TABLE "public"."p_ucenter_platform" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(25) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6)
)
;
COMMENT ON COLUMN "public"."p_ucenter_platform"."name" IS 'åç§°';
COMMENT ON COLUMN "public"."p_ucenter_platform"."code" IS 'ç¼å·';

-- ----------------------------
-- Table structure for p_ucenter_tenant
-- ----------------------------
DROP TABLE IF EXISTS "public"."p_ucenter_tenant";
CREATE TABLE "public"."p_ucenter_tenant" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int2,
  "settled_time" timestamp(6),
  "expire_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_address" varchar(50) COLLATE "pg_catalog"."default",
  "tenant_contacts" varchar(10) COLLATE "pg_catalog"."default",
  "tenant_phone" varchar(50) COLLATE "pg_catalog"."default",
  "tenant_mail" varchar(40) COLLATE "pg_catalog"."default",
  "province_code" varchar(40) COLLATE "pg_catalog"."default",
  "city_code" varchar(40) COLLATE "pg_catalog"."default",
  "county_code" varchar(40) COLLATE "pg_catalog"."default",
  "street_code" varchar(40) COLLATE "pg_catalog"."default",
  "area_fullname" varchar(100) COLLATE "pg_catalog"."default",
  "tenant_coo" varchar(50) COLLATE "pg_catalog"."default",
  "pcode" varchar(20) COLLATE "pg_catalog"."default",
  "client_id" varchar(50) COLLATE "pg_catalog"."default",
  "management" int2,
  "manage_num" int4,
  "logo" varchar(150) COLLATE "pg_catalog"."default",
  "tel" varchar(150) COLLATE "pg_catalog"."default",
  "del_flag" int2 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_name" IS 'ç§æ·å';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_code" IS 'ç§æ·ç¼å·';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."status" IS 'ç§æ·ç¶æ';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."settled_time" IS 'å¥é©»æ¶é´';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."expire_time" IS 'è¿ææ¶é´';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_id" IS 'éåæ¡æ¶å ä½';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_address" IS 'ç§æ·/æºææå¨å°å';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_contacts" IS 'èç³»äººå§å';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_phone" IS 'æå¡çµè¯';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_mail" IS 'èç³»é®ç®±';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."province_code" IS 'çä»½codeï¼è¯¦è§yl_elder.r_city_dictçå¸çº§èå­å¸è¡¨code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."city_code" IS 'åå¸codeï¼è¯¦è§yl_elder.r_city_dictçå¸çº§èå­å¸è¡¨code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."county_code" IS 'åºå¿codeï¼è¯¦è§yl_elder.r_city_dictçå¸çº§èå­å¸è¡¨code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."street_code" IS 'è¡écodeï¼è¯¦è§yl_elder.r_city_dictçå¸çº§èå­å¸è¡¨code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."area_fullname" IS 'æå¨åºåï¼ç-å¸-åºå¿-è¡éæ¼æ¥';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_coo" IS 'ç§æ·æºæç»çº¬åº¦åæ ';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."client_id" IS 'è¡¥åä¸´æ¶å­æ®µï¼ä½ç¨å¾å®';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."management" IS 'ç®¡æ§æé';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."manage_num" IS 'ç®¡æ§æ°é';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."logo" IS 'logo';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tel" IS 'å®¢æçµè¯';
COMMENT ON TABLE "public"."p_ucenter_tenant" IS 'ç§æ·è¡¨';

-- ----------------------------
-- Table structure for r_bom_d
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_bom_d";
CREATE TABLE "public"."r_bom_d" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "del_bit" int2 DEFAULT 0,
  "mat_id" varchar(20) COLLATE "pg_catalog"."default",
  "mat_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_code" varchar(50) COLLATE "pg_catalog"."default",
  "mcode" varchar(50) COLLATE "pg_catalog"."default",
  "mat_scale" numeric(10,4),
  "dev_no" varchar(4) COLLATE "pg_catalog"."default",
  "attribute" int2,
  "priority" int4
)
;
COMMENT ON COLUMN "public"."r_bom_d"."mat_id" IS 'ç©æid';
COMMENT ON COLUMN "public"."r_bom_d"."mat_name" IS 'ç©æåç§°';
COMMENT ON COLUMN "public"."r_bom_d"."mat_code" IS 'ç©æç¼å·';
COMMENT ON COLUMN "public"."r_bom_d"."mcode" IS 'ä¸»ä½ç¼å·';
COMMENT ON COLUMN "public"."r_bom_d"."mat_scale" IS 'ç©ææ°å¼ï¼å¯¹åºç©ææ¸åçç±»åï¼å³å®æ¯ç¾åæ¯è¿æ¯åºå®å¼ï¼';
COMMENT ON COLUMN "public"."r_bom_d"."dev_no" IS 'è®¾å¤ç¼å·';
COMMENT ON COLUMN "public"."r_bom_d"."attribute" IS 'ç»ä»½æ§è´¨ï¼0=ä¸»æï¼1=é¢æ··æï¼æ·»å åï¼,2=åæºæ,3=æ²¹,4=æ°´';
COMMENT ON COLUMN "public"."r_bom_d"."priority" IS 'ä¼åçº§ï¼æ°å¼è¶å¤§ï¼ä¼åçº§è¶é«ï¼';
COMMENT ON TABLE "public"."r_bom_d" IS 'éæ¹è¯¦æ';

-- ----------------------------
-- Table structure for r_dg_d
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_dg_d";
CREATE TABLE "public"."r_dg_d" (
  "dg_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "d_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_dg_d"."dg_id" IS 'è®¾å¤ç»id';
COMMENT ON COLUMN "public"."r_dg_d"."d_id" IS 'è®¾å¤id';
COMMENT ON COLUMN "public"."r_dg_d"."mat_id" IS 'ç©æid';
COMMENT ON TABLE "public"."r_dg_d" IS 'è®¾å¤ç»å³è';

-- ----------------------------
-- Table structure for r_dg_equip
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_dg_equip";
CREATE TABLE "public"."r_dg_equip" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "dg_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  CONSTRAINT "r_dg_equip_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "uk_r_dg_equip" UNIQUE ("dg_id", "equip_id")
)
;
COMMENT ON COLUMN "public"."r_dg_equip"."dg_id" IS 'è®¾å¤è½åid';
COMMENT ON COLUMN "public"."r_dg_equip"."equip_id" IS 'è®¾å¤idï¼t_equipï¼';
COMMENT ON TABLE "public"."r_dg_equip" IS 'è®¾å¤è½åç»å®è®¾å¤';

-- ----------------------------
-- Table structure for r_dg_equip_mat
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_dg_equip_mat";
CREATE TABLE "public"."r_dg_equip_mat" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "dg_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "max_capacity" numeric(18,4),
  CONSTRAINT "r_dg_equip_mat_pkey" PRIMARY KEY ("id"),
  CONSTRAINT "uk_r_dg_equip_mat" UNIQUE ("dg_id", "equip_id", "mat_code")
)
;
COMMENT ON COLUMN "public"."r_dg_equip_mat"."dg_id" IS 'è®¾å¤è½åid';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."equip_id" IS 'è®¾å¤id';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."mat_code" IS 'åæç¼å·ï¼t_mat.self_codeï¼';
COMMENT ON COLUMN "public"."r_dg_equip_mat"."max_capacity" IS 'è¯¥åæå¨æ¬è½åæ¹æ¡ä¸çå®¹é';
COMMENT ON TABLE "public"."r_dg_equip_mat" IS 'è®¾å¤è½åå å·¥åæåå®¹é';

-- ----------------------------
-- Table structure for r_gw_binding
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_gw_binding";
CREATE TABLE "public"."r_gw_binding" (
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "gw_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."r_gw_binding"."equip_id" IS 'è®¾å¤id';
COMMENT ON COLUMN "public"."r_gw_binding"."gw_id" IS 'ç½å³id';

-- ----------------------------
-- Table structure for r_message_read
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_message_read";
CREATE TABLE "public"."r_message_read" (
  "message_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "time" timestamp(6),
  "is_read" bool NOT NULL DEFAULT false
)
;
COMMENT ON COLUMN "public"."r_message_read"."message_id" IS 'æ¶æ¯id';
COMMENT ON COLUMN "public"."r_message_read"."acc_id" IS 'è´¦æ·id';
COMMENT ON COLUMN "public"."r_message_read"."time" IS 'è¯»åæ¶é´';
COMMENT ON COLUMN "public"."r_message_read"."is_read" IS 'æ¯å¦å·²è¯»';

-- ----------------------------
-- Table structure for r_mo_d
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_mo_d";
CREATE TABLE "public"."r_mo_d" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "del_bit" int2 DEFAULT 0,
  "mat_id" varchar(20) COLLATE "pg_catalog"."default",
  "mat_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_num" numeric(10,5),
  "priority" int4,
  "mcode" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "dev_no" varchar(20) COLLATE "pg_catalog"."default",
  "dev_name" varchar(50) COLLATE "pg_catalog"."default",
  "dg_code" varchar(20) COLLATE "pg_catalog"."default",
  "dg_name" varchar(50) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."r_mo_d"."mat_id" IS 'ç©æid';
COMMENT ON COLUMN "public"."r_mo_d"."mat_name" IS 'ç©æåç§°';
COMMENT ON COLUMN "public"."r_mo_d"."mat_code" IS 'ç©æç¼å·';
COMMENT ON COLUMN "public"."r_mo_d"."mat_num" IS 'ç©ææ°é';
COMMENT ON COLUMN "public"."r_mo_d"."priority" IS 'ä¼åçº§ï¼æ°å¼è¶å¤§ï¼ä¼åçº§è¶é«ï¼';
COMMENT ON COLUMN "public"."r_mo_d"."mcode" IS 'å½å±æ¸å';
COMMENT ON COLUMN "public"."r_mo_d"."dev_no" IS 'è®¾å¤ç¼å·';
COMMENT ON COLUMN "public"."r_mo_d"."dev_name" IS 'è®¾å¤å';
COMMENT ON COLUMN "public"."r_mo_d"."dg_code" IS 'è®¾å¤è½åcode';
COMMENT ON COLUMN "public"."r_mo_d"."dg_name" IS 'è®¾å¤è½åå';
COMMENT ON TABLE "public"."r_mo_d" IS 'çäº§æ¸åè¯¦æ';

-- ----------------------------
-- Table structure for r_mps_d
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_mps_d";
CREATE TABLE "public"."r_mps_d" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "del_bit" int2 DEFAULT 0,
  "mat_id" varchar(20) COLLATE "pg_catalog"."default",
  "mat_name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mat_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mid" varchar(50) COLLATE "pg_catalog"."default",
  "mat_num" numeric(25,5),
  "actual_num" numeric(25,5),
  "dev_no" varchar(20) COLLATE "pg_catalog"."default",
  "dev_name" varchar(50) COLLATE "pg_catalog"."default",
  "attribute" int2,
  "priority" int4,
  "mo_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "dg_code" varchar(20) COLLATE "pg_catalog"."default",
  "dg_name" varchar(50) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."r_mps_d"."mat_id" IS 'åæid';
COMMENT ON COLUMN "public"."r_mps_d"."mat_name" IS 'åæåç§°';
COMMENT ON COLUMN "public"."r_mps_d"."mat_code" IS 'åæç¼å·';
COMMENT ON COLUMN "public"."r_mps_d"."mid" IS 'ä¸»ä½id';
COMMENT ON COLUMN "public"."r_mps_d"."mat_num" IS 'çè®ºå¼';
COMMENT ON COLUMN "public"."r_mps_d"."actual_num" IS 'å®éå¼';
COMMENT ON COLUMN "public"."r_mps_d"."dev_no" IS 'è®¾å¤å·';
COMMENT ON COLUMN "public"."r_mps_d"."dev_name" IS 'è®¾å¤å';
COMMENT ON COLUMN "public"."r_mps_d"."attribute" IS 'ç»ä»½æ§è´¨ï¼0=ä¸»æï¼1=é¢æ··æï¼æ·»å åï¼,2=åæºæ,3=æ²¹,4=æ°´';
COMMENT ON COLUMN "public"."r_mps_d"."priority" IS 'ææé¡ºåº';
COMMENT ON COLUMN "public"."r_mps_d"."mo_code" IS 'å½å±æ¸å';
COMMENT ON COLUMN "public"."r_mps_d"."dg_code" IS 'è®¾å¤è½åcode';
COMMENT ON COLUMN "public"."r_mps_d"."dg_name" IS 'è®¾å¤è½åå';
COMMENT ON TABLE "public"."r_mps_d" IS 'çäº§è®¡åè¯¦æ';

-- ----------------------------
-- Table structure for r_mps_tf
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_mps_tf";
CREATE TABLE "public"."r_mps_tf" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "mps_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" timestamp(6),
  "end_time" timestamp(6),
  "start_temperature" numeric(7,2),
  "end_temperature" numeric(7,2),
  "status" int2,
  "pre" varchar(50) COLLATE "pg_catalog"."default",
  "nex" varchar(50) COLLATE "pg_catalog"."default",
  "mo_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_mps_tf"."mps_id" IS 'æ§è¡è®¡åid';
COMMENT ON COLUMN "public"."r_mps_tf"."self_code" IS 'å·¥èºæµç¨ç¼å·(å¯ä»¥ä¸å·¥èºè®¾ç½®è¿è¡æ å°)';
COMMENT ON COLUMN "public"."r_mps_tf"."name" IS 'å·¥èºæµç¨åç§°';
COMMENT ON COLUMN "public"."r_mps_tf"."start_time" IS 'å¼å§æ§è¡çæ¶é´';
COMMENT ON COLUMN "public"."r_mps_tf"."end_time" IS 'æ§è¡å®ææ¶é´';
COMMENT ON COLUMN "public"."r_mps_tf"."start_temperature" IS 'å¥æ§½æ¸©åº¦';
COMMENT ON COLUMN "public"."r_mps_tf"."end_temperature" IS 'åºæ§½æ¸©åº¦';
COMMENT ON COLUMN "public"."r_mps_tf"."status" IS 'æµç¨ç¶æ(0:å¾æ§è¡|1æ§è¡ä¸­|2å·²å®æ|3åæ­¢|4å¼å¸¸ä¸­æ­)';
COMMENT ON COLUMN "public"."r_mps_tf"."pre" IS 'ä¸ä¸æµç¨ç¼å·';
COMMENT ON COLUMN "public"."r_mps_tf"."nex" IS 'ä¸ä¸æµç¨ç¼å·';
COMMENT ON COLUMN "public"."r_mps_tf"."mo_code" IS 'å½å±æ¸å';
COMMENT ON TABLE "public"."r_mps_tf" IS 'å·¥èºæµç¨';

-- ----------------------------
-- Table structure for r_sync_resource
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_sync_resource";
CREATE TABLE "public"."r_sync_resource" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "sync_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "point" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "status" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "req_data" text COLLATE "pg_catalog"."default",
  "resp_data" text COLLATE "pg_catalog"."default",
  "excep" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."r_sync_resource"."sync_id" IS 'åæ­¥äºå¡id';
COMMENT ON COLUMN "public"."r_sync_resource"."point" IS 'å½åäºå¡èµæº';
COMMENT ON COLUMN "public"."r_sync_resource"."status" IS 'å½åèµæºç¶æï¼start|error|endï¼';
COMMENT ON COLUMN "public"."r_sync_resource"."req_data" IS 'å½åèµæºå¥å';
COMMENT ON COLUMN "public"."r_sync_resource"."resp_data" IS 'å½åèµæºåºå';
COMMENT ON COLUMN "public"."r_sync_resource"."excep" IS 'å¼å¸¸ä¿¡æ¯';
COMMENT ON TABLE "public"."r_sync_resource" IS 'åæ­¥äºå¡èµæº';

-- ----------------------------
-- Table structure for r_ucenter_acc_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_acc_role";
CREATE TABLE "public"."r_ucenter_acc_role" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_acc_role"."role_id" IS 'è§è²id';
COMMENT ON COLUMN "public"."r_ucenter_acc_role"."acc_id" IS 'ç¨æ·id';
COMMENT ON TABLE "public"."r_ucenter_acc_role" IS 'è´¦æ·è§è²';

-- ----------------------------
-- Table structure for r_ucenter_depart_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_depart_role";
CREATE TABLE "public"."r_ucenter_depart_role" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "depart_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_depart_role"."role_id" IS 'è§è²id';
COMMENT ON COLUMN "public"."r_ucenter_depart_role"."depart_id" IS 'é¨é¨id';
COMMENT ON TABLE "public"."r_ucenter_depart_role" IS 'é¨é¨è§è²';

-- ----------------------------
-- Table structure for r_ucenter_depart_users
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_depart_users";
CREATE TABLE "public"."r_ucenter_depart_users" (
  "depart_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_depart_users"."depart_id" IS 'ç¨æ·ç»id';
COMMENT ON COLUMN "public"."r_ucenter_depart_users"."acc_id" IS 'ç¨æ·id';
COMMENT ON TABLE "public"."r_ucenter_depart_users" IS 'ç¨æ·ç»ç¨æ·å³èè¡¨';

-- ----------------------------
-- Table structure for r_ucenter_role_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_role_permission";
CREATE TABLE "public"."r_ucenter_role_permission" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_role_permission"."role_id" IS 'è§è²id';
COMMENT ON COLUMN "public"."r_ucenter_role_permission"."permission_id" IS 'æéid';
COMMENT ON TABLE "public"."r_ucenter_role_permission" IS 'è§è²æé';

-- ----------------------------
-- Table structure for r_ucenter_tenant_acc
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_tenant_acc";
CREATE TABLE "public"."r_ucenter_tenant_acc" (
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "role" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."tenant_id" IS 'ç§æ·id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."acc_id" IS 'ç¨æ·id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."role" IS 'è§è²';
COMMENT ON TABLE "public"."r_ucenter_tenant_acc" IS 'ç§æ·ç¨æ·å³èè¡¨';

-- ----------------------------
-- Table structure for r_ucenter_tenant_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_tenant_permission";
CREATE TABLE "public"."r_ucenter_tenant_permission" (
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_tenant_permission"."tenant_id" IS 'ç§æ·id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_permission"."permission_id" IS 'æéid';
COMMENT ON TABLE "public"."r_ucenter_tenant_permission" IS 'ç§æ·æé';

-- ----------------------------
-- Table structure for r_workshop_assign
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_workshop_assign";
CREATE TABLE "public"."r_workshop_assign" (
  "assign_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "workshop_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_workshop_assign"."assign_id" IS 'è§è²id';
COMMENT ON COLUMN "public"."r_workshop_assign"."workshop_code" IS 'åºæ¯ç¼å·';
COMMENT ON TABLE "public"."r_workshop_assign" IS 'è§è²åºæ¯';

-- ----------------------------
-- Table structure for r_workshop_config_collect
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_workshop_config_collect";
CREATE TABLE "public"."r_workshop_config_collect" (
  "workshop_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "config" text COLLATE "pg_catalog"."default",
  "gateway_ids" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."r_workshop_config_collect"."workshop_id" IS 'è®¾å¤id';
COMMENT ON COLUMN "public"."r_workshop_config_collect"."gateway_ids" IS 'å³èçç½å³IDåè¡¨ï¼ä» config.attrs è§£æ';
COMMENT ON TABLE "public"."r_workshop_config_collect" IS 'åºæ¯éééç½®';

-- ----------------------------
-- Table structure for r_workshop_config_meta2d
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_workshop_config_meta2d";
CREATE TABLE "public"."r_workshop_config_meta2d" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "workshop_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "meta2d_config" text COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6)
)
;

-- ----------------------------
-- Table structure for r_workshop_config_scada
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_workshop_config_scada";
CREATE TABLE "public"."r_workshop_config_scada" (
  "workshop_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "scada_config" text COLLATE "pg_catalog"."default",
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6)
)
;

-- ----------------------------
-- Table structure for t_bom
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_bom";
CREATE TABLE "public"."t_bom" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(50) COLLATE "pg_catalog"."default",
  "classify_code" varchar(50) COLLATE "pg_catalog"."default",
  "type" int2,
  "line" int4,
  "del_bit" int2 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."t_bom"."self_code" IS 'éæ¹ç¼å·';
COMMENT ON COLUMN "public"."t_bom"."classify_code" IS 'æå±åç±»ç¼å·';
COMMENT ON COLUMN "public"."t_bom"."type" IS 'éæ¹ç±»å«ï¼0: åºå®æ°å¼ï¼1ï¼ç¾åæ¯æ°å¼ï¼';
COMMENT ON COLUMN "public"."t_bom"."line" IS 'ææ¶æ ç¨';
COMMENT ON TABLE "public"."t_bom" IS 'éæ¹';

-- ----------------------------
-- Table structure for t_bom_c
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_bom_c";
CREATE TABLE "public"."t_bom_c" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(50) COLLATE "pg_catalog"."default",
  "code" varchar(60) COLLATE "pg_catalog"."default",
  "pcode" varchar(60) COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_bom_c"."name" IS 'åç±»åç§°';
COMMENT ON COLUMN "public"."t_bom_c"."self_code" IS 'åç±»ç¼å·';
COMMENT ON COLUMN "public"."t_bom_c"."code" IS 'çº§èç¼å·';
COMMENT ON COLUMN "public"."t_bom_c"."pcode" IS 'çº§èç¶ç¼å·';
COMMENT ON COLUMN "public"."t_bom_c"."description" IS 'æè¿°';
COMMENT ON TABLE "public"."t_bom_c" IS 'éæ¹åç±»';

-- ----------------------------
-- Table structure for t_connect
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_connect";
CREATE TABLE "public"."t_connect" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "server_name" varchar(50) COLLATE "pg_catalog"."default",
  "host" varchar(255) COLLATE "pg_catalog"."default",
  "port" int4,
  "suffix" varchar(50) COLLATE "pg_catalog"."default",
  "protocol" varchar(20) COLLATE "pg_catalog"."default",
  "enabled" varchar(1) COLLATE "pg_catalog"."default",
  "username" varchar(150) COLLATE "pg_catalog"."default",
  "password" varchar(150) COLLATE "pg_catalog"."default",
  "valid_type" int2,
  "collect_cron" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_connect"."server_name" IS 'æå¡å';
COMMENT ON COLUMN "public"."t_connect"."host" IS 'ip';
COMMENT ON COLUMN "public"."t_connect"."suffix" IS 'åç¼';
COMMENT ON COLUMN "public"."t_connect"."protocol" IS 'åè®®ç±»å';
COMMENT ON COLUMN "public"."t_connect"."enabled" IS 'æ¯å¦å¯ç¨';
COMMENT ON COLUMN "public"."t_connect"."valid_type" IS 'è®¤è¯ç±»åï¼0:æ è®¤è¯ï¼ 1:Baiscï¼';
COMMENT ON COLUMN "public"."t_connect"."collect_cron" IS 'éécronè¡¨è¾¾å¼';
COMMENT ON TABLE "public"."t_connect" IS 'ç½å³';

-- ----------------------------
-- Table structure for t_dg
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_dg";
CREATE TABLE "public"."t_dg" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(50) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_dg"."self_code" IS 'ç¼å·';
COMMENT ON TABLE "public"."t_dg" IS 'è®¾å¤è½å';

-- ----------------------------
-- Table structure for t_equip
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip";
CREATE TABLE "public"."t_equip" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(150) COLLATE "pg_catalog"."default" NOT NULL,
  "type" varchar(64) COLLATE "pg_catalog"."default",
  "workshop_code" varchar(50) COLLATE "pg_catalog"."default",
  "enable_date" timestamp(6),
  "health_template_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip"."name" IS 'è®¾å¤å';
COMMENT ON COLUMN "public"."t_equip"."self_code" IS 'è®¾å¤ç¼å·';
COMMENT ON COLUMN "public"."t_equip"."type" IS 'è®¾å¤ç±»å«';
COMMENT ON COLUMN "public"."t_equip"."workshop_code" IS 'åºæ¯ç¼å·';
COMMENT ON COLUMN "public"."t_equip"."enable_date" IS 'å¯ç¨æ¥æï¼ç¨äºå¥åº·åä½¿ç¨å¹´éè®¡ç®';
COMMENT ON COLUMN "public"."t_equip"."health_template_id" IS 'å³èå¥åº·è§åæ¨¡æ¿ID';
COMMENT ON TABLE "public"."t_equip" IS 'è®¾å¤è¡¨';

-- ----------------------------
-- Table structure for t_equip_collect
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_collect";
CREATE TABLE "public"."t_equip_collect" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "data" jsonb,
  "time" timestamp(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."t_equip_collect"."sn" IS 'è®¾å¤snå·';
COMMENT ON COLUMN "public"."t_equip_collect"."data" IS 'ééçæ°æ®';
COMMENT ON COLUMN "public"."t_equip_collect"."time" IS 'ééæ¶é´';

-- ----------------------------
-- Table structure for t_equip_health_indicator
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_health_indicator";
CREATE TABLE "public"."t_equip_health_indicator" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(32) COLLATE "pg_catalog"."default",
  "sn" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "stat_time" timestamp(6) NOT NULL,
  "period_start" timestamp(6),
  "period_end" timestamp(6),
  "score" int4,
  "health_level" int4,
  "alarm_count" int4,
  "alarm_duration_minutes" int8,
  "run_duration_minutes" int8,
  "online_duration_minutes" int8,
  "period_minutes" int8,
  "template_id" varchar(32) COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip_health_indicator"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."equip_id" IS 'è®¾å¤ID';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."sn" IS 'è®¾å¤èªç¼ç ';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."stat_time" IS 'ç»è®¡æ¶é´ç¹';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_start" IS 'å¨æå¼å§';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_end" IS 'å¨æç»æ';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."score" IS 'å¥åº·å¾å0-100';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."health_level" IS 'å¥åº·ç­çº§0=å¥åº·,1=å³æ³¨,2=é¢è­¦,3=æé';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."alarm_count" IS 'æ¥è­¦æ¬¡æ°';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."alarm_duration_minutes" IS 'æ¥è­¦æ»æ¶é¿(åé)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."run_duration_minutes" IS 'è¿è¡æ»æ¶é¿(åé)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."online_duration_minutes" IS 'å¨çº¿æ»æ¶é¿(åé)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_minutes" IS 'ç»è®¡å¨æ(åé)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."template_id" IS 'è§åæ¨¡æ¿ID';
COMMENT ON TABLE "public"."t_equip_health_indicator" IS 'è®¾å¤å¥åº·ææ ';

-- ----------------------------
-- Table structure for t_equip_health_rule_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_health_rule_template";
CREATE TABLE "public"."t_equip_health_rule_template" (
  "id" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "config" text COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."name" IS 'æ¨¡æ¿åç§°';
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."config" IS 'è§åéç½®JSON';
COMMENT ON TABLE "public"."t_equip_health_rule_template" IS 'è®¾å¤å¥åº·è§åæ¨¡æ¿';

-- ----------------------------
-- Table structure for t_equip_realtime
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_realtime";
CREATE TABLE "public"."t_equip_realtime" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "gw_id" varchar(64) COLLATE "pg_catalog"."default",
  "online_state" int4 NOT NULL DEFAULT 0,
  "run_state" int4 NOT NULL DEFAULT 0,
  "alarm_state" int4 NOT NULL DEFAULT 0,
  "online_change_time" timestamp(6),
  "run_change_time" timestamp(6),
  "alarm_change_time" timestamp(6),
  "workshop_code" varchar(128) COLLATE "pg_catalog"."default",
  "name" varchar(256) COLLATE "pg_catalog"."default",
  "equip_time" timestamp(6),
  "alarm_level" int4,
  "payload" jsonb NOT NULL,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4 DEFAULT 0
)
;

-- ----------------------------
-- Table structure for t_equip_record_alarm
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_record_alarm";
CREATE TABLE "public"."t_equip_record_alarm" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" timestamp(6) NOT NULL,
  "end_time" timestamp(6),
  "state" int2 NOT NULL,
  "reason" varchar(255) COLLATE "pg_catalog"."default" DEFAULT 0,
  "level" int2,
  "event_id" varchar(60) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip_record_alarm"."sn" IS 'è®¾å¤snå·';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."start_time" IS 'æç»­å¼å§æ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."end_time" IS 'æç»­ç»ææ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."state" IS 'ç¶æ';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."reason" IS 'æ¥è­¦åå ';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."level" IS 'æ¥è­¦ç­çº§: 0=è½»å¾®, 1=ä¸è¬, 2=ä¸¥é, 3=æé';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."event_id" IS 'äºä»¶id';
COMMENT ON TABLE "public"."t_equip_record_alarm" IS 'è®¾å¤æ¥è­¦ç¶æè®°å½';

-- ----------------------------
-- Table structure for t_equip_record_online
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_record_online";
CREATE TABLE "public"."t_equip_record_online" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" timestamp(6) NOT NULL,
  "end_time" timestamp(6),
  "state" int2 NOT NULL,
  "event_id" varchar(60) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip_record_online"."sn" IS 'è®¾å¤snå·';
COMMENT ON COLUMN "public"."t_equip_record_online"."start_time" IS 'æç»­å¼å§æ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_online"."end_time" IS 'æç»­ç»ææ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_online"."state" IS 'ç¶æ';
COMMENT ON COLUMN "public"."t_equip_record_online"."event_id" IS 'äºä»¶id';
COMMENT ON TABLE "public"."t_equip_record_online" IS 'è®¾å¤å¨çº¿è®°å½';

-- ----------------------------
-- Table structure for t_equip_record_run
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_record_run";
CREATE TABLE "public"."t_equip_record_run" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time" timestamp(6) NOT NULL,
  "end_time" timestamp(6),
  "power_start" numeric(10,3),
  "power_end" numeric(10,3),
  "state" int2 NOT NULL,
  "event_id" varchar(60) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip_record_run"."sn" IS 'è®¾å¤snå·';
COMMENT ON COLUMN "public"."t_equip_record_run"."start_time" IS 'è¿è¡å¼å§æ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_run"."end_time" IS 'è¿è¡ç»ææ¶é´';
COMMENT ON COLUMN "public"."t_equip_record_run"."power_start" IS 'å¼å§æ¶çµé';
COMMENT ON COLUMN "public"."t_equip_record_run"."power_end" IS 'ç»ææ¶çµé';
COMMENT ON COLUMN "public"."t_equip_record_run"."state" IS 'è¿è¡ç¶æ';
COMMENT ON COLUMN "public"."t_equip_record_run"."event_id" IS 'äºä»¶id';
COMMENT ON TABLE "public"."t_equip_record_run" IS 'è®¾å¤è¿è¡è¡¨';

-- ----------------------------
-- Table structure for t_equip_state_snapshot
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_equip_state_snapshot";
CREATE TABLE "public"."t_equip_state_snapshot" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sn" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "time" timestamp(6) NOT NULL,
  "run_state" int2,
  "alarm_state" int2,
  "online_state" int2
)
;
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."sn" IS 'è®¾å¤snå·';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."time" IS 'è®°å½æ¶é´';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."run_state" IS 'è¿è¡ç¶æ';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."alarm_state" IS 'æ¥è­¦ç¶æ';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."online_state" IS 'å¨çº¿ç¶æ';
COMMENT ON TABLE "public"."t_equip_state_snapshot" IS 'è®¾å¤ç¶æå¿«ç§';

-- ----------------------------
-- Table structure for t_system_config
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_system_config";
CREATE TABLE "public"."t_system_config" (
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
)
;
COMMENT ON COLUMN "public"."t_system_config"."config_key" IS 'éç½®é®ï¼å¦ app';
COMMENT ON COLUMN "public"."t_system_config"."config" IS 'éç½®åå®¹ JSON';
COMMENT ON TABLE "public"."t_system_config" IS 'ç³»ç»éç½®';

-- ----------------------------
-- Table structure for t_gateway
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_gateway";
CREATE TABLE "public"."t_gateway" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "server_name" varchar(50) COLLATE "pg_catalog"."default",
  "uri" varchar(255) COLLATE "pg_catalog"."default",
  "topic" varchar(50) COLLATE "pg_catalog"."default",
  "params" varchar(255) COLLATE "pg_catalog"."default",
  "protocol" varchar(20) COLLATE "pg_catalog"."default",
  "enabled" bool,
  "username" varchar(150) COLLATE "pg_catalog"."default",
  "password" varchar(150) COLLATE "pg_catalog"."default",
  "valid_type" int2,
  "collect_cron" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_gateway"."server_name" IS 'æå¡å';
COMMENT ON COLUMN "public"."t_gateway"."uri" IS 'å°å';
COMMENT ON COLUMN "public"."t_gateway"."topic" IS 'ä¸»é¢';
COMMENT ON COLUMN "public"."t_gateway"."params" IS 'åæ°';
COMMENT ON COLUMN "public"."t_gateway"."protocol" IS 'åè®®ç±»å';
COMMENT ON COLUMN "public"."t_gateway"."enabled" IS 'æ¯å¦å¯ç¨';
COMMENT ON COLUMN "public"."t_gateway"."valid_type" IS 'è®¤è¯ç±»åï¼0:æ è®¤è¯ï¼ 1:Baiscï¼';
COMMENT ON COLUMN "public"."t_gateway"."collect_cron" IS 'éécronè¡¨è¾¾å¼';
COMMENT ON TABLE "public"."t_gateway" IS 'ç½å³';

-- ----------------------------
-- Table structure for t_inspect_item
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_item";
CREATE TABLE "public"."t_inspect_item" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "item_name" varchar(128) COLLATE "pg_catalog"."default",
  "item_type" int4,
  "unit" varchar(128) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4,
  "min_value" float8,
  "max_value" float8,
  "required_flag" int2
)
;
COMMENT ON COLUMN "public"."t_inspect_item"."item_name" IS 'å·¡æ£é¡¹åç§°';
COMMENT ON COLUMN "public"."t_inspect_item"."item_type" IS 'ç±»åï¼1éæ© 2æ°å¼ 3æ¯å¦';
COMMENT ON COLUMN "public"."t_inspect_item"."unit" IS 'åä½æéé¡¹ï¼æ°å¼å¡«A/MPaç­ï¼éæ©å¯å¡« æ­£å¸¸/ä¸è¶³ãæ¾å¨/æ­£å¸¸';
COMMENT ON COLUMN "public"."t_inspect_item"."min_value" IS 'æå°å¼';
COMMENT ON COLUMN "public"."t_inspect_item"."max_value" IS 'æå¤§å¼';
COMMENT ON COLUMN "public"."t_inspect_item"."required_flag" IS 'æ¯å¦å¿å¡«ï¼0å¦ 1æ¯';

-- ----------------------------
-- Table structure for t_inspect_person
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_person";
CREATE TABLE "public"."t_inspect_person" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(64) COLLATE "pg_catalog"."default",
  "account_id" varchar(64) COLLATE "pg_catalog"."default",
  "account_name" varchar(128) COLLATE "pg_catalog"."default",
  "mobile" varchar(32) COLLATE "pg_catalog"."default",
  "job_number" varchar(64) COLLATE "pg_catalog"."default",
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4
)
;
COMMENT ON COLUMN "public"."t_inspect_person"."name" IS 'å§å';
COMMENT ON COLUMN "public"."t_inspect_person"."account_id" IS 'å³èè´¦æ·ID';
COMMENT ON COLUMN "public"."t_inspect_person"."account_name" IS 'å³èè´¦æ·åç§°';
COMMENT ON COLUMN "public"."t_inspect_person"."mobile" IS 'ææºå·';
COMMENT ON COLUMN "public"."t_inspect_person"."job_number" IS 'å·¥å·';
COMMENT ON COLUMN "public"."t_inspect_person"."remark" IS 'å¤æ³¨';

-- ----------------------------
-- Table structure for t_inspect_plan
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_plan";
CREATE TABLE "public"."t_inspect_plan" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "cycle_type" int4,
  "cycle_config" varchar(128) COLLATE "pg_catalog"."default",
  "workshop_code" text COLLATE "pg_catalog"."default",
  "equip_ids" text COLLATE "pg_catalog"."default",
  "status" int4,
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "template_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_inspect_plan"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_inspect_plan"."name" IS 'è®¡ååç§°';
COMMENT ON COLUMN "public"."t_inspect_plan"."cycle_type" IS 'å¨æç±»åï¼1æ¯æ¥ 2æ¯å¨ 3æ¯æ';
COMMENT ON COLUMN "public"."t_inspect_plan"."cycle_config" IS 'å¨æéç½®';
COMMENT ON COLUMN "public"."t_inspect_plan"."workshop_code" IS 'åºæ¯ç¼å·';
COMMENT ON COLUMN "public"."t_inspect_plan"."equip_ids" IS 'å³èè®¾å¤IDåè¡¨';
COMMENT ON COLUMN "public"."t_inspect_plan"."status" IS 'ç¶æï¼0ç¦ç¨ 1å¯ç¨';
COMMENT ON COLUMN "public"."t_inspect_plan"."remark" IS 'å¤æ³¨';
COMMENT ON COLUMN "public"."t_inspect_plan"."template_id" IS 'å³èå·¡æ£æ¨¡æ¿ID';
COMMENT ON TABLE "public"."t_inspect_plan" IS 'å·¡æ£è®¡å';

-- ----------------------------
-- Table structure for t_inspect_project
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_project";
CREATE TABLE "public"."t_inspect_project" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default",
  "code" varchar(64) COLLATE "pg_catalog"."default",
  "status" int4,
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4
)
;
COMMENT ON COLUMN "public"."t_inspect_project"."name" IS 'é¡¹ç®åç§°';
COMMENT ON COLUMN "public"."t_inspect_project"."code" IS 'é¡¹ç®ç¼å·';
COMMENT ON COLUMN "public"."t_inspect_project"."status" IS 'ç¶æï¼0ç¦ç¨ 1å¯ç¨';
COMMENT ON COLUMN "public"."t_inspect_project"."remark" IS 'å¤æ³¨';

-- ----------------------------
-- Table structure for t_inspect_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_record";
CREATE TABLE "public"."t_inspect_record" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "equip_id" varchar(64) COLLATE "pg_catalog"."default",
  "equip_name" varchar(128) COLLATE "pg_catalog"."default",
  "score" int4,
  "item_name" varchar(128) COLLATE "pg_catalog"."default",
  "result" int4,
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "photo_urls" varchar(1024) COLLATE "pg_catalog"."default",
  "record_time" timestamp(6),
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_inspect_record"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_inspect_record"."task_id" IS 'å·¡æ£ä»»å¡ID';
COMMENT ON COLUMN "public"."t_inspect_record"."equip_id" IS 'è®¾å¤ID';
COMMENT ON COLUMN "public"."t_inspect_record"."equip_name" IS 'è®¾å¤ç¼å·/åç§°';
COMMENT ON COLUMN "public"."t_inspect_record"."score" IS 'å·¡æ£åå¼';
COMMENT ON COLUMN "public"."t_inspect_record"."item_name" IS 'å·¡æ£é¡¹åç§°';
COMMENT ON COLUMN "public"."t_inspect_record"."result" IS 'å·¡æ£ç»æï¼0æ­£å¸¸ 1å¼å¸¸';
COMMENT ON COLUMN "public"."t_inspect_record"."remark" IS 'å¤æ³¨';
COMMENT ON COLUMN "public"."t_inspect_record"."photo_urls" IS 'ç§çURLï¼å¤å¼ éå·åé';
COMMENT ON COLUMN "public"."t_inspect_record"."record_time" IS 'è®°å½æ¶é´';
COMMENT ON TABLE "public"."t_inspect_record" IS 'å·¡æ£è®°å½';

-- ----------------------------
-- Table structure for t_inspect_record_item
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_record_item";
CREATE TABLE "public"."t_inspect_record_item" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "record_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "item_id" varchar(64) COLLATE "pg_catalog"."default",
  "item_name" varchar(256) COLLATE "pg_catalog"."default",
  "content" varchar(512) COLLATE "pg_catalog"."default",
  "result" int2,
  "rule_score" int4,
  "score" int4,
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "photo_urls" varchar(2000) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4
)
;
COMMENT ON COLUMN "public"."t_inspect_record_item"."record_id" IS 'å·¡æ£è®°å½IDï¼ä¸»è¡¨ï¼';
COMMENT ON COLUMN "public"."t_inspect_record_item"."item_id" IS 'å·¡æ£é¡¹IDï¼æ¨¡æ¿é¡¹ä¸»é®ï¼';
COMMENT ON COLUMN "public"."t_inspect_record_item"."item_name" IS 'å·¡æ£é¡¹åç§°';
COMMENT ON COLUMN "public"."t_inspect_record_item"."content" IS 'å·¡æ£é¡¹åå®¹ï¼å¡«åå¼/ç»æææ¬';
COMMENT ON COLUMN "public"."t_inspect_record_item"."result" IS 'å·¡æ£ç»æï¼0æ­£å¸¸ 1å¼å¸¸';
COMMENT ON COLUMN "public"."t_inspect_record_item"."rule_score" IS 'è§ååå¼ï¼éç½®é¡¹å¹éå¾åï¼æªä¹æéï¼';
COMMENT ON COLUMN "public"."t_inspect_record_item"."score" IS 'è¯¥é¡¹å¾åï¼ææéä¸éç½®è§åè®¡ç®ï¼';
COMMENT ON COLUMN "public"."t_inspect_record_item"."remark" IS 'å¤æ³¨';
COMMENT ON COLUMN "public"."t_inspect_record_item"."photo_urls" IS 'ç§çURLï¼å¤å¼ éå·åé';
COMMENT ON TABLE "public"."t_inspect_record_item" IS 'å·¡æ£è®°å½æç»ï¼éè¡¨ï¼';

-- ----------------------------
-- Table structure for t_inspect_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_task";
CREATE TABLE "public"."t_inspect_task" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "plan_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "scheduled_time" timestamp(6),
  "status" int4,
  "executor_id" varchar(64) COLLATE "pg_catalog"."default",
  "executor_name" varchar(64) COLLATE "pg_catalog"."default",
  "actual_start_time" timestamp(6),
  "actual_end_time" timestamp(6),
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "workshop_code" varchar(40) COLLATE "pg_catalog"."default",
  "executor_person_id" varchar(64) COLLATE "pg_catalog"."default",
  "template_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_inspect_task"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_inspect_task"."plan_id" IS 'å·¡æ£è®¡åID';
COMMENT ON COLUMN "public"."t_inspect_task"."scheduled_time" IS 'è®¡åæ§è¡æ¶é´';
COMMENT ON COLUMN "public"."t_inspect_task"."status" IS 'ç¶æï¼0å¾æ§è¡ 1æ§è¡ä¸­ 2å·²å®æ 3å·²é¾æ';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_id" IS 'æ§è¡äººID';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_name" IS 'æ§è¡äººå§å';
COMMENT ON COLUMN "public"."t_inspect_task"."actual_start_time" IS 'å®éå¼å§æ¶é´';
COMMENT ON COLUMN "public"."t_inspect_task"."actual_end_time" IS 'å®éå®ææ¶é´';
COMMENT ON COLUMN "public"."t_inspect_task"."remark" IS 'å¤æ³¨';
COMMENT ON COLUMN "public"."t_inspect_task"."workshop_code" IS 'å³èè®¾å¤IDåè¡¨';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_person_id" IS 'ææ´¾çå·¡æ£äººåID';
COMMENT ON COLUMN "public"."t_inspect_task"."template_id" IS 'å³èå·¡æ£æ¨¡æ¿IDï¼æ¥èªè®¡åï¼';
COMMENT ON TABLE "public"."t_inspect_task" IS 'å·¡æ£ä»»å¡';

-- ----------------------------
-- Table structure for t_inspect_template
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_template";
CREATE TABLE "public"."t_inspect_template" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(128) COLLATE "pg_catalog"."default",
  "remark" varchar(512) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4
)
;
COMMENT ON COLUMN "public"."t_inspect_template"."name" IS 'æ¨¡æ¿åç§°';
COMMENT ON COLUMN "public"."t_inspect_template"."remark" IS 'å¤æ³¨';

-- ----------------------------
-- Table structure for t_inspect_template_item
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_template_item";
CREATE TABLE "public"."t_inspect_template_item" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "template_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "product_code" varchar(64) COLLATE "pg_catalog"."default",
  "reference_item_id" varchar(64) COLLATE "pg_catalog"."default",
  "sort_order" int4,
  "weight" int4,
  "weight_rate" numeric(10,6),
  "rule_config" text COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_time" timestamp(6),
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_inspect_template_item"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_inspect_template_item"."template_id" IS 'æå±æ¨¡æ¿ID';
COMMENT ON COLUMN "public"."t_inspect_template_item"."product_code" IS 'æå±äº§åç¼å·ï¼æ¨¡æ¿ä¸­çäº§ååï¼';
COMMENT ON COLUMN "public"."t_inspect_template_item"."reference_item_id" IS 'å¼ç¨çå·¡æ£é¡¹IDï¼ä»å·¡æ£é¡¹æ± è½½å¥æ¶ç»å®ï¼';
COMMENT ON COLUMN "public"."t_inspect_template_item"."sort_order" IS 'æåºå·';
COMMENT ON COLUMN "public"."t_inspect_template_item"."weight" IS 'æéï¼æ¨¡æ¿åè¯¥é¡¹çæéï¼';
COMMENT ON COLUMN "public"."t_inspect_template_item"."weight_rate" IS 'ç¾åæ¯æéï¼è¯¥é¡¹æé/åäº§ååæéæ»å¼ï¼å¦0.3ï¼';
COMMENT ON COLUMN "public"."t_inspect_template_item"."rule_config" IS 'è§åéç½®JSONï¼å¦æ°å¼åºé´/æ¯å¦/éé¡¹ä¸åå¼';
COMMENT ON COLUMN "public"."t_inspect_template_item"."created_time" IS 'åå»ºæ¶é´';
COMMENT ON COLUMN "public"."t_inspect_template_item"."updated_time" IS 'æ´æ°æ¶é´';
COMMENT ON COLUMN "public"."t_inspect_template_item"."tenant_id" IS 'ç§æ·ID';
COMMENT ON TABLE "public"."t_inspect_template_item" IS 'æ¨¡æ¿-å·¡æ£é¡¹å³èè¡¨';

-- ----------------------------
-- Table structure for t_inspect_template_item_rule
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_inspect_template_item_rule";
CREATE TABLE "public"."t_inspect_template_item_rule" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "template_item_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rule_type" int4 NOT NULL,
  "bool_value" int2,
  "min_value" float8,
  "max_value" float8,
  "option_value" varchar(64) COLLATE "pg_catalog"."default",
  "weight" int4 NOT NULL,
  "sort_order" int4,
  "created_time" timestamp(6),
  "updated_time" timestamp(6),
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "revision" int4
)
;
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."id" IS 'ä¸»é®';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."template_item_id" IS 'æ¨¡æ¿å·¡æ£é¡¹IDï¼å³è t_inspect_template_item.id';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."rule_type" IS 'è§åç±»åï¼1=æ¯å¦åï¼2=æ°å¼èå´ï¼3=éæ©å¼';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."bool_value" IS 'æ¯å¦åï¼0=å¦ 1=æ¯ï¼rule_type=1æ¶ææï¼';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."min_value" IS 'æ°å¼åï¼æå°å¼ï¼å«ï¼ï¼rule_type=2';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."max_value" IS 'æ°å¼åï¼æå¤§å¼ï¼å«ï¼ï¼rule_type=2';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."option_value" IS 'éæ©åï¼éé¡¹å¼ææ¬ï¼rule_type=3';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."weight" IS 'è¯¥æ¡ä»¶å½ä¸­æ¶çå¾å/æé';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."sort_order" IS 'æåºï¼åä¸é¡¹å¤æ¡è§åæ¶çä¼åçº§ï¼';
COMMENT ON TABLE "public"."t_inspect_template_item_rule" IS 'æ¨¡æ¿å·¡æ£é¡¹è¯åè§å';

-- ----------------------------
-- Table structure for t_line
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_line";
CREATE TABLE "public"."t_line" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "self_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "product_code" varchar(64) COLLATE "pg_catalog"."default",
  "product_name" varchar(128) COLLATE "pg_catalog"."default",
  "material_code" varchar(64) COLLATE "pg_catalog"."default",
  "material_name" varchar(128) COLLATE "pg_catalog"."default",
  "version_no" varchar(32) COLLATE "pg_catalog"."default",
  "throughput" numeric(14,5),
  "step_interval" int4,
  "del_bit" int2 DEFAULT 0,
  "type" int2,
  "plc_time" timestamp(6),
  "sync_time" timestamp(6),
  "map_db" int4,
  "map_offset" varchar(100) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_line"."self_code" IS 'äº§çº¿ç¼å·(å¯ä»¥ä¸å·¥èºè®¾ç½®è¿è¡æ å°)';
COMMENT ON COLUMN "public"."t_line"."name" IS 'äº§çº¿åç§°';
COMMENT ON COLUMN "public"."t_line"."product_code" IS 'å å·¥å¯¹è±¡ç¼å·';
COMMENT ON COLUMN "public"."t_line"."product_name" IS 'å å·¥å¯¹è±¡åç§°';
COMMENT ON COLUMN "public"."t_line"."material_code" IS 'åææç¼å·';
COMMENT ON COLUMN "public"."t_line"."material_name" IS 'åææåç§°';
COMMENT ON COLUMN "public"."t_line"."version_no" IS 'çæ¬å·';
COMMENT ON COLUMN "public"."t_line"."throughput" IS 'ååé(åæ¬¡çäº§æ»é)';
COMMENT ON COLUMN "public"."t_line"."step_interval" IS 'æ­¥è¿å¼ï¼åä½ç§ï¼';
COMMENT ON COLUMN "public"."t_line"."type" IS 'ç±»å(0:åºå®æ å° 1:åæ°æ å° 2:æµç¨æ å°)';
COMMENT ON COLUMN "public"."t_line"."plc_time" IS 'plcä¸è½½æ¶é´';
COMMENT ON COLUMN "public"."t_line"."sync_time" IS 'åæ­¥æ¶é´';
COMMENT ON COLUMN "public"."t_line"."map_db" IS 'æ å°db';
COMMENT ON COLUMN "public"."t_line"."map_offset" IS 'æ å°åç§»';
COMMENT ON TABLE "public"."t_line" IS 'äº§çº¿/å·¥èº';

-- ----------------------------
-- Table structure for t_mat
-- ----------------------------

-- ----------------------------
-- Table structure for t_mo (²¹ÆëÈ±±í£¬ÓëÊµÌå¶ÔÆë)
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mo";
CREATE TABLE "public"."t_mo" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "self_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int4 DEFAULT 0,
  "product_id" varchar(20) COLLATE "pg_catalog"."default",
  "product_name" varchar(100) COLLATE "pg_catalog"."default",
  "product_code" varchar(50) COLLATE "pg_catalog"."default",
  "num" int4,
  "weight" numeric(25,5),
  "line_code" varchar(50) COLLATE "pg_catalog"."default",
  "surplus" int4,
  "exec_time" timestamp(6),
  "del_bit" int2 DEFAULT 0,
  "source" int4,
  "source_id" varchar(64) COLLATE "pg_catalog"."default",
  PRIMARY KEY ("id")
)
;
CREATE UNIQUE INDEX "uk_t_mo_self_code" ON "public"."t_mo" USING btree ("self_code");
COMMENT ON COLUMN "public"."t_mo"."status" IS '0INIT 1PART 2RUN 3COMPLETE 4CANCEL';
COMMENT ON TABLE "public"."t_mo" IS 'manufacturing order';

-- ----------------------------
-- Table structure for t_mps
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mps";
CREATE TABLE "public"."t_mps" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "mo_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "sequence" int4,
  "line" varchar(50) COLLATE "pg_catalog"."default",
  "exec_time" timestamp(6),
  "num" int4,
  "weight" numeric(25,5),
  "batch" int4,
  "status" int4 DEFAULT 0,
  "priority" numeric(25,5),
  "del_bit" int2 DEFAULT 0,
  PRIMARY KEY ("id")
)
;
CREATE INDEX "idx_t_mps_mo_code" ON "public"."t_mps" USING btree ("mo_code");
CREATE INDEX "idx_t_mps_status" ON "public"."t_mps" USING btree ("status");
COMMENT ON COLUMN "public"."t_mps"."status" IS '0WAIT_QUE 1WAIT_EXEC 2EXECING 3COMPLETE 4FILE 5CANCEL';
COMMENT ON TABLE "public"."t_mps" IS 'mps batch plan';

-- ----------------------------
-- Table structure for t_mo_adjust_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mo_adjust_log";
CREATE TABLE "public"."t_mo_adjust_log" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "request_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "mo_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "adjust_type" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "source" varchar(16) COLLATE "pg_catalog"."default",
  "before_json" text COLLATE "pg_catalog"."default",
  "after_json" text COLLATE "pg_catalog"."default",
  "affect_mps_ids" text COLLATE "pg_catalog"."default",
  "status" int4 DEFAULT 0,
  "err_msg" varchar(500) COLLATE "pg_catalog"."default",
  "operator" varchar(64) COLLATE "pg_catalog"."default",
  "create_time" timestamp(6),
  PRIMARY KEY ("id")
)
;
CREATE UNIQUE INDEX "uk_t_mo_adjust_log_request_id" ON "public"."t_mo_adjust_log" USING btree ("request_id");
CREATE INDEX "idx_t_mo_adjust_log_mo_code" ON "public"."t_mo_adjust_log" USING btree ("mo_code");
COMMENT ON COLUMN "public"."t_mo_adjust_log"."request_id" IS 'idempotency key';
COMMENT ON COLUMN "public"."t_mo_adjust_log"."adjust_type" IS 'CANCEL_MO/CANCEL_MPS/RESCHEDULE/PRIORITY/CHANGE_LINE/CHANGE_DEV/QTY_UP/QTY_DOWN';
COMMENT ON COLUMN "public"."t_mo_adjust_log"."status" IS '0PENDING 1SUCCESS 2FAILED';
COMMENT ON TABLE "public"."t_mo_adjust_log" IS 'mo adjust audit log';
