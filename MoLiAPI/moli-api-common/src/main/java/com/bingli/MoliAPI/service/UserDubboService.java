package com.bingli.MoliAPI.service;

import com.bingli.MoliAPI.model.entity.User;

/**
 * 用户 Dubbo 服务
 */
public interface UserDubboService {

    /**
     * 根据 accessKey 获取可调用用户
     */
    User getInvokeUser(String accessKey);

    /**
     * 扣减用户积分（不足时抛异常）
     */
    void subtractPointsWithCheck(Long userId, long amount);
}
