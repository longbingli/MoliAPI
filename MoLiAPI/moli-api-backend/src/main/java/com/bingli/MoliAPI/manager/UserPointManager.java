package com.bingli.MoliAPI.manager;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.mapper.UserMapper;
import com.bingli.MoliAPI.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserPointManager {

    private static final long MAX_CHANGE_AMOUNT = 60;

    private static final long MAX_SCORE = 99999;

    private final UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 增加积分
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoints(Long userId, long amount, String requestId) {
        validateUserId(userId);
        validateAmountForAdd(amount);
        checkRateLimit(userId);
        checkIdempotent(requestId);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                .setSql("points = LEAST(points + " + amount + ", " + MAX_SCORE + ")");
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 扣减积分，使用 points >= amount 避免超扣
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean subtractPoints(Long userId, long amount) {
        validateUserId(userId);
        validateAmountForSubtract(amount);

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                .ge("points", amount)
                .setSql("points = points - " + amount);
        return userMapper.update(null, updateWrapper) > 0;
    }

    /**
     * 扣减积分并校验结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void subtractPointsWithCheck(Long userId, long amount) {
        boolean success = subtractPoints(userId, amount);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "积分不足");
    }

    /**
     * 校验用户是否合法
     */
    private void validateUserId(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 Id 非法");
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(user.getStatus() != 0, ErrorCode.OPERATION_ERROR, "账号已被封禁");
    }

    /**
     * 增加积分时的额度校验
     */
    private void validateAmountForAdd(long amount) {
        ThrowUtils.throwIf(amount <= 0, ErrorCode.PARAMS_ERROR, "积分变更值必须大于 0");
        ThrowUtils.throwIf(amount > MAX_CHANGE_AMOUNT, ErrorCode.PARAMS_ERROR, "操作非法");
    }

    /**
     * 扣减积分时的额度校验
     */
    private void validateAmountForSubtract(long amount) {
        ThrowUtils.throwIf(amount <= 0, ErrorCode.PARAMS_ERROR, "积分变更值必须大于 0");
        ThrowUtils.throwIf(amount > MAX_SCORE, ErrorCode.PARAMS_ERROR, "操作非法");
    }

    /**
     * Redis 限流校验，Redis 异常时降级放行
     */
    private void checkRateLimit(Long userId) {
        try {
            String key = "points:limit:" + userId;
            Long count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1) {
                redisTemplate.expire(key, 10, TimeUnit.SECONDS);
            }

            ThrowUtils.throwIf(count != null && count > 5,
                    ErrorCode.OPERATION_ERROR,
                    "操作过于频繁，请稍后再试");
        } catch (Exception e) {
            // Redis 出现网络抖动时，不能让积分附加能力拖垮主流程
            log.warn("Redis 限流校验失败，已降级放行, userId={}", userId, e);
        }
    }

    /**
     * Redis 幂等校验，Redis 异常时降级放行
     */
    private void checkIdempotent(String requestId) {
        ThrowUtils.throwIf(StringUtils.isBlank(requestId), ErrorCode.PARAMS_ERROR, "请求号不能为空");

        try {
            String key = "points:idempotent:" + requestId;
            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(key, "1", 1, TimeUnit.MINUTES);

            ThrowUtils.throwIf(Boolean.FALSE.equals(success),
                    ErrorCode.OPERATION_ERROR,
                    "请勿重复提交");
        } catch (Exception e) {
            // Redis 故障时允许继续执行，避免注册奖励等核心流程直接 500
            log.warn("Redis 幂等校验失败，已降级放行, requestId={}", requestId, e);
        }
    }
}
