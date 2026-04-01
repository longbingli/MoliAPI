package com.bingli.MoliAPI.service;

import com.bingli.MoliAPI.model.entity.AppInfo;

/**
 * 应用 Dubbo 服务
 */
public interface AppInfoDubboService {

    /**
     * 根据 appId 获取下游 host
     */
    String getAppHostByAppId(Long appId);

    /**
     * 根据 appId 获取应用信息
     */
    AppInfo getAppInfoByAppId(Long appId);
}
