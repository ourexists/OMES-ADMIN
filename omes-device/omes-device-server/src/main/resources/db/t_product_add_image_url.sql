-- 产品表增加产品图片地址字段（表已存在时执行）
ALTER TABLE t_product ADD COLUMN image_url VARCHAR(512) DEFAULT NULL COMMENT '产品图片地址' AFTER code;
