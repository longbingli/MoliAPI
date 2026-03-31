# 数据库初始化

CREATE DATABASE IF NOT EXISTS MoLiAPI;
USE MoLiAPI;

-- 用户表
CREATE TABLE IF NOT EXISTS `user`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '账号',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '密码',
    `unionId` VARCHAR(256) NULL COMMENT '微信开放平台id',
    `mpOpenId` VARCHAR(256) NULL COMMENT '公众号openId',
    `userName` VARCHAR(256) NULL COMMENT '用户昵称',
    `userAvatar` VARCHAR(1024) NULL COMMENT '用户头像',
    `userProfile` VARCHAR(512) NULL COMMENT '用户简介',
    `userRole` VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_unionId` (`unionId`)
) COMMENT '用户' COLLATE = utf8mb4_unicode_ci;

-- 帖子表
CREATE TABLE IF NOT EXISTS `post`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `title` VARCHAR(512) NULL COMMENT '标题',
    `content` TEXT NULL COMMENT '内容',
    `tags` VARCHAR(1024) NULL COMMENT '标签列表(json数组)',
    `thumbNum` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `favourNum` INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    `userId` BIGINT NOT NULL COMMENT '创建用户id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_userId` (`userId`)
) COMMENT '帖子' COLLATE = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
CREATE TABLE IF NOT EXISTS `post_thumb`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `postId` BIGINT NOT NULL COMMENT '帖子id',
    `userId` BIGINT NOT NULL COMMENT '创建用户id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_postId` (`postId`),
    INDEX `idx_userId` (`userId`)
) COMMENT '帖子点赞';

-- 帖子收藏表（硬删除）
CREATE TABLE IF NOT EXISTS `post_favour`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `postId` BIGINT NOT NULL COMMENT '帖子id',
    `userId` BIGINT NOT NULL COMMENT '创建用户id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_postId` (`postId`),
    INDEX `idx_userId` (`userId`)
) COMMENT '帖子收藏';

-- 接口信息表
CREATE TABLE IF NOT EXISTS `interface_info`
(
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `appId` INT NOT NULL COMMENT 'appId',
    `name` VARCHAR(256) NOT NULL COMMENT '名称',
    `description` VARCHAR(256) NULL DEFAULT NULL COMMENT '描述',
    `url` VARCHAR(512) NOT NULL COMMENT '接口地址',
    `requestHeader` TEXT NULL COMMENT '请求头',
    `responseHeader` TEXT NULL COMMENT '响应头',
    `requestParams` VARCHAR(255) NULL DEFAULT NULL COMMENT '接口请求参数',
    `requestExample` VARCHAR(255) NULL DEFAULT NULL COMMENT '请求示例',
    `responseParams` VARCHAR(255) NULL DEFAULT NULL COMMENT '接口响应参数',
    `returnFormat` VARCHAR(255) NULL DEFAULT NULL COMMENT '返回格式(JSON等等)',
    `status` INT NOT NULL DEFAULT 0 COMMENT '接口状态（1-关闭，0-开启）',
    `method` VARCHAR(256) NOT NULL COMMENT '请求类型',
    `totalNum` INT UNSIGNED NOT NULL COMMENT '调用次数',
    `userId` BIGINT NOT NULL COMMENT '创建人',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口信息' ROW_FORMAT = Dynamic;
