package com.bingli.MoliAPI.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 接口应用表
 * @TableName app_info
 */
@Data
public class AppInfo  implements Serializable {
    /**
     * 应用ID（主键）
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 总调用次数
     */
    private Integer totalNum;
     /**
     * 调用扣除积分
     */
    private Integer deductPoints;
    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-正常，1-删除)
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}