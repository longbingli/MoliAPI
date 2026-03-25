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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 接口管理服务实现
 *
 */
@Service
@Slf4j
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo> implements InterfaceInfoService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param interfaceInfo
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        ThrowUtils.throwIf(interfaceInfo == null, ErrorCode.PARAMS_ERROR);

        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();
        Integer appId = interfaceInfo.getAppId();

        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "接口名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(url), ErrorCode.PARAMS_ERROR, "接口地址不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(method), ErrorCode.PARAMS_ERROR, "请求方法不能为空");
            ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用信息不能为空");
            ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用信息不能为空");
        }
        if (StringUtils.isNotBlank(name)) {
            name = name.trim();
            ThrowUtils.throwIf(name.length() > 50, ErrorCode.PARAMS_ERROR, "接口名称过长");
        }

        if (StringUtils.isNotBlank(description)) {
            description = description.trim();
            ThrowUtils.throwIf(description.length() > 500, ErrorCode.PARAMS_ERROR, "接口描述过长");
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
            ThrowUtils.throwIf(requestHeader.length() > 2048, ErrorCode.PARAMS_ERROR, "请求头过长");
            ThrowUtils.throwIf(!isValidJson(requestHeader), ErrorCode.PARAMS_ERROR, "请求头必须为合法 JSON");
        }

        if (StringUtils.isNotBlank(responseHeader)) {
            responseHeader = responseHeader.trim();
            ThrowUtils.throwIf(responseHeader.length() > 2048, ErrorCode.PARAMS_ERROR, "响应头过长");
            ThrowUtils.throwIf(!isValidJson(responseHeader), ErrorCode.PARAMS_ERROR, "响应头必须为合法 JSON");
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

    /**
     * 获取查询条件
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        if (interfaceInfoQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = interfaceInfoQueryRequest.getId();
        String name = interfaceInfoQueryRequest.getName();
        String description = interfaceInfoQueryRequest.getDescription();
        String requestHeader = interfaceInfoQueryRequest.getRequestHeader();
        String responseHeader = interfaceInfoQueryRequest.getResponseHeader();
        Integer status = interfaceInfoQueryRequest.getStatus();
        String method = interfaceInfoQueryRequest.getMethod();
        Long userId = interfaceInfoQueryRequest.getUserId();
        Date createTime = interfaceInfoQueryRequest.getCreateTime();
        Date updateTime = interfaceInfoQueryRequest.getUpdateTime();
        Long notId = interfaceInfoQueryRequest.getNotId();
        String searchText = interfaceInfoQueryRequest.getSearchText();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();


        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(requestHeader), "requestHeader", requestHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(responseHeader), "responseHeader", responseHeader);
        queryWrapper.eq(ObjectUtils.isNotEmpty(method), "method", method);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取接口管理封装
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    @Override
    public InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request) {
        // 对象转封装类
        InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);


        // region 可选
        // 1. 关联查询用户信息
        Long userId = interfaceInfo.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        interfaceInfoVO.setUser(userVO);
        return interfaceInfoVO;
    }

    /**
     * 分页获取接口管理封装
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    @Override
    public Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request) {
        List<InterfaceInfo> interfaceInfoList = interfaceInfoPage.getRecords();
        Page<InterfaceInfoVO> interfaceInfoVOPage = new Page<>(interfaceInfoPage.getCurrent(), interfaceInfoPage.getSize(), interfaceInfoPage.getTotal());
        if (CollUtil.isEmpty(interfaceInfoList)) {
            return interfaceInfoVOPage;
        }
        // 对象列表 => 封装对象列表
        List<InterfaceInfoVO> interfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            return InterfaceInfoVO.objToVo(interfaceInfo);
        }).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = interfaceInfoList.stream().map(InterfaceInfo::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        interfaceInfoVOList.forEach(interfaceInfoVO -> {
            Long userId = interfaceInfoVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            interfaceInfoVO.setUser(userService.getUserVO(user));
        });
        // endregion

        interfaceInfoVOPage.setRecords(interfaceInfoVOList);
        return interfaceInfoVOPage;
    }

}
