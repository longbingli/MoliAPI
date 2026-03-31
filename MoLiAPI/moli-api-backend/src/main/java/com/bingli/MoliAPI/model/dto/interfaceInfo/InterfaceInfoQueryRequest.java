package com.bingli.MoliAPI.model.dto.interfaceInfo;

import com.bingli.MoliAPI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询接口请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 应用 id
     */
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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 排除 id
     */
    private Long notId;

    /**
     * 关键词搜索
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}
