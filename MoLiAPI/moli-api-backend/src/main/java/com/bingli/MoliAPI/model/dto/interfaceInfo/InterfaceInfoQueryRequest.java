package com.bingli.MoliAPI.model.dto.interfaceInfo;

import com.bingli.MoliAPI.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询接口管理请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    private Integer appId;

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
     * 接口状态（0-关闭，1-开启）
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
     * id
     */
    private Long notId;

    /**
     * 搜索词
     */
    private String searchText;




    private static final long serialVersionUID = 1L;
}