package com.bingli.MoliAPI.model.dto.appinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 创建应用管理请求
 *
 */
@Data
public class AppInfoAddRequest implements Serializable {

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
     * 调用扣除积分数
     */
    private Integer deductPoints;


    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}