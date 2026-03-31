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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@DubboService
@Service
@Slf4j
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfo> implements AppInfoService {

    @Resource
    private UserService userService;

    @Value("${moli.client.gateway-host}")
    private String gatewayHost;

    @Override
    public void validAppInfo(AppInfo appInfo, boolean add) {
        ThrowUtils.throwIf(appInfo == null, ErrorCode.PARAMS_ERROR, "应用信息不能为空");

        String appName = appInfo.getAppName();
        String description = appInfo.getDescription();
        String host = appInfo.getHost();
        Integer deductPoints = appInfo.getDeductPoints();

        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(host), ErrorCode.PARAMS_ERROR, "接口地址不能为空");
            ThrowUtils.throwIf(deductPoints == null, ErrorCode.PARAMS_ERROR, "扣除积分不能为空");
            ThrowUtils.throwIf(deductPoints < 0, ErrorCode.PARAMS_ERROR, "扣除积分不能小于 0");
        }

        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 30, ErrorCode.PARAMS_ERROR, "应用名称过长（最多30字符）");
        }

        if (StringUtils.isNotBlank(description)) {
            ThrowUtils.throwIf(description.length() > 200, ErrorCode.PARAMS_ERROR, "描述过长（最多200字符）");
        }

        if (StringUtils.isNotBlank(host)) {
            ThrowUtils.throwIf(host.length() > 255, ErrorCode.PARAMS_ERROR, "接口地址过长");
            ThrowUtils.throwIf(!isValidUrl(host), ErrorCode.PARAMS_ERROR, "接口地址格式错误");
        }
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

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

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("description", searchText));
        }

        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "appId", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "appId", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField);
        return queryWrapper;
    }

    @Override
    public AppInfoVO getAppInfoVO(AppInfo appInfo, HttpServletRequest request) {
        AppInfoVO appInfoVO = AppInfoVO.objToVo(appInfo);
        appInfoVO.setGatewayHost(gatewayHost);

        Long userId = appInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appInfoVO.setUser(userVO);
        return appInfoVO;
    }

    @Override
    public Page<AppInfoVO> getAppInfoVOPage(Page<AppInfo> appInfoPage, HttpServletRequest request) {
        List<AppInfo> appInfoList = appInfoPage.getRecords();
        Page<AppInfoVO> appInfoVOPage = new Page<>(appInfoPage.getCurrent(), appInfoPage.getSize(), appInfoPage.getTotal());
        if (CollUtil.isEmpty(appInfoList)) {
            return appInfoVOPage;
        }

        List<AppInfoVO> appInfoVOList = appInfoList.stream().map(appInfo -> {
            AppInfoVO appInfoVO = AppInfoVO.objToVo(appInfo);
            appInfoVO.setGatewayHost(gatewayHost);
            return appInfoVO;
        }).collect(Collectors.toList());

        Set<Long> userIdSet = appInfoList.stream().map(AppInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        appInfoVOList.forEach(appInfoVO -> {
            Long userId = appInfoVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appInfoVO.setUser(userService.getUserVO(user));
        });

        appInfoVOPage.setRecords(appInfoVOList);
        return appInfoVOPage;
    }
}
