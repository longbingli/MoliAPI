package com.bingli.MoliAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bingli.MoliAPI.model.dto.appinfo.AppInfoQueryRequest;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.model.vo.AppInfoVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 应用管理服务
 *
 */
public interface AppInfoService extends IService<AppInfo> {

    /**
     * 校验数据
     *
     * @param appInfo
     * @param add 对创建的数据进行校验
     */
    void validAppInfo(AppInfo appInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param appInfoQueryRequest
     * @return
     */
    QueryWrapper<AppInfo> getQueryWrapper(AppInfoQueryRequest appInfoQueryRequest);
    
    /**
     * 获取应用管理封装
     *
     * @param appInfo
     * @param request
     * @return
     */
    AppInfoVO getAppInfoVO(AppInfo appInfo, HttpServletRequest request);

    /**
     * 分页获取应用管理封装
     *
     * @param appInfoPage
     * @param request
     * @return
     */
    Page<AppInfoVO> getAppInfoVOPage(Page<AppInfo> appInfoPage, HttpServletRequest request);
}
