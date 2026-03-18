/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.ProductDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_product")
public class Product extends EraEntity {

    private String name;

    private String code;

    /** 产品图片地址 */
    private String imageUrl;

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
