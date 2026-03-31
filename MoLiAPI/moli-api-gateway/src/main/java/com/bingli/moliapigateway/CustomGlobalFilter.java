package com.bingli.moliapigateway;

import com.bingli.MoliAPI.common.BaseResponse;
import com.bingli.MoliAPI.common.ErrorCode;
import com.bingli.MoliAPI.common.ResultUtils;
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
     * 时间戳允许误差：5分钟
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

        // 1. 获取请求头
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");

        // 2. 基础参数校验
        if (StringUtils.isAnyBlank(accessKey, nonce, timestamp, sign)) {
            return writeErrorResponse(response, ErrorCode.PARAMS_ERROR, "请求头参数不完整");
        }

        // 3. 查询调用用户
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

        // 4. nonce 校验
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

        // 5. timestamp 校验
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

        // 6. 验签
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

        // 7. 查询接口信息
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

        // 8. 根据 appId 查询唯一 host
        String interfaceHost;
        try {
            interfaceHost = appInfoDubboService.getAppHostByAppId(interfaceInfo.getAppId());
        } catch (Exception e) {
            log.error("查询下游 host 失败", e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "查询下游端口失败");
        }

        if (StringUtils.isBlank(interfaceHost)) {
            log.error("未查询到下游 host, appId={}", interfaceInfo.getAppId());
            return writeErrorResponse(response, ErrorCode.NOT_FOUND_ERROR, "下游服务不存在或未配置");
        }

        // 9. 构造真正转发地址
        URI targetUri;
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder
                    .fromHttpUrl(interfaceHost)
                    .path(path);

            String rawQuery = request.getURI().getRawQuery();
            if (StringUtils.isNotBlank(rawQuery)) {
                uriBuilder.query(rawQuery);
            }

            targetUri = uriBuilder.build(true).toUri();
        } catch (Exception e) {
            log.error("构造目标 URI 失败, host={}, path={}", interfaceHost, path, e);
            return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "构造目标URI失败");
        }

        log.info("转发目标地址: {}", targetUri);

// 10. 改写 request + 设置网关真实路由地址
        ServerHttpRequest newRequest = request.mutate()
                .uri(targetUri)
                .header(HttpHeaders.HOST, targetUri.getHost() + (targetUri.getPort() > 0 ? ":" + targetUri.getPort() : ""))
                .build();

        ServerWebExchange newExchange = exchange.mutate()
                .request(newRequest)
                .build();

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
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                        return super.writeWith(
                                fluxBody.map(dataBuffer -> {
                                    byte[] content = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(content);
                                    DataBufferUtils.release(dataBuffer);

                                    String responseData = new String(content, StandardCharsets.UTF_8);
                                    log.info("响应结果: {}", responseData);

                                    // 只有响应成功才统计调用次数
                                    if (getStatusCode() == HttpStatus.OK) {
                                        try {
                                            interfaceInfoDubboService.invokeCount(interfaceInfoId, userId);

                                        } catch (Exception e) {
                                            log.error("调用次数统计失败", e);
                                        }
                                    }

                                    return bufferFactory.wrap(content);
                                })
                        );
                    }

                    log.warn("响应体不是 Flux，status={}", getStatusCode());
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        } catch (Exception e) {
            log.error("处理网关响应异常", e);
            return writeErrorResponse(exchange.getResponse(), ErrorCode.SYSTEM_ERROR, "网关处理响应异常");
        }
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
     * 这里 HTTP 状态码固定返回 200，业务状态看 code
     */
    private Mono<Void> writeErrorResponse(ServerHttpResponse response,
                                          ErrorCode errorCode,
                                          String message) {
        return writeJsonResponse(response, HttpStatus.OK, ResultUtils.error(errorCode, message));
    }

    /**
     * 无权限
     */
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        return writeErrorResponse(response, ErrorCode.NO_AUTH_ERROR, "无权限");
    }

    /**
     * 调用异常
     */
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        return writeErrorResponse(response, ErrorCode.SYSTEM_ERROR, "系统内部异常");
    }

    @Override
    public int getOrder() {
        return 10001;
    }
}