-- 修复 app_info 逻辑删除字段默认值和历史脏数据
ALTER TABLE `app_info`
    MODIFY COLUMN `isDelete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除(0-正常，1-删除)';

UPDATE `app_info`
SET `isDelete` = 0
WHERE `isDelete` IS NULL;
