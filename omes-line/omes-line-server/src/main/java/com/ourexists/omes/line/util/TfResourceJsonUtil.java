/*
 * Copyright (c) 2025. created by ourexists.https://gitee.com/ourexists
 */

package com.ourexists.omes.line.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.omes.line.model.TfEquipmentRef;
import com.ourexists.omes.line.model.TfToolingRef;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class TfResourceJsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<TfEquipmentRef>> EQUIP_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<TfToolingRef>> TOOLING_TYPE = new TypeReference<>() {
    };

    private TfResourceJsonUtil() {
    }

    public static List<TfEquipmentRef> parseEquipments(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<TfEquipmentRef> list = MAPPER.readValue(json, EQUIP_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static List<TfToolingRef> parseToolings(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<TfToolingRef> list = MAPPER.readValue(json, TOOLING_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public static String writeEquipments(List<TfEquipmentRef> equipments) {
        if (equipments == null || equipments.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(equipments);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String writeToolings(List<TfToolingRef> toolings) {
        if (toolings == null || toolings.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(toolings);
        } catch (Exception ex) {
            return null;
        }
    }
}
