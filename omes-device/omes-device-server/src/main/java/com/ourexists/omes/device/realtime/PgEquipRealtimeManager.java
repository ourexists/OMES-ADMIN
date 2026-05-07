package com.ourexists.omes.device.realtime;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.omes.device.core.equip.cache.*;
import com.ourexists.omes.device.mapper.EquipRealtimeMapper;
import com.ourexists.omes.device.model.EquipDto;
import com.ourexists.omes.device.model.EquipPageQuery;
import com.ourexists.omes.device.model.GwBindingDto;
import com.ourexists.omes.device.pojo.Equip;
import com.ourexists.omes.device.pojo.EquipRealtimeRecord;
import com.ourexists.omes.device.pojo.GwBinding;
import com.ourexists.omes.device.service.EquipService;
import com.ourexists.omes.device.service.GwBindingService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 设备实时态：写入 PostgreSQL 表 {@code t_equip_realtime}，语义对齐原 Redis 实现。
 */
@Slf4j
@Primary
@Component
public class PgEquipRealtimeManager implements EquipRealtimeManager {

    /** 与 pg_try_advisory_lock 族一致的命名空间常量（任意固定值即可） */
    private static final int ADV_LOCK_K1 = 0x4F4D4553;
    private static final int ADV_LOCK_K2 = 0x45515254;

    private static final int RELOAD_PAGE_SIZE = 500;

    /** 避免同一 JVM 内父子容器多次 refresh 触发重复 reload（跨实例互斥仍靠 PG advisory lock） */
    private final AtomicBoolean startupReloadOnce = new AtomicBoolean(false);

    @Autowired
    private EquipRealtimeMapper equipRealtimeMapper;

    @Autowired
    private EquipService equipService;

    @Autowired
    private GwBindingService gwBindingService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private ApplicationContext applicationContext;

    /** reload 独立事务，避免加入调用方已有事务导致 advisory xact lock 与写库不在同一事务边界 */
    private TransactionTemplate reloadTransactionTemplate;

    private static String resolveGwId(EquipRealtime rt) {
        if (rt == null || rt.getEquipRealtimeConfig() == null) {
            return null;
        }
        String gwId = rt.getEquipRealtimeConfig().getGwId();
        return StringUtils.hasText(gwId) ? gwId : null;
    }

    private static EquipRealtimeRecord toRow(EquipRealtime rt) {
        EquipRealtimeRecord row = new EquipRealtimeRecord();
        row.setId(rt.getId());
        row.setTenantId(rt.getTenantId());
        row.setSelfCode(rt.getSelfCode());
        row.setGwId(resolveGwId(rt));
        row.setOnlineState(rt.getOnlineState());
        row.setRunState(rt.getRunState());
        row.setAlarmState(rt.getAlarmState());
        row.setOnlineChangeTime(rt.getOnlineChangeTime());
        row.setRunChangeTime(rt.getRunChangeTime());
        row.setAlarmChangeTime(rt.getAlarmChangeTime());
        row.setWorkshopCode(rt.getWorkshopCode());
        row.setName(rt.getName());
        row.setEquipTime(rt.getTime());
        row.setAlarmLevel(rt.getAlarmLevel());
        row.setPayload(rt);
        return row;
    }

    @PostConstruct
    void initReloadTransactionTemplate() {
        this.reloadTransactionTemplate = new TransactionTemplate(platformTransactionManager);
        this.reloadTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void reloadAfterContextReady(ContextRefreshedEvent event) {
        if (!event.getApplicationContext().equals(this.applicationContext)) {
            return;
        }
        if (!startupReloadOnce.compareAndSet(false, true)) {
            return;
        }
        reload();
    }

    @Override
    public void addOrUpdate(EquipRealtime equipRealtime) {
        withEquipDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            if (!StringUtils.hasText(equipRealtime.getTenantId())) {
                equipRealtime.setTenantId(tenantId);
            }
            equipRealtimeMapper.upsert(toRow(equipRealtime));
        });
    }

    @Override
    public void remove(String sn) {
        withEquipDataSyncMutation(() -> {
            String tenantId = UserContext.getTenant().getTenantId();
            equipRealtimeMapper.delete(new LambdaQueryWrapper<EquipRealtimeRecord>()
                    .eq(EquipRealtimeRecord::getTenantId, tenantId)
                    .eq(EquipRealtimeRecord::getSelfCode, sn));
        });
    }


    @Override
    public EquipRealtime get(String sn) {
        String tenantId = UserContext.getTenant().getTenantId();
        EquipRealtimeRecord row = equipRealtimeMapper.selectOne(new LambdaQueryWrapper<EquipRealtimeRecord>()
                .eq(EquipRealtimeRecord::getTenantId, tenantId)
                .eq(EquipRealtimeRecord::getSelfCode, sn));
        return row == null ? null : row.getPayload();
    }

    @Override
    public EquipRealtime getById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        String tenantId = UserContext.getTenant().getTenantId();
        EquipRealtimeRecord row = equipRealtimeMapper.selectOne(new LambdaQueryWrapper<EquipRealtimeRecord>()
                .eq(EquipRealtimeRecord::getTenantId, tenantId)
                .eq(EquipRealtimeRecord::getId, id));
        return row == null ? null : row.getPayload();
    }

    @Override
    public List<EquipRealtime> listByGwId(String gwId) {
        if (!StringUtils.hasText(gwId)) {
            return Collections.emptyList();
        }
        String tenantId = UserContext.getTenant().getTenantId();
        List<EquipRealtimeRecord> rows = equipRealtimeMapper.selectList(new LambdaQueryWrapper<EquipRealtimeRecord>()
                .eq(EquipRealtimeRecord::getTenantId, tenantId)
                .eq(EquipRealtimeRecord::getGwId, gwId));
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        List<EquipRealtime> list = new ArrayList<>(rows.size());
        for (EquipRealtimeRecord row : rows) {
            if (row.getPayload() != null) {
                list.add(row.getPayload());
            }
        }
        return list;
    }


    @Override
    public void reload() {
        reloadTransactionTemplate.executeWithoutResult(status -> {
            Boolean ok = equipRealtimeMapper.tryAdvisoryXactLock(ADV_LOCK_K1, ADV_LOCK_K2);
            if (!Boolean.TRUE.equals(ok)) {
                log.info("reload equip realtime skipped: another instance holds pg advisory xact lock");
                return;
            }
            UserContext.defaultTenant();
            runReloadBody();
        });
    }

    private void runReloadBody() {
        try {
            Set<String> tenantsCleared = new HashSet<>();
            int pageNum = 1;
            while (true) {
                EquipPageQuery query = new EquipPageQuery();
                query.setRequirePage(true);
                query.setPage(pageNum);
                query.setPageSize(RELOAD_PAGE_SIZE);
                query.setQueryConfig(true);
                Page<Equip> equipPage = equipService.selectByPage(query);
                List<Equip> records = equipPage.getRecords();
                if (CollectionUtils.isEmpty(records)) {
                    break;
                }
                List<EquipDto> equipDtos = buildEquipDtosWithConfig(records);
                putReloadBatch(equipDtos, tenantsCleared);
                if (records.size() < RELOAD_PAGE_SIZE) {
                    break;
                }
                pageNum++;
            }
        } catch (Exception e) {
            log.error("reload equip realtime failed: {}", e.getMessage(), e);
        }
    }

    private List<EquipDto> buildEquipDtosWithConfig(List<Equip> records) {
        List<EquipDto> r = Equip.covert(records);
        if (CollectionUtils.isEmpty(r)) {
            return r;
        }
        List<String> ids = r.stream().map(EquipDto::getId).toList();
        List<GwBindingDto> equipConfigs = GwBinding.covert(gwBindingService.queryByEquip(ids));
        Map<String, GwBindingDto> configByEquipId = new HashMap<>();
        if (!CollectionUtils.isEmpty(equipConfigs)) {
            for (GwBindingDto g : equipConfigs) {
                configByEquipId.put(g.getEquipId(), g);
            }
        }
        for (EquipDto equipDto : r) {
            GwBindingDto cfg = configByEquipId.get(equipDto.getId());
            if (cfg != null) {
                equipDto.setConfig(cfg);
            }
        }
        return r;
    }

    private void putReloadBatch(List<EquipDto> equipDtos, Set<String> tenantsCleared) {
        for (EquipDto equipDto : equipDtos) {
            String tid = equipDto.getTenantId();
            if (tid != null && tenantsCleared.add(tid)) {
                equipRealtimeMapper.delete(new LambdaQueryWrapper<EquipRealtimeRecord>()
                        .eq(EquipRealtimeRecord::getTenantId, tid));
            }
            if (equipDto.getTenantId() == null) {
                log.warn("reload: skip equip with null tenantId selfCode={}", equipDto.getSelfCode());
                continue;
            }
            EquipRealtime equipRealtime = buildRealtimeFromEquipDto(equipDto);
            equipRealtimeMapper.upsert(toRow(equipRealtime));
        }
    }

    private static EquipRealtime buildRealtimeFromEquipDto(EquipDto equipDto) {
        EquipRealtime equipRealtime = new EquipRealtime();
        BeanUtils.copyProperties(equipDto, equipRealtime);
        if (equipDto.getConfig() != null && equipDto.getConfig().getConfig() != null) {
            EquipRealtimeConfig equipRealtimeConfig = new EquipRealtimeConfig();
            BeanUtils.copyProperties(equipDto.getConfig().getConfig(), equipRealtimeConfig);
            if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getAttrs())) {
                List<EquipAttrRealtime> attrs = new ArrayList<>();
                equipDto.getConfig().getConfig().getAttrs().forEach(attr -> {
                    EquipAttrRealtime equipAttrRealtime = new EquipAttrRealtime();
                    BeanUtils.copyProperties(attr, equipAttrRealtime);
                    attrs.add(equipAttrRealtime);
                });
                equipRealtimeConfig.setAttrs(attrs);
            }
            if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getAlarms())) {
                List<EquipAlarmRealtime> alarms = new ArrayList<>();
                equipDto.getConfig().getConfig().getAlarms().forEach(alarm -> {
                    EquipAlarmRealtime equipAlarmRealtime = new EquipAlarmRealtime();
                    BeanUtils.copyProperties(alarm, equipAlarmRealtime);
                    alarms.add(equipAlarmRealtime);
                });
                equipRealtimeConfig.setAlarms(alarms);
            }
            if (!CollectionUtils.isEmpty(equipDto.getConfig().getConfig().getControls())) {
                List<EquipControlRealtime> controls = new ArrayList<>();
                equipDto.getConfig().getConfig().getControls().forEach(ctrl -> {
                    EquipControlRealtime equipControlRealtime = new EquipControlRealtime();
                    BeanUtils.copyProperties(ctrl, equipControlRealtime);
                    controls.add(equipControlRealtime);
                });
                equipRealtimeConfig.setControls(controls);
            }
            equipRealtime.setEquipRealtimeConfig(equipRealtimeConfig);
            equipRealtime.setEquipAttrRealtimes(equipRealtimeConfig.getAttrs());
            equipRealtime.setEquipControlRealtimes(equipRealtimeConfig.getControls());
            Date currentDate = new Date();
            equipRealtime.setAlarmChangeTime(currentDate);
            equipRealtime.setRunChangeTime(currentDate);
            equipRealtime.setOnlineChangeTime(currentDate);
        }
        return equipRealtime;
    }

    private void withEquipDataSyncMutation(Runnable action) {
        transactionTemplate.executeWithoutResult(status -> {
            equipRealtimeMapper.advisoryXactLock(ADV_LOCK_K1, ADV_LOCK_K2);
            action.run();
        });
    }
}
