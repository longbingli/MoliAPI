package com.bingli.MoliAPI.service.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.service.AppInfoDubboService;
import com.bingli.MoliAPI.service.AppInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;


@DubboService
public class AppInfoDubboServiceImpl  implements AppInfoDubboService {

    @Resource
    private AppInfoService appInfoService;


    @Override
    public String getAppInfoHosts(String path, String method) {
        return appInfoService.getOne(new QueryWrapper<AppInfo>().eq("path", path).eq("method", method)).getHost();
    }
}
