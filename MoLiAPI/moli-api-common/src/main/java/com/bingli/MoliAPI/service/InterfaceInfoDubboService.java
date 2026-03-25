package com.bingli.MoliAPI.service;

import com.bingli.MoliAPI.model.entity.InterfaceInfo;

public interface InterfaceInfoDubboService {
    InterfaceInfo getInterfaceInfo(String path, String method);
}
