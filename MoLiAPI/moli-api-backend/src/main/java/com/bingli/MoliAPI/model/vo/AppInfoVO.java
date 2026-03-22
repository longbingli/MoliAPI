package com.bingli.MoliAPI.model.vo;

import cn.hutool.json.JSONUtil;
import com.bingli.MoliAPI.model.entity.AppInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

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
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
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
     * 标签列表
     */
    private List<String> tagList;

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
