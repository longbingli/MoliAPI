package com.bingli.MoliAPI.model.dto.invoke;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 通用转发请求参数
 */
@Data
public class InvokeRequest implements Serializable {

    /**
     * 请求方法（GET/POST/PUT/DELETE/PATCH）
     */
    private String method;

    /**
     * 转发路径，例如 /api/poetry/random
     */
    private String path;

    /**
     * 查询参数
     */
    private Map<String, Object> queryParams;

    /**
     * 透传请求头（会自动过滤签名头）
     */
    private Map<String, String> headers;

    /**
     * 原始请求体字符串
     */
    private String body;

    private static final long serialVersionUID = 1L;
}
