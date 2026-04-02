# Molli-API 开发者文档

Molli-API 通过统一网关完成接口鉴权、请求转发、调用统计与积分扣减。你可以直接调用网关，也可以使用平台前端提供的在线调试能力，或者接入项目内置的 Java SDK。

> 当前平台接口状态统一约定为 `0 = 开启`，`1 = 关闭`。只有开启状态的接口才允许被网关转发。

## 快速开始

1. 登录平台，在“令牌管理”页获取自己的 `Access Key` 和 `Secret Key`。
2. 在首页选择应用和接口，确认接口状态、请求路径和调用积分。
3. 按签名规则构造请求头，通过网关地址发起调用。
4. 调用成功后，平台会累计接口调用次数，并按应用配置扣减积分。

## 网关鉴权规则

所有通过网关发起的请求都需要带上以下请求头：

- `accessKey`：当前用户的访问标识。
- `nonce`：随机数，网关会做基础合法性校验。
- `timestamp`：Unix 秒级时间戳，超出 5 分钟会被拒绝。
- `body`：请求体原始字符串，没有请求体时传空字符串。
- `sign`：使用 `body + secretKey` 生成的签名值。

网关校验顺序如下：

1. 校验请求头是否完整。
2. 根据 `accessKey` 查找调用用户。
3. 校验 `nonce` 与 `timestamp`。
4. 用用户的 `secretKey` 重新生成签名并比对。
5. 校验接口是否存在、是否开启。
6. 转发到对应应用的真实下游地址。
7. 下游返回成功后，执行调用次数统计和积分扣减。

## 直接调用网关

网关会根据接口路径自动路由到真实下游服务，所以调用方只需要关心网关地址和业务路径。

```bash
curl --request GET "http://localhost:8090/api/poetry/random" \
  --header "accessKey: your_access_key" \
  --header "nonce: 1234" \
  --header "timestamp: 1711996800" \
  --header "body: " \
  --header "sign: your_sign"
```

如果是 `POST`、`PUT` 这类有请求体的方法，需要保证：

- 请求头里的 `body` 与实际请求体内容一致。
- `sign` 也是基于这个原始请求体计算。

## 在线调试接口

前端在线调试不是直接让浏览器拼签名，而是通过后端统一转发接口：

`POST /api/invoke/forward`

请求结构如下：

```json
{
  "method": "GET",
  "path": "/api/poetry/random",
  "queryParams": {},
  "headers": {
    "Accept": "application/json"
  },
  "body": ""
}
```

字段说明：

- `method`：支持 `GET` / `POST` / `PUT` / `DELETE` / `PATCH`
- `path`：接口路径，例如 `/api/poetry/random`
- `queryParams`：查询参数对象
- `headers`：自定义透传请求头，系统会自动过滤签名相关头
- `body`：原始请求体字符串

返回结构如下：

```json
{
  "code": 0,
  "data": {
    "statusCode": 200,
    "durationMs": 48,
    "body": "{\"title\":\"...\"}",
    "headers": {
      "content-type": [
        "application/json"
      ]
    }
  },
  "message": "ok"
}
```

## Java SDK 调用

项目已经内置了 Java SDK，代码位于：

- `moli-client-sdk`
- `moli-client-demo`

SDK 核心类是 `com.bingli.moliclientsdk.client.MoliClient`，当前封装了统一请求方法：

```java
public String doRequest(String method, String path, Object body)
```

### Maven 依赖

如果你把 SDK 发布到了私服或本地仓库，可以这样引入：

```xml
<dependency>
    <groupId>com.bingli</groupId>
    <artifactId>moli-client-sdk</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Spring Boot 配置方式

SDK 已经提供配置类 `moli.client`，示例配置如下：

```yaml
moli:
  client:
    accessKey: your_access_key
    secretKey: your_secret_key
    gatewayHost: http://localhost:8090
```

### Spring 注入调用示例

下面的例子来自项目里的 `moli-client-demo`：

```java
@RestController
@RequestMapping("/demo")
public class DemoController {

    @Resource
    private MoliClient moliClient;

    @GetMapping("/poetry")
    public String random() {
        return moliClient.doRequest("GET", "/api/poetry/random", null);
    }
}
```

### 直接 new Client 的调用方式

如果你不走 Spring 自动注入，也可以手动创建客户端：

```java
MoliClient client = new MoliClient(
    "your_access_key",
    "your_secret_key",
    "http://localhost:8090"
);

String result = client.doRequest("GET", "/api/poetry/random", null);
System.out.println(result);
```

## 签名示例

项目中签名逻辑的核心用法如下：

```java
String body = "";
String sign = SignUtils.getSign(body, secretKey);
```

SDK 内部会自动补齐这些头：

- `accessKey`
- `nonce`
- `body`
- `timestamp`
- `sign`

也就是说，使用 SDK 时你不需要手动再拼这些鉴权头。

## 积分与调用次数规则

调用经过网关校验并成功转发后，会触发以下逻辑：

1. `interface_info.totalNum + 1`
2. `app_info.totalNum + 1`
3. 当前用户 `points - app_info.deductPoints`

需要注意：

- 只有下游成功响应时，才会统计次数和扣减积分。
- 如果用户积分不足，扣分会失败，调用不会作为成功计费完成。
- 如果接口状态为关闭，网关会直接拒绝，不会进入下游。

## 常见错误码

- `0`：请求成功
- `40000`：请求参数错误
- `40100`：未登录或鉴权失败
- `40300`：请求被拒绝，例如时间戳非法、接口已关闭
- `40400`：接口不存在或下游服务未配置
- `50000`：系统内部错误

## 调试建议

- 先在前端接口详情页使用在线调试，确认接口本身可用。
- 再把同样的路径和参数迁移到 SDK 或自定义客户端。
- 如果出现 `40100` 或 `40300`，优先检查 `accessKey`、`secretKey`、`timestamp`、`nonce`、`sign`。
- 如果调用成功但积分或次数没有变化，优先确认网关是否已经重启到最新版本。
