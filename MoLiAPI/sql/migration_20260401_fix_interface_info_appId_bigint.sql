-- 修复 interface_info.appId 列类型，避免雪花ID溢出
ALTER TABLE `interface_info`
    MODIFY COLUMN `appId` BIGINT NOT NULL COMMENT 'appId';
