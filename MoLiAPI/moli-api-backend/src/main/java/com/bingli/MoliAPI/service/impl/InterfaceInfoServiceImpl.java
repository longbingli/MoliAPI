package com.bingli.MoliAPI.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.constant.CommonConstant;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.mapper.InterfaceInfoMapper;
import com.bingli.MoliAPI.model.dto.interfaceInfo.InterfaceInfoQueryRequest;
import com.bingli.MoliAPI.model.entity.InterfaceInfo;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.model.vo.InterfaceInfoVO;
import com.bingli.MoliAPI.model.vo.UserVO;
import com.bingli.MoliAPI.service.InterfaceInfoService;
import com.bingli.MoliAPI.service.UserService;
import com.bingli.MoliAPI.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 接口服务实现
 */
@Service
@Slf4j
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    @Resource
    private UserService userService;

    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.PARAMS_ERROR);

        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String requestParams = interfaceInfo.getRequestParams();
        String requestExample = interfaceInfo.getRequestExample();
        String responseParams = interfaceInfo.getResponseParams();
        String returnFormat = interfaceInfo.getReturnFormat();
        String method = interfaceInfo.getMethod();
        Long appId = interfaceInfo.getAppId();

        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "接口名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(url), ErrorCode.PARAMS_ERROR, "接口地址不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(method), ErrorCode.PARAMS_ERROR, "请求方法不能为空");
            ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用信息不能为空");
            ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用信息不能为空");
        }

        if (ObjectUtils.isNotEmpty(appId)) {
            ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用信息不能为空");
        }

        if (StringUtils.isNotBlank(name)) {
            name = name.trim();
            ThrowUtils.throwIf(name.length() > 256, ErrorCode.PARAMS_ERROR, "接口名称过长");
        }

        if (StringUtils.isNotBlank(description)) {
            description = description.trim();
            ThrowUtils.throwIf(description.length() > 256, ErrorCode.PARAMS_ERROR, "接口描述过长");
        }

        if (StringUtils.isNotBlank(url)) {
            url = url.trim();
            ThrowUtils.throwIf(url.length() > 512, ErrorCode.PARAMS_ERROR, "接口地址过长");
            ThrowUtils.throwIf(!url.startsWith("/"), ErrorCode.PARAMS_ERROR, "接口地址必须以 / 开头");
            ThrowUtils.throwIf(url.contains(" "), ErrorCode.PARAMS_ERROR, "接口地址不能包含空格");
        }

        if (StringUtils.isNotBlank(method)) {
            method = method.trim().toUpperCase();
            List<String> methodList = Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH");
            ThrowUtils.throwIf(!methodList.contains(method), ErrorCode.PARAMS_ERROR, "请求方法非法");
        }

        if (StringUtils.isNotBlank(requestHeader)) {
            requestHeader = requestHeader.trim();
            ThrowUtils.throwIf(requestHeader.length() > 65535, ErrorCode.PARAMS_ERROR, "请求头过长");
            ThrowUtils.throwIf(!isValidJson(requestHeader), ErrorCode.PARAMS_ERROR, "请求头必须为合法 JSON");
        }

        if (StringUtils.isNotBlank(responseHeader)) {
            responseHeader = responseHeader.trim();
            ThrowUtils.throwIf(responseHeader.length() > 65535, ErrorCode.PARAMS_ERROR, "响应头过长");
            ThrowUtils.throwIf(!isValidJson(responseHeader), ErrorCode.PARAMS_ERROR, "响应头必须为合法 JSON");
        }

        if (StringUtils.isNotBlank(requestParams)) {
            ThrowUtils.throwIf(requestParams.trim().length() > 255, ErrorCode.PARAMS_ERROR, "请求参数过长");
        }

        if (StringUtils.isNotBlank(requestExample)) {
            ThrowUtils.throwIf(requestExample.trim().length() > 255, ErrorCode.PARAMS_ERROR, "请求示例过长");
        }

        if (StringUtils.isNotBlank(responseParams)) {
            ThrowUtils.throwIf(responseParams.trim().length() > 255, ErrorCode.PARAMS_ERROR, "响应参数过长");
        }

        if (StringUtils.isNotBlank(returnFormat)) {
            ThrowUtils.throwIf(returnFormat.trim().length() > 255, ErrorCode.PARAMS_ERROR, "返回格式过长");
        }
    }

    private boolean isValidJson(String str) {
        try {
            JSONUtil.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        Long id = interfaceInfoQueryRequest.getId();
        Long appId = interfaceInfoQueryRequest.getAppId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        String requestParams = interfaceInfoQueryRequest.getRequestParams();
        String requestExample = interfaceInfoQueryRequest.getRequestExample();
        String responseParams = interfaceInfoQueryRequest.getResponseParams();
        String returnFormat = interfaceInfoQueryRequest.getReturnFormat();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        Long userId = interfaceInfoQueryRequest.getUserId();
        Date createTime = interfaceInfoQueryRequest.getCreateTime();
        Date updateTime = interfaceInfoQueryRequest.getUpdateTime();
        Long notId = interfaceInfoQueryRequest.getNotId();
        String searchText = interfaceInfoQueryRequest.getSearchText();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();

        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }

        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);

        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(requestHeader), "requestHeader", requestHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(responseHeader), "responseHeader", responseHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(requestParams), "requestParams", requestParams);
        queryWrapper.eq(ObjectUtils.isNotEmpty(requestExample), "requestExample", requestExample);
        queryWrapper.eq(ObjectUtils.isNotEmpty(responseParams), "responseParams", responseParams);
        queryWrapper.eq(ObjectUtils.isNotEmpty(returnFormat), "returnFormat", returnFormat);
        queryWrapper.eq(ObjectUtils.isNotEmpty(method), "method", method);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(createTime), "createTime", createTime);
        queryWrapper.eq(ObjectUtils.isNotEmpty(updateTime), "updateTime", updateTime);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField);
        return queryWrapper;
    }

    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);

        Long userId = interfaceInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        interfaceInfoVO.setUser(userVO);
        return interfaceInfoVO;
    }

    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage =
                new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }

        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream()
                .map(InterfaceInfoVO::objToVo)
                .collect(Collectors.toList());

        Set<Long> userIdSet = interfaceInfoList.stream()
                .map(InterfaceInfo::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        interfaceInfoVOList.forEach(interfaceInfoVO -> {
            Long userId = interfaceInfoVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            interfaceInfoVO.setUser(userService.getUserVO(user));
        });

        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }
}
