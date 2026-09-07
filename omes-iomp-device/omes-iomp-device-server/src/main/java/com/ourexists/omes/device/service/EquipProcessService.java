package com.ourexists.omes.device.service;

import com.ourexists.omes.device.model.DeviceTreeNode;
import com.ourexists.omes.device.model.DgEquipProcessDto;
import com.ourexists.omes.device.model.EquipDto;

import java.util.List;

public interface EquipProcessService {

    void bindEquips(String dgId, List<String> equipIds);

    void unbindEquips(String dgId, List<String> equipIds);

    void unbindByDgIds(List<String> dgIds);

    void removeByEquipIds(List<String> equipIds);

    void saveProcess(DgEquipProcessDto dto);

    List<EquipDto> listBoundEquips(String dgId);

    List<DeviceTreeNode> listBoundAsDeviceNodes(String dgId);

    boolean isUseMat(List<String> matCodes);
}
