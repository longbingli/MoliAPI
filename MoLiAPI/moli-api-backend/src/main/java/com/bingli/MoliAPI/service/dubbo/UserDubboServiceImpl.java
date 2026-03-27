package com.bingli.MoliAPI.service.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.service.UserDubboService;
import com.bingli.MoliAPI.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

@DubboService
public class UserDubboServiceImpl implements UserDubboService {

    @Resource
    private UserService userService;
    @Override
    public User getInvokeUser(String accessKey) {

            ThrowUtils.throwIf(accessKey == null, ErrorCode.PARAMS_ERROR, "访问密钥不能为空");
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("accessKey", accessKey);
            queryWrapper.eq("status", 0);
            return userService.getOne(queryWrapper);
    }

}
