package com.ourexists.mesedge.message.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.core.NotifyMsg;
import com.ourexists.mesedge.message.model.NotifyDto;
import com.ourexists.mesedge.message.model.NotifyVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_notify")
public class Notify extends EraEntity {

    private String title;

    private String context;

    private Integer type;

    private Integer status;

    private String platform;

    private Integer step = 0;

    public static NotifyVo covert(Notify source) {
        if (source == null) {
            return null;
        }
        NotifyVo target = new NotifyVo();
        BeanUtils.copyProperties(source, target);
        if (StringUtils.hasText(source.getPlatform())) {
            target.setPlatforms(Arrays.stream(source.getPlatform().split("\\|")).toList());
        }
        return target;
    }

    public static List<NotifyMsg> covertMsg(Notify source) {
        if (source == null) {
            return null;
        }
        List<NotifyMsg> msgs = new ArrayList<>();
        if (StringUtils.hasText(source.getPlatform())) {
            List<String> platforms = Arrays.stream(source.getPlatform().split("\\|")).toList();
            for (String platform : platforms) {
                NotifyMsg target = new NotifyMsg();
                BeanUtils.copyProperties(source, target, "platform");
                target.setPlatform(platform);
                msgs.add(target);
            }
        }
        return msgs;
    }

    public static List<NotifyVo> covert(List<Notify> sources) {
        List<NotifyVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Notify source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends NotifyDto> Notify wrap(T source) {
        Notify target = new Notify();
        BeanUtils.copyProperties(source, target);
        if (!CollectionUtils.isEmpty(source.getPlatforms())) {
            target.setPlatform(CollectionUtil.join(source.getPlatforms(), "|"));
        }
        return target;
    }

    public static <T extends NotifyDto> List<Notify> wrap(List<T> sources) {
        List<Notify> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (NotifyDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }

}
