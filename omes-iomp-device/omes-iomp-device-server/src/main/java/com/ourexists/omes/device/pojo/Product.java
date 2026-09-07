/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.ProductAttrConfig;
import com.ourexists.omes.device.model.ProductDto;
import com.ourexists.omes.device.typehandler.PgJsonbProductAttrConfigTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName(value = "t_product", autoResultMap = true)
public class Product extends EraEntity {

    private String name;

    private String code;

    /** 产品图片地址 */
    private String imageUrl;

    /** 产品属性模板（attrs/alarms/controls），型号按名称填写采集映射 */
    @TableField(value = "attr_config", typeHandler = PgJsonbProductAttrConfigTypeHandler.class, jdbcType = JdbcType.OTHER,
            updateStrategy = FieldStrategy.NOT_NULL)
    private ProductAttrConfig attrConfig;

    public static ProductDto covert(Product source) {
        if (source == null) {
            return null;
        }
        ProductDto target = new ProductDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<ProductDto> covert(List<Product> sources) {
        List<ProductDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Product source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static Product wrap(ProductDto source) {
        if (source == null) {
            return null;
        }
        Product target = new Product();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
