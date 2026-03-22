package com.bingli.MoliAPI.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bingli.MoliAPI.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.bingli.MoliAPI.model.entity.InterfaceInfo;
import com.bingli.MoliAPI.model.vo.InterfaceInfoVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 接口管理服务
 *
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * 校验数据
     *
     * @param interfaceInfo
     * @param add 对创建的数据进行校验
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);
    
    /**
     * 获取接口管理封装
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    /**
     * 分页获取接口管理封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);
}
