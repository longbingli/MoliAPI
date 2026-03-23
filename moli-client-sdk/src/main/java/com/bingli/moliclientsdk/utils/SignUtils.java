package com.bingli.moliclientsdk.utils;


import org.springframework.util.DigestUtils;
/**
 * 签名工具类
 */
public class SignUtils {

    /**
     * 获取签名
     *
     * @param body
     * @param secretKey
     * @return 签名
     */
    public static String getSign(String body, String secretKey) {
        if (body == null) {
            body = "";
        }
        String content = body + "." + secretKey;
        return DigestUtils.md5DigestAsHex(content.getBytes());
    }
}
