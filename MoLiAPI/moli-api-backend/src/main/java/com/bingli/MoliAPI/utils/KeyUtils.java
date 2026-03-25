package com.bingli.MoliAPI.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
/**
 * 密钥工具类
 */
public class KeyUtils {
    private static final String AK_PREFIX = "ak_moli_";
    private static final String SK_PREFIX = "sk_moli_";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成 AccessKey
     */
    public static String generateAccessKey(String userAccount,String SALT) {
        String raw = new StringBuilder()
                .append(SALT)
                .append(userAccount)
                .append(UUID.randomUUID()) // 高质量随机数
                .toString();

        String hash = DigestUtils.md5DigestAsHex(raw.getBytes(StandardCharsets.UTF_8));

        return AK_PREFIX + hash;
    }

    /**
     * 生成 SecretKey（带前缀 + 高强度）
     */
    public static String generateSecretKey() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);

        String key = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        return SK_PREFIX + key;
    }
}
