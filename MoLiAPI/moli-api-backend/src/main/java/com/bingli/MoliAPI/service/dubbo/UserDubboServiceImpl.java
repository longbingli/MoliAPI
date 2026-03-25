package com.bingli.MoliAPI.service.dubbo;

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

        return userService.getInvokeUser(accessKey);
    }

}
