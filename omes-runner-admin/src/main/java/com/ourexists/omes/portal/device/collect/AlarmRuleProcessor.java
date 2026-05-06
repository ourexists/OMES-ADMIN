package com.ourexists.omes.portal.device.collect;

import com.ourexists.omes.device.core.equip.cache.EquipAlarmRealtime;
import org.springframework.stereotype.Component;

/**
 * 报警规则处理器：判断采集值是否命中报警条件
 * 类型: 0=相等, 1=大于, 2=大于等于, 3=小于, 4=小于等于, 5=范围(超出[min,max]报警)
 */
@Component
public class AlarmRuleProcessor {

    /**
     * 判断单条报警条件是否命中
     *
     * @param raw            原始采集值
     * @param alarmRealtime  报警规则配置
     * @return true=命中报警
     */
    public boolean match(Object raw, EquipAlarmRealtime alarmRealtime) {
        Integer type = alarmRealtime.getType();
        if (type == null) type = 0;
        String strVal = raw != null ? raw.toString() : null;
        if (strVal == null) return false;
        if (type == 0) return strVal.equals(alarmRealtime.getVal());
        Double numVal = parseDouble(strVal);
        if (numVal == null) return false;
        switch (type) {
            case 1:
                return numVal > parseDouble(alarmRealtime.getVal(), 0);
            case 2:
                return numVal >= parseDouble(alarmRealtime.getVal(), 0);
            case 3:
                return numVal < parseDouble(alarmRealtime.getVal(), 0);
            case 4:
                return numVal <= parseDouble(alarmRealtime.getVal(), 0);
            case 5:
                Double min = parseDouble(alarmRealtime.getMin(), Double.NEGATIVE_INFINITY);
                Double max = parseDouble(alarmRealtime.getMax(), Double.POSITIVE_INFINITY);
                return numVal < min || numVal > max;
            default:
                return strVal.equals(alarmRealtime.getVal());
        }
    }

    private static Double parseDouble(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static double parseDouble(String s, double defaultValue) {
        Double d = parseDouble(s);
        return d != null ? d : defaultValue;
    }
}
