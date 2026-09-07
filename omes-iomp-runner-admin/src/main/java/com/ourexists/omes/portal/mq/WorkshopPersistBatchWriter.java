package com.ourexists.omes.portal.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ourexists.era.framework.core.user.UserContext;
import com.ourexists.era.framework.core.utils.RemoteHandleUtils;
import com.ourexists.omes.device.feign.WorkshopCollectFeign;
import com.ourexists.omes.device.model.WorkshopCollectDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkshopPersistBatchWriter {

    private static final String FIELD_COLLECT = "collect";

    private final ObjectMapper objectMapper;
    private final WorkshopCollectFeign workshopCollectFeign;

    public void writeWorkshopCollectSnapshotBatch(Collection<?> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return;
        }
        Map<String, List<WorkshopCollectDto>> byTenant = new LinkedHashMap<>();
        try {
            UserContext.defaultTenant();
            for (Object o : payloads) {
                JsonNode root = unwrapPersistRoot(o);
                if (root == null || !root.hasNonNull(FIELD_COLLECT)) {
                    continue;
                }
                WorkshopCollectDto dto = objectMapper.treeToValue(root.get(FIELD_COLLECT), WorkshopCollectDto.class);
                if (dto == null || !StringUtils.hasText(dto.getWorkshopId())) {
                    continue;
                }
                String tid = StringUtils.hasText(dto.getTenantId())
                        ? dto.getTenantId()
                        : UserContext.getTenant().getTenantId();
                byTenant.computeIfAbsent(tid, k -> new ArrayList<>()).add(dto);
            }
            for (Map.Entry<String, List<WorkshopCollectDto>> e : byTenant.entrySet()) {
                UserContext.getTenant().setTenantId(e.getKey());
                RemoteHandleUtils.getDataFormResponse(workshopCollectFeign.addBatch(e.getValue()));
            }
        } catch (Exception ex) {
            log.error("Workshop stream persist: collect snapshot batch write failed, messageCount={}", payloads.size(), ex);
        } finally {
            UserContext.remove();
        }
    }

    private static JsonNode unwrapPersistRoot(Object o) {
        if (o instanceof JsonNode j) {
            return j;
        }
        if (o instanceof WorkshopPersistMqEvent e) {
            return e.getRoot();
        }
        return null;
    }
}
