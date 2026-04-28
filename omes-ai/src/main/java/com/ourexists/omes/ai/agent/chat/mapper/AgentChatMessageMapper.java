package com.ourexists.omes.ai.agent.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ourexists.omes.ai.agent.chat.entity.AgentChatMessageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentChatMessageMapper extends BaseMapper<AgentChatMessageEntity> {
}
