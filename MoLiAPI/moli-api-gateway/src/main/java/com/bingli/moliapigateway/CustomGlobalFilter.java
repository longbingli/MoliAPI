package com.bingli.moliapigateway;

import com.bingli.MoliAPI.common.BaseResponse;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.common.ResultUtils;
import com.bingli.MoliAPI.model.entity.AppInfo;
import com.bingli.MoliAPI.model.entity.InterfaceInfo;
import com.bingli.MoliAPI.model.entity.User;
import com.bingli.MoliAPI.service.AppInfoDubboService;
import com.bingli.MoliAPI.service.InterfaceInfoDubboService;
import com.bingli.MoliAPI.service.UserDubboService;
import com.bingli.MoliAPI.utils.AESUtil;
import com.bingli.moliclientsdk.utils.SignUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 时间戳允许误差：5 分钟
     */
    private static final long FIVE_MINUTES = 60 * 5L;

    /**
     * nonce 最大值
     */
    private static final long MAX_NONCE = 10000L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DubboReference
    private UserDubboService userDubboService;

    @DubboReference
    private InterfaceInfoDubboService interfaceInfoDubboService;

    @DubboReference
    private AppInfoDubboService appInfoDubboService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getPath().value();
        String method = request.getMethod() == null ? "" : request.getMethod().name();

        log.info("请求唯一标识: {}", request.getId());
        log.info("请求路径: {}", path);
        log.info("请求方法: {}", method);
        log.info("请求参数: {}", request.getQueryParams());
        log.info("请求来源地址: {}", request.getRemoteAddress());

        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");

        if (StringUtils.isAnyBlank(accessKey, nonce, timestamp, sign)) {
            return writeErrorResponse(response, ErrorCode.PARAMS_ERROR, "请求头参数不完整");
        }

        User invokeUser;
        try {
            invokeUser = userDubboService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("查询调用用户失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "查询调用用户失败");
        }

        if (invokeUser == null) {
            return writeErrorResponse(response, ErrorCode.NO_AUTH_ERROR, "用户不存在");
        }

        try {
            long nonceValue = Long.parseLong(nonce);
            if (nonceValue > MAX_NONCE) {
                log.error("nonce 非法: {}", nonce);
                return writeErrorResponse(response, ErrorCode.PARAMS_ERROR, "nonce 非法");
            }
        } catch (Exception e) {
            log.error("nonce 格式错误: {}", nonce, e);
            return writeErrorResponse(response, ErrorCode.PARAMS_ERROR, "nonce 格式错误");
        }

        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis() / 1000;
            if (Math.abs(currentTime - requestTime) > FIVE_MINUTES) {
                log.error("timestamp 超出允许范围, requestTime={}, currentTime={}", requestTime, currentTime);
                return writeErrorResponse(response, ErrorCode.FORBIDDEN_ERROR, "非法请求");
            }
        } catch (Exception e) {
            log.error("timestamp 格式错误: {}", timestamp, e);
            return writeErrorResponse(response, ErrorCode.PARAMS_ERROR, "timestamp 格式错误");
        }

        String secretKey;
        try {
            secretKey = AESUtil.decrypt(invokeUser.getSecretKey());
        } catch (Exception e) {
            log.error("secretKey 解密失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "secretKey 解密失败");
        }

        String serverSign;
        try {
            serverSign = SignUtils.getSign(body == null ? "" : body, secretKey);
        } catch (Exception e) {
            log.error("服务端签名生成失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "服务端签名生成失败");
        }

        if (!sign.equals(serverSign)) {
            log.error("签名不匹配, clientSign={}, serverSign={}", sign, serverSign);
            return writeErrorResponse(response, ErrorCode.NO_AUTH_ERROR, "密钥非法");
        }

        InterfaceInfo interfaceInfo;
        try {
            interfaceInfo = interfaceInfoDubboService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("查询接口信息失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "查询接口信息失败");
        }

        if (interfaceInfo == null) {
            log.error("接口不存在, path={}, method={}", path, method);
            return writeErrorResponse(response, ErrorCode.NOT_FOUND_ERROR, "接口不存在或已关闭");
        }
        if (interfaceInfo.getStatus() != null && interfaceInfo.getStatus() == 1) {
            log.warn("接口已关闭, interfaceId={}, path={}, method={}", interfaceInfo.getId(), path, method);
            return writeErrorResponse(response, ErrorCode.FORBIDDEN_ERROR, "接口已关闭");
        }

        AppInfo appInfo;
        try {
            appInfo = appInfoDubboService.getAppInfoByAppId(interfaceInfo.getAppId());
        } catch (Exception e) {
            log.error("查询应用信息失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "查询应用信息失败");
        }

        if (!hasEnoughPoints(invokeUser, appInfo)) {
            log.warn("用户积分不足，拒绝调用, userId={}, interfaceId={}, appId={}, userPoints={}, deductPoints={}",
                    invokeUser.getId(),
                    interfaceInfo.getId(),
                    interfaceInfo.getAppId(),
                    invokeUser.getPoints(),
                    appInfo == null ? null : appInfo.getDeductPoints());
            return writeErrorResponse(response, ErrorCode.OPERATION_ERROR, "积分不足");
        }

        String interfaceHost = appInfo == null ? null : appInfo.getHost();
        if (StringUtils.isBlank(interfaceHost)) {
            log.error("未查询到下游 host, appId={}", interfaceInfo.getAppId());
            return writeErrorResponse(response, ErrorCode.NOT_FOUND_ERROR, "下游服务不存在或未配置");
        }

        String normalizedHost = normalizeHost(interfaceHost);
        if (StringUtils.isBlank(normalizedHost)) {
            log.error("下游 host 非法, rawHost='{}', appId={}", interfaceHost, interfaceInfo.getAppId());
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "下游 host 配置非法");
        }

        URI targetUri;
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(normalizedHost).path(path);
            String rawQuery = request.getURI().getRawQuery();
            if (StringUtils.isNotBlank(rawQuery)) {
                uriBuilder.query(rawQuery);
            }
            targetUri = uriBuilder.build(true).toUri();
        } catch (Exception e) {
            log.error("构造目标 URI 失败, host={}, path={}", interfaceHost, path, e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "构造目标 URI 失败");
        }

        log.info("转发目标地址: {}", targetUri);

        ServerHttpRequest newRequest = request.mutate()
                .uri(targetUri)
                .header(HttpHeaders.HOST, targetUri.getHost() + (targetUri.getPort() > 0 ? ":" + targetUri.getPort() : ""))
                .build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        newExchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, targetUri);

        return handleResponse(newExchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    /**
     * 处理响应
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange,
                                     GatewayFilterChain chain,
                                     long interfaceInfoId,
                                     long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body == null) {
                        return super.writeWith(body);
                    }
                    return DataBufferUtils.join(Flux.from(body)).flatMap(dataBuffer -> {
                        byte[] content = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(content);
                        DataBufferUtils.release(dataBuffer);

                        String responseData = new String(content, StandardCharsets.UTF_8);
                        log.info("响应结果: {}", responseData);
                        return super.writeWith(Mono.just(bufferFactory.wrap(content)));
                    });
                }

                @Override
                public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
                    // 兼容分块响应，统一转换成单流处理
                    return writeWith(Flux.from(body).flatMapSequential(publisher -> publisher));
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build())
                    .then(Mono.fromRunnable(() -> recordInvokeSuccess(originalResponse, interfaceInfoId, userId)));
        } catch (Exception e) {
            log.error("处理网关响应异常", e);
            return writeErrorResponse(exchange.getResponse(), ErrorCode.SYSTEM_ERROR, "网关处理响应异常");
        }
    }

    /**
     * 仅在转发成功后统计次数和扣减积分
     */
    private void recordInvokeSuccess(ServerHttpResponse response, long interfaceInfoId, long userId) {
        HttpStatusCode currentStatus = response.getStatusCode();
        boolean success = currentStatus == null || currentStatus.is2xxSuccessful();
        if (!success) {
            log.warn("调用未成功，不统计次数和积分, interfaceInfoId={}, userId={}, status={}",
                    interfaceInfoId, userId, currentStatus);
            return;
        }
        try {
            interfaceInfoDubboService.invokeCount(interfaceInfoId, userId);
            log.info("调用统计成功, interfaceInfoId={}, userId={}", interfaceInfoId, userId);
        } catch (Exception e) {
            log.error("调用次数统计或扣分失败, interfaceInfoId={}, userId={}", interfaceInfoId, userId, e);
        }
    }

    /**
     * 转发前先校验积分，避免积分不足时下游接口已经被真正调用
     */
    private boolean hasEnoughPoints(User invokeUser, AppInfo appInfo) {
        if (invokeUser == null || appInfo == null) {
            return false;
        }
        Integer deductPoints = appInfo.getDeductPoints();
        if (deductPoints == null || deductPoints <= 0) {
            return true;
        }
        Integer userPoints = invokeUser.getPoints();
        return userPoints != null && userPoints >= deductPoints;
    }

    /**
     * 统一写出 JSON 响应
     */
    private Mono<Void> writeJsonResponse(ServerHttpResponse response,
                                         HttpStatus httpStatus,
                                         BaseResponse<?> result) {
        try {
            response.setStatusCode(httpStatus);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            response.getHeaders().set("Charset", StandardCharsets.UTF_8.name());

            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            log.error("写出统一返回结果失败", e);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    /**
     * 统一错误返回
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response,
                                          ErrorCode errorCode,
                                          String message) {
        return writeJsonResponse(response, HttpStatus.OK, ResultUtils.error(errorCode, message));
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        return writeErrorResponse(response, ErrorCode.NO_AUTH_ERROR, "无权限");
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "系统内部异常");
    }

    /**
     * 统一清洗 host，避免前后空白或控制字符导致 URI 构造失败
     */
    private String normalizeHost(String rawHost) {
        String host = StringUtils.trimToEmpty(rawHost);
        host = host.replaceAll("\\p{Cntrl}", "");
        host = StringUtils.trimToEmpty(host);
        if (StringUtils.isBlank(host)) {
            return host;
        }
        if (!StringUtils.startsWithIgnoreCase(host, "http://")
                && !StringUtils.startsWithIgnoreCase(host, "https://")) {
            host = "http://" + host;
        }
        return StringUtils.removeEnd(host, "/");
    }

    @Override
    public int getOrder() {
        return 10001;
    }
}
