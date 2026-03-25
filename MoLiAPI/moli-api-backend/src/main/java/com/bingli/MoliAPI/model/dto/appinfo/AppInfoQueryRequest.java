package com.bingli.MoliAPI.model.dto.appinfo;

import com.bingli.MoliAPI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询应用管理请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppInfoQueryRequest extends PageRequest implements Serializable {


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
     * 扣除积分数
     */
    private Integer deductPoints;
    /**
     * 创建用户ID
     */
    private Long userId;




    /**
     * id
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;




    private static final long serialVersionUID = 1L;
}