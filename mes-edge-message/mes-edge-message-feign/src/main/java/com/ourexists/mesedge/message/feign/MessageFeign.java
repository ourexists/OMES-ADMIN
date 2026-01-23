/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.mesedge.message.feign;

import com.ourexists.era.framework.core.model.dto.IdsDto;
import com.ourexists.era.framework.core.model.vo.JsonResponseEntity;
import com.ourexists.mesedge.message.model.MessageDto;
import com.ourexists.mesedge.message.model.MessageVo;
import com.ourexists.mesedge.message.model.query.MessagePageQuery;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@Tag(name = "生产订单")
//@RestController
//@RequestMapping("/mo")
public interface MessageFeign {

    //    @Operation(summary = "分页", description = "")
//    @PostMapping("selectByPage")
    JsonResponseEntity<List<MessageVo>> selectByPage(@RequestBody MessagePageQuery dto);

    //    @Operation(summary = "新增或修改根据id", description = "")
//    @PostMapping("addOrUpdate")
    JsonResponseEntity<MessageVo> produce(@Validated @RequestBody MessageDto dto);

    //    @Operation(summary = "删除", description = "")
//    @PostMapping("delete")
    JsonResponseEntity<Boolean> delete(@Validated @RequestBody IdsDto idsDto);

    JsonResponseEntity<Boolean> read(@RequestParam String messageId);

    JsonResponseEntity<MessageVo> selectById(@RequestParam String id, @RequestParam String accId);

    JsonResponseEntity<Long> countReadStatus(@RequestParam String userId,
                                         @RequestParam String platform,
                                         @RequestParam Integer readStatus);
}
