package com.bingli.MoliAPI.controller;

import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.service.UserService;
import com.bingli.MoliAPI.utils.AESUtil;
import com.bingli.moliclientsdk.client.MoliClient;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/poetry")
public class DemoController {

    @Resource
    private UserService userService;

    @Value("${moli.client.gateway-host}")
    private String gatewayHost;



    @GetMapping("/random")
    public String random(HttpServletRequest request) throws Exception {

        User user =  userService.getLoginUser(request);

        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        String accessKey = user.getAccessKey();
        String secretKey = AESUtil.decrypt(user.getSecretKey());


        // 2. 创建 client
        MoliClient client = new MoliClient(
                accessKey,
                secretKey,
                gatewayHost
        );


        return client.doRequest("GET", "/api/poetry/random", null);
    }
}