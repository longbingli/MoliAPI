# 古诗词查询接口

## 项目简介

基于Spring Boot 3.5.12和MyBatis-Plus的古诗词查询RESTful API接口，支持多种查询方式。

## 技术栈

- Java 17
- Spring Boot 3.5.12
- MyBatis-Plus 3.5.9
- MySQL 8.0+
- Lombok

## 数据库配置

### 1. 创建数据库

执行 `src/main/resources/schema.sql` 文件创建数据库和表结构：

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 2. 配置数据库连接

修改 `src/main/resources/application.yml` 文件中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moliapi_interface?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

## 启动应用

### 方式一：使用Maven

```bash
mvn spring-boot:run
```

### 方式二：打包后运行

```bash
mvn clean package
java -jar target/moli-api-interface-0.0.1-SNAPSHOT.jar
```

应用将在 `http://localhost:8080` 启动。

## API接口文档

### 统一响应格式

所有接口返回统一的JSON格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 接口列表

#### 1. 根据ID查询诗词

**请求**
```
GET /api/poetry/{id}
```

**示例**
```bash
curl http://localhost:8080/api/poetry/1
```

**响应**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "title": "静夜思",
    "author": "李白",
    "dynasty": "唐",
    "content": "床前明月光，疑是地上霜。举头望明月，低头思故乡。",
    "tags": "思乡,月亮",
    "createTime": "2024-01-01T00:00:00"
  }
}
```

#### 2. 根据标题查询

**请求**
```
GET /api/poetry/title?title={title}
```

**示例**
```bash
curl "http://localhost:8080/api/poetry/title?title=静夜思"
```

#### 3. 根据作者查询

**请求**
```
GET /api/poetry/author?author={author}
```

**示例**
```bash
curl "http://localhost:8080/api/poetry/author?author=李白"
```

#### 4. 根据朝代查询

**请求**
```
GET /api/poetry/dynasty?dynasty={dynasty}
```

**示例**
```bash
curl "http://localhost:8080/api/poetry/dynasty?dynasty=唐"
```

#### 5. 关键词搜索

**请求**
```
GET /api/poetry/keyword?keyword={keyword}
```

**说明**
- 在标题、作者、内容中搜索包含关键词的诗词

**示例**
```bash
curl "http://localhost:8080/api/poetry/keyword?keyword=李白"
```

#### 6. 高级搜索（支持分页）

**请求**
```
GET /api/poetry/search?title={title}&author={author}&dynasty={dynasty}&current={current}&pageSize={pageSize}
```

**参数说明**
- `title`: 诗词标题（可选）
- `author`: 作者（可选）
- `dynasty`: 朝代（可选）
- `current`: 当前页码，默认1
- `pageSize`: 每页大小，默认10

**示例**
```bash
curl "http://localhost:8080/api/poetry/search?dynasty=唐&current=1&pageSize=5"
```

**响应**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "records": [...],
    "total": 8,
    "current": 1,
    "pageSize": 5,
    "totalPages": 2
  }
}
```

## 错误处理

接口返回的错误码说明：

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 数据不存在 |
| 500 | 系统错误 |

## 运行测试

```bash
mvn test
```

## 项目结构

```
src/main/java/com/bingli/moliapiinterface/
├── controller/          # 控制器层
│   └── PoetryController.java
├── service/            # 服务层
│   ├── PoetryService.java
│   └── impl/
│       └── PoetryServiceImpl.java
├── mapper/             # 数据访问层
│   └── PoetryMapper.java
├── model/              # 模型类
│   ├── entity/
│   │   └── Poetry.java
│   ├── dto/
│   │   ├── PoetryQuery.java
│   │   └── PoetryResponse.java
│   └── vo/
│       └── PageResponse.java
├── util/               # 工具类
│   ├── Result.java
│   ├── ResponseEnum.java
│   └── ResultUtils.java
└── exception/          # 异常处理
    ├── BusinessException.java
    └── GlobalExceptionHandler.java
```

## 数据库表结构

### poetry 表

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| title | VARCHAR(200) | 诗词标题 |
| author | VARCHAR(100) | 作者 |
| dynasty | VARCHAR(50) | 朝代 |
| content | TEXT | 诗词内容 |
| tags | VARCHAR(200) | 标签 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| is_delete | TINYINT | 逻辑删除标记 |

## 注意事项

1. 确保MySQL服务已启动
2. 数据库连接配置正确
3. 端口8080未被占用
4. Java版本为17或以上
