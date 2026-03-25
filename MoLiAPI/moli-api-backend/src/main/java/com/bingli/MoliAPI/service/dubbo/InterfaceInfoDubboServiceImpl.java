package com.bingli.MoliAPI.service.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.entity.InterfaceInfo;
import com.bingli.MoliAPI.service.InterfaceInfoDubboService;
import com.bingli.MoliAPI.service.InterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class InterfaceInfoDubboServiceImpl implements InterfaceInfoDubboService {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        ThrowUtils.throwIf(path == null || method == null, ErrorCode.PARAMS_ERROR, "参数为空");
        return interfaceInfoService.getOne(new QueryWrapper<InterfaceInfo>().eq("url", path).eq("method", method));
    }
}
