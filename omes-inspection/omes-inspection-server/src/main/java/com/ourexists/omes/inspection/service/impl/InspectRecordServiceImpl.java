/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.orm.mybatisplus.service.AbstractMyBatisPlusService;
import com.ourexists.omes.inspection.enums.InspectResultEnum;
import com.ourexists.omes.inspection.mapper.InspectRecordMapper;
import com.ourexists.omes.inspection.model.InspectRecordPageQuery;
import com.ourexists.omes.inspection.model.InspectRecordSaveRequest;
import com.ourexists.omes.inspection.pojo.InspectItem;
import com.ourexists.omes.inspection.pojo.InspectRecord;
import com.ourexists.omes.inspection.pojo.InspectRecordItem;
import com.ourexists.omes.inspection.pojo.InspectTemplateItem;
import com.ourexists.omes.inspection.service.InspectItemService;
import com.ourexists.omes.inspection.service.InspectRecordItemService;
import com.ourexists.omes.inspection.service.InspectRecordService;
import com.ourexists.omes.inspection.service.InspectTemplateItemService;
import com.ourexists.omes.inspection.service.InspectTaskService;
import com.ourexists.omes.inspection.pojo.InspectTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class InspectRecordServiceImpl extends AbstractMyBatisPlusService<InspectRecordMapper, InspectRecord> implements InspectRecordService {

    @Autowired
    private InspectTaskService inspectTaskService;
    @Autowired
    private InspectTemplateItemService inspectTemplateItemService;
    @Autowired
    private InspectItemService inspectItemService;
    @Autowired
    private InspectRecordItemService inspectRecordItemService;

    @Override
    public void saveRecord(InspectRecordSaveRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getResults())) {
            return;
        }
        InspectTask task = inspectTaskService.getById(request.getTaskId());
        if (task == null) {
            throw new IllegalArgumentException("巡检任务不存在");
        }
        Date now = new Date();
        // 先求权重总值，用于未配置百分比权重时的回退计算
        int weightTotal = 0;
        for (InspectRecordSaveRequest.InspectRecordItemResult res : request.getResults()) {
            InspectTemplateItem ti = inspectTemplateItemService.getById(res.getItemId());
            if (ti != null) weightTotal += (ti.getWeight() != null ? ti.getWeight() : 0);
        }
        // 最终得分优先使用百分比权重：单项贡献 = itemScore * weightRate；否则回退为 权重*itemScore/权重总值
        double totalScoreSum = 0;
        List<InspectRecordItem> detailList = new ArrayList<>();
        for (InspectRecordSaveRequest.InspectRecordItemResult res : request.getResults()) {
            InspectTemplateItem templateItem = inspectTemplateItemService.getById(res.getItemId());
            if (templateItem == null) {
                continue;
            }
            InspectItem inspectItem = null;
            String itemName = "";
            if (StringUtils.hasText(templateItem.getReferenceItemId())) {
                inspectItem = inspectItemService.getById(templateItem.getReferenceItemId());
                if (inspectItem != null) {
                    itemName = inspectItem.getItemName() != null ? inspectItem.getItemName() : "";
                }
            }
            String value = res.getValue() != null ? res.getValue().trim() : "";
            Integer resultCode = mapValueToResult(value);
            int itemScore = computeItemScore(templateItem, inspectItem, value, resultCode);
            int weight = templateItem.getWeight() != null ? templateItem.getWeight() : 0;
            BigDecimal weightRate = templateItem.getWeightRate();
            double contribution;
            int itemFinalScore;
            if (weightRate != null) {
                double rate = weightRate.doubleValue();
                contribution = itemScore * rate;
                itemFinalScore = (int) Math.round(contribution);
            } else {
                contribution = weightTotal > 0 ? (weight * itemScore / (double) weightTotal) : 0;
                itemFinalScore = (int) Math.round(weight * itemScore / 100.0);
            }
            totalScoreSum += contribution;
            // 备注不默认带入填写内容，由用户单独录入
            InspectRecordItem detail = new InspectRecordItem()
                    .setItemId(res.getItemId())
                    .setItemName(itemName)
                    .setContent(value)
                    .setResult(resultCode)
                    .setRuleScore(itemScore)
                    .setScore(itemFinalScore)
                    .setRemark(null);
            detailList.add(detail);
        }
        if (detailList.isEmpty()) {
            return;
        }
        int totalScore = (int) Math.round(totalScoreSum);
        InspectRecord main = new InspectRecord()
                .setTaskId(request.getTaskId())
                .setEquipId(request.getEquipId())
                .setEquipName(null)
                .setScore(totalScore)
                .setRecordTime(now);
        save(main);
        String recordId = main.getId();
        for (InspectRecordItem d : detailList) {
            d.setRecordId(recordId);
        }
        inspectRecordItemService.saveBatch(detailList);
    }

    /** 将前端填写的值映射为 0正常 1异常 */
    private static Integer mapValueToResult(String value) {
        if (value == null) return InspectResultEnum.NORMAL.getCode();
        switch (value) {
            case "异常":
            case "否":
                return InspectResultEnum.ABNORMAL.getCode();
            case "正常":
            case "是":
            default:
                return InspectResultEnum.NORMAL.getCode();
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 根据模板项权重与配置规则计算该项得分。
     * 规则配置 JSON 与前端一致：ruleType 1=是否(boolValue,weight) 2=数值(minValue,maxValue,weight) 3=选择(optionValue,weight)。
     * 未配置规则或未匹配时按原逻辑：正常=权重，异常=0。
     */
    private int computeItemScore(InspectTemplateItem templateItem, InspectItem inspectItem,
                                 String content, Integer resultCode) {
        int defaultWeight = templateItem.getWeight() != null ? templateItem.getWeight() : 0;
        String ruleConfig = templateItem.getRuleConfig();
        if (!StringUtils.hasText(ruleConfig)) {
            return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
        }
        int itemType = (inspectItem != null && inspectItem.getItemType() != null) ? inspectItem.getItemType() : 2;
        try {
            List<Map<String, Object>> rules = OBJECT_MAPPER.readValue(ruleConfig, new TypeReference<List<Map<String, Object>>>() {});
            if (rules == null || rules.isEmpty()) {
                return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
            }
            // 类型3 是否：content 是/否 -> boolValue 1/0
            if (itemType == 3) {
                int boolVal = ("是".equals(content) || "正常".equals(content)) ? 1 : (("否".equals(content) || "异常".equals(content)) ? 0 : -1);
                if (boolVal >= 0) {
                    for (Map<String, Object> r : rules) {
                        Object bv = r.get("boolValue");
                        if (bv != null && Integer.parseInt(bv.toString()) == boolVal) {
                            Object w = r.get("weight");
                            return w != null ? Integer.parseInt(w.toString()) : 0;
                        }
                    }
                }
                return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
            }
            // 类型2 数值：content 解析为数值，落在某区间则取该条 weight
            if (itemType == 2) {
                double numVal;
                try {
                    numVal = Double.parseDouble(content.trim());
                } catch (NumberFormatException e) {
                    return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
                }
                for (Map<String, Object> r : rules) {
                    Object minO = r.get("minValue");
                    Object maxO = r.get("maxValue");
                    double min = minO != null && !"".equals(minO.toString()) ? Double.parseDouble(minO.toString()) : Double.NEGATIVE_INFINITY;
                    double max = maxO != null && !"".equals(maxO.toString()) ? Double.parseDouble(maxO.toString()) : Double.POSITIVE_INFINITY;
                    if (numVal >= min && numVal <= max) {
                        Object w = r.get("weight");
                        return w != null ? Integer.parseInt(w.toString()) : 0;
                    }
                }
                return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
            }
            // 类型1 选择：content 与 optionValue 匹配
            if (itemType == 1) {
                String contentNorm = content != null ? content.trim() : "";
                for (Map<String, Object> r : rules) {
                    Object opt = r.get("optionValue");
                    if (opt != null && contentNorm.equals(opt.toString().trim())) {
                        Object w = r.get("weight");
                        return w != null ? Integer.parseInt(w.toString()) : 0;
                    }
                }
                return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
            }
        } catch (Exception ignored) {
            // 解析失败时回退为按结果+权重
        }
        return (resultCode != null && resultCode == InspectResultEnum.NORMAL.getCode()) ? defaultWeight : 0;
    }

    @Override
    public Page<InspectRecord> selectByPage(InspectRecordPageQuery query) {
        LambdaQueryWrapper<InspectRecord> qw = new LambdaQueryWrapper<InspectRecord>()
                .eq(StringUtils.hasText(query.getTaskId()), InspectRecord::getTaskId, query.getTaskId())
                .eq(StringUtils.hasText(query.getEquipId()), InspectRecord::getEquipId, query.getEquipId())
                .like(StringUtils.hasText(query.getEquipName()), InspectRecord::getEquipName, query.getEquipName())
                .ge(query.getRecordTimeStart() != null, InspectRecord::getRecordTime, query.getRecordTimeStart())
                .le(query.getRecordTimeEnd() != null, InspectRecord::getRecordTime, query.getRecordTimeEnd())
                .in(!CollectionUtils.isEmpty(query.getTaskIds()), InspectRecord::getTaskId, query.getTaskIds())
                .orderByDesc(InspectRecord::getRecordTime);
        return page(new Page<>(query.getPage(), query.getPageSize()), qw);
    }

    @Override
    public List<InspectRecord> listByTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty()) return new ArrayList<>();
        return list(new LambdaQueryWrapper<InspectRecord>().eq(InspectRecord::getTaskId, taskId).orderByAsc(InspectRecord::getRecordTime));
    }

    @Override
    public List<InspectRecord> listByEquipIdAndRecordTimeBetween(String equipId, Date recordTimeStart, Date recordTimeEnd) {
        if (equipId == null || equipId.isEmpty()) return new ArrayList<>();
        LambdaQueryWrapper<InspectRecord> qw = new LambdaQueryWrapper<InspectRecord>()
                .eq(InspectRecord::getEquipId, equipId)
                .ge(recordTimeStart != null, InspectRecord::getRecordTime, recordTimeStart)
                .le(recordTimeEnd != null, InspectRecord::getRecordTime, recordTimeEnd)
                .orderByAsc(InspectRecord::getRecordTime);
        return list(qw);
    }
}
