package com.ourexists.mesedge.message.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.era.framework.orm.mybatisplus.EraEntity;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("t_message")
public class Message extends EraEntity {

    private String title;

    private String context;

    private Integer type;

    private String platform;

    protected String notifyId;

    protected String source;

    private String sourceId;

    public static MessageVo covert(Message source) {
        if (source == null) {
            return null;
        }
        MessageVo target = new MessageVo();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MessageVo> covert(List<Message> sources) {
        List<MessageVo> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (Message source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends MessageDto> Message wrap(T source) {
        Message target = new Message();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends MessageDto> List<Message> wrap(List<T> sources) {
        List<Message> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MessageDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
