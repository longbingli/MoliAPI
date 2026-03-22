package com.bingli.MoliAPI.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bingli.MoliAPI.annotation.AuthCheck;
import com.bingli.MoliAPI.common.BaseResponse;
import com.bingli.MoliAPI.common.DeleteRequest;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.common.ResultUtils;
import com.bingli.MoliAPI.constant.UserConstant;
import com.bingli.MoliAPI.exception.BusinessException;
import com.bingli.MoliAPI.exception.ThrowUtils;

import com.bingli.MoliAPI.model.dto.appinfo.AppInfoAddRequest;
import com.bingli.MoliAPI.model.dto.appinfo.AppInfoEditRequest;
import com.bingli.MoliAPI.model.dto.appinfo.AppInfoQueryRequest;
import com.bingli.MoliAPI.model.dto.appinfo.AppInfoUpdateRequest;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.model.vo.AppInfoVO;
import com.bingli.MoliAPI.service.AppInfoService;
import com.bingli.MoliAPI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 应用管理接口
 *
 */
@RestController
@RequestMapping("/appInfo")
@Slf4j
public class AppInfoController {

    @Resource
    private AppInfoService appInfoService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建应用管理
     *
     * @param appInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAppInfo(@RequestBody AppInfoAddRequest appInfoAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appInfoAddRequest == null, ErrorCode.PARAMS_ERROR);
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoAddRequest, appInfo);
        // 数据校验
        appInfoService.validAppInfo(appInfo, true);
        User loginUser = userService.getLoginUser(request);
        appInfo.setUserId(loginUser.getId());
        appInfo.setStatus(1);
        // 写入数据库
        boolean result = appInfoService.save(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newAppInfoId = appInfo.getAppId();
        return ResultUtils.success(newAppInfoId);
    }

    /**
     * 删除应用管理
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAppInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAppInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = appInfoService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新应用管理（仅管理员可用）
     *
     * @param appInfoUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppInfo(@RequestBody AppInfoUpdateRequest appInfoUpdateRequest) {
        if (appInfoUpdateRequest == null || appInfoUpdateRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoUpdateRequest, appInfo);
        // 数据校验
        appInfoService.validAppInfo(appInfo, false);
        // 判断是否存在
        long id = appInfoUpdateRequest.getAppId();
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = appInfoService.updateById(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取应用管理（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppInfoVO> getAppInfoVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        AppInfo appInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(appInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appInfoService.getAppInfoVO(appInfo, request));
    }

    /**
     * 分页获取应用管理列表（仅管理员可用）
     *
     * @param appInfoQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppInfo>> listAppInfoByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest) {
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        // 查询数据库
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        return ResultUtils.success(appInfoPage);
    }

    /**
     * 分页获取应用管理列表（封装类）
     *
     * @param appInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppInfoVO>> listAppInfoVOByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest,
                                                               HttpServletRequest request) {
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        // 获取封装类
        return ResultUtils.success(appInfoService.getAppInfoVOPage(appInfoPage, request));
    }

    /**
     * 分页获取当前登录用户创建的应用管理列表
     *
     * @param appInfoQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppInfoVO>> listMyAppInfoVOByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(appInfoQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        appInfoQueryRequest.setUserId(loginUser.getId());
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        // 获取封装类
        return ResultUtils.success(appInfoService.getAppInfoVOPage(appInfoPage, request));
    }

    /**
     * 编辑应用管理（给用户使用）
     *
     * @param appInfoEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editAppInfo(@RequestBody AppInfoEditRequest appInfoEditRequest, HttpServletRequest request) {
        if (appInfoEditRequest == null || appInfoEditRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoEditRequest, appInfo);
        // 数据校验
        appInfoService.validAppInfo(appInfo, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = appInfoEditRequest.getAppId();
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAppInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = appInfoService.updateById(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
