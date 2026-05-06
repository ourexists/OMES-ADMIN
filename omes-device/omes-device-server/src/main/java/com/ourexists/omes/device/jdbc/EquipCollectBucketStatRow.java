package com.ourexists.omes.device.jdbc;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * PostgreSQL 聚合查询扁平行：按 bucket_start + 属性名分组统计。
 */
@Getter
@Setter
public class EquipCollectBucketStatRow {

    private Date bucketStart;

    private String attrKey;

    private Double avgVal;

    private Double minVal;

    private Double maxVal;

    private Long cnt;
}
