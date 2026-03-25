package com.bingli.MoliAPI.service;

import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Description:
 * @Author: bingli
 * @Date: 2021/9/27 10:05 上午
 **/
public interface AppInfoDubboService {

   String getAppInfoHosts(String path, String method);

}
