package com.bingli.MoliAPI.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.Method;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.bingli.MoliAPI.common.BaseResponse;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.common.ResultUtils;
import com.bingli.MoliAPI.exception.ThrowUtils;
import com.bingli.MoliAPI.model.dto.invoke.InvokeRequest;
import com.bingli.MoliAPI.model.dto.invoke.InvokeResponse;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.service.UserService;
import com.bingli.MoliAPI.utils.AESUtil;
import com.bingli.moliclientsdk.utils.SignUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/invoke")
public class DemoController {

    private static final int NONCE_LENGTH = 4;

    @Resource
    private UserService userService;

    @Value("${moli.client.gateway-host}")
    private String gatewayHost;

    /**
     * 通用在线调试转发
     */
    @PostMapping("/forward")
    public BaseResponse<InvokeResponse> forward(@RequestBody InvokeRequest invokeRequest,
                                                HttpServletRequest request) throws Exception {
        ThrowUtils.throwIf(invokeRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        String method = StringUtils.upperCase(StringUtils.trimToEmpty(invokeRequest.getMethod()));
        String path = StringUtils.trimToEmpty(invokeRequest.getPath());
        ThrowUtils.throwIf(StringUtils.isAnyBlank(method, path), ErrorCode.PARAMS_ERROR, "请求方法和路径不能为空");

        boolean methodSupported = "GET".equals(method)
                || "POST".equals(method)
                || "PUT".equals(method)
                || "DELETE".equals(method)
                || "PATCH".equals(method);
        ThrowUtils.throwIf(!methodSupported, ErrorCode.PARAMS_ERROR, "暂不支持该请求方法");

        User user = userService.getLoginUser(request);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);

        String accessKey = user.getAccessKey();
        String secretKey = AESUtil.decrypt(user.getSecretKey());
        String normalizedPath = path.startsWith("/") ? path : "/" + path;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(gatewayHost)
                .path(normalizedPath);

        Map<String, Object> queryParams = invokeRequest.getQueryParams();
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (StringUtils.isNotBlank(key) && value != null) {
                    uriBuilder.queryParam(key, value);
                }
            });
        }

        String url = uriBuilder.build(true).toUriString();
        String body = StringUtils.defaultString(invokeRequest.getBody());

        HttpRequest downstreamRequest = HttpRequest.of(url)
                .method(Method.valueOf(method))
                .header("accessKey", accessKey)
                .header("nonce", RandomUtil.randomNumbers(NONCE_LENGTH))
                .header("timestamp", String.valueOf(System.currentTimeMillis() / 1000))
                .header("body", body)
                .header("sign", SignUtils.getSign(body, secretKey));

        // 透传用户设置的请求头，避免覆盖签名相关头
        Map<String, String> customHeaders = invokeRequest.getHeaders();
        if (customHeaders != null) {
            customHeaders.forEach((key, value) -> {
                if (StringUtils.isAnyBlank(key, value)) {
                    return;
                }
                if ("accessKey".equalsIgnoreCase(key)
                        || "nonce".equalsIgnoreCase(key)
                        || "timestamp".equalsIgnoreCase(key)
                        || "sign".equalsIgnoreCase(key)
                        || "body".equalsIgnoreCase(key)) {
                    return;
                }
                downstreamRequest.header(key, value);
            });
        }

        if (!"GET".equals(method) && !"DELETE".equals(method) && StringUtils.isNotBlank(body)) {
            downstreamRequest.body(body);
        }

        long start = System.currentTimeMillis();
        HttpResponse downstreamResponse = downstreamRequest.execute();
        long end = System.currentTimeMillis();

        InvokeResponse invokeResponse = new InvokeResponse();
        invokeResponse.setStatusCode(downstreamResponse.getStatus());
        invokeResponse.setDurationMs(end - start);
        invokeResponse.setBody(downstreamResponse.body());
        invokeResponse.setHeaders(downstreamResponse.headers());

        return ResultUtils.success(invokeResponse);
    }
}
