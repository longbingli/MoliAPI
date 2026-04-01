# 鏁版嵁搴撳垵濮嬪寲

CREATE DATABASE IF NOT EXISTS MoLiAPI;
USE MoLiAPI;

-- 鐢ㄦ埛琛?CREATE TABLE IF NOT EXISTS `user`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `userAccount` VARCHAR(256) NOT NULL COMMENT '璐﹀彿',
    `userPassword` VARCHAR(512) NOT NULL COMMENT '瀵嗙爜',
    `unionId` VARCHAR(256) NULL COMMENT '寰俊寮€鏀惧钩鍙癷d',
    `mpOpenId` VARCHAR(256) NULL COMMENT '鍏紬鍙穙penId',
    `userName` VARCHAR(256) NULL COMMENT '鐢ㄦ埛鏄电О',
    `userAvatar` VARCHAR(1024) NULL COMMENT '鐢ㄦ埛澶村儚',
    `userProfile` VARCHAR(512) NULL COMMENT '鐢ㄦ埛绠€浠?,
    `userRole` VARCHAR(256) NOT NULL DEFAULT 'user' COMMENT '鐢ㄦ埛瑙掕壊锛歶ser/admin/ban',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    INDEX `idx_unionId` (`unionId`)
) COMMENT '鐢ㄦ埛' COLLATE = utf8mb4_unicode_ci;

-- 甯栧瓙琛?CREATE TABLE IF NOT EXISTS `post`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `title` VARCHAR(512) NULL COMMENT '鏍囬',
    `content` TEXT NULL COMMENT '鍐呭',
    `tags` VARCHAR(1024) NULL COMMENT '鏍囩鍒楄〃(json鏁扮粍)',
    `thumbNum` INT NOT NULL DEFAULT 0 COMMENT '鐐硅禐鏁?,
    `favourNum` INT NOT NULL DEFAULT 0 COMMENT '鏀惰棌鏁?,
    `userId` BIGINT NOT NULL COMMENT '鍒涘缓鐢ㄦ埛id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '鏄惁鍒犻櫎',
    INDEX `idx_userId` (`userId`)
) COMMENT '甯栧瓙' COLLATE = utf8mb4_unicode_ci;

-- 甯栧瓙鐐硅禐琛紙纭垹闄わ級
CREATE TABLE IF NOT EXISTS `post_thumb`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `postId` BIGINT NOT NULL COMMENT '甯栧瓙id',
    `userId` BIGINT NOT NULL COMMENT '鍒涘缓鐢ㄦ埛id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX `idx_postId` (`postId`),
    INDEX `idx_userId` (`userId`)
) COMMENT '甯栧瓙鐐硅禐';

-- 甯栧瓙鏀惰棌琛紙纭垹闄わ級
CREATE TABLE IF NOT EXISTS `post_favour`
(
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
    `postId` BIGINT NOT NULL COMMENT '甯栧瓙id',
    `userId` BIGINT NOT NULL COMMENT '鍒涘缓鐢ㄦ埛id',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    INDEX `idx_postId` (`postId`),
    INDEX `idx_userId` (`userId`)
) COMMENT '甯栧瓙鏀惰棌';

-- 应用信息表
CREATE TABLE IF NOT EXISTS `app_info`
(
    `appId` BIGINT NOT NULL AUTO_INCREMENT COMMENT '应用ID（主键）',
    `appName` VARCHAR(256) NOT NULL COMMENT '应用名称',
    `description` VARCHAR(256) NULL DEFAULT NULL COMMENT '应用描述',
    `host` VARCHAR(512) NOT NULL COMMENT '接口主机地址（网关地址）',
    `userId` BIGINT NOT NULL COMMENT '创建用户ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-正常，1-删除)',
    PRIMARY KEY (`appId`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口应用表' ROW_FORMAT = Dynamic;
-- 鎺ュ彛淇℃伅琛?CREATE TABLE IF NOT EXISTS `interface_info`
(
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '涓婚敭',
    `appId` BIGINT NOT NULL COMMENT 'appId',
    `name` VARCHAR(256) NOT NULL COMMENT '鍚嶇О',
    `description` VARCHAR(256) NULL DEFAULT NULL COMMENT '鎻忚堪',
    `url` VARCHAR(512) NOT NULL COMMENT '鎺ュ彛鍦板潃',
    `requestHeader` TEXT NULL COMMENT '璇锋眰澶?,
    `responseHeader` TEXT NULL COMMENT '鍝嶅簲澶?,
    `requestParams` VARCHAR(255) NULL DEFAULT NULL COMMENT '鎺ュ彛璇锋眰鍙傛暟',
    `requestExample` VARCHAR(255) NULL DEFAULT NULL COMMENT '璇锋眰绀轰緥',
    `responseParams` VARCHAR(255) NULL DEFAULT NULL COMMENT '鎺ュ彛鍝嶅簲鍙傛暟',
    `returnFormat` VARCHAR(255) NULL DEFAULT NULL COMMENT '杩斿洖鏍煎紡(JSON绛夌瓑)',
    `status` INT NOT NULL DEFAULT 0 COMMENT '鎺ュ彛鐘舵€侊紙1-鍏抽棴锛?-寮€鍚級',
    `method` VARCHAR(256) NOT NULL COMMENT '璇锋眰绫诲瀷',
    `totalNum` INT UNSIGNED NOT NULL COMMENT '璋冪敤娆℃暟',
    `userId` BIGINT NOT NULL COMMENT '鍒涘缓浜?,
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '鍒涘缓鏃堕棿',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',
    `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '鏄惁鍒犻櫎(0-鏈垹, 1-宸插垹)',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鎺ュ彛淇℃伅' ROW_FORMAT = Dynamic;



