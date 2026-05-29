-- 检查 emoji 表数据
USE iotthermo;

SELECT '=== emoji 表数据 ===' AS '';
SELECT * FROM emoji;

SELECT '=== tempemoji 表数据 ===' AS '';
SELECT * FROM tempemoji;

-- 如果 emoji 表为空，重新插入数据
-- 检查 emoji 表是否有数据
SELECT COUNT(*) AS emoji_count FROM emoji;

-- 如果 emoji_count 为 0，则执行以下插入
INSERT INTO `emoji` (`name`, `face`) VALUES ('freezed', '(⊙﹏⊙)');
INSERT INTO `emoji` (`name`, `face`) VALUES ('hot', '(≧ｗ≦；)');
INSERT INTO `emoji` (`name`, `face`) VALUES ('comfort', '(*￣︶￣)');
INSERT INTO `emoji` (`name`, `face`) VALUES ('burn', 'ヽ(#`Д´)ﾉ');

-- 再次查询确认
SELECT * FROM emoji;
