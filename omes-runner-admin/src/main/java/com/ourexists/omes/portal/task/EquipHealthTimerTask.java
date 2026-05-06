package com.ourexists.omes.portal.task;

import com.ourexists.era.framework.core.exceptions.EraCommonException;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.EquipFeign;
import com.ourexists.omes.device.feign.EquipHealthFeign;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipHealthBatchComputeQuery;
import com.ourexists.omes.device.model.EquipPageQuery;
import com.ourexists.omes.task.process.task.TimerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备健康指标定时任务：按周期（默认每小时整点）为所有设备生成健康指标。
 * 使用 ForkJoin 框架并行计算评分，计算完成后一次性批量写入数据库。
 */
@Slf4j
@Component("EquipHealth")
public class EquipHealthTimerTask extends TimerTask {

    /** 统计周期小时数，与默认模板一致 */
    private static final int PERIOD_HOURS = 24;

    @Autowired
    private EquipHealthFeign equipHealthFeign;

    @Autowired
    private EquipFeign equipFeign;

    /**
     * 每小时整点执行一次，统计过去 24 小时的设备健康。
     * 按租户汇总设备 SN 列表后调用批量接口，由设备服务内使用 ForkJoin 并行计算并一次性写入数据库。
     */
    public void doRun() {
        UserContext.defaultTenant();
        EquipPageQuery pageQuery = new EquipPageQuery()
                .setExistHealth(true);
        pageQuery.setRequirePage(false);
        try {
            List<EquipDto> equipDtos = RemoteHandleUtils.getDataFormResponse(equipFeign.selectByPage(pageQuery));
            List<String> snList = equipDtos.stream()
                    .map(EquipDto::getSelfCode)
                    .filter(sn -> sn != null && !sn.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(snList)) {
                return;
            }
            Date periodEnd = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(periodEnd);
            cal.add(Calendar.HOUR_OF_DAY, -PERIOD_HOURS);
            Date periodStart = cal.getTime();

            EquipHealthBatchComputeQuery query = new EquipHealthBatchComputeQuery();
            query.setPeriodStart(periodStart);
            query.setPeriodEnd(periodEnd);
            query.setSnList(snList);
            RemoteHandleUtils.getDataFormResponse(equipHealthFeign.computeBatchAndSave(query));
        } catch (EraCommonException e) {
            log.warn("equip health batch compute failed, msg={}", e.getMessage());
        }
    }
}
