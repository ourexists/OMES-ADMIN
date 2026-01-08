package com.ourexists.mesedge.message.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ourexists.era.framework.core.utils.CollectionUtil;
import com.ourexists.mesedge.message.model.MessageReadDto;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@TableName("r_message_read")
public class MessageRead {

    private String messageId;

    private String accId;

    private Date time;

    private Boolean isRead;

    public static MessageReadDto covert(MessageRead source) {
        if (source == null) {
            return null;
        }
        MessageReadDto target = new MessageReadDto();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static List<MessageReadDto> covert(List<MessageRead> sources) {
        List<MessageReadDto> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MessageRead source : sources) {
                targets.add(covert(source));
            }
        }
        return targets;
    }

    public static <T extends MessageReadDto> MessageRead wrap(T source) {
        MessageRead target = new MessageRead();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T extends MessageReadDto> List<MessageRead> wrap(List<T> sources) {
        List<MessageRead> targets = new ArrayList<>();
        if (CollectionUtil.isNotBlank(sources)) {
            for (MessageReadDto source : sources) {
                targets.add(wrap(source));
            }
        }
        return targets;
    }
}
