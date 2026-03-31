package com.bingli.MoliAPI.model.vo;

import cn.hutool.json.JSONUtil;
import com.bingli.MoliAPI.model.entity.AppInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 应用管理视图
 *
 */
@Data
public class AppInfoVO implements Serializable {

    /**
     * id
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

    private String gatewayHost;

//    private String host;
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
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param appInfoVO
     * @return
     */
    public static AppInfo voToObj(AppInfoVO appInfoVO) {
        if (appInfoVO == null) {
            return null;
        }
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoVO, appInfo);

        return appInfo;
    }

    /**
     * 对象转封装类
     *
     * @param appInfo
     * @return
     */
    public static AppInfoVO objToVo(AppInfo appInfo) {
        if (appInfo == null) {
            return null;
        }
        AppInfoVO appInfoVO = new AppInfoVO();
        BeanUtils.copyProperties(appInfo, appInfoVO);
        return appInfoVO;
    }
}
