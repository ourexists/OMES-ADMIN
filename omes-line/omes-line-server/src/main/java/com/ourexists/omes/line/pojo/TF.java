/*

 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists

 */



package com.ourexists.omes.line.pojo;



import com.baomidou.mybatisplus.annotation.TableName;

import com.ourexists.era.framework.core.utils.CollectionUtil;

import com.ourexists.era.framework.orm.mybatisplus.EraEntity;

import com.ourexists.omes.line.model.TFDto;

import com.ourexists.omes.line.model.TFVo;

import com.ourexists.omes.line.util.TfResourceJsonUtil;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Getter;

import lombok.Setter;

import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;



import java.util.ArrayList;

import java.util.List;



@Getter

@Setter

@Accessors(chain = true)

@TableName("t_tf")

public class TF extends EraEntity {



    private String selfCode;



    private String name;



    private Integer stepNo;



    private String stepContent;



    private String equipJson;



    private String toolingJson;



    private String lineId;



    @Schema(description = "工序执行脚本 JSON（流程图/规则引擎）")

    private String stepScript;



    @Schema(description = "流程引擎编译配置 JSON（落库时生成）")

    private String stepEngineConfig;



    public static TFVo covert(TF source) {

        if (source == null) {

            return null;

        }

        TFVo target = new TFVo();

        BeanUtils.copyProperties(source, target);

        target.setEquipments(TfResourceJsonUtil.parseEquipments(source.getEquipJson()));

        target.setToolings(TfResourceJsonUtil.parseToolings(source.getToolingJson()));

        return target;

    }



    public static List<TFVo> covert(List<TF> sources) {

        List<TFVo> targets = new ArrayList<>();

        if (CollectionUtil.isNotBlank(sources)) {

            for (TF source : sources) {

                targets.add(covert(source));

            }

        }

        return targets;

    }





    public static TF wrap(TFDto source) {

        TF target = new TF();

        BeanUtils.copyProperties(source, target);

        target.setEquipJson(TfResourceJsonUtil.writeEquipments(source.getEquipments()));

        target.setToolingJson(TfResourceJsonUtil.writeToolings(source.getToolings()));

        if (StringUtils.isNotBlank(source.getStepContent())) {

            target.setStepContent(source.getStepContent());

        }

        return target;

    }



    public static List<TF> wrap(List<TFDto> sources) {

        List<TF> targets = new ArrayList<>();

        if (CollectionUtil.isNotBlank(sources)) {

            for (TFDto source : sources) {

                targets.add(wrap(source));

            }

        }

        return targets;

    }



    public static List<TF> wrap(List<TFDto> sources, String lineId) {

        List<TF> targets = new ArrayList<>();

        if (CollectionUtil.isNotBlank(sources)) {

            for (TFDto source : sources) {

                TF tf = wrap(source);

                tf.setLineId(lineId);

                targets.add(tf);

            }

        }

        return targets;

    }

}

