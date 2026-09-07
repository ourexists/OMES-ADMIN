package com.ourexists.omes.device.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 将产品属性模板与型号已保存的采集映射合并。
 */
public final class ProductAttrMerge {

    private ProductAttrMerge() {
    }

    public static EquipConfigDetail merge(ProductAttrConfig template, EquipConfigDetail device) {
        EquipConfigDetail result = copyConfig(device);
        if (template == null) {
            return result;
        }
        result.setAttrs(mergeAttrs(template.getAttrs(), result.getAttrs()));
        result.setAlarms(mergeAlarms(template.getAlarms(), result.getAlarms()));
        result.setControls(mergeControls(template.getControls(), result.getControls()));
        return result;
    }

    private static EquipConfigDetail copyConfig(EquipConfigDetail source) {
        EquipConfigDetail result = new EquipConfigDetail();
        if (source == null) {
            return result;
        }
        result.setGwId(source.getGwId());
        result.setCollectType(source.getCollectType());
        result.setDeviceIdMap(source.getDeviceIdMap());
        result.setRunMap(source.getRunMap());
        result.setAlarmMap(source.getAlarmMap());
        result.setAttrs(source.getAttrs());
        result.setAlarms(source.getAlarms());
        result.setControls(source.getControls());
        return result;
    }

    static List<EquipAttr> mergeAttrs(List<EquipAttr> templates, List<EquipAttr> existing) {
        Map<String, EquipAttr> byName = index(existing, ProductAttrMerge::attrKey);
        List<EquipAttr> result = new ArrayList<>();
        if (templates != null) {
            for (EquipAttr tpl : templates) {
                if (tpl == null) {
                    continue;
                }
                EquipAttr row = copyAttr(tpl);
                EquipAttr hit = byName.remove(attrKey(tpl));
                if (hit != null) {
                    row.setMap(hit.getMap());
                    if (hasText(hit.getValue())) {
                        row.setValue(hit.getValue());
                    }
                    if (hit.getNeedCollect() != null) {
                        row.setNeedCollect(hit.getNeedCollect());
                    }
                    if (hit.getFluctuationEnabled() != null) {
                        row.setFluctuationEnabled(hit.getFluctuationEnabled());
                    }
                    if (hit.getFluctuationThresholdRatio() != null) {
                        row.setFluctuationThresholdRatio(hit.getFluctuationThresholdRatio());
                    }
                    if (hit.getFluctuationMinDelta() != null) {
                        row.setFluctuationMinDelta(hit.getFluctuationMinDelta());
                    }
                    if (hit.getFluctuationConsecutiveWindows() != null) {
                        row.setFluctuationConsecutiveWindows(hit.getFluctuationConsecutiveWindows());
                    }
                }
                result.add(row);
            }
        }
        return result;
    }

    static List<EquipAlarm> mergeAlarms(List<EquipAlarm> templates, List<EquipAlarm> existing) {
        Map<String, EquipAlarm> byKey = index(existing, ProductAttrMerge::alarmKey);
        List<EquipAlarm> result = new ArrayList<>();
        if (templates != null) {
            for (EquipAlarm tpl : templates) {
                if (tpl == null) {
                    continue;
                }
                EquipAlarm row = copyAlarm(tpl);
                EquipAlarm hit = byKey.remove(alarmKey(tpl));
                if (hit == null && hasText(tpl.getText())) {
                    hit = byKey.remove(normalize(tpl.getText()));
                }
                if (hit != null) {
                    row.setMap(hit.getMap());
                }
                result.add(row);
            }
        }
        return result;
    }

    static List<EquipControl> mergeControls(List<EquipControl> templates, List<EquipControl> existing) {
        Map<String, EquipControl> byName = index(existing, ProductAttrMerge::controlKey);
        List<EquipControl> result = new ArrayList<>();
        if (templates != null) {
            for (EquipControl tpl : templates) {
                if (tpl == null) {
                    continue;
                }
                EquipControl row = copyControl(tpl);
                EquipControl hit = byName.remove(controlKey(tpl));
                if (hit != null) {
                    row.setMap(hit.getMap());
                    if (hasText(hit.getValue())) {
                        row.setValue(hit.getValue());
                    }
                }
                result.add(row);
            }
        }
        return result;
    }

    private static <T> Map<String, T> index(List<T> list, Function<T, String> keyFn) {
        Map<String, T> map = new LinkedHashMap<>();
        if (list == null || list.isEmpty()) {
            return map;
        }
        for (T item : list) {
            if (item == null) {
                continue;
            }
            String key = keyFn.apply(item);
            if (hasText(key) && !map.containsKey(key)) {
                map.put(key, item);
            }
        }
        return map;
    }

    private static EquipAttr copyAttr(EquipAttr source) {
        EquipAttr target = new EquipAttr();
        target.setName(source.getName());
        target.setMap(source.getMap());
        target.setValue(source.getValue());
        target.setUnit(source.getUnit());
        target.setNeedCollect(source.getNeedCollect());
        target.setFluctuationEnabled(source.getFluctuationEnabled());
        target.setFluctuationThresholdRatio(source.getFluctuationThresholdRatio());
        target.setFluctuationMinDelta(source.getFluctuationMinDelta());
        target.setFluctuationConsecutiveWindows(source.getFluctuationConsecutiveWindows());
        return target;
    }

    private static EquipAlarm copyAlarm(EquipAlarm source) {
        EquipAlarm target = new EquipAlarm();
        target.setName(source.getName());
        target.setMap(source.getMap());
        target.setType(source.getType());
        target.setVal(source.getVal());
        target.setMin(source.getMin());
        target.setMax(source.getMax());
        target.setText(source.getText());
        target.setLevel(source.getLevel());
        return target;
    }

    private static EquipControl copyControl(EquipControl source) {
        EquipControl target = new EquipControl();
        target.setName(source.getName());
        target.setMap(source.getMap());
        target.setType(source.getType());
        target.setValue(source.getValue());
        target.setUnit(source.getUnit());
        target.setMin(source.getMin());
        target.setMax(source.getMax());
        return target;
    }

    private static String attrKey(EquipAttr attr) {
        return attr == null ? "" : normalize(attr.getName());
    }

    private static String alarmKey(EquipAlarm alarm) {
        if (alarm == null) {
            return "";
        }
        if (hasText(alarm.getName())) {
            return normalize(alarm.getName());
        }
        return normalize(alarm.getText());
    }

    private static String controlKey(EquipControl control) {
        return control == null ? "" : normalize(control.getName());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
