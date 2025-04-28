/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.mat.enums;

import com.ourexists.era.framework.webmvc.I18nUtil;
import lombok.Getter;

@Getter
public enum IngredientAttributeEnum {

    UNKNOWN(-1, "Unknown"),
    main(0, "${product.attribute.main}"),
    extra(1, "${product.attribute.extra}"),
    retur(2, "${product.attribute.retur}"),
    oil(3, "${product.attribute.oil}"),
    water(4, "${product.attribute.water}");

    private final Integer code;

    private final String name;

    IngredientAttributeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static IngredientAttributeEnum valueOf(Integer code) {
        for (IngredientAttributeEnum ingredientAttributeEnum : IngredientAttributeEnum.values()) {
            if (ingredientAttributeEnum.getCode().equals(code)) {
                return ingredientAttributeEnum;
            }
        }
        return IngredientAttributeEnum.UNKNOWN;
    }

    public String getName() {
        return I18nUtil.i18nParser(name);
    }
}
