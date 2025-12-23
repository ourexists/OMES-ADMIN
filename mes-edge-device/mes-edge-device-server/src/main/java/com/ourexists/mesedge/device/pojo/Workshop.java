package com.ourexists.mesedge.device.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.MainEntity;
import com.ourexists.mesedge.device.model.WorkshopDto;
import com.ourexists.mesedge.device.model.WorkshopTreeNode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_workshop")
public class Workshop extends MainEntity {

    private String name;

    private String selfCode;

    private String code;

    private String pcode;

    public static WorkshopTreeNode covert(Workshop source) {
        if (source == null) {
            return null;
        }
        WorkshopTreeNode target = new WorkshopTreeNode();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<WorkshopTreeNode> covert(List<Workshop> sources) {
        List<WorkshopTreeNode> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Workshop source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends WorkshopDto> Workshop wrap(T source) {
        Workshop target = new Workshop();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends WorkshopDto> List<Workshop> wrap(List<T> sources) {
        List<Workshop> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (T source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
