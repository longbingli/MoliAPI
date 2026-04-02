# Molli-API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-API%20Platform-6db33f)
![React](https://img.shields.io/badge/React-Frontend-61dafb)
![License](https://img.shields.io/badge/License-Learning-blue)

Molli-API 是一个面向开发者的 **API 开放平台**，提供从 **接口发布、网关鉴权、在线调试、调用统计、积分计费，到 SDK 接入** 的完整能力。

## 功能特性

- 接口管理：统一维护接口路径、请求方式、参数说明与接口状态
- 应用管理：按应用维度配置下游服务地址、调用积分与统计信息
- 网关鉴权：基于 `AccessKey / SecretKey` 的签名认证机制
- 在线调试：支持前端直接调试接口并查看返回结果
- 调用统计：自动统计接口与应用调用次数
- 积分计费：按应用配置扣减用户积分
- Java SDK：提供 `moli-client-sdk` 快速接入平台接口
- 开发者文档：前端内置开发者文档与调用说明

## 项目架构

```mermaid
flowchart LR
    U["开发者 / 前端用户"]

    subgraph 前端层
        W["moli-api-web<br/>前端管理后台"]
    end

    subgraph 接入层
        G["moli-api-gateway<br/>API 网关<br/>鉴权 / 限流 / 日志"]
    end

    subgraph 平台核心层
        B["moli-api-backend<br/>接口管理 / 用户 / 计费"]
        C["moli-api-common<br/>公共模型 & Dubbo 接口"]
    end

    subgraph 数据层
        D["MySQL"]
        R["Redis"]
    end

    subgraph 业务服务层
        S["下游接口服务（天气 / AI / 数据）"]
    end

    subgraph SDK层
        SDK["moli-client-sdk<br/>Java SDK"]
    end

    %% 用户访问
    U --> W
    U --> SDK

    %% 前端调用后端
    W --> B

    %% 后端依赖
    B --> C
    B --> D
    B --> R

    %% 核心调用链（重点！）
    SDK --> G
    G --> S

    %% 网关校验
    G --> C

    %% 可选：网关统计
    G --> R
```

## 项目结构

- `MoLiAPI/moli-api-backend`：开放平台核心后端
- `MoLiAPI/moli-api-gateway`：API 网关
- `MoLiAPI/moli-api-common`：公共模块
- `moli-api-web/api-web`：前端开发者平台
- `moli-client-sdk`：Java SDK
- `moli-client-demo`：SDK 示例项目





![Snipaste_2026-04-02_15-23-49](README.assets/Snipaste_2026-04-02_15-23-49.png)

![Snipaste_2026-04-02_15-24-28](README.assets/Snipaste_2026-04-02_15-24-28.png)   ![Snipaste_2026-04-02_15-24-48](README.assets/Snipaste_2026-04-02_15-24-48.png)

## 快速启动

### 1. 启动基础环境

- 准备 MySQL
- 准备 Redis
- 导入项目 SQL 初始化脚本

### 2. 启动后端与网关

```bash
cd MoLiAPI
mvn clean install
```

分别启动：

- `moli-api-backend`
- `moli-api-gateway`

### 3. 启动前端

```bash
cd moli-api-web/api-web
npm install
npm run dev
```

### 4. 启动 SDK 示例项目

```bash
cd moli-client-demo
mvn spring-boot:run
```

## 技术栈

### 后端

- Java 17
- Spring Boot
- Spring Cloud Gateway
- MyBatis-Plus
- Dubbo
- MySQL
- Redis

### 前端

- React
- Umi Max
- TypeScript
- Ant Design
- Ant Design Pro Components



## License

This project is for learning and secondary development.
