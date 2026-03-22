package com.bingli.MoliAPI.model.dto.appinfo;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新应用管理请求
 *
 */
@Data
public class AppInfoUpdateRequest implements Serializable {

    /**
     * 应用ID（主键）
     */
    private Long appId;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用描述
     */
    private String description;

    /**
     * 接口主机地址（网关地址）
     */
    private String host;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;



    private static final long serialVersionUID = 1L;
}