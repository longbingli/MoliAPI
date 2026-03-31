package com.bingli.MoliAPI.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新接口请求
 */
@Data
public class InterfaceInfoUpdateRequest implements Serializable {

    private Long id;

    private Long appId;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 请求示例
     */
    private String requestExample;

    /**
     * 接口响应参数
     */
    private String responseParams;

    /**
     * 返回格式（JSON 等）
     */
    private String returnFormat;

    /**
     * 接口状态（1-关闭，0-开启）
     */
    private Integer status;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 创建人
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
