package com.bingli.MoliAPI.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.constant.CommonConstant;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.mapper.AppInfoMapper;

import com.bingli.MoliAPI.model.dto.appinfo.AppInfoQueryRequest;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.model.vo.AppInfoVO;
import com.bingli.MoliAPI.model.vo.UserVO;
import com.bingli.MoliAPI.service.AppInfoService;
import com.bingli.MoliAPI.service.UserService;
import com.bingli.MoliAPI.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用管理服务实现
 *
 */
@DubboService
@Service
@Slf4j
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfo> implements AppInfoService {

    @Resource
    private UserService userService;

    @Value("${moli.client.gateway-host}")
    private String gatewayHost;

    /**
     * 校验数据
     *
     * @param appInfo
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validAppInfo(AppInfo appInfo, boolean add) {
        // 1. 判空
        ThrowUtils.throwIf(appInfo == null, ErrorCode.PARAMS_ERROR, "应用信息不能为空");

        String appName = appInfo.getAppName();
        String description = appInfo.getDescription();
        String host = appInfo.getHost();
        Integer deductPoints = appInfo.getDeductPoints();

        // 2. 新增校验（必须字段）
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(host), ErrorCode.PARAMS_ERROR, "接口地址不能为空");
            ThrowUtils.throwIf(deductPoints == null, ErrorCode.PARAMS_ERROR, "扣除积分不能为空");
            ThrowUtils.throwIf(deductPoints < 0, ErrorCode.PARAMS_ERROR, "扣除积分不能小于 0");
        }

        // 3. 通用校验（有值才校验）
        // 应用名称
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 30, ErrorCode.PARAMS_ERROR, "应用名称过长（最多30字符）");
        }

        // 描述
        if (StringUtils.isNotBlank(description)) {
            ThrowUtils.throwIf(description.length() > 200, ErrorCode.PARAMS_ERROR, "描述过长（最多200字符）");
        }

        // host 地址
        if (StringUtils.isNotBlank(host)) {
            ThrowUtils.throwIf(host.length() > 255, ErrorCode.PARAMS_ERROR, "接口地址过长");

            ThrowUtils.throwIf(!isValidUrl(host), ErrorCode.PARAMS_ERROR, "接口地址格式错误");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new java.net.URL(url);
            return true;
        } catch (java.net.MalformedURLException e) {
            return false;
        }
    }
    /**
     * 获取查询条件
     *
     * @param appInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<AppInfo> getQueryWrapper(AppInfoQueryRequest appInfoQueryRequest) {
        QueryWrapper<AppInfo> queryWrapper = new QueryWrapper<>();
        if (appInfoQueryRequest == null) {
            return queryWrapper;
        }
        String appName = appInfoQueryRequest.getAppName();
        String description = appInfoQueryRequest.getDescription();
        Long id = appInfoQueryRequest.getAppId();
        Long notId = appInfoQueryRequest.getNotId();
        String searchText = appInfoQueryRequest.getSearchText();
        String sortField = appInfoQueryRequest.getSortField();
        String sortOrder = appInfoQueryRequest.getSortOrder();
        Long userId = appInfoQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "appId", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "appId", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用管理封装
     *
     * @param appInfo
     * @param request
     * @return
     */
    @Override
    public AppInfoVO getAppInfoVO(AppInfo appInfo, HttpServletRequest request) {
        // 对象转封装类
        AppInfoVO appInfoVO = AppInfoVO.objToVo(appInfo);
        appInfoVO.setGatewayHost(gatewayHost);
        // region 可选
        // 1. 关联查询用户信息
        Long userId = appInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appInfoVO.setUser(userVO);
        // endregion

        return appInfoVO;
    }

    /**
     * 分页获取应用管理封装
     *
     * @param appInfoPage
     * @param request
     * @return
     */
    @Override
    public Page<AppInfoVO> getAppInfoVOPage(Page<AppInfo> appInfoPage, HttpServletRequest request) {
        List<AppInfo> appInfoList = appInfoPage.getRecords();


        Page<AppInfoVO> appInfoVOPage = new Page<>(appInfoPage.getCurrent(), appInfoPage.getSize(), appInfoPage.getTotal());
        if (CollUtil.isEmpty(appInfoList)) {
            return appInfoVOPage;
        }
        // 对象列表 => 封装对象列表
//        List<AppInfoVO> appInfoVOList = appInfoList.stream().map(appInfo -> {
//            return AppInfoVO.objToVo(appInfo);
//        }).collect(Collectors.toList());


        List<AppInfoVO> appInfoVOList = appInfoList.stream().map(appInfo -> {
            AppInfoVO appInfoVO = AppInfoVO.objToVo(appInfo);
            appInfoVO.setGatewayHost(gatewayHost);
            return appInfoVO;
        }).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appInfoList.stream().map(AppInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        appInfoVOList.forEach(appInfoVO -> {
            Long userId = appInfoVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appInfoVO.setUser(userService.getUserVO(user));
        });
        // endregion

        appInfoVOPage.setRecords(appInfoVOList);
        return appInfoVOPage;
    }

}
