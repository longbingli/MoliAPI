package com.bingli.MoliAPI.service.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.model.entity.InterfaceInfo;
import com.bingli.MoliAPI.service.AppInfoService;
import com.bingli.MoliAPI.service.InterfaceInfoDubboService;
import com.bingli.MoliAPI.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@DubboService
public class InterfaceInfoDubboServiceImpl implements InterfaceInfoDubboService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private AppInfoService appInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        ThrowUtils.throwIf(StringUtils.isAnyBlank(path, method), ErrorCode.PARAMS_ERROR, "参数为空");

        LambdaQueryWrapper<InterfaceInfo> queryWrapper = Wrappers.lambdaQuery(InterfaceInfo.class)
                .eq(InterfaceInfo::getUrl, path)
                .eq(InterfaceInfo::getMethod, method)
                .last("limit 1");

        return interfaceInfoService.getOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invokeCount(long interfaceInfoId, long userId) {
        ThrowUtils.throwIf(interfaceInfoId <= 0 || userId <= 0, ErrorCode.PARAMS_ERROR, "参数非法");

        // 1. 查询接口是否存在
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceInfoId);
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.NOT_FOUND_ERROR, "接口信息不存在");

        Long appId = Long.valueOf(interfaceInfo.getAppId());
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.NOT_FOUND_ERROR, "接口未关联应用");

        // 2. 接口调用次数原子 +1
        boolean interfaceUpdateResult = interfaceInfoService.lambdaUpdate()
                .eq(InterfaceInfo::getId, interfaceInfoId)
                .setSql("total_num = IFNULL(total_num, 0) + 1")
                .update();
        ThrowUtils.throwIf(!interfaceUpdateResult, ErrorCode.OPERATION_ERROR, "接口调用次数更新失败");

        // 3. 应用调用次数原子 +1
        boolean appUpdateResult = appInfoService.lambdaUpdate()
                .eq(AppInfo::getAppId, appId)
                .setSql("total_num = IFNULL(total_num, 0) + 1")
                .update();
        ThrowUtils.throwIf(!appUpdateResult, ErrorCode.OPERATION_ERROR, "应用调用次数更新失败");
    }
}