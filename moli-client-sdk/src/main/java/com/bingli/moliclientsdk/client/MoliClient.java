package com.bingli.moliclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.bingli.moliclientsdk.utils.SignUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoliClient {

    private String accessKey;
    private String secretKey;
    private String gatewayHost;

    /**
     * 随机数长度
     */
    private static final int NONCE_LENGTH = 4;

    /**
     * 构建请求头
     */
    private Map<String, String> buildHeaders(String body) {
        String safeBody = body == null ? "" : body;

        Map<String, String> headers = new HashMap<>(8);
        headers.put("accessKey", accessKey);
        headers.put("nonce", RandomUtil.randomNumbers(NONCE_LENGTH));
        headers.put("body", safeBody);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        headers.put("sign", SignUtils.getSign(safeBody, secretKey));

        return headers;
    }

    /**
     * 通用请求方法（支持 GET / POST / PUT / DELETE）
     */
    public String doRequest(String method, String path, Object body) {

        if (gatewayHost == null || gatewayHost.isEmpty()) {
            throw new RuntimeException("gatewayHost 未配置！");
        }

        String url = gatewayHost + path;
        String json = body == null ? "" : JSONUtil.toJsonStr(body);

        HttpRequest request;

        switch (method.toUpperCase()) {
            case "GET":
                request = HttpRequest.get(url);
                break;
            case "POST":
                request = HttpRequest.post(url).body(json);
                break;
            case "PUT":
                request = HttpRequest.put(url).body(json);
                break;
            case "DELETE":
                request = HttpRequest.delete(url);
                break;
            default:
                throw new IllegalArgumentException("不支持的请求方法：" + method);
        }

        HttpResponse response = request
                .addHeaders(buildHeaders(json))
                .execute();

        return response.body();
    }
}