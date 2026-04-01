CREATE DATABASE IF NOT EXISTS moliapi_interface CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE moliapi_interface;

CREATE TABLE IF NOT EXISTS poetry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key ID',
    title VARCHAR(200) NOT NULL COMMENT 'Poetry title',
    author VARCHAR(100) NOT NULL COMMENT 'Author',
    dynasty VARCHAR(50) NOT NULL COMMENT 'Dynasty',
    content TEXT NOT NULL COMMENT 'Poetry content',
    tags VARCHAR(200) COMMENT 'Tags',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    is_delete TINYINT DEFAULT 0 COMMENT 'Logical delete flag: 0-active, 1-deleted',
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_dynasty (dynasty),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Poetry table';

INSERT INTO poetry (title, author, dynasty, content, tags) VALUES
('静夜思', '李白', '唐', '床前明月光，疑是地上霜。举头望明月，低头思故乡。', '思乡,月亮'),
('春晓', '孟浩然', '唐', '春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。', '春天,惜时'),
('登鹳雀楼', '王之涣', '唐', '白日依山尽，黄河入海流。欲穷千里目，更上一层楼。', '励志,山水'),
('望庐山瀑布', '李白', '唐', '日照香炉生紫烟，遥看瀑布挂前川。飞流直下三千尺，疑是银河落九天。', '山水,瀑布'),
('江雪', '柳宗元', '唐', '千山鸟飞绝，万径人踪灭。孤舟蓑笠翁，独钓寒江雪。', '冬天,山水'),
('咏鹅', '骆宾王', '唐', '鹅，鹅，鹅，曲项向天歌。白毛浮绿水，红掌拨清波。', '动物,儿童'),
('赋得古原草送别', '白居易', '唐', '离离原上草，一岁一枯荣。野火烧不尽，春风吹又生。', '励志,送别'),
('早发白帝城', '李白', '唐', '朝辞白帝彩云间，千里江陵一日还。两岸猿声啼不住，轻舟已过万重山。', '山水,旅途');

CREATE TABLE IF NOT EXISTS answer_book (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key ID',
    answer VARCHAR(255) NOT NULL COMMENT 'Book of answers content',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    is_delete TINYINT DEFAULT 0 COMMENT 'Logical delete flag: 0-active, 1-deleted',
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Book of answers table';

INSERT INTO answer_book (answer) VALUES
('是的，顺其自然会有好结果。'),
('先缓一缓，再做决定。'),
('现在不是最佳时机。'),
('你已经知道答案了。'),
('大胆一点，值得一试。'),
('换个角度看，会更清晰。'),
('耐心等待，机会正在路上。'),
('先完成眼前最小的一步。'),
('请相信你的直觉。'),
('结果会比你预期更好。'),
('暂时不要公开这个计划。'),
('和靠谱的人讨论后再行动。');
