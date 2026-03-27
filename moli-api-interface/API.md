# 随机诗词接口文档

## 接口概述

| 属性 | 值 |
|------|-----|
| 接口名称 | 获取随机诗词 |
| 接口地址 | `/api/poetry/random` |
| 请求方法 | GET |
| 认证方式 | 无需认证 |

## 请求参数

无需任何请求参数。

## 请求示例

```http
GET /api/poetry/random HTTP/1.1
Host: localhost:8095
```

```bash
curl -X GET http://localhost:8095/api/poetry/random
```

## 响应格式

### 成功响应

**HTTP状态码:** 200

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "title": "静夜思",
    "author": "李白",
    "dynasty": "唐",
    "content": "床前明月光，疑是地上霜。\n举头望明月，低头思故乡。",
    "translation": "明亮的月光洒在床前的窗户纸上，好像地上泛起了一层霜。",
    "appreciation": "这是一首描写游子思乡的名作。",
    "tags": "思乡,月亮,唐诗"
  }
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 响应状态码 |
| message | string | 响应消息 |
| data | object | 诗词数据对象 |
| data.id | long | 诗词ID |
| data.title | string | 诗词标题 |
| data.author | string | 作者姓名 |
| data.dynasty | string | 所属朝代 |
| data.content | string | 诗词正文内容 |
| data.translation | string | 诗词译文 |
| data.appreciation | string | 诗词赏析 |
| data.tags | string | 诗词标签（逗号分隔） |

## 错误响应

### 数据不存在

**HTTP状态码:** 404

```json
{
  "code": 404,
  "message": "数据不存在",
  "data": null
}
```

### 系统错误

**HTTP状态码:** 500

```json
{
  "code": 500,
  "message": "系统错误",
  "data": null
}
```

## 错误码说明

| 错误码 | HTTP状态码 | 说明 |
|--------|------------|------|
| 200 | 200 | 请求成功 |
| 404 | 404 | 数据不存在（数据库中无有效诗词数据） |
| 500 | 500 | 系统内部错误 |

## 使用说明

1. 该接口每次调用返回一条随机诗词
2. 返回的诗词内容包含完整的标题、作者、朝代、正文等信息
3. 适合用于诗词展示、每日一句等场景
4. 无需任何参数，直接调用即可

## 注意事项

- 接口返回的诗词为随机选取，多次调用可能返回不同结果
- 如数据库中无有效诗词数据，将返回404错误
- 建议对接口进行适当的缓存处理以提升性能
