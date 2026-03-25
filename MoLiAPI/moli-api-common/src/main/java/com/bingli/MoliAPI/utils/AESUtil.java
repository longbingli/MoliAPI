package com.bingli.MoliAPI.utils;

import javax.crypto.Cipher;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
/**
 * AES 加密解密工具类
 */
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128;

    // 16 / 24 / 32 字节
    //放配置
    private static final String AES_KEY = "moli_36687615601";

    /**
     * 加密
     */
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] iv = new byte[12]; // GCM 推荐 12字节
        new java.security.SecureRandom().nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), ALGORITHM);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

        byte[] encrypted = cipher.doFinal(data.getBytes());

        // iv + 密文 一起存
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * 解密
     */
    public static String decrypt(String encryptedData) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[12];
        byte[] cipherText = new byte[decoded.length - 12];

        System.arraycopy(decoded, 0, iv, 0, 12);
        System.arraycopy(decoded, 12, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), ALGORITHM);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

        return new String(cipher.doFinal(cipherText));
    }
}