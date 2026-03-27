-- 创建数据库
CREATE DATABASE IF NOT EXISTS moliapi_interface CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE moliapi_interface;

-- 创建诗词表
CREATE TABLE IF NOT EXISTS poetry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '诗词标题',
    author VARCHAR(100) NOT NULL COMMENT '作者',
    dynasty VARCHAR(50) NOT NULL COMMENT '朝代',
    content TEXT NOT NULL COMMENT '诗词内容',
    tags VARCHAR(200) COMMENT '标签',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_dynasty (dynasty),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='古诗词表';

-- 插入示例数据
INSERT INTO poetry (title, author, dynasty, content, tags) VALUES
('静夜思', '李白', '唐', '床前明月光，疑是地上霜。举头望明月，低头思故乡。', '思乡,月亮'),
('春晓', '孟浩然', '唐', '春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。', '春天,惜时'),
('登鹳雀楼', '王之涣', '唐', '白日依山尽，黄河入海流。欲穷千里目，更上一层楼。', '励志,山水'),
('望庐山瀑布', '李白', '唐', '日照香炉生紫烟，遥看瀑布挂前川。飞流直下三千尺，疑是银河落九天。', '山水,瀑布'),
('江雪', '柳宗元', '唐', '千山鸟飞绝，万径人踪灭。孤舟蓑笠翁，独钓寒江雪。', '冬天,山水'),
('咏鹅', '骆宾王', '唐', '鹅，鹅，鹅，曲项向天歌。白毛浮绿水，红掌拨清波。', '动物,儿童'),
('赋得古原草送别', '白居易', '唐', '离离原上草，一岁一枯荣。野火烧不尽，春风吹又生。', '励志,送别'),
('早发白帝城', '李白', '唐', '朝辞白帝彩云间，千里江陵一日还。两岸猿声啼不住，轻舟已过万重山。', '山水,旅途');
