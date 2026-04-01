-- Run this file if tables already exist and comments are garbled.
USE moliapi_interface;

ALTER TABLE poetry COMMENT='Poetry table';
ALTER TABLE poetry
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key ID',
    MODIFY COLUMN title VARCHAR(200) NOT NULL COMMENT 'Poetry title',
    MODIFY COLUMN author VARCHAR(100) NOT NULL COMMENT 'Author',
    MODIFY COLUMN dynasty VARCHAR(50) NOT NULL COMMENT 'Dynasty',
    MODIFY COLUMN content TEXT NOT NULL COMMENT 'Poetry content',
    MODIFY COLUMN tags VARCHAR(200) COMMENT 'Tags',
    MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    MODIFY COLUMN is_delete TINYINT DEFAULT 0 COMMENT 'Logical delete flag: 0-active, 1-deleted';

ALTER TABLE answer_book COMMENT='Book of answers table';
ALTER TABLE answer_book
    MODIFY COLUMN id BIGINT AUTO_INCREMENT COMMENT 'Primary key ID',
    MODIFY COLUMN answer VARCHAR(255) NOT NULL COMMENT 'Book of answers content',
    MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    MODIFY COLUMN is_delete TINYINT DEFAULT 0 COMMENT 'Logical delete flag: 0-active, 1-deleted';
