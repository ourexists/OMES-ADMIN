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
COMMENT ON COLUMN "public"."p_ucenter_acc"."acc_name" IS '账户名';
COMMENT ON COLUMN "public"."p_ucenter_acc"."password" IS '账户密码';
COMMENT ON COLUMN "public"."p_ucenter_acc"."nick_name" IS '账户昵称';
COMMENT ON COLUMN "public"."p_ucenter_acc"."user_name" IS '用户名';
COMMENT ON COLUMN "public"."p_ucenter_acc"."id_card" IS '身份证号';
COMMENT ON COLUMN "public"."p_ucenter_acc"."mobile" IS '账户手机号';
COMMENT ON COLUMN "public"."p_ucenter_acc"."email" IS '账户邮箱';
COMMENT ON COLUMN "public"."p_ucenter_acc"."sex" IS '用户性别';
COMMENT ON COLUMN "public"."p_ucenter_acc"."init" IS '初始状态(1:是初始化账户)';
COMMENT ON COLUMN "public"."p_ucenter_acc"."status" IS '账户状态';
COMMENT ON COLUMN "public"."p_ucenter_acc"."settled_time" IS '入驻时间';
COMMENT ON COLUMN "public"."p_ucenter_acc"."expire_time" IS '过期时间';
COMMENT ON COLUMN "public"."p_ucenter_acc"."birth_day" IS '出生年月';
COMMENT ON COLUMN "public"."p_ucenter_acc"."del_flag" IS '逻辑删除';
COMMENT ON COLUMN "public"."p_ucenter_acc"."source" IS '来源';
COMMENT ON COLUMN "public"."p_ucenter_acc"."source_id" IS '来源id';
COMMENT ON COLUMN "public"."p_ucenter_acc"."platform" IS '平台';
COMMENT ON COLUMN "public"."p_ucenter_acc"."union_id" IS '来源方互通id';
COMMENT ON COLUMN "public"."p_ucenter_acc"."avatar_url" IS '头像路径';
COMMENT ON COLUMN "public"."p_ucenter_acc"."country" IS '国家';
COMMENT ON COLUMN "public"."p_ucenter_acc"."province" IS '省';
COMMENT ON COLUMN "public"."p_ucenter_acc"."city" IS '市';
COMMENT ON COLUMN "public"."p_ucenter_acc"."language" IS '语言';
COMMENT ON COLUMN "public"."p_ucenter_acc"."perfection" IS '账户完善度（0: 未完善，1:已完善）';
COMMENT ON TABLE "public"."p_ucenter_acc" IS '账户表';

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
COMMENT ON COLUMN "public"."p_ucenter_permission"."name" IS '名称';
COMMENT ON COLUMN "public"."p_ucenter_permission"."code" IS '编号';
COMMENT ON COLUMN "public"."p_ucenter_permission"."pcode" IS '父编号';
COMMENT ON COLUMN "public"."p_ucenter_permission"."ppcode" IS '祖宗编号';
COMMENT ON COLUMN "public"."p_ucenter_permission"."strategy" IS '权限策略';
COMMENT ON COLUMN "public"."p_ucenter_permission"."icon" IS '图标';
COMMENT ON COLUMN "public"."p_ucenter_permission"."component" IS '组件';
COMMENT ON COLUMN "public"."p_ucenter_permission"."url" IS '跳转网页链接';
COMMENT ON COLUMN "public"."p_ucenter_permission"."sort_no" IS '菜单排序';
COMMENT ON COLUMN "public"."p_ucenter_permission"."type" IS '权限类型(0:菜单权限  1:按钮权限  2:空描述权限)';
COMMENT ON COLUMN "public"."p_ucenter_permission"."description" IS '描述';
COMMENT ON COLUMN "public"."p_ucenter_permission"."internal_or_external" IS '外链菜单打开方式(0:内部打开 1:外部打开)';
COMMENT ON COLUMN "public"."p_ucenter_permission"."platform" IS '所属平台';
COMMENT ON TABLE "public"."p_ucenter_permission" IS '权限表';

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
COMMENT ON COLUMN "public"."p_ucenter_platform"."name" IS '名称';
COMMENT ON COLUMN "public"."p_ucenter_platform"."code" IS '编号';

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
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_name" IS '租户名';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_code" IS '租户编号';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."status" IS '租户状态';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."settled_time" IS '入驻时间';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."expire_time" IS '过期时间';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_id" IS '配合框架占位';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_address" IS '租户/机构所在地址';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_contacts" IS '联系人姓名';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_phone" IS '服务电话';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_mail" IS '联系邮箱';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."province_code" IS '省份code：详见yl_elder.r_city_dict省市级联字典表code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."city_code" IS '城市code：详见yl_elder.r_city_dict省市级联字典表code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."county_code" IS '区县code：详见yl_elder.r_city_dict省市级联字典表code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."street_code" IS '街道code：详见yl_elder.r_city_dict省市级联字典表code';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."area_fullname" IS '所在区域：省-市-区县-街道拼接';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tenant_coo" IS '租户机构经纬度坐标';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."client_id" IS '补充临时字段，作用待定';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."management" IS '管控权限';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."manage_num" IS '管控数量';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."logo" IS 'logo';
COMMENT ON COLUMN "public"."p_ucenter_tenant"."tel" IS '客服电话';
COMMENT ON TABLE "public"."p_ucenter_tenant" IS '租户表';

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
COMMENT ON COLUMN "public"."r_bom_d"."mat_id" IS '物料id';
COMMENT ON COLUMN "public"."r_bom_d"."mat_name" IS '物料名称';
COMMENT ON COLUMN "public"."r_bom_d"."mat_code" IS '物料编号';
COMMENT ON COLUMN "public"."r_bom_d"."mcode" IS '主体编号';
COMMENT ON COLUMN "public"."r_bom_d"."mat_scale" IS '物料数值（对应物料清单的类型，决定是百分比还是固定值）';
COMMENT ON COLUMN "public"."r_bom_d"."dev_no" IS '设备编号';
COMMENT ON COLUMN "public"."r_bom_d"."attribute" IS '组份性质：0=主料，1=预混料（添加剂）,2=回机料,3=油,4=水';
COMMENT ON COLUMN "public"."r_bom_d"."priority" IS '优先级（数值越大，优先级越高）';
COMMENT ON TABLE "public"."r_bom_d" IS '配方详情';

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
COMMENT ON COLUMN "public"."r_dg_d"."dg_id" IS '设备组id';
COMMENT ON COLUMN "public"."r_dg_d"."d_id" IS '设备id';
COMMENT ON COLUMN "public"."r_dg_d"."mat_id" IS '物料id';
COMMENT ON TABLE "public"."r_dg_d" IS '设备组关联';

-- ----------------------------
-- Table structure for r_gw_binding
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_gw_binding";
CREATE TABLE "public"."r_gw_binding" (
  "equip_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "gw_id" varchar(20) COLLATE "pg_catalog"."default",
  "config" jsonb
)
;
COMMENT ON COLUMN "public"."r_gw_binding"."equip_id" IS '设备id';
COMMENT ON COLUMN "public"."r_gw_binding"."gw_id" IS '网关id';

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
COMMENT ON COLUMN "public"."r_message_read"."message_id" IS '消息id';
COMMENT ON COLUMN "public"."r_message_read"."acc_id" IS '账户id';
COMMENT ON COLUMN "public"."r_message_read"."time" IS '读取时间';
COMMENT ON COLUMN "public"."r_message_read"."is_read" IS '是否已读';

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
COMMENT ON COLUMN "public"."r_mo_d"."mat_id" IS '物料id';
COMMENT ON COLUMN "public"."r_mo_d"."mat_name" IS '物料名称';
COMMENT ON COLUMN "public"."r_mo_d"."mat_code" IS '物料编号';
COMMENT ON COLUMN "public"."r_mo_d"."mat_num" IS '物料数量';
COMMENT ON COLUMN "public"."r_mo_d"."priority" IS '优先级（数值越大，优先级越高）';
COMMENT ON COLUMN "public"."r_mo_d"."mcode" IS '归属清单';
COMMENT ON COLUMN "public"."r_mo_d"."dev_no" IS '设备编号';
COMMENT ON COLUMN "public"."r_mo_d"."dev_name" IS '设备名';
COMMENT ON COLUMN "public"."r_mo_d"."dg_code" IS '设备工艺code';
COMMENT ON COLUMN "public"."r_mo_d"."dg_name" IS '设备工艺名';
COMMENT ON TABLE "public"."r_mo_d" IS '生产清单详情';

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
COMMENT ON COLUMN "public"."r_mps_d"."mat_id" IS '原料id';
COMMENT ON COLUMN "public"."r_mps_d"."mat_name" IS '原料名称';
COMMENT ON COLUMN "public"."r_mps_d"."mat_code" IS '原料编号';
COMMENT ON COLUMN "public"."r_mps_d"."mid" IS '主体id';
COMMENT ON COLUMN "public"."r_mps_d"."mat_num" IS '理论值';
COMMENT ON COLUMN "public"."r_mps_d"."actual_num" IS '实际值';
COMMENT ON COLUMN "public"."r_mps_d"."dev_no" IS '设备号';
COMMENT ON COLUMN "public"."r_mps_d"."dev_name" IS '设备名';
COMMENT ON COLUMN "public"."r_mps_d"."attribute" IS '组份性质：0=主料，1=预混料（添加剂）,2=回机料,3=油,4=水';
COMMENT ON COLUMN "public"."r_mps_d"."priority" IS '投料顺序';
COMMENT ON COLUMN "public"."r_mps_d"."mo_code" IS '归属清单';
COMMENT ON COLUMN "public"."r_mps_d"."dg_code" IS '设备工艺code';
COMMENT ON COLUMN "public"."r_mps_d"."dg_name" IS '设备工艺名';
COMMENT ON TABLE "public"."r_mps_d" IS '生产计划详情';

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
COMMENT ON COLUMN "public"."r_mps_tf"."mps_id" IS '执行计划id';
COMMENT ON COLUMN "public"."r_mps_tf"."self_code" IS '工艺流程编号(可以与工艺设置进行映射)';
COMMENT ON COLUMN "public"."r_mps_tf"."name" IS '工艺流程名称';
COMMENT ON COLUMN "public"."r_mps_tf"."start_time" IS '开始执行的时间';
COMMENT ON COLUMN "public"."r_mps_tf"."end_time" IS '执行完成时间';
COMMENT ON COLUMN "public"."r_mps_tf"."start_temperature" IS '入槽温度';
COMMENT ON COLUMN "public"."r_mps_tf"."end_temperature" IS '出槽温度';
COMMENT ON COLUMN "public"."r_mps_tf"."status" IS '流程状态(0:待执行|1执行中|2已完成|3停止|4异常中断)';
COMMENT ON COLUMN "public"."r_mps_tf"."pre" IS '上一流程编号';
COMMENT ON COLUMN "public"."r_mps_tf"."nex" IS '下一流程编号';
COMMENT ON COLUMN "public"."r_mps_tf"."mo_code" IS '归属清单';
COMMENT ON TABLE "public"."r_mps_tf" IS '工艺流程';

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
COMMENT ON COLUMN "public"."r_sync_resource"."sync_id" IS '同步事务id';
COMMENT ON COLUMN "public"."r_sync_resource"."point" IS '当前事务资源';
COMMENT ON COLUMN "public"."r_sync_resource"."status" IS '当前资源状态（start|error|end）';
COMMENT ON COLUMN "public"."r_sync_resource"."req_data" IS '当前资源入参';
COMMENT ON COLUMN "public"."r_sync_resource"."resp_data" IS '当前资源出参';
COMMENT ON COLUMN "public"."r_sync_resource"."excep" IS '异常信息';
COMMENT ON TABLE "public"."r_sync_resource" IS '同步事务资源';

-- ----------------------------
-- Table structure for r_ucenter_acc_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_acc_role";
CREATE TABLE "public"."r_ucenter_acc_role" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_acc_role"."role_id" IS '角色id';
COMMENT ON COLUMN "public"."r_ucenter_acc_role"."acc_id" IS '用户id';
COMMENT ON TABLE "public"."r_ucenter_acc_role" IS '账户角色';

-- ----------------------------
-- Table structure for r_ucenter_depart_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_depart_role";
CREATE TABLE "public"."r_ucenter_depart_role" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "depart_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_depart_role"."role_id" IS '角色id';
COMMENT ON COLUMN "public"."r_ucenter_depart_role"."depart_id" IS '部门id';
COMMENT ON TABLE "public"."r_ucenter_depart_role" IS '部门角色';

-- ----------------------------
-- Table structure for r_ucenter_depart_users
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_depart_users";
CREATE TABLE "public"."r_ucenter_depart_users" (
  "depart_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "acc_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_depart_users"."depart_id" IS '用户组id';
COMMENT ON COLUMN "public"."r_ucenter_depart_users"."acc_id" IS '用户id';
COMMENT ON TABLE "public"."r_ucenter_depart_users" IS '用户组用户关联表';

-- ----------------------------
-- Table structure for r_ucenter_role_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_role_permission";
CREATE TABLE "public"."r_ucenter_role_permission" (
  "role_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_role_permission"."role_id" IS '角色id';
COMMENT ON COLUMN "public"."r_ucenter_role_permission"."permission_id" IS '权限id';
COMMENT ON TABLE "public"."r_ucenter_role_permission" IS '角色权限';

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
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."acc_id" IS '用户id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_acc"."role" IS '角色';
COMMENT ON TABLE "public"."r_ucenter_tenant_acc" IS '租户用户关联表';

-- ----------------------------
-- Table structure for r_ucenter_tenant_permission
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_ucenter_tenant_permission";
CREATE TABLE "public"."r_ucenter_tenant_permission" (
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "permission_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_ucenter_tenant_permission"."tenant_id" IS '租户id';
COMMENT ON COLUMN "public"."r_ucenter_tenant_permission"."permission_id" IS '权限id';
COMMENT ON TABLE "public"."r_ucenter_tenant_permission" IS '租户权限';

-- ----------------------------
-- Table structure for r_workshop_assign
-- ----------------------------
DROP TABLE IF EXISTS "public"."r_workshop_assign";
CREATE TABLE "public"."r_workshop_assign" (
  "assign_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "workshop_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."r_workshop_assign"."assign_id" IS '角色id';
COMMENT ON COLUMN "public"."r_workshop_assign"."workshop_code" IS '场景编号';
COMMENT ON TABLE "public"."r_workshop_assign" IS '角色场景';

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
COMMENT ON COLUMN "public"."r_workshop_config_collect"."workshop_id" IS '设备id';
COMMENT ON COLUMN "public"."r_workshop_config_collect"."gateway_ids" IS '关联的网关ID列表，从 config.attrs 解析';
COMMENT ON TABLE "public"."r_workshop_config_collect" IS '场景采集配置';

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
COMMENT ON COLUMN "public"."t_bom"."self_code" IS '配方编号';
COMMENT ON COLUMN "public"."t_bom"."classify_code" IS '所属分类编号';
COMMENT ON COLUMN "public"."t_bom"."type" IS '配方类别（0: 固定数值，1：百分比数值）';
COMMENT ON COLUMN "public"."t_bom"."line" IS '暂时无用';
COMMENT ON TABLE "public"."t_bom" IS '配方';

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
COMMENT ON COLUMN "public"."t_bom_c"."name" IS '分类名称';
COMMENT ON COLUMN "public"."t_bom_c"."self_code" IS '分类编号';
COMMENT ON COLUMN "public"."t_bom_c"."code" IS '级联编号';
COMMENT ON COLUMN "public"."t_bom_c"."pcode" IS '级联父编号';
COMMENT ON COLUMN "public"."t_bom_c"."description" IS '描述';
COMMENT ON TABLE "public"."t_bom_c" IS '配方分类';

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
COMMENT ON COLUMN "public"."t_connect"."server_name" IS '服务名';
COMMENT ON COLUMN "public"."t_connect"."host" IS 'ip';
COMMENT ON COLUMN "public"."t_connect"."suffix" IS '后缀';
COMMENT ON COLUMN "public"."t_connect"."protocol" IS '协议类型';
COMMENT ON COLUMN "public"."t_connect"."enabled" IS '是否启用';
COMMENT ON COLUMN "public"."t_connect"."valid_type" IS '认证类型（0:无认证， 1:Baisc）';
COMMENT ON COLUMN "public"."t_connect"."collect_cron" IS '采集cron表达式';
COMMENT ON TABLE "public"."t_connect" IS '网关';

-- ----------------------------
-- Table structure for t_device
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_device";
CREATE TABLE "public"."t_device" (
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
  "code" varchar(50) COLLATE "pg_catalog"."default",
  "self_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "pcode" varchar(50) COLLATE "pg_catalog"."default",
  "dg_id" varchar(50) COLLATE "pg_catalog"."default",
  "type" int2,
  "localization" int2,
  "max_capacity" numeric(18,1),
  "available_capacity" numeric(18,1),
  "status" int2,
  "mat_code" varchar(20) COLLATE "pg_catalog"."default",
  "del_bit" int2 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."t_device"."code" IS '级联编号';
COMMENT ON COLUMN "public"."t_device"."self_code" IS '设备编号';
COMMENT ON COLUMN "public"."t_device"."pcode" IS '上级设备';
COMMENT ON COLUMN "public"."t_device"."dg_id" IS '设备工艺id';
COMMENT ON COLUMN "public"."t_device"."type" IS '设备类别（0: 秤，1：仓 ）';
COMMENT ON COLUMN "public"."t_device"."localization" IS '设备定位（子类，0:原料仓）';
COMMENT ON COLUMN "public"."t_device"."max_capacity" IS '最大容量（吨）';
COMMENT ON COLUMN "public"."t_device"."available_capacity" IS '可用余量';
COMMENT ON COLUMN "public"."t_device"."status" IS '状态（0：启用，1:禁用）';
COMMENT ON COLUMN "public"."t_device"."mat_code" IS '原料编号';
COMMENT ON COLUMN "public"."t_device"."del_bit" IS '删除标记';
COMMENT ON TABLE "public"."t_device" IS '设备仓';

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
COMMENT ON COLUMN "public"."t_dg"."self_code" IS '编号';
COMMENT ON TABLE "public"."t_dg" IS '设备工艺';

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
  "lng" numeric(12,8),
  "lat" numeric(12,8),
  "address" varchar(255) COLLATE "pg_catalog"."default",
  "enable_date" timestamp(6),
  "health_template_id" varchar(64) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_equip"."name" IS '设备名';
COMMENT ON COLUMN "public"."t_equip"."self_code" IS '设备编号';
COMMENT ON COLUMN "public"."t_equip"."type" IS '设备类别';
COMMENT ON COLUMN "public"."t_equip"."workshop_code" IS '场景编号';
COMMENT ON COLUMN "public"."t_equip"."lng" IS '经度';
COMMENT ON COLUMN "public"."t_equip"."lat" IS '纬度';
COMMENT ON COLUMN "public"."t_equip"."address" IS '地址';
COMMENT ON COLUMN "public"."t_equip"."enable_date" IS '启用日期，用于健康分使用年限计算';
COMMENT ON COLUMN "public"."t_equip"."health_template_id" IS '关联健康规则模板ID';
COMMENT ON TABLE "public"."t_equip" IS '设备表';

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
COMMENT ON COLUMN "public"."t_equip_collect"."sn" IS '设备sn号';
COMMENT ON COLUMN "public"."t_equip_collect"."data" IS '采集的数据';
COMMENT ON COLUMN "public"."t_equip_collect"."time" IS '采集时间';

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
COMMENT ON COLUMN "public"."t_equip_health_indicator"."id" IS '主键';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."equip_id" IS '设备ID';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."sn" IS '设备自编码';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."stat_time" IS '统计时间点';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_start" IS '周期开始';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_end" IS '周期结束';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."score" IS '健康得分0-100';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."health_level" IS '健康等级0=健康,1=关注,2=预警,3=故障';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."alarm_count" IS '报警次数';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."alarm_duration_minutes" IS '报警总时长(分钟)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."run_duration_minutes" IS '运行总时长(分钟)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."online_duration_minutes" IS '在线总时长(分钟)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."period_minutes" IS '统计周期(分钟)';
COMMENT ON COLUMN "public"."t_equip_health_indicator"."template_id" IS '规则模板ID';
COMMENT ON TABLE "public"."t_equip_health_indicator" IS '设备健康指标';

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
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."id" IS '主键';
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."name" IS '模板名称';
COMMENT ON COLUMN "public"."t_equip_health_rule_template"."config" IS '规则配置JSON';
COMMENT ON TABLE "public"."t_equip_health_rule_template" IS '设备健康规则模板';

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
COMMENT ON COLUMN "public"."t_equip_record_alarm"."sn" IS '设备sn号';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."start_time" IS '持续开始时间';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."end_time" IS '持续结束时间';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."state" IS '状态';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."reason" IS '报警原因';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."level" IS '报警等级: 0=轻微, 1=一般, 2=严重, 3=故障';
COMMENT ON COLUMN "public"."t_equip_record_alarm"."event_id" IS '事件id';
COMMENT ON TABLE "public"."t_equip_record_alarm" IS '设备报警状态记录';

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
COMMENT ON COLUMN "public"."t_equip_record_online"."sn" IS '设备sn号';
COMMENT ON COLUMN "public"."t_equip_record_online"."start_time" IS '持续开始时间';
COMMENT ON COLUMN "public"."t_equip_record_online"."end_time" IS '持续结束时间';
COMMENT ON COLUMN "public"."t_equip_record_online"."state" IS '状态';
COMMENT ON COLUMN "public"."t_equip_record_online"."event_id" IS '事件id';
COMMENT ON TABLE "public"."t_equip_record_online" IS '设备在线记录';

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
COMMENT ON COLUMN "public"."t_equip_record_run"."sn" IS '设备sn号';
COMMENT ON COLUMN "public"."t_equip_record_run"."start_time" IS '运行开始时间';
COMMENT ON COLUMN "public"."t_equip_record_run"."end_time" IS '运行结束时间';
COMMENT ON COLUMN "public"."t_equip_record_run"."power_start" IS '开始时电量';
COMMENT ON COLUMN "public"."t_equip_record_run"."power_end" IS '结束时电量';
COMMENT ON COLUMN "public"."t_equip_record_run"."state" IS '运行状态';
COMMENT ON COLUMN "public"."t_equip_record_run"."event_id" IS '事件id';
COMMENT ON TABLE "public"."t_equip_record_run" IS '设备运行表';

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
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."sn" IS '设备sn号';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."time" IS '记录时间';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."run_state" IS '运行状态';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."alarm_state" IS '报警状态';
COMMENT ON COLUMN "public"."t_equip_state_snapshot"."online_state" IS '在线状态';
COMMENT ON TABLE "public"."t_equip_state_snapshot" IS '设备状态快照';

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
COMMENT ON COLUMN "public"."t_gateway"."server_name" IS '服务名';
COMMENT ON COLUMN "public"."t_gateway"."uri" IS '地址';
COMMENT ON COLUMN "public"."t_gateway"."topic" IS '主题';
COMMENT ON COLUMN "public"."t_gateway"."params" IS '参数';
COMMENT ON COLUMN "public"."t_gateway"."protocol" IS '协议类型';
COMMENT ON COLUMN "public"."t_gateway"."enabled" IS '是否启用';
COMMENT ON COLUMN "public"."t_gateway"."valid_type" IS '认证类型（0:无认证， 1:Baisc）';
COMMENT ON COLUMN "public"."t_gateway"."collect_cron" IS '采集cron表达式';
COMMENT ON TABLE "public"."t_gateway" IS '网关';

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
COMMENT ON COLUMN "public"."t_inspect_item"."item_name" IS '巡检项名称';
COMMENT ON COLUMN "public"."t_inspect_item"."item_type" IS '类型：1选择 2数值 3是否';
COMMENT ON COLUMN "public"."t_inspect_item"."unit" IS '单位或选项：数值填A/MPa等；选择可填 正常/不足、松动/正常';
COMMENT ON COLUMN "public"."t_inspect_item"."min_value" IS '最小值';
COMMENT ON COLUMN "public"."t_inspect_item"."max_value" IS '最大值';
COMMENT ON COLUMN "public"."t_inspect_item"."required_flag" IS '是否必填：0否 1是';

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
COMMENT ON COLUMN "public"."t_inspect_person"."name" IS '姓名';
COMMENT ON COLUMN "public"."t_inspect_person"."account_id" IS '关联账户ID';
COMMENT ON COLUMN "public"."t_inspect_person"."account_name" IS '关联账户名称';
COMMENT ON COLUMN "public"."t_inspect_person"."mobile" IS '手机号';
COMMENT ON COLUMN "public"."t_inspect_person"."job_number" IS '工号';
COMMENT ON COLUMN "public"."t_inspect_person"."remark" IS '备注';

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
COMMENT ON COLUMN "public"."t_inspect_plan"."id" IS '主键';
COMMENT ON COLUMN "public"."t_inspect_plan"."name" IS '计划名称';
COMMENT ON COLUMN "public"."t_inspect_plan"."cycle_type" IS '周期类型：1每日 2每周 3每月';
COMMENT ON COLUMN "public"."t_inspect_plan"."cycle_config" IS '周期配置';
COMMENT ON COLUMN "public"."t_inspect_plan"."workshop_code" IS '场景编号';
COMMENT ON COLUMN "public"."t_inspect_plan"."equip_ids" IS '关联设备ID列表';
COMMENT ON COLUMN "public"."t_inspect_plan"."status" IS '状态：0禁用 1启用';
COMMENT ON COLUMN "public"."t_inspect_plan"."remark" IS '备注';
COMMENT ON COLUMN "public"."t_inspect_plan"."template_id" IS '关联巡检模板ID';
COMMENT ON TABLE "public"."t_inspect_plan" IS '巡检计划';

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
COMMENT ON COLUMN "public"."t_inspect_project"."name" IS '项目名称';
COMMENT ON COLUMN "public"."t_inspect_project"."code" IS '项目编号';
COMMENT ON COLUMN "public"."t_inspect_project"."status" IS '状态：0禁用 1启用';
COMMENT ON COLUMN "public"."t_inspect_project"."remark" IS '备注';

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
COMMENT ON COLUMN "public"."t_inspect_record"."id" IS '主键';
COMMENT ON COLUMN "public"."t_inspect_record"."task_id" IS '巡检任务ID';
COMMENT ON COLUMN "public"."t_inspect_record"."equip_id" IS '设备ID';
COMMENT ON COLUMN "public"."t_inspect_record"."equip_name" IS '设备编号/名称';
COMMENT ON COLUMN "public"."t_inspect_record"."score" IS '巡检分值';
COMMENT ON COLUMN "public"."t_inspect_record"."item_name" IS '巡检项名称';
COMMENT ON COLUMN "public"."t_inspect_record"."result" IS '巡检结果：0正常 1异常';
COMMENT ON COLUMN "public"."t_inspect_record"."remark" IS '备注';
COMMENT ON COLUMN "public"."t_inspect_record"."photo_urls" IS '照片URL，多张逗号分隔';
COMMENT ON COLUMN "public"."t_inspect_record"."record_time" IS '记录时间';
COMMENT ON TABLE "public"."t_inspect_record" IS '巡检记录';

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
COMMENT ON COLUMN "public"."t_inspect_record_item"."record_id" IS '巡检记录ID（主表）';
COMMENT ON COLUMN "public"."t_inspect_record_item"."item_id" IS '巡检项ID（模板项主键）';
COMMENT ON COLUMN "public"."t_inspect_record_item"."item_name" IS '巡检项名称';
COMMENT ON COLUMN "public"."t_inspect_record_item"."content" IS '巡检项内容：填写值/结果文本';
COMMENT ON COLUMN "public"."t_inspect_record_item"."result" IS '巡检结果：0正常 1异常';
COMMENT ON COLUMN "public"."t_inspect_record_item"."rule_score" IS '规则分值（配置项匹配得分，未乘权重）';
COMMENT ON COLUMN "public"."t_inspect_record_item"."score" IS '该项得分（按权重与配置规则计算）';
COMMENT ON COLUMN "public"."t_inspect_record_item"."remark" IS '备注';
COMMENT ON COLUMN "public"."t_inspect_record_item"."photo_urls" IS '照片URL，多张逗号分隔';
COMMENT ON TABLE "public"."t_inspect_record_item" IS '巡检记录明细（附表）';

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
COMMENT ON COLUMN "public"."t_inspect_task"."id" IS '主键';
COMMENT ON COLUMN "public"."t_inspect_task"."plan_id" IS '巡检计划ID';
COMMENT ON COLUMN "public"."t_inspect_task"."scheduled_time" IS '计划执行时间';
COMMENT ON COLUMN "public"."t_inspect_task"."status" IS '状态：0待执行 1执行中 2已完成 3已逾期';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_id" IS '执行人ID';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_name" IS '执行人姓名';
COMMENT ON COLUMN "public"."t_inspect_task"."actual_start_time" IS '实际开始时间';
COMMENT ON COLUMN "public"."t_inspect_task"."actual_end_time" IS '实际完成时间';
COMMENT ON COLUMN "public"."t_inspect_task"."remark" IS '备注';
COMMENT ON COLUMN "public"."t_inspect_task"."workshop_code" IS '关联设备ID列表';
COMMENT ON COLUMN "public"."t_inspect_task"."executor_person_id" IS '指派的巡检人员ID';
COMMENT ON COLUMN "public"."t_inspect_task"."template_id" IS '关联巡检模板ID（来自计划）';
COMMENT ON TABLE "public"."t_inspect_task" IS '巡检任务';

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
COMMENT ON COLUMN "public"."t_inspect_template"."name" IS '模板名称';
COMMENT ON COLUMN "public"."t_inspect_template"."remark" IS '备注';

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
COMMENT ON COLUMN "public"."t_inspect_template_item"."id" IS '主键';
COMMENT ON COLUMN "public"."t_inspect_template_item"."template_id" IS '所属模板ID';
COMMENT ON COLUMN "public"."t_inspect_template_item"."product_code" IS '所属产品编号（模板中的产品块）';
COMMENT ON COLUMN "public"."t_inspect_template_item"."reference_item_id" IS '引用的巡检项ID（从巡检项池载入时绑定）';
COMMENT ON COLUMN "public"."t_inspect_template_item"."sort_order" IS '排序号';
COMMENT ON COLUMN "public"."t_inspect_template_item"."weight" IS '权重（模板内该项的权重）';
COMMENT ON COLUMN "public"."t_inspect_template_item"."weight_rate" IS '百分比权重（该项权重/同产品块权重总值，如0.3）';
COMMENT ON COLUMN "public"."t_inspect_template_item"."rule_config" IS '规则配置JSON，如数值区间/是否/选项与分值';
COMMENT ON COLUMN "public"."t_inspect_template_item"."created_time" IS '创建时间';
COMMENT ON COLUMN "public"."t_inspect_template_item"."updated_time" IS '更新时间';
COMMENT ON COLUMN "public"."t_inspect_template_item"."tenant_id" IS '租户ID';
COMMENT ON TABLE "public"."t_inspect_template_item" IS '模板-巡检项关联表';

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
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."id" IS '主键';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."template_item_id" IS '模板巡检项ID，关联 t_inspect_template_item.id';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."rule_type" IS '规则类型：1=是否型，2=数值范围，3=选择值';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."bool_value" IS '是否型：0=否 1=是（rule_type=1时有效）';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."min_value" IS '数值型：最小值（含），rule_type=2';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."max_value" IS '数值型：最大值（含），rule_type=2';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."option_value" IS '选择型：选项值文本，rule_type=3';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."weight" IS '该条件命中时的得分/权重';
COMMENT ON COLUMN "public"."t_inspect_template_item_rule"."sort_order" IS '排序（同一项多条规则时的优先级）';
COMMENT ON TABLE "public"."t_inspect_template_item_rule" IS '模板巡检项评分规则';

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
COMMENT ON COLUMN "public"."t_line"."self_code" IS '产线编号(可以与工艺设置进行映射)';
COMMENT ON COLUMN "public"."t_line"."name" IS '产线名称';
COMMENT ON COLUMN "public"."t_line"."product_code" IS '加工对象编号';
COMMENT ON COLUMN "public"."t_line"."product_name" IS '加工对象名称';
COMMENT ON COLUMN "public"."t_line"."material_code" IS '原材料编号';
COMMENT ON COLUMN "public"."t_line"."material_name" IS '原材料名称';
COMMENT ON COLUMN "public"."t_line"."version_no" IS '版本号';
COMMENT ON COLUMN "public"."t_line"."throughput" IS '吞吐量(单次生产总量)';
COMMENT ON COLUMN "public"."t_line"."step_interval" IS '步进值（单位秒）';
COMMENT ON COLUMN "public"."t_line"."type" IS '类型(0:固定映射 1:参数映射 2:流程映射)';
COMMENT ON COLUMN "public"."t_line"."plc_time" IS 'plc下载时间';
COMMENT ON COLUMN "public"."t_line"."sync_time" IS '同步时间';
COMMENT ON COLUMN "public"."t_line"."map_db" IS '映射db';
COMMENT ON COLUMN "public"."t_line"."map_offset" IS '映射偏移';
COMMENT ON TABLE "public"."t_line" IS '产线/工艺';

-- ----------------------------
-- Table structure for t_biz_process (工艺卡片)
-- ----------------------------
DROP TABLE IF EXISTS "public"."m_biz_process_step_tooling";
DROP TABLE IF EXISTS "public"."m_biz_process_step_equipment";
DROP TABLE IF EXISTS "public"."r_biz_process_step_wip";
DROP TABLE IF EXISTS "public"."r_biz_process_step";
DROP TABLE IF EXISTS "public"."r_biz_process_mold";
DROP TABLE IF EXISTS "public"."t_biz_process";

CREATE TABLE "public"."t_biz_process" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "process_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "process_image_url" varchar(512) COLLATE "pg_catalog"."default",
  "process_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "product_code" varchar(64) COLLATE "pg_catalog"."default",
  "product_name" varchar(128) COLLATE "pg_catalog"."default",
  "component_code" varchar(64) COLLATE "pg_catalog"."default",
  "component_name" varchar(128) COLLATE "pg_catalog"."default",
  "material_code" varchar(64) COLLATE "pg_catalog"."default",
  "material_name" varchar(128) COLLATE "pg_catalog"."default",
  "tech_condition" varchar(64) COLLATE "pg_catalog"."default",
  "material_preheat" varchar(256) COLLATE "pg_catalog"."default",
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
CREATE UNIQUE INDEX uk_t_biz_process_code ON "public"."t_biz_process" ("process_code");
CREATE INDEX idx_t_biz_process_name ON "public"."t_biz_process" ("process_name");
COMMENT ON TABLE "public"."t_biz_process" IS '工艺卡片';

CREATE TABLE "public"."r_biz_process_mold" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "process_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "mold_drawing_no" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "slot_count" int4 NOT NULL,
  "sort_order" int4 DEFAULT 0 NOT NULL,
  PRIMARY KEY ("id")
);
CREATE INDEX idx_r_biz_process_mold_process ON "public"."r_biz_process_mold" ("process_id");
COMMENT ON TABLE "public"."r_biz_process_mold" IS '工艺压模图号';

CREATE TABLE "public"."r_biz_process_step" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "process_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "step_no" int4,
  "step_code" varchar(64) COLLATE "pg_catalog"."default",
  "step_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "step_content" varchar(2000) COLLATE "pg_catalog"."default",
  "step_script" text,
  "step_engine_config" text,
  "params" text,
  "sort_order" int4 DEFAULT 0 NOT NULL,
  PRIMARY KEY ("id")
);
CREATE INDEX idx_r_biz_process_step_process ON "public"."r_biz_process_step" ("process_id");
CREATE INDEX idx_r_biz_process_step_code ON "public"."r_biz_process_step" ("process_id", "step_code");
COMMENT ON TABLE "public"."r_biz_process_step" IS '工艺工序';

CREATE TABLE "public"."r_biz_process_step_wip" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "step_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  "produce_wip_flag" int2 DEFAULT 0 NOT NULL,
  "direct_transfer_flag" int2 DEFAULT 0 NOT NULL,
  "wip_type" varchar(64) COLLATE "pg_catalog"."default",
  "wip_hold_time_hours" numeric(10,2),
  "schedule_device_code" varchar(64) COLLATE "pg_catalog"."default",
  "wip_trigger_target_step_name" varchar(128) COLLATE "pg_catalog"."default",
  PRIMARY KEY ("id")
);
CREATE UNIQUE INDEX uk_r_biz_process_step_wip_name ON "public"."r_biz_process_step_wip" ("step_name");
CREATE INDEX idx_r_biz_process_step_wip_type ON "public"."r_biz_process_step_wip" ("wip_type");
COMMENT ON TABLE "public"."r_biz_process_step_wip" IS '工序WIP排产配置';

CREATE TABLE "public"."m_biz_process_step_equipment" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "step_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "equipment_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "equipment_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  PRIMARY KEY ("id")
);
CREATE INDEX idx_m_biz_process_step_equip_step ON "public"."m_biz_process_step_equipment" ("step_id");
COMMENT ON TABLE "public"."m_biz_process_step_equipment" IS '工序设备关联';

CREATE TABLE "public"."m_biz_process_step_tooling" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "step_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "tooling_code" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "tooling_name" varchar(128) COLLATE "pg_catalog"."default" NOT NULL,
  PRIMARY KEY ("id")
);
CREATE INDEX idx_m_biz_process_step_tool_step ON "public"."m_biz_process_step_tooling" ("step_id");
COMMENT ON TABLE "public"."m_biz_process_step_tooling" IS '工序工装关联';

-- ----------------------------
-- Table structure for t_mat
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mat";
CREATE TABLE "public"."t_mat" (
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
  "del_bit" int2 DEFAULT 0
)
;
COMMENT ON COLUMN "public"."t_mat"."self_code" IS '配方编号';
COMMENT ON COLUMN "public"."t_mat"."classify_code" IS '所属分类编号';
COMMENT ON COLUMN "public"."t_mat"."del_bit" IS '删除标记';
COMMENT ON TABLE "public"."t_mat" IS '原料';

-- ----------------------------
-- Table structure for t_mc
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mc";
CREATE TABLE "public"."t_mc" (
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
  "pcode" varchar(60) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_mc"."name" IS '分类名称';
COMMENT ON COLUMN "public"."t_mc"."self_code" IS '分类编号';
COMMENT ON COLUMN "public"."t_mc"."code" IS '级联编号';
COMMENT ON COLUMN "public"."t_mc"."pcode" IS '级联父编号';
COMMENT ON TABLE "public"."t_mc" IS '原料分类';

-- ----------------------------
-- Table structure for t_message
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_message";
CREATE TABLE "public"."t_message" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "title" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "context" varchar(255) COLLATE "pg_catalog"."default",
  "type" int2,
  "platform" varchar(40) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "source_id" varchar(20) COLLATE "pg_catalog"."default",
  "notify_id" varchar(20) COLLATE "pg_catalog"."default",
  "source" varchar(10) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_message"."title" IS '消息标题';
COMMENT ON COLUMN "public"."t_message"."context" IS '消息内容';
COMMENT ON COLUMN "public"."t_message"."type" IS '0:普通消息 1:报警消息';
COMMENT ON COLUMN "public"."t_message"."platform" IS '通知到的平台';
COMMENT ON COLUMN "public"."t_message"."source_id" IS '消息来源id';
COMMENT ON COLUMN "public"."t_message"."notify_id" IS '通知id';
COMMENT ON COLUMN "public"."t_message"."source" IS '来源类型';
COMMENT ON TABLE "public"."t_message" IS '消息';

-- ----------------------------
-- Table structure for t_mo
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
  "status" int2 NOT NULL,
  "product_id" varchar(20) COLLATE "pg_catalog"."default",
  "product_name" varchar(50) COLLATE "pg_catalog"."default",
  "product_code" varchar(50) COLLATE "pg_catalog"."default",
  "num" int4 NOT NULL,
  "weight" numeric(10,5),
  "del_bit" int2 DEFAULT 0,
  "exec_time" timestamp(6),
  "source" int2 NOT NULL,
  "surplus" int4 NOT NULL,
  "line_code" varchar(50) COLLATE "pg_catalog"."default",
  "source_id" varchar(50) COLLATE "pg_catalog"."default",
  "dg_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_mo"."self_code" IS '编号';
COMMENT ON COLUMN "public"."t_mo"."status" IS '清单状态';
COMMENT ON COLUMN "public"."t_mo"."product_id" IS '产品id';
COMMENT ON COLUMN "public"."t_mo"."product_name" IS '产品名称';
COMMENT ON COLUMN "public"."t_mo"."product_code" IS '产品编号';
COMMENT ON COLUMN "public"."t_mo"."num" IS '生产批次数量';
COMMENT ON COLUMN "public"."t_mo"."weight" IS '单批次生产重量';
COMMENT ON COLUMN "public"."t_mo"."exec_time" IS '预期执行时间';
COMMENT ON COLUMN "public"."t_mo"."source" IS '订单来源（0:系统下单，1:远程录入）';
COMMENT ON COLUMN "public"."t_mo"."surplus" IS '余量';
COMMENT ON COLUMN "public"."t_mo"."line_code" IS '产线/工艺编号';
COMMENT ON COLUMN "public"."t_mo"."source_id" IS '来源id，针对远程录入';
COMMENT ON COLUMN "public"."t_mo"."dg_id" IS '生产设备组id';
COMMENT ON TABLE "public"."t_mo" IS '生产清单';

-- ----------------------------
-- Table structure for t_mps
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_mps";
CREATE TABLE "public"."t_mps" (
  "id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "mo_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "sequence" int4,
  "line" varchar(20) COLLATE "pg_catalog"."default",
  "exec_time" timestamp(6),
  "batch" int4,
  "weight" numeric(10,5),
  "status" int2 NOT NULL,
  "priority" numeric(15,5),
  "del_bit" int2 DEFAULT 0,
  "qa_res" int2,
  "qa_status" int2,
  "line_source" int2,
  "num" int4,
  "dg_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_mps"."mo_code" IS '订单编号';
COMMENT ON COLUMN "public"."t_mps"."sequence" IS '生产执行序列号';
COMMENT ON COLUMN "public"."t_mps"."line" IS '产线编号';
COMMENT ON COLUMN "public"."t_mps"."exec_time" IS '执行时间';
COMMENT ON COLUMN "public"."t_mps"."batch" IS '批次';
COMMENT ON COLUMN "public"."t_mps"."weight" IS '生产目标';
COMMENT ON COLUMN "public"."t_mps"."status" IS '状态(0:待入队|1:待执行|2:执行中|3:生产完成|4.检测完成)';
COMMENT ON COLUMN "public"."t_mps"."priority" IS '生产优先级(从小到大),执行生产计划时根据它排序';
COMMENT ON COLUMN "public"."t_mps"."del_bit" IS '删除标记';
COMMENT ON COLUMN "public"."t_mps"."qa_res" IS 'QA结果（0:无结果，1:检测通过， 2:检测不合格）';
COMMENT ON COLUMN "public"."t_mps"."qa_status" IS 'QA状态（0:待检 1:检测中 2:检测完成）';
COMMENT ON COLUMN "public"."t_mps"."line_source" IS '产线工艺来源（0:系统选择 1:自定义传递）';
COMMENT ON COLUMN "public"."t_mps"."num" IS '生产数量';
COMMENT ON COLUMN "public"."t_mps"."dg_id" IS '生产设备组id';
COMMENT ON TABLE "public"."t_mps" IS '生产计划（生产排程）';

-- ----------------------------
-- Table structure for t_notify
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_notify";
CREATE TABLE "public"."t_notify" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "title" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "context" varchar(255) COLLATE "pg_catalog"."default",
  "type" int2,
  "status" int2,
  "platform" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "step" int4,
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "source" varchar(20) COLLATE "pg_catalog"."default",
  "source_id" varchar(20) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_notify"."title" IS '消息标题';
COMMENT ON COLUMN "public"."t_notify"."context" IS '消息内容';
COMMENT ON COLUMN "public"."t_notify"."type" IS '0:普通消息 1:报警消息';
COMMENT ON COLUMN "public"."t_notify"."status" IS '0:待通知 1:通知中 2:通知结束';
COMMENT ON COLUMN "public"."t_notify"."platform" IS '通知平台编号（，分割）';
COMMENT ON COLUMN "public"."t_notify"."step" IS '通知间隔（s，适用于报警消息）';
COMMENT ON COLUMN "public"."t_notify"."source" IS '通知来源(System, Equip）';
COMMENT ON COLUMN "public"."t_notify"."source_id" IS '来源id';
COMMENT ON TABLE "public"."t_notify" IS '通知';

-- ----------------------------
-- Table structure for t_product
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_product";
CREATE TABLE "public"."t_product" (
  "id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(50) COLLATE "pg_catalog"."default",
  "code" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "revision" int4,
  "image_url" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_product"."name" IS '产品名称';
COMMENT ON COLUMN "public"."t_product"."code" IS '产品编号';
COMMENT ON COLUMN "public"."t_product"."image_url" IS '产品图片地址';
COMMENT ON TABLE "public"."t_product" IS '产品（设备所属产品）';

-- ----------------------------
-- Table structure for t_qa
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_qa";
CREATE TABLE "public"."t_qa" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "mps_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "mps_tf_id" varchar(20) COLLATE "pg_catalog"."default",
  "msg" varchar(255) COLLATE "pg_catalog"."default",
  "result" int2,
  "pass" numeric(10,4)
)
;
COMMENT ON COLUMN "public"."t_qa"."mps_id" IS '执行计划id';
COMMENT ON COLUMN "public"."t_qa"."mps_tf_id" IS '执行流程id';
COMMENT ON COLUMN "public"."t_qa"."msg" IS '检测描述';
COMMENT ON COLUMN "public"."t_qa"."result" IS '检测结果（0:未检测，1:检测通过， 2:检测不合格）';
COMMENT ON COLUMN "public"."t_qa"."pass" IS '目标合格量';
COMMENT ON TABLE "public"."t_qa" IS '质量';

-- ----------------------------
-- Table structure for t_sync
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_sync";
CREATE TABLE "public"."t_sync" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "sync_tx" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "part_start_timestamp" timestamp(6) NOT NULL,
  "part_end_timestamp" timestamp(6) NOT NULL,
  "status" varchar(10) COLLATE "pg_catalog"."default" NOT NULL,
  "pre_min" varchar(50) COLLATE "pg_catalog"."default",
  "pre_max" varchar(50) COLLATE "pg_catalog"."default",
  "part_min" varchar(50) COLLATE "pg_catalog"."default",
  "part_max" varchar(50) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_sync"."sync_tx" IS '同步事务';
COMMENT ON COLUMN "public"."t_sync"."part_start_timestamp" IS '分片开始时间';
COMMENT ON COLUMN "public"."t_sync"."part_end_timestamp" IS '分片结束时间';
COMMENT ON COLUMN "public"."t_sync"."status" IS '同步状态（start|error|end）';
COMMENT ON COLUMN "public"."t_sync"."pre_min" IS '上一分片的最小值';
COMMENT ON COLUMN "public"."t_sync"."pre_max" IS '上一分片的最大值';
COMMENT ON COLUMN "public"."t_sync"."part_min" IS '分片最小值';
COMMENT ON COLUMN "public"."t_sync"."part_max" IS '分片最大值';
COMMENT ON TABLE "public"."t_sync" IS '同步事务管理器';

-- ----------------------------
-- Table structure for t_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_task";
CREATE TABLE "public"."t_task" (
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
  "type" varchar(40) COLLATE "pg_catalog"."default" NOT NULL,
  "cron" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "status" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."t_task"."name" IS '任务名称';
COMMENT ON COLUMN "public"."t_task"."type" IS '任务类型';
COMMENT ON COLUMN "public"."t_task"."cron" IS 'cron表达式';
COMMENT ON COLUMN "public"."t_task"."status" IS '0:未启用 1:已启用';
COMMENT ON TABLE "public"."t_task" IS '定时任务管理';

-- ----------------------------
-- Table structure for t_tf
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_tf";
CREATE TABLE "public"."t_tf" (
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
  "step_no" int4,
  "step_content" text,
  "equip_json" text,
  "tooling_json" text,
  "step_script" text,
  "step_engine_config" text,
  "line_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."t_tf"."self_code" IS '工艺流程编号(可以与工艺设置进行映射)';
COMMENT ON COLUMN "public"."t_tf"."name" IS '工艺流程名称';
COMMENT ON COLUMN "public"."t_tf"."step_no" IS '工序号';
COMMENT ON COLUMN "public"."t_tf"."step_content" IS '工序内容';
COMMENT ON COLUMN "public"."t_tf"."equip_json" IS '关联设备 JSON';
COMMENT ON COLUMN "public"."t_tf"."tooling_json" IS '关联工装 JSON';
COMMENT ON COLUMN "public"."t_tf"."step_script" IS '工序执行脚本 JSON';
COMMENT ON COLUMN "public"."t_tf"."step_engine_config" IS '流程引擎编译配置 JSON';
COMMENT ON COLUMN "public"."t_tf"."line_id" IS '产线id';
COMMENT ON TABLE "public"."t_tf" IS '工艺流程';

-- ----------------------------
-- Table structure for t_tf_edge
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_tf_edge";
CREATE TABLE "public"."t_tf_edge" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int8,
  "line_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "from_tf_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "to_tf_id" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(64) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(64) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default"
)
;

-- ----------------------------
-- Table structure for t_ucenter_depart
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_ucenter_depart";
CREATE TABLE "public"."t_ucenter_depart" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "name" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "pcode" varchar(20) COLLATE "pg_catalog"."default",
  "ppcode" varchar(150) COLLATE "pg_catalog"."default",
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."t_ucenter_depart"."name" IS '组名';
COMMENT ON COLUMN "public"."t_ucenter_depart"."code" IS '组编号';
COMMENT ON COLUMN "public"."t_ucenter_depart"."pcode" IS '父编号';
COMMENT ON COLUMN "public"."t_ucenter_depart"."ppcode" IS '祖宗编号链';
COMMENT ON COLUMN "public"."t_ucenter_depart"."tenant_id" IS '租户id';
COMMENT ON TABLE "public"."t_ucenter_depart" IS '用户组';

-- ----------------------------
-- Table structure for t_ucenter_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_ucenter_role";
CREATE TABLE "public"."t_ucenter_role" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(64) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(64) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "name" varchar(30) COLLATE "pg_catalog"."default" NOT NULL,
  "code" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "description" varchar(120) COLLATE "pg_catalog"."default" NOT NULL,
  "type" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."t_ucenter_role"."name" IS '角色名';
COMMENT ON COLUMN "public"."t_ucenter_role"."code" IS '角色编号';
COMMENT ON COLUMN "public"."t_ucenter_role"."description" IS '角色描述';
COMMENT ON COLUMN "public"."t_ucenter_role"."type" IS '角色分类';
COMMENT ON TABLE "public"."t_ucenter_role" IS '角色表';

-- ----------------------------
-- Table structure for t_workshop
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_workshop";
CREATE TABLE "public"."t_workshop" (
  "name" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "self_code" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "code" varchar(50) COLLATE "pg_catalog"."default",
  "pcode" varchar(50) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."t_workshop"."name" IS '车间名';
COMMENT ON COLUMN "public"."t_workshop"."self_code" IS '车间编号';
COMMENT ON COLUMN "public"."t_workshop"."code" IS '级联编号';
COMMENT ON COLUMN "public"."t_workshop"."pcode" IS '父级编号';

-- ----------------------------
-- Table structure for t_workshop_collect
-- ----------------------------
DROP TABLE IF EXISTS "public"."t_workshop_collect";
CREATE TABLE "public"."t_workshop_collect" (
  "id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "revision" int4,
  "created_by" varchar(20) COLLATE "pg_catalog"."default",
  "created_id" varchar(20) COLLATE "pg_catalog"."default",
  "created_time" timestamp(6),
  "updated_by" varchar(20) COLLATE "pg_catalog"."default",
  "updated_id" varchar(20) COLLATE "pg_catalog"."default",
  "updated_time" timestamp(6),
  "tenant_id" varchar(20) COLLATE "pg_catalog"."default",
  "workshop_id" varchar(50) COLLATE "pg_catalog"."default" NOT NULL,
  "data" jsonb,
  "time" timestamp(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."t_workshop_collect"."workshop_id" IS '场景id';
COMMENT ON COLUMN "public"."t_workshop_collect"."data" IS '采集的数据';
COMMENT ON COLUMN "public"."t_workshop_collect"."time" IS '采集时间';
COMMENT ON TABLE "public"."t_workshop_collect" IS '场景属性采集';

-- ----------------------------
-- Procedure structure for add_columnstore_policy
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."add_columnstore_policy"("hypertable" regclass, "after" any, "if_not_exists" bool, "schedule_interval" interval, "initial_start" timestamptz, "timezone" text, "created_before" interval);
CREATE OR REPLACE PROCEDURE "public"."add_columnstore_policy"("hypertable" regclass, "after" any=NULL::unknown, "if_not_exists" bool=false, "schedule_interval" interval=NULL::interval, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text, "created_before" interval=NULL::interval)
 AS '$libdir/timescaledb-2.26.3', 'ts_policy_compression_add'
  LANGUAGE c;

-- ----------------------------
-- Function structure for add_compression_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_compression_policy"("hypertable" regclass, "compress_after" any, "if_not_exists" bool, "schedule_interval" interval, "initial_start" timestamptz, "timezone" text, "compress_created_before" interval);
CREATE OR REPLACE FUNCTION "public"."add_compression_policy"("hypertable" regclass, "compress_after" any=NULL::unknown, "if_not_exists" bool=false, "schedule_interval" interval=NULL::interval, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text, "compress_created_before" interval=NULL::interval)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_policy_compression_add'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for add_continuous_aggregate_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_continuous_aggregate_policy"("continuous_aggregate" regclass, "start_offset" any, "end_offset" any, "schedule_interval" interval, "if_not_exists" bool, "initial_start" timestamptz, "timezone" text, "include_tiered_data" bool, "buckets_per_batch" int4, "max_batches_per_execution" int4, "refresh_newest_first" bool);
CREATE OR REPLACE FUNCTION "public"."add_continuous_aggregate_policy"("continuous_aggregate" regclass, "start_offset" any, "end_offset" any, "schedule_interval" interval, "if_not_exists" bool=false, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text, "include_tiered_data" bool=NULL::boolean, "buckets_per_batch" int4=NULL::integer, "max_batches_per_execution" int4=NULL::integer, "refresh_newest_first" bool=NULL::boolean)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_policy_refresh_cagg_add'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for add_dimension
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_dimension"("hypertable" regclass, "dimension" "_timescaledb_internal"."dimension_info", "if_not_exists" bool);
CREATE OR REPLACE FUNCTION "public"."add_dimension"("hypertable" regclass, "dimension" "_timescaledb_internal"."dimension_info", "if_not_exists" bool=false)
  RETURNS TABLE("dimension_id" int4, "created" bool) AS '$libdir/timescaledb-2.26.3', 'ts_dimension_add_general'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for add_dimension
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_dimension"("hypertable" regclass, "column_name" name, "number_partitions" int4, "chunk_time_interval" anyelement, "partitioning_func" regproc, "if_not_exists" bool);
CREATE OR REPLACE FUNCTION "public"."add_dimension"("hypertable" regclass, "column_name" name, "number_partitions" int4=NULL::integer, "chunk_time_interval" anyelement=NULL::bigint, "partitioning_func" regproc=NULL::regproc, "if_not_exists" bool=false)
  RETURNS TABLE("dimension_id" int4, "schema_name" name, "table_name" name, "column_name" name, "created" bool) AS '$libdir/timescaledb-2.26.3', 'ts_dimension_add'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for add_job
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_job"("proc" regproc, "schedule_interval" interval, "config" jsonb, "initial_start" timestamptz, "scheduled" bool, "check_config" regproc, "fixed_schedule" bool, "timezone" text, "job_name" text);
CREATE OR REPLACE FUNCTION "public"."add_job"("proc" regproc, "schedule_interval" interval, "config" jsonb=NULL::jsonb, "initial_start" timestamptz=NULL::timestamp with time zone, "scheduled" bool=true, "check_config" regproc=NULL::regproc, "fixed_schedule" bool=true, "timezone" text=NULL::text, "job_name" text=NULL::text)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_job_add'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for add_process_hypertable_invalidations_policy
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."add_process_hypertable_invalidations_policy"("hypertable" regclass, "schedule_interval" interval, "if_not_exists" bool, "initial_start" timestamptz, "timezone" text);
CREATE OR REPLACE PROCEDURE "public"."add_process_hypertable_invalidations_policy"("hypertable" regclass, "schedule_interval" interval, "if_not_exists" bool=false, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text)
 AS '$libdir/timescaledb-2.26.3', 'ts_policy_process_hyper_inval_add'
  LANGUAGE c;

-- ----------------------------
-- Function structure for add_reorder_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_reorder_policy"("hypertable" regclass, "index_name" name, "if_not_exists" bool, "initial_start" timestamptz, "timezone" text);
CREATE OR REPLACE FUNCTION "public"."add_reorder_policy"("hypertable" regclass, "index_name" name, "if_not_exists" bool=false, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_policy_reorder_add'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for add_retention_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."add_retention_policy"("relation" regclass, "drop_after" any, "if_not_exists" bool, "schedule_interval" interval, "initial_start" timestamptz, "timezone" text, "drop_created_before" interval);
CREATE OR REPLACE FUNCTION "public"."add_retention_policy"("relation" regclass, "drop_after" any=NULL::unknown, "if_not_exists" bool=false, "schedule_interval" interval=NULL::interval, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text, "drop_created_before" interval=NULL::interval)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_policy_retention_add'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for alter_job
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."alter_job"("job_id" int4, "schedule_interval" interval, "max_runtime" interval, "max_retries" int4, "retry_period" interval, "scheduled" bool, "config" jsonb, "next_start" timestamptz, "if_exists" bool, "check_config" regproc, "fixed_schedule" bool, "initial_start" timestamptz, "timezone" text, "job_name" text);
CREATE OR REPLACE FUNCTION "public"."alter_job"("job_id" int4, "schedule_interval" interval=NULL::interval, "max_runtime" interval=NULL::interval, "max_retries" int4=NULL::integer, "retry_period" interval=NULL::interval, "scheduled" bool=NULL::boolean, "config" jsonb=NULL::jsonb, "next_start" timestamptz=NULL::timestamp with time zone, "if_exists" bool=false, "check_config" regproc=NULL::regproc, "fixed_schedule" bool=NULL::boolean, "initial_start" timestamptz=NULL::timestamp with time zone, "timezone" text=NULL::text, "job_name" text=NULL::text)
  RETURNS TABLE("job_id" int4, "schedule_interval" interval, "max_runtime" interval, "max_retries" int4, "retry_period" interval, "scheduled" bool, "config" jsonb, "next_start" timestamptz, "check_config" text, "fixed_schedule" bool, "initial_start" timestamptz, "timezone" text, "application_name" name) AS '$libdir/timescaledb-2.26.3', 'ts_job_alter'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for approximate_row_count
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."approximate_row_count"("relation" regclass);
CREATE OR REPLACE FUNCTION "public"."approximate_row_count"("relation" regclass)
  RETURNS "pg_catalog"."int8" AS $BODY$
DECLARE
    v_mat_ht REGCLASS = NULL;
    v_name NAME = NULL;
    v_schema NAME = NULL;
    v_hypertable_id INTEGER;
BEGIN
    -- Check if input relation is continuous aggregate view then
    -- get the corresponding materialized hypertable and schema name
    SELECT format('%I.%I', ht.schema_name, ht.table_name)::regclass INTO v_mat_ht
      FROM pg_class c
      JOIN pg_namespace n ON (n.OID = c.relnamespace)
      JOIN _timescaledb_catalog.continuous_agg a ON (a.user_view_schema = n.nspname AND a.user_view_name = c.relname)
      JOIN _timescaledb_catalog.hypertable ht ON (a.mat_hypertable_id = ht.id)
      WHERE c.OID = relation;

    IF FOUND THEN
        relation = v_mat_ht;
    END IF;

    SELECT nspname, relname FROM pg_class c
    INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
    INTO v_schema, v_name
    WHERE c.OID = relation;

    -- for hypertables return the sum of the row counts of all chunks
    SELECT id FROM _timescaledb_catalog.hypertable INTO v_hypertable_id WHERE table_name = v_name AND schema_name = v_schema;
    IF FOUND THEN
        RETURN (SELECT coalesce(sum(_timescaledb_functions.get_approx_row_count(format('%I.%I',schema_name,table_name))),0)
          FROM _timescaledb_catalog.chunk
          WHERE hypertable_id = v_hypertable_id);
    END IF;

		IF EXISTS (SELECT FROM pg_inherits WHERE inhparent = relation) THEN
		RETURN (
        SELECT _timescaledb_functions.get_approx_row_count(relation) + COALESCE(SUM(public.approximate_row_count(i.inhrelid)),0) FROM pg_inherits i
        WHERE i.inhparent = relation
     );
    END IF;

    -- Check for input relation is Plain RELATION
    RETURN _timescaledb_functions.get_approx_row_count(relation);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Procedure structure for attach_chunk
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."attach_chunk"("hypertable" regclass, "chunk" regclass, "slices" jsonb);
CREATE OR REPLACE PROCEDURE "public"."attach_chunk"("hypertable" regclass, "chunk" regclass, "slices" jsonb)
 AS '$libdir/timescaledb-2.26.3', 'ts_attach_chunk'
  LANGUAGE c;

-- ----------------------------
-- Function structure for attach_tablespace
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."attach_tablespace"("tablespace" name, "hypertable" regclass, "if_not_attached" bool);
CREATE OR REPLACE FUNCTION "public"."attach_tablespace"("tablespace" name, "hypertable" regclass, "if_not_attached" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_tablespace_attach'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for by_hash
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."by_hash"("column_name" name, "number_partitions" int4, "partition_func" regproc);
CREATE OR REPLACE FUNCTION "public"."by_hash"("column_name" name, "number_partitions" int4, "partition_func" regproc=NULL::regproc)
  RETURNS "_timescaledb_internal"."dimension_info" AS '$libdir/timescaledb-2.26.3', 'ts_hash_dimension'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for by_range
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."by_range"("column_name" name, "partition_interval" anyelement, "partition_func" regproc);
CREATE OR REPLACE FUNCTION "public"."by_range"("column_name" name, "partition_interval" anyelement=NULL::bigint, "partition_func" regproc=NULL::regproc)
  RETURNS "_timescaledb_internal"."dimension_info" AS '$libdir/timescaledb-2.26.3', 'ts_range_dimension'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for chunk_columnstore_stats
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."chunk_columnstore_stats"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."chunk_columnstore_stats"("hypertable" regclass)
  RETURNS TABLE("chunk_schema" name, "chunk_name" name, "compression_status" text, "before_compression_table_bytes" int8, "before_compression_index_bytes" int8, "before_compression_toast_bytes" int8, "before_compression_total_bytes" int8, "after_compression_table_bytes" int8, "after_compression_index_bytes" int8, "after_compression_toast_bytes" int8, "after_compression_total_bytes" int8, "node_name" name) AS $BODY$SELECT * FROM public.chunk_compression_stats($1)$BODY$
  LANGUAGE sql STABLE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for chunk_compression_stats
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."chunk_compression_stats"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."chunk_compression_stats"("hypertable" regclass)
  RETURNS TABLE("chunk_schema" name, "chunk_name" name, "compression_status" text, "before_compression_table_bytes" int8, "before_compression_index_bytes" int8, "before_compression_toast_bytes" int8, "before_compression_total_bytes" int8, "after_compression_table_bytes" int8, "after_compression_index_bytes" int8, "after_compression_toast_bytes" int8, "after_compression_total_bytes" int8, "node_name" name) AS $BODY$
DECLARE
    table_name name;
    schema_name name;
BEGIN
    SELECT
      relname, nspname
    INTO
	    table_name, schema_name
    FROM
        pg_class c
        INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
        INNER JOIN _timescaledb_catalog.hypertable ht ON (ht.schema_name = n.nspname
                AND ht.table_name = c.relname)
    WHERE
        c.OID = hypertable;

    IF table_name IS NULL THEN
	    RETURN;
	END IF;

  RETURN QUERY
  SELECT
      *,
      NULL::name
  FROM
      _timescaledb_functions.compressed_chunk_local_stats(schema_name, table_name);
END;
$BODY$
  LANGUAGE plpgsql STABLE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for chunks_detailed_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."chunks_detailed_size"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."chunks_detailed_size"("hypertable" regclass)
  RETURNS TABLE("chunk_schema" name, "chunk_name" name, "table_bytes" int8, "index_bytes" int8, "toast_bytes" int8, "total_bytes" int8, "node_name" name) AS $BODY$
DECLARE
        table_name       NAME;
        schema_name      NAME;
BEGIN
        SELECT relname, nspname
        INTO table_name, schema_name
        FROM pg_class c
        INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
        INNER JOIN _timescaledb_catalog.hypertable ht ON (ht.schema_name = n.nspname AND ht.table_name = c.relname)
        WHERE c.OID = hypertable;

        IF table_name IS NULL THEN
            SELECT h.schema_name, h.table_name
            INTO schema_name, table_name
            FROM pg_class c
            INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
            INNER JOIN _timescaledb_catalog.continuous_agg a ON (a.user_view_schema = n.nspname AND a.user_view_name = c.relname)
            INNER JOIN _timescaledb_catalog.hypertable h ON h.id = a.mat_hypertable_id
            WHERE c.OID = hypertable;

            IF table_name IS NULL THEN
                RETURN;
            END IF;
		END IF;

    RETURN QUERY SELECT chl.chunk_schema, chl.chunk_name, chl.table_bytes, chl.index_bytes,
                        chl.toast_bytes, chl.total_bytes, NULL::NAME
            FROM _timescaledb_functions.chunks_local_size(schema_name, table_name) chl;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for compress_chunk
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."compress_chunk"("uncompressed_chunk" regclass, "if_not_compressed" bool, "recompress" bool);
CREATE OR REPLACE FUNCTION "public"."compress_chunk"("uncompressed_chunk" regclass, "if_not_compressed" bool=true, "recompress" bool=false)
  RETURNS "pg_catalog"."regclass" AS '$libdir/timescaledb-2.26.3', 'ts_compress_chunk'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for convert_to_columnstore
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."convert_to_columnstore"("chunk" regclass, "if_not_columnstore" bool, "recompress" bool);
CREATE OR REPLACE PROCEDURE "public"."convert_to_columnstore"("chunk" regclass, "if_not_columnstore" bool=true, "recompress" bool=false)
 AS '$libdir/timescaledb-2.26.3', 'ts_compress_chunk'
  LANGUAGE c;

-- ----------------------------
-- Procedure structure for convert_to_rowstore
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."convert_to_rowstore"("chunk" regclass, "if_columnstore" bool);
CREATE OR REPLACE PROCEDURE "public"."convert_to_rowstore"("chunk" regclass, "if_columnstore" bool=true)
 AS '$libdir/timescaledb-2.26.3', 'ts_decompress_chunk'
  LANGUAGE c;

-- ----------------------------
-- Function structure for create_hypertable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."create_hypertable"("relation" regclass, "dimension" "_timescaledb_internal"."dimension_info", "create_default_indexes" bool, "if_not_exists" bool, "migrate_data" bool);
CREATE OR REPLACE FUNCTION "public"."create_hypertable"("relation" regclass, "dimension" "_timescaledb_internal"."dimension_info", "create_default_indexes" bool=true, "if_not_exists" bool=false, "migrate_data" bool=false)
  RETURNS TABLE("hypertable_id" int4, "created" bool) AS '$libdir/timescaledb-2.26.3', 'ts_hypertable_create_general'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for create_hypertable
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."create_hypertable"("relation" regclass, "time_column_name" name, "partitioning_column" name, "number_partitions" int4, "associated_schema_name" name, "associated_table_prefix" name, "chunk_time_interval" anyelement, "create_default_indexes" bool, "if_not_exists" bool, "partitioning_func" regproc, "migrate_data" bool, "chunk_target_size" text, "chunk_sizing_func" regproc, "time_partitioning_func" regproc);
CREATE OR REPLACE FUNCTION "public"."create_hypertable"("relation" regclass, "time_column_name" name, "partitioning_column" name=NULL::name, "number_partitions" int4=NULL::integer, "associated_schema_name" name=NULL::name, "associated_table_prefix" name=NULL::name, "chunk_time_interval" anyelement=NULL::bigint, "create_default_indexes" bool=true, "if_not_exists" bool=false, "partitioning_func" regproc=NULL::regproc, "migrate_data" bool=false, "chunk_target_size" text=NULL::text, "chunk_sizing_func" regproc='_timescaledb_functions.calculate_chunk_interval'::regproc, "time_partitioning_func" regproc=NULL::regproc)
  RETURNS TABLE("hypertable_id" int4, "schema_name" name, "table_name" name, "created" bool) AS '$libdir/timescaledb-2.26.3', 'ts_hypertable_create'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for decompress_chunk
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."decompress_chunk"("uncompressed_chunk" regclass, "if_compressed" bool);
CREATE OR REPLACE FUNCTION "public"."decompress_chunk"("uncompressed_chunk" regclass, "if_compressed" bool=true)
  RETURNS "pg_catalog"."regclass" AS '$libdir/timescaledb-2.26.3', 'ts_decompress_chunk'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for delete_job
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."delete_job"("job_id" int4);
CREATE OR REPLACE FUNCTION "public"."delete_job"("job_id" int4)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_job_delete'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Procedure structure for detach_chunk
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."detach_chunk"("chunk" regclass);
CREATE OR REPLACE PROCEDURE "public"."detach_chunk"("chunk" regclass)
 AS '$libdir/timescaledb-2.26.3', 'ts_detach_chunk'
  LANGUAGE c;

-- ----------------------------
-- Function structure for detach_tablespace
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."detach_tablespace"("tablespace" name, "hypertable" regclass, "if_attached" bool);
CREATE OR REPLACE FUNCTION "public"."detach_tablespace"("tablespace" name, "hypertable" regclass=NULL::regclass, "if_attached" bool=false)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_tablespace_detach'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for detach_tablespaces
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."detach_tablespaces"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."detach_tablespaces"("hypertable" regclass)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_tablespace_detach_all_from_hypertable'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for disable_chunk_skipping
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."disable_chunk_skipping"("hypertable" regclass, "column_name" name, "if_not_exists" bool);
CREATE OR REPLACE FUNCTION "public"."disable_chunk_skipping"("hypertable" regclass, "column_name" name, "if_not_exists" bool=false)
  RETURNS TABLE("hypertable_id" int4, "column_name" name, "disabled" bool) AS '$libdir/timescaledb-2.26.3', 'ts_chunk_column_stats_disable'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for drop_chunks
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."drop_chunks"("relation" regclass, "older_than" any, "newer_than" any, "verbose" bool, "created_before" any, "created_after" any);
CREATE OR REPLACE FUNCTION "public"."drop_chunks"("relation" regclass, "older_than" any=NULL::unknown, "newer_than" any=NULL::unknown, "verbose" bool=false, "created_before" any=NULL::unknown, "created_after" any=NULL::unknown)
  RETURNS SETOF "pg_catalog"."text" AS '$libdir/timescaledb-2.26.3', 'ts_chunk_drop_chunks'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for enable_chunk_skipping
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."enable_chunk_skipping"("hypertable" regclass, "column_name" name, "if_not_exists" bool);
CREATE OR REPLACE FUNCTION "public"."enable_chunk_skipping"("hypertable" regclass, "column_name" name, "if_not_exists" bool=false)
  RETURNS TABLE("column_stats_id" int4, "enabled" bool) AS '$libdir/timescaledb-2.26.3', 'ts_chunk_column_stats_enable'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for generate_uuidv7
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."generate_uuidv7"();
CREATE OR REPLACE FUNCTION "public"."generate_uuidv7"()
  RETURNS "pg_catalog"."uuid" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_generate_v7'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for get_telemetry_report
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."get_telemetry_report"();
CREATE OR REPLACE FUNCTION "public"."get_telemetry_report"()
  RETURNS "pg_catalog"."jsonb" AS '$libdir/timescaledb-2.26.3', 'ts_telemetry_get_report_jsonb'
  LANGUAGE c STABLE
  COST 1;

-- ----------------------------
-- Function structure for hypertable_approximate_detailed_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_approximate_detailed_size"("relation" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_approximate_detailed_size"("relation" regclass)
  RETURNS TABLE("table_bytes" int8, "index_bytes" int8, "toast_bytes" int8, "total_bytes" int8) AS '$libdir/timescaledb-2.26.3', 'ts_hypertable_approximate_size'
  LANGUAGE c VOLATILE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for hypertable_approximate_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_approximate_size"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_approximate_size"("hypertable" regclass)
  RETURNS "pg_catalog"."int8" AS $BODY$
   SELECT sum(total_bytes)::bigint
   FROM public.hypertable_approximate_detailed_size(hypertable);
$BODY$
  LANGUAGE sql VOLATILE STRICT
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for hypertable_columnstore_stats
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_columnstore_stats"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_columnstore_stats"("hypertable" regclass)
  RETURNS TABLE("total_chunks" int8, "number_compressed_chunks" int8, "before_compression_table_bytes" int8, "before_compression_index_bytes" int8, "before_compression_toast_bytes" int8, "before_compression_total_bytes" int8, "after_compression_table_bytes" int8, "after_compression_index_bytes" int8, "after_compression_toast_bytes" int8, "after_compression_total_bytes" int8, "node_name" name) AS $BODY$SELECT * FROM public.hypertable_compression_stats($1)$BODY$
  LANGUAGE sql STABLE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for hypertable_compression_stats
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_compression_stats"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_compression_stats"("hypertable" regclass)
  RETURNS TABLE("total_chunks" int8, "number_compressed_chunks" int8, "before_compression_table_bytes" int8, "before_compression_index_bytes" int8, "before_compression_toast_bytes" int8, "before_compression_total_bytes" int8, "after_compression_table_bytes" int8, "after_compression_index_bytes" int8, "after_compression_toast_bytes" int8, "after_compression_total_bytes" int8, "node_name" name) AS $BODY$
	SELECT
        count(*)::bigint AS total_chunks,
        (count(*) FILTER (WHERE ch.compression_status = 'Compressed'))::bigint AS number_compressed_chunks,
        sum(ch.before_compression_table_bytes)::bigint AS before_compression_table_bytes,
        sum(ch.before_compression_index_bytes)::bigint AS before_compression_index_bytes,
        sum(ch.before_compression_toast_bytes)::bigint AS before_compression_toast_bytes,
        sum(ch.before_compression_total_bytes)::bigint AS before_compression_total_bytes,
        sum(ch.after_compression_table_bytes)::bigint AS after_compression_table_bytes,
        sum(ch.after_compression_index_bytes)::bigint AS after_compression_index_bytes,
        sum(ch.after_compression_toast_bytes)::bigint AS after_compression_toast_bytes,
        sum(ch.after_compression_total_bytes)::bigint AS after_compression_total_bytes,
        ch.node_name
    FROM
	    public.chunk_compression_stats(hypertable) ch
    GROUP BY
        ch.node_name;
$BODY$
  LANGUAGE sql STABLE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for hypertable_detailed_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_detailed_size"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_detailed_size"("hypertable" regclass)
  RETURNS TABLE("table_bytes" int8, "index_bytes" int8, "toast_bytes" int8, "total_bytes" int8, "node_name" name) AS $BODY$
DECLARE
        table_name       NAME = NULL;
        schema_name      NAME = NULL;
BEGIN
        SELECT relname, nspname
        INTO table_name, schema_name
        FROM pg_class c
        INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
        INNER JOIN _timescaledb_catalog.hypertable ht ON (ht.schema_name = n.nspname AND ht.table_name = c.relname)
        WHERE c.OID = hypertable;

        IF table_name IS NULL THEN
                SELECT h.schema_name, h.table_name
                INTO schema_name, table_name
                FROM pg_class c
                INNER JOIN pg_namespace n ON (n.OID = c.relnamespace)
                INNER JOIN _timescaledb_catalog.continuous_agg a ON (a.user_view_schema = n.nspname AND a.user_view_name = c.relname)
                INNER JOIN _timescaledb_catalog.hypertable h ON h.id = a.mat_hypertable_id
                WHERE c.OID = hypertable;

	        IF table_name IS NULL THEN
                        RETURN;
                END IF;
        END IF;

			RETURN QUERY
			SELECT *, NULL::name
			FROM _timescaledb_functions.hypertable_local_size(schema_name, table_name);
END;
$BODY$
  LANGUAGE plpgsql VOLATILE STRICT
  COST 100
  ROWS 1000
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for hypertable_index_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_index_size"("index_name" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_index_size"("index_name" regclass)
  RETURNS "pg_catalog"."int8" AS $BODY$
  SELECT
  	pg_relation_size(ht_i.indexrelid) + COALESCE(sum(pg_relation_size(ch_i.indexrelid)), 0)
  FROM pg_index ht_i
  LEFT JOIN pg_inherits ch on ch.inhparent = ht_i.indrelid
  LEFT JOIN pg_index ch_i on ch_i.indrelid = ch.inhrelid and _timescaledb_functions.index_matches(ht_i.indexrelid, ch_i.indexrelid)
  WHERE ht_i.indexrelid = index_name
  GROUP BY ht_i.indexrelid;
$BODY$
  LANGUAGE sql VOLATILE STRICT
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for hypertable_size
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."hypertable_size"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."hypertable_size"("hypertable" regclass)
  RETURNS "pg_catalog"."int8" AS $BODY$
   SELECT total_bytes::bigint FROM public.hypertable_detailed_size(hypertable);
$BODY$
  LANGUAGE sql VOLATILE STRICT
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for interpolate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."interpolate"("value" int8, "prev" record, "next" record);
CREATE OR REPLACE FUNCTION "public"."interpolate"("value" int8, "prev" record=NULL::record, "next" record=NULL::record)
  RETURNS "pg_catalog"."int8" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for interpolate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."interpolate"("value" float4, "prev" record, "next" record);
CREATE OR REPLACE FUNCTION "public"."interpolate"("value" float4, "prev" record=NULL::record, "next" record=NULL::record)
  RETURNS "pg_catalog"."float4" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for interpolate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."interpolate"("value" int4, "prev" record, "next" record);
CREATE OR REPLACE FUNCTION "public"."interpolate"("value" int4, "prev" record=NULL::record, "next" record=NULL::record)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for interpolate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."interpolate"("value" float8, "prev" record, "next" record);
CREATE OR REPLACE FUNCTION "public"."interpolate"("value" float8, "prev" record=NULL::record, "next" record=NULL::record)
  RETURNS "pg_catalog"."float8" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for interpolate
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."interpolate"("value" int2, "prev" record, "next" record);
CREATE OR REPLACE FUNCTION "public"."interpolate"("value" int2, "prev" record=NULL::record, "next" record=NULL::record)
  RETURNS "pg_catalog"."int2" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for locf
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."locf"("value" anyelement, "prev" anyelement, "treat_null_as_missing" bool);
CREATE OR REPLACE FUNCTION "public"."locf"("value" anyelement, "prev" anyelement=NULL::unknown, "treat_null_as_missing" bool=false)
  RETURNS "pg_catalog"."anyelement" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_marker'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for merge_chunks
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."merge_chunks"("chunk1" regclass, "chunk2" regclass, "concurrently" bool);
CREATE OR REPLACE PROCEDURE "public"."merge_chunks"("chunk1" regclass, "chunk2" regclass, "concurrently" bool=false)
 AS '$libdir/timescaledb-2.26.3', 'ts_merge_two_chunks'
  LANGUAGE c;

-- ----------------------------
-- Procedure structure for merge_chunks
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."merge_chunks"("chunks" _regclass);
CREATE OR REPLACE PROCEDURE "public"."merge_chunks"("chunks" _regclass)
 AS '$libdir/timescaledb-2.26.3', 'ts_merge_chunks'
  LANGUAGE c;

-- ----------------------------
-- Procedure structure for merge_chunks_concurrently
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."merge_chunks_concurrently"("chunks" _regclass);
CREATE OR REPLACE PROCEDURE "public"."merge_chunks_concurrently"("chunks" _regclass)
 AS '$libdir/timescaledb-2.26.3', 'ts_merge_chunks_concurrently'
  LANGUAGE c;

-- ----------------------------
-- Function structure for move_chunk
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."move_chunk"("chunk" regclass, "destination_tablespace" name, "index_destination_tablespace" name, "reorder_index" regclass, "verbose" bool);
CREATE OR REPLACE FUNCTION "public"."move_chunk"("chunk" regclass, "destination_tablespace" name, "index_destination_tablespace" name=NULL::name, "reorder_index" regclass=NULL::regclass, "verbose" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_move_chunk'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for recompress_chunk
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."recompress_chunk"("chunk" regclass, "if_not_compressed" bool);
CREATE OR REPLACE PROCEDURE "public"."recompress_chunk"("chunk" regclass, "if_not_compressed" bool=true)
 AS $BODY$
BEGIN
  IF current_setting('timescaledb.enable_deprecation_warnings', true)::bool THEN
    RAISE WARNING 'procedure public.recompress_chunk(regclass,boolean) is deprecated and the functionality is now included in public.compress_chunk. this compatibility function will be removed in a future version.';
  END IF;
  PERFORM public.compress_chunk(chunk, if_not_compressed);
END$BODY$
  LANGUAGE plpgsql
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Procedure structure for refresh_continuous_aggregate
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."refresh_continuous_aggregate"("continuous_aggregate" regclass, "window_start" any, "window_end" any, "force" bool, "options" jsonb);
CREATE OR REPLACE PROCEDURE "public"."refresh_continuous_aggregate"("continuous_aggregate" regclass, "window_start" any, "window_end" any, "force" bool=false, "options" jsonb=NULL::jsonb)
 AS '$libdir/timescaledb-2.26.3', 'ts_continuous_agg_refresh'
  LANGUAGE c;

-- ----------------------------
-- Procedure structure for remove_columnstore_policy
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."remove_columnstore_policy"("hypertable" regclass, "if_exists" bool);
CREATE OR REPLACE PROCEDURE "public"."remove_columnstore_policy"("hypertable" regclass, "if_exists" bool=false)
 AS '$libdir/timescaledb-2.26.3', 'ts_policy_compression_remove'
  LANGUAGE c;

-- ----------------------------
-- Function structure for remove_compression_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."remove_compression_policy"("hypertable" regclass, "if_exists" bool);
CREATE OR REPLACE FUNCTION "public"."remove_compression_policy"("hypertable" regclass, "if_exists" bool=false)
  RETURNS "pg_catalog"."bool" AS '$libdir/timescaledb-2.26.3', 'ts_policy_compression_remove'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for remove_continuous_aggregate_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."remove_continuous_aggregate_policy"("continuous_aggregate" regclass, "if_not_exists" bool, "if_exists" bool);
CREATE OR REPLACE FUNCTION "public"."remove_continuous_aggregate_policy"("continuous_aggregate" regclass, "if_not_exists" bool=false, "if_exists" bool=NULL::boolean)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_policy_refresh_cagg_remove'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for remove_process_hypertable_invalidations_policy
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."remove_process_hypertable_invalidations_policy"("hypertable" regclass, "if_exists" bool);
CREATE OR REPLACE PROCEDURE "public"."remove_process_hypertable_invalidations_policy"("hypertable" regclass, "if_exists" bool=false)
 AS '$libdir/timescaledb-2.26.3', 'ts_policy_process_hyper_inval_remove'
  LANGUAGE c;

-- ----------------------------
-- Function structure for remove_reorder_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."remove_reorder_policy"("hypertable" regclass, "if_exists" bool);
CREATE OR REPLACE FUNCTION "public"."remove_reorder_policy"("hypertable" regclass, "if_exists" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_policy_reorder_remove'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for remove_retention_policy
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."remove_retention_policy"("relation" regclass, "if_exists" bool);
CREATE OR REPLACE FUNCTION "public"."remove_retention_policy"("relation" regclass, "if_exists" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_policy_retention_remove'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for reorder_chunk
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."reorder_chunk"("chunk" regclass, "index" regclass, "verbose" bool);
CREATE OR REPLACE FUNCTION "public"."reorder_chunk"("chunk" regclass, "index" regclass=NULL::regclass, "verbose" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_reorder_chunk'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Procedure structure for run_job
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."run_job"("job_id" int4);
CREATE OR REPLACE PROCEDURE "public"."run_job"("job_id" int4)
 AS '$libdir/timescaledb-2.26.3', 'ts_job_run'
  LANGUAGE c;

-- ----------------------------
-- Function structure for set_adaptive_chunking
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."set_adaptive_chunking"("hypertable" regclass, "chunk_target_size" text, INOUT "chunk_sizing_func" regproc, OUT "chunk_target_size" int8);
CREATE OR REPLACE FUNCTION "public"."set_adaptive_chunking"(IN "hypertable" regclass, IN "chunk_target_size" text, INOUT "chunk_sizing_func" regproc='_timescaledb_functions.calculate_chunk_interval'::regproc, OUT "chunk_target_size" int8)
  RETURNS "pg_catalog"."record" AS '$libdir/timescaledb-2.26.3', 'ts_chunk_adaptive_set'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for set_chunk_time_interval
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."set_chunk_time_interval"("hypertable" regclass, "chunk_time_interval" anyelement, "dimension_name" name);
CREATE OR REPLACE FUNCTION "public"."set_chunk_time_interval"("hypertable" regclass, "chunk_time_interval" anyelement, "dimension_name" name=NULL::name)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_dimension_set_interval'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for set_integer_now_func
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."set_integer_now_func"("hypertable" regclass, "integer_now_func" regproc, "replace_if_exists" bool);
CREATE OR REPLACE FUNCTION "public"."set_integer_now_func"("hypertable" regclass, "integer_now_func" regproc, "replace_if_exists" bool=false)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_hypertable_set_integer_now_func'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for set_number_partitions
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."set_number_partitions"("hypertable" regclass, "number_partitions" int4, "dimension_name" name);
CREATE OR REPLACE FUNCTION "public"."set_number_partitions"("hypertable" regclass, "number_partitions" int4, "dimension_name" name=NULL::name)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_dimension_set_num_slices'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for set_partitioning_interval
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."set_partitioning_interval"("hypertable" regclass, "partition_interval" anyelement, "dimension_name" name);
CREATE OR REPLACE FUNCTION "public"."set_partitioning_interval"("hypertable" regclass, "partition_interval" anyelement, "dimension_name" name=NULL::name)
  RETURNS "pg_catalog"."void" AS '$libdir/timescaledb-2.26.3', 'ts_dimension_set_interval'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for show_chunks
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."show_chunks"("relation" regclass, "older_than" any, "newer_than" any, "created_before" any, "created_after" any);
CREATE OR REPLACE FUNCTION "public"."show_chunks"("relation" regclass, "older_than" any=NULL::unknown, "newer_than" any=NULL::unknown, "created_before" any=NULL::unknown, "created_after" any=NULL::unknown)
  RETURNS SETOF "pg_catalog"."regclass" AS '$libdir/timescaledb-2.26.3', 'ts_chunk_show_chunks'
  LANGUAGE c STABLE
  COST 1
  ROWS 1000;

-- ----------------------------
-- Function structure for show_tablespaces
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."show_tablespaces"("hypertable" regclass);
CREATE OR REPLACE FUNCTION "public"."show_tablespaces"("hypertable" regclass)
  RETURNS SETOF "pg_catalog"."name" AS '$libdir/timescaledb-2.26.3', 'ts_tablespace_show'
  LANGUAGE c VOLATILE STRICT
  COST 1
  ROWS 1000;

-- ----------------------------
-- Procedure structure for split_chunk
-- ----------------------------
DROP PROCEDURE IF EXISTS "public"."split_chunk"("chunk" regclass, "split_at" any);
CREATE OR REPLACE PROCEDURE "public"."split_chunk"("chunk" regclass, "split_at" any=NULL::unknown)
 AS '$libdir/timescaledb-2.26.3', 'ts_split_chunk'
  LANGUAGE c;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" uuid);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" uuid)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" date, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" date, "offset" interval)
  RETURNS "pg_catalog"."date" AS '$libdir/timescaledb-2.26.3', 'ts_date_offset_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "offset" interval)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_offset_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamp, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamp, "offset" interval)
  RETURNS "pg_catalog"."timestamp" AS '$libdir/timescaledb-2.26.3', 'ts_timestamp_offset_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" uuid, "origin" timestamptz);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" uuid, "origin" timestamptz)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" date, "origin" date);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" date, "origin" date)
  RETURNS "pg_catalog"."date" AS '$libdir/timescaledb-2.26.3', 'ts_date_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "origin" timestamptz);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "origin" timestamptz)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamp, "origin" timestamp);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamp, "origin" timestamp)
  RETURNS "pg_catalog"."timestamp" AS '$libdir/timescaledb-2.26.3', 'ts_timestamp_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" date);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" date)
  RETURNS "pg_catalog"."date" AS '$libdir/timescaledb-2.26.3', 'ts_date_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamptz);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamptz)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamp);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamp)
  RETURNS "pg_catalog"."timestamp" AS '$libdir/timescaledb-2.26.3', 'ts_timestamp_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int8, "ts" int8, "offset" int8);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int8, "ts" int8, "offset" int8)
  RETURNS "pg_catalog"."int8" AS '$libdir/timescaledb-2.26.3', 'ts_int64_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int4, "ts" int4, "offset" int4);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int4, "ts" int4, "offset" int4)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_int32_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int2, "ts" int2, "offset" int2);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int2, "ts" int2, "offset" int2)
  RETURNS "pg_catalog"."int2" AS '$libdir/timescaledb-2.26.3', 'ts_int16_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int8, "ts" int8);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int8, "ts" int8)
  RETURNS "pg_catalog"."int8" AS '$libdir/timescaledb-2.26.3', 'ts_int64_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int4, "ts" int4);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int4, "ts" int4)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_int32_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" int2, "ts" int2);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" int2, "ts" int2)
  RETURNS "pg_catalog"."int2" AS '$libdir/timescaledb-2.26.3', 'ts_int16_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" uuid, "timezone" text, "origin" timestamptz, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" uuid, "timezone" text, "origin" timestamptz=NULL::timestamp with time zone, "offset" interval=NULL::interval)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_timezone_bucket'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "timezone" text, "origin" timestamptz, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" timestamptz, "timezone" text, "origin" timestamptz=NULL::timestamp with time zone, "offset" interval=NULL::interval)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_timezone_bucket'
  LANGUAGE c IMMUTABLE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket"("bucket_width" interval, "ts" uuid, "offset" interval);
CREATE OR REPLACE FUNCTION "public"."time_bucket"("bucket_width" interval, "ts" uuid, "offset" interval)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_offset_bucket'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" int4, "ts" int4, "start" int4, "finish" int4);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" int4, "ts" int4, "start" int4=NULL::integer, "finish" int4=NULL::integer)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_int32_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamp, "start" timestamp, "finish" timestamp);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamp, "start" timestamp=NULL::timestamp without time zone, "finish" timestamp=NULL::timestamp without time zone)
  RETURNS "pg_catalog"."timestamp" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_timestamp_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" interval, "ts" date, "start" date, "finish" date);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" interval, "ts" date, "start" date=NULL::date, "finish" date=NULL::date)
  RETURNS "pg_catalog"."date" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_date_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" int2, "ts" int2, "start" int2, "finish" int2);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" int2, "ts" int2, "start" int2=NULL::smallint, "finish" int2=NULL::smallint)
  RETURNS "pg_catalog"."int2" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_int16_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamptz, "timezone" text, "start" timestamptz, "finish" timestamptz);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamptz, "timezone" text, "start" timestamptz=NULL::timestamp with time zone, "finish" timestamptz=NULL::timestamp with time zone)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_timestamptz_timezone_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamptz, "start" timestamptz, "finish" timestamptz);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" interval, "ts" timestamptz, "start" timestamptz=NULL::timestamp with time zone, "finish" timestamptz=NULL::timestamp with time zone)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_timestamptz_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for time_bucket_gapfill
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."time_bucket_gapfill"("bucket_width" int8, "ts" int8, "start" int8, "finish" int8);
CREATE OR REPLACE FUNCTION "public"."time_bucket_gapfill"("bucket_width" int8, "ts" int8, "start" int8=NULL::bigint, "finish" int8=NULL::bigint)
  RETURNS "pg_catalog"."int8" AS '$libdir/timescaledb-2.26.3', 'ts_gapfill_int64_bucket'
  LANGUAGE c VOLATILE
  COST 1;

-- ----------------------------
-- Function structure for timescaledb_post_restore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."timescaledb_post_restore"();
CREATE OR REPLACE FUNCTION "public"."timescaledb_post_restore"()
  RETURNS "pg_catalog"."bool" AS $BODY$
DECLARE
    db text;
    catalog_version text;
BEGIN
    SELECT m.value INTO catalog_version FROM pg_extension x
    JOIN _timescaledb_catalog.metadata m ON m.key='timescaledb_version'
    WHERE x.extname='timescaledb' AND x.extversion <> m.value;

    -- check that a loaded dump is compatible with the currently running code
    IF FOUND THEN
        RAISE EXCEPTION 'catalog version mismatch, expected "%" seen "%"', '2.26.3', catalog_version;
    END IF;

    SELECT current_database() INTO db;
    EXECUTE format($$ALTER DATABASE %I RESET timescaledb.restoring $$, db);
    -- we cannot use reset here because the reset_val might not be off
    SET timescaledb.restoring TO off;
    PERFORM _timescaledb_functions.restart_background_workers();

    RETURN true;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for timescaledb_pre_restore
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."timescaledb_pre_restore"();
CREATE OR REPLACE FUNCTION "public"."timescaledb_pre_restore"()
  RETURNS "pg_catalog"."bool" AS $BODY$
DECLARE
    db text;
BEGIN
    SELECT current_database() INTO db;
    EXECUTE format($$ALTER DATABASE %I SET timescaledb.restoring ='on'$$, db);
    SET SESSION timescaledb.restoring = 'on';
    PERFORM _timescaledb_functions.stop_background_workers();
    RETURN true;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  SET "search_path"="pg_catalog, pg_temp";

-- ----------------------------
-- Function structure for to_uuidv7
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."to_uuidv7"("ts" timestamptz);
CREATE OR REPLACE FUNCTION "public"."to_uuidv7"("ts" timestamptz)
  RETURNS "pg_catalog"."uuid" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_v7_from_timestamptz'
  LANGUAGE c VOLATILE STRICT
  COST 1;

-- ----------------------------
-- Function structure for to_uuidv7_boundary
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."to_uuidv7_boundary"("ts" timestamptz);
CREATE OR REPLACE FUNCTION "public"."to_uuidv7_boundary"("ts" timestamptz)
  RETURNS "pg_catalog"."uuid" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_v7_from_timestamptz_boundary'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_timestamp
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_timestamp"("uuid" uuid);
CREATE OR REPLACE FUNCTION "public"."uuid_timestamp"("uuid" uuid)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_from_uuid_v7'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_timestamp_micros
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_timestamp_micros"("uuid" uuid);
CREATE OR REPLACE FUNCTION "public"."uuid_timestamp_micros"("uuid" uuid)
  RETURNS "pg_catalog"."timestamptz" AS '$libdir/timescaledb-2.26.3', 'ts_timestamptz_from_uuid_v7_with_microseconds'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Function structure for uuid_version
-- ----------------------------
DROP FUNCTION IF EXISTS "public"."uuid_version"("uuid" uuid);
CREATE OR REPLACE FUNCTION "public"."uuid_version"("uuid" uuid)
  RETURNS "pg_catalog"."int4" AS '$libdir/timescaledb-2.26.3', 'ts_uuid_version'
  LANGUAGE c IMMUTABLE STRICT
  COST 1;

-- ----------------------------
-- Indexes structure for table ai_agent_audit_log
-- ----------------------------
CREATE INDEX "idx_ai_agent_audit_log_operator_id_time" ON "public"."ai_agent_audit_log" USING btree (
  "operator_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_agent_audit_log_session_created_at" ON "public"."ai_agent_audit_log" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_ai_agent_audit_log_session_time" ON "public"."ai_agent_audit_log" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_agent_audit_log
-- ----------------------------
ALTER TABLE "public"."ai_agent_audit_log" ADD CONSTRAINT "ai_agent_audit_log_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ai_agent_chat_message
-- ----------------------------
CREATE INDEX "idx_ai_agent_chat_message_session_created_at" ON "public"."ai_agent_chat_message" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ai_agent_chat_message_session_time" ON "public"."ai_agent_chat_message" USING btree (
  "session_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "created_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_agent_chat_message
-- ----------------------------
ALTER TABLE "public"."ai_agent_chat_message" ADD CONSTRAINT "ai_agent_chat_message_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table ai_agent_chat_session
-- ----------------------------
CREATE INDEX "idx_ai_agent_chat_session_updated_at" ON "public"."ai_agent_chat_session" USING btree (
  "updated_at" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table ai_agent_chat_session
-- ----------------------------
ALTER TABLE "public"."ai_agent_chat_session" ADD CONSTRAINT "ai_agent_chat_session_pkey" PRIMARY KEY ("session_id");

-- ----------------------------
-- Primary Key structure for table oauth2_authorization
-- ----------------------------
ALTER TABLE "public"."oauth2_authorization" ADD CONSTRAINT "oauth2_authorization_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table oauth2_authorization_consent
-- ----------------------------
ALTER TABLE "public"."oauth2_authorization_consent" ADD CONSTRAINT "oauth2_authorization_consent_pkey" PRIMARY KEY ("registered_client_id", "principal_name");

-- ----------------------------
-- Primary Key structure for table oauth2_registered_client
-- ----------------------------
ALTER TABLE "public"."oauth2_registered_client" ADD CONSTRAINT "oauth2_registered_client_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table oauth_client_details
-- ----------------------------
ALTER TABLE "public"."oauth_client_details" ADD CONSTRAINT "oauth_client_details_pkey" PRIMARY KEY ("client_id");

-- ----------------------------
-- Indexes structure for table p_ucenter_acc
-- ----------------------------
CREATE UNIQUE INDEX "unq_accname" ON "public"."p_ucenter_acc" USING btree (
  "acc_name" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "platform" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table p_ucenter_acc
-- ----------------------------
ALTER TABLE "public"."p_ucenter_acc" ADD CONSTRAINT "p_ucenter_acc_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table p_ucenter_permission
-- ----------------------------
CREATE UNIQUE INDEX "unq_code" ON "public"."p_ucenter_permission" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table p_ucenter_permission
-- ----------------------------
ALTER TABLE "public"."p_ucenter_permission" ADD CONSTRAINT "p_ucenter_permission_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table p_ucenter_platform
-- ----------------------------
CREATE UNIQUE INDEX "unq_plat_code" ON "public"."p_ucenter_platform" USING btree (
  "code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table p_ucenter_platform
-- ----------------------------
ALTER TABLE "public"."p_ucenter_platform" ADD CONSTRAINT "p_ucenter_platform_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table p_ucenter_tenant
-- ----------------------------
ALTER TABLE "public"."p_ucenter_tenant" ADD CONSTRAINT "p_ucenter_tenant_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_bom_d
-- ----------------------------
ALTER TABLE "public"."r_bom_d" ADD CONSTRAINT "r_bom_d_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_gw_binding
-- ----------------------------
ALTER TABLE "public"."r_gw_binding" ADD CONSTRAINT "r_gw_binding_pkey" PRIMARY KEY ("equip_id");

-- ----------------------------
-- Primary Key structure for table r_mo_d
-- ----------------------------
ALTER TABLE "public"."r_mo_d" ADD CONSTRAINT "r_mo_d_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_mps_d
-- ----------------------------
ALTER TABLE "public"."r_mps_d" ADD CONSTRAINT "r_mps_d_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_mps_tf
-- ----------------------------
ALTER TABLE "public"."r_mps_tf" ADD CONSTRAINT "r_mps_tf_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_sync_resource
-- ----------------------------
ALTER TABLE "public"."r_sync_resource" ADD CONSTRAINT "r_sync_resource_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_workshop_config_meta2d
-- ----------------------------
ALTER TABLE "public"."r_workshop_config_meta2d" ADD CONSTRAINT "r_workshop_config_meta2d_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table r_workshop_config_scada
-- ----------------------------
ALTER TABLE "public"."r_workshop_config_scada" ADD CONSTRAINT "r_workshop_config_scada_pkey" PRIMARY KEY ("workshop_id");

-- ----------------------------
-- Primary Key structure for table t_bom
-- ----------------------------
ALTER TABLE "public"."t_bom" ADD CONSTRAINT "t_bom_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_bom_c
-- ----------------------------
ALTER TABLE "public"."t_bom_c" ADD CONSTRAINT "t_bom_c_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_connect
-- ----------------------------
ALTER TABLE "public"."t_connect" ADD CONSTRAINT "t_connect_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_device
-- ----------------------------
ALTER TABLE "public"."t_device" ADD CONSTRAINT "t_device_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_dg
-- ----------------------------
ALTER TABLE "public"."t_dg" ADD CONSTRAINT "t_dg_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_equip
-- ----------------------------
ALTER TABLE "public"."t_equip" ADD CONSTRAINT "unq_equip_code" UNIQUE ("self_code");

-- ----------------------------
-- Primary Key structure for table t_equip
-- ----------------------------
ALTER TABLE "public"."t_equip" ADD CONSTRAINT "t_equip_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_equip_collect
-- ----------------------------
CREATE INDEX "idx_tenant_time" ON "public"."t_equip_collect" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_equip_collect_new_sn_time_idx" ON "public"."t_equip_collect" USING btree (
  "sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_equip_collect_new_time_idx" ON "public"."t_equip_collect" USING btree (
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);

-- ----------------------------
-- Primary Key structure for table t_equip_health_indicator
-- ----------------------------
ALTER TABLE "public"."t_equip_health_indicator" ADD CONSTRAINT "t_equip_health_indicator_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_equip_health_rule_template
-- ----------------------------
ALTER TABLE "public"."t_equip_health_rule_template" ADD CONSTRAINT "t_equip_health_rule_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_equip_realtime
-- ----------------------------
CREATE INDEX "idx_equip_rt_tenant_alarm" ON "public"."t_equip_realtime" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "alarm_change_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
) WHERE alarm_state = 1;
CREATE INDEX "idx_equip_rt_tenant_gw" ON "public"."t_equip_realtime" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "gw_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_equip_rt_tenant_online" ON "public"."t_equip_realtime" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "online_change_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
) WHERE online_state = 1;
CREATE INDEX "idx_equip_rt_tenant_run" ON "public"."t_equip_realtime" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "run_change_time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
) WHERE run_state = 1;
CREATE UNIQUE INDEX "uk_equip_rt_tenant_sn" ON "public"."t_equip_realtime" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "self_code" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table t_equip_realtime
-- ----------------------------
ALTER TABLE "public"."t_equip_realtime" ADD CONSTRAINT "t_equip_realtime_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_equip_record_alarm
-- ----------------------------
ALTER TABLE "public"."t_equip_record_alarm" ADD CONSTRAINT "uk_equip_record_alarm_event_id " UNIQUE ("event_id");

-- ----------------------------
-- Primary Key structure for table t_equip_record_alarm
-- ----------------------------
ALTER TABLE "public"."t_equip_record_alarm" ADD CONSTRAINT "t_equip_record_alarm_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table t_equip_record_online
-- ----------------------------
ALTER TABLE "public"."t_equip_record_online" ADD CONSTRAINT "uk_equip_record_online_event_id " UNIQUE ("event_id");

-- ----------------------------
-- Primary Key structure for table t_equip_record_online
-- ----------------------------
ALTER TABLE "public"."t_equip_record_online" ADD CONSTRAINT "t_equip_record_online_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_equip_record_run
-- ----------------------------
CREATE UNIQUE INDEX "uk_equip_record_run_event_id " ON "public"."t_equip_record_run" USING btree (
  "event_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table t_equip_record_run
-- ----------------------------
ALTER TABLE "public"."t_equip_record_run" ADD CONSTRAINT "t_equip_record_run_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_equip_state_snapshot
-- ----------------------------
CREATE INDEX "idx_snapshot_latest" ON "public"."t_equip_state_snapshot" USING btree (
  "sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_snapshot_sn_time" ON "public"."t_equip_state_snapshot" USING btree (
  "sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_snapshot_tenant_time" ON "public"."t_equip_state_snapshot" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_equip_state_snapshot_sn_time_idx" ON "public"."t_equip_state_snapshot" USING btree (
  "sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_equip_state_snapshot_time_idx" ON "public"."t_equip_state_snapshot" USING btree (
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE UNIQUE INDEX "uniq_sn_time" ON "public"."t_equip_state_snapshot" USING btree (
  "sn" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table t_gateway
-- ----------------------------
ALTER TABLE "public"."t_gateway" ADD CONSTRAINT "t_gateway_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_item
-- ----------------------------
ALTER TABLE "public"."t_inspect_item" ADD CONSTRAINT "t_inspect_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_person
-- ----------------------------
ALTER TABLE "public"."t_inspect_person" ADD CONSTRAINT "t_inspect_person_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_plan
-- ----------------------------
ALTER TABLE "public"."t_inspect_plan" ADD CONSTRAINT "t_inspect_plan_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_project
-- ----------------------------
ALTER TABLE "public"."t_inspect_project" ADD CONSTRAINT "t_inspect_project_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_record
-- ----------------------------
ALTER TABLE "public"."t_inspect_record" ADD CONSTRAINT "t_inspect_record_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_record_item
-- ----------------------------
ALTER TABLE "public"."t_inspect_record_item" ADD CONSTRAINT "t_inspect_record_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_task
-- ----------------------------
ALTER TABLE "public"."t_inspect_task" ADD CONSTRAINT "t_inspect_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_template
-- ----------------------------
ALTER TABLE "public"."t_inspect_template" ADD CONSTRAINT "t_inspect_template_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_template_item
-- ----------------------------
ALTER TABLE "public"."t_inspect_template_item" ADD CONSTRAINT "t_inspect_template_item_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_inspect_template_item_rule
-- ----------------------------
ALTER TABLE "public"."t_inspect_template_item_rule" ADD CONSTRAINT "t_inspect_template_item_rule_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_line
-- ----------------------------
ALTER TABLE "public"."t_line" ADD CONSTRAINT "t_line_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_mat
-- ----------------------------
ALTER TABLE "public"."t_mat" ADD CONSTRAINT "t_mat_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_mc
-- ----------------------------
ALTER TABLE "public"."t_mc" ADD CONSTRAINT "t_mc_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_message
-- ----------------------------
ALTER TABLE "public"."t_message" ADD CONSTRAINT "t_message_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_mo
-- ----------------------------
ALTER TABLE "public"."t_mo" ADD CONSTRAINT "t_mo_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_mps
-- ----------------------------
ALTER TABLE "public"."t_mps" ADD CONSTRAINT "t_mps_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_mps
-- ----------------------------
CREATE INDEX IF NOT EXISTS "idx_mps_status_priority" ON "public"."t_mps" USING btree (
  "status" ASC NULLS LAST,
  "priority" ASC NULLS LAST,
  "id" DESC NULLS LAST
);
CREATE INDEX IF NOT EXISTS "idx_mps_mo_code" ON "public"."t_mps" USING btree (
  "mo_code" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table t_notify
-- ----------------------------
ALTER TABLE "public"."t_notify" ADD CONSTRAINT "t_notify_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_product
-- ----------------------------
ALTER TABLE "public"."t_product" ADD CONSTRAINT "t_product_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_qa
-- ----------------------------
ALTER TABLE "public"."t_qa" ADD CONSTRAINT "t_qa_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_sync
-- ----------------------------
ALTER TABLE "public"."t_sync" ADD CONSTRAINT "t_sync_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_task
-- ----------------------------
ALTER TABLE "public"."t_task" ADD CONSTRAINT "t_task_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_tf
-- ----------------------------
ALTER TABLE "public"."t_tf" ADD CONSTRAINT "t_tf_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_tf_edge
-- ----------------------------
ALTER TABLE "public"."t_tf_edge" ADD CONSTRAINT "t_tf_edge_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_ucenter_depart
-- ----------------------------
ALTER TABLE "public"."t_ucenter_depart" ADD CONSTRAINT "t_ucenter_depart_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_ucenter_role
-- ----------------------------
ALTER TABLE "public"."t_ucenter_role" ADD CONSTRAINT "t_ucenter_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table t_workshop
-- ----------------------------
ALTER TABLE "public"."t_workshop" ADD CONSTRAINT "t_workshop_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table t_workshop_collect
-- ----------------------------
CREATE INDEX "idx_ws_id_time" ON "public"."t_workshop_collect" USING btree (
  "workshop_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "idx_ws_tenant" ON "public"."t_workshop_collect" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "idx_ws_tenant_time" ON "public"."t_workshop_collect" USING btree (
  "tenant_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_workshop_collect_time_idx" ON "public"."t_workshop_collect" USING btree (
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
CREATE INDEX "t_workshop_collect_workshop_id_time_idx" ON "public"."t_workshop_collect" USING btree (
  "workshop_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "time" "pg_catalog"."timestamp_ops" DESC NULLS FIRST
);
