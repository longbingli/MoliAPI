# 古诗词查询接口 - 快速启动指南

## 前置要求

1. **Java 17** 或更高版本
2. **Maven 3.6+**
3. **MySQL 8.0+**

## 快速启动步骤

### 1. 初始化数据库

```bash
# 方式一：使用MySQL命令行
mysql -u root -p < database-init.sql

# 方式二：在MySQL客户端中执行
source database-init.sql;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moliapi_interface?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true
    username: root
    password: your_password  # 修改为你的MySQL密码
```

### 3. 启动应用

```bash
# 方式一：使用Maven
mvn spring-boot:run

# 方式二：打包后运行
mvn clean package
java -jar target/moli-api-interface-0.0.1-SNAPSHOT.jar
```

应用将在 `http://localhost:8080` 启动。

### 4. 测试接口

```bash
# 运行测试脚本
.\test-api.ps1

# 或手动测试
curl http://localhost:8080/api/poetry/1
```

## API接口列表

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/poetry/{id}` | GET | 根据ID查询诗词 |
| `/api/poetry/title?title={title}` | GET | 根据标题查询 |
| `/api/poetry/author?author={author}` | GET | 根据作者查询 |
| `/api/poetry/dynasty?dynasty={dynasty}` | GET | 根据朝代查询 |
| `/api/poetry/keyword?keyword={keyword}` | GET | 关键词搜索 |
| `/api/poetry/search` | GET | 高级搜索（支持分页） |

## 常见问题

### 1. 数据库连接失败

- 确认MySQL服务已启动
- 检查用户名和密码是否正确
- 确认数据库 `moliapi_interface` 已创建

### 2. 端口占用

如果8080端口被占用，可以在 `application.yml` 中修改端口：

```yaml
server:
  port: 8081
```

### 3. 编译错误

确保使用正确的MyBatis-Plus版本：

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    <version>3.5.10</version>
</dependency>
```

## 项目结构

```
moli-api-interface/
├── src/main/java/
│   └── com/bingli/moliapiinterface/
│       ├── controller/          # REST API控制器
│       ├── service/             # 业务逻辑层
│       ├── mapper/              # 数据访问层
│       ├── model/               # 数据模型
│       │   ├── entity/          # 实体类
│       │   ├── dto/             # 数据传输对象
│       │   └── vo/              # 视图对象
│       ├── util/                # 工具类
│       └── exception/           # 异常处理
├── src/main/resources/
│   ├── application.yml          # 应用配置
│   └── schema.sql               # 数据库脚本
├── database-init.sql            # 数据库初始化脚本
├── test-api.ps1                 # API测试脚本
└── README.md                    # 项目文档
```

## 技术栈

- **Spring Boot 3.5.12** - 应用框架
- **MyBatis-Plus 3.5.10** - ORM框架
- **MySQL 8.0+** - 数据库
- **Lombok** - 简化代码

## 下一步

1. 添加更多诗词数据
2. 实现诗词收藏功能
3. 添加用户认证和授权
4. 实现诗词推荐算法
5. 添加缓存机制提升性能

## 支持

如有问题，请查看 `README.md` 获取详细文档。
