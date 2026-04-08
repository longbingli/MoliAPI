package com.bingli.MoliAPI.service.dubbo;

import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.service.AppInfoDubboService;
import com.bingli.MoliAPI.service.AppInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class AppInfoDubboServiceImpl implements AppInfoDubboService {

    @Resource
    private AppInfoService appInfoService;

    @Override
    public String getAppHostByAppId(Long appId) {
        AppInfo appInfo = getAppInfoByAppId(appId);
        // 兜底清洗配置中的空白和控制字符，避免网关拼接 URI 失败
        String host = StringUtils.trimToEmpty(appInfo.getHost());
        host = host.replaceAll("\\p{Cntrl}", "");
        return StringUtils.trimToEmpty(host);
    }

    @Override
    public AppInfo getAppInfoByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "appId不能小于等于0");
        AppInfo appInfo = appInfoService.getById(appId);
        ThrowUtils.throwIf(appInfo == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在或已关闭");
        return appInfo;
    }
}
