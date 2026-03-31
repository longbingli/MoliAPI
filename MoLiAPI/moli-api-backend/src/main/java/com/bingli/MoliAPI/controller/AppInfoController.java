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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/appInfo")
@Slf4j
public class AppInfoController {

    @Resource
    private AppInfoService appInfoService;

    @Resource
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addAppInfo(@RequestBody AppInfoAddRequest appInfoAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appInfoAddRequest == null, ErrorCode.PARAMS_ERROR);
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoAddRequest, appInfo);
        appInfoService.validAppInfo(appInfo, true);

        User loginUser = userService.getLoginUser(request);
        appInfo.setUserId(loginUser.getId());
        appInfo.setTotalNum(0);
        appInfo.setStatus(0);
        appInfo.setIsDelete(0);

        boolean result = appInfoService.save(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(appInfo.getAppId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAppInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldAppInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appInfoService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppInfo(@RequestBody AppInfoUpdateRequest appInfoUpdateRequest) {
        if (appInfoUpdateRequest == null || appInfoUpdateRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoUpdateRequest, appInfo);
        appInfoService.validAppInfo(appInfo, false);

        long id = appInfoUpdateRequest.getAppId();
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = appInfoService.updateById(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/get/vo")
    public BaseResponse<AppInfoVO> getAppInfoVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        AppInfo appInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(appInfo == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appInfoService.getAppInfoVO(appInfo, request));
    }

    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppInfo>> listAppInfoByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest) {
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        return ResultUtils.success(appInfoPage);
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppInfoVO>> listAppInfoVOByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest,
                                                              HttpServletRequest request) {
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        return ResultUtils.success(appInfoService.getAppInfoVOPage(appInfoPage, request));
    }

    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppInfoVO>> listMyAppInfoVOByPage(@RequestBody AppInfoQueryRequest appInfoQueryRequest,
                                                                HttpServletRequest request) {
        ThrowUtils.throwIf(appInfoQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        appInfoQueryRequest.setUserId(loginUser.getId());
        long current = appInfoQueryRequest.getCurrent();
        long size = appInfoQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<AppInfo> appInfoPage = appInfoService.page(new Page<>(current, size),
                appInfoService.getQueryWrapper(appInfoQueryRequest));
        return ResultUtils.success(appInfoService.getAppInfoVOPage(appInfoPage, request));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editAppInfo(@RequestBody AppInfoEditRequest appInfoEditRequest, HttpServletRequest request) {
        if (appInfoEditRequest == null || appInfoEditRequest.getAppId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        AppInfo appInfo = new AppInfo();
        BeanUtils.copyProperties(appInfoEditRequest, appInfo);
        appInfoService.validAppInfo(appInfo, false);
        User loginUser = userService.getLoginUser(request);

        long id = appInfoEditRequest.getAppId();
        AppInfo oldAppInfo = appInfoService.getById(id);
        ThrowUtils.throwIf(oldAppInfo == null, ErrorCode.NOT_FOUND_ERROR);
        if (!oldAppInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = appInfoService.updateById(appInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
