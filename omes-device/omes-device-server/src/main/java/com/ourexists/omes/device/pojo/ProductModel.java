package com.ourexists.omes.device.pojo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.omes.device.model.EquipConfigDetail;
import com.ourexists.omes.device.model.ProductModelDto;
import com.ourexists.omes.device.typehandler.PgJsonbEquipConfigTypeHandler;
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
@TableName(value = "t_product_model", autoResultMap = true)
public class ProductModel extends EraEntity {

    private String productCode;

    private String name;

    private String code;

    @TableField(value = "attr_config", typeHandler = PgJsonbEquipConfigTypeHandler.class, jdbcType = JdbcType.OTHER,
            updateStrategy = FieldStrategy.NOT_NULL)
    private EquipConfigDetail attrConfig;

    public static ProductModelDto covert(ProductModel source) {
        if (source == null) {
            return null;
        }
        ProductModelDto target = new ProductModelDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<ProductModelDto> covert(List<ProductModel> sources) {
        List<ProductModelDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (ProductModel source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static ProductModel wrap(ProductModelDto source) {
        if (source == null) {
            return null;
        }
        ProductModel target = new ProductModel();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
