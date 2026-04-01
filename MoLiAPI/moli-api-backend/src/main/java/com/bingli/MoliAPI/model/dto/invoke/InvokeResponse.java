package com.bingli.MoliAPI.model.dto.invoke;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 通用转发响应结果
 */
@Data
public class InvokeResponse implements Serializable {

    /**
     * 下游状态码
     */
    private Integer statusCode;

    /**
     * 调用耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 下游响应体
     */
    private String body;

    /**
     * 下游响应头
     */
    private Map<String, List<String>> headers;

    private static final long serialVersionUID = 1L;
}
