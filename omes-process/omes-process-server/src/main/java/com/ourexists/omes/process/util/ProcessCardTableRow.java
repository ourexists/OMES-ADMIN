package com.ourexists.omes.process.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** 工艺卡片工序表一行（工序号 / 内容 / 设备 / 工装）。 */
public record ProcessCardTableRow(String stepNo, String content, String equipment, String tooling) {

    public List<String> asCells() {
        List<String> cells = new ArrayList<>(4);
        cells.add(stepNo == null ? "" : stepNo);
        cells.add(content == null ? "" : content);
        cells.add(equipment == null ? "" : equipment);
        cells.add(tooling == null ? "" : tooling);
        return cells;
    }

    public boolean isBlank() {
        return !StringUtils.hasText(stepNo) && !StringUtils.hasText(content)
                && !StringUtils.hasText(equipment) && !StringUtils.hasText(tooling);
    }
}
