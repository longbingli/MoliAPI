package com.bingli.MoliAPI.service;


import com.bingli.MoliAPI.model.entity.User;

public interface UserDubboService {

    User getInvokeUser(String accessKey);
}
