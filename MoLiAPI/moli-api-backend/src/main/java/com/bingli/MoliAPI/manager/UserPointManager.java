package com.bingli.MoliAPI.manager;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.mapper.UserMapper;
import com.bingli.MoliAPI.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserPointManager {

    private final UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 增加积分时允许的单次最大变更值
     */
    private static final long MAX_CHANGE_AMOUNT = 60;

    /**
     * 用户积分上限
     */
    private static final long MAX_SCORE = 99999;

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
     * 扣减积分（通过 points >= amount 防止并发超扣）
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
     * 扣减积分并校验
     */
    @Transactional(rollbackFor = Exception.class)
    public void subtractPointsWithCheck(Long userId, long amount) {
        boolean success = subtractPoints(userId, amount);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "积分不足");
    }

    /**
     * 校验用户
     */
    private void validateUserId(Long userId) {
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户Id非法");
        User user = userMapper.selectById(userId);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        ThrowUtils.throwIf(user.getStatus() != 0, ErrorCode.OPERATION_ERROR, "账号已被封禁");
    }

    /**
     * 增加积分时的额度校验
     */
    private void validateAmountForAdd(long amount) {
        ThrowUtils.throwIf(amount <= 0, ErrorCode.PARAMS_ERROR, "积分变更值必须大于0");
        ThrowUtils.throwIf(amount > MAX_CHANGE_AMOUNT, ErrorCode.PARAMS_ERROR, "操作非法");
    }

    /**
     * 扣减积分时允许更大额度，只限制不能超过积分上限
     */
    private void validateAmountForSubtract(long amount) {
        ThrowUtils.throwIf(amount <= 0, ErrorCode.PARAMS_ERROR, "积分变更值必须大于0");
        ThrowUtils.throwIf(amount > MAX_SCORE, ErrorCode.PARAMS_ERROR, "操作非法");
    }

    /**
     * 检查操作频率
     */
    private void checkRateLimit(Long userId) {
        String key = "points:limit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, 10, TimeUnit.SECONDS);
        }

        ThrowUtils.throwIf(count != null && count > 5,
                ErrorCode.OPERATION_ERROR,
                "操作过于频繁，请稍后再试");
    }

    /**
     * 检查幂等
     */
    private void checkIdempotent(String requestId) {
        ThrowUtils.throwIf(StringUtils.isBlank(requestId),
                ErrorCode.PARAMS_ERROR,
                "请求号不能为空");

        String key = "points:idempotent:" + requestId;
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", 1, TimeUnit.MINUTES);

        ThrowUtils.throwIf(Boolean.FALSE.equals(success),
                ErrorCode.OPERATION_ERROR,
                "请勿重复提交");
    }
}
