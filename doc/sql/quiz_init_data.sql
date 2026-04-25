-- ========================================
-- 刷题模块 - 初始化数据
-- ========================================

USE `clx_quiz`;

-- ========================================
-- 题目分类初始数据
-- ========================================
INSERT INTO `subject_category` (`id`, `category_name`, `parent_id`, `sort_num`, `created_by`) VALUES
(1, '后端', 0, 1, 'system'),
(2, '缓存', 1, 1, 'system'),
(3, '数据库', 1, 2, 'system'),
(4, '前端', 0, 2, 'system'),
(5, '算法', 0, 3, 'system');

-- ========================================
-- 题目标签初始数据
-- ========================================
INSERT INTO `subject_label` (`id`, `label_name`, `category_id`, `sort_num`, `created_by`) VALUES
(1, 'Redis', 2, 1, 'system'),
(2, 'MySQL', 3, 1, 'system'),
(3, 'Java', 1, 1, 'system'),
(4, 'Spring', 1, 2, 'system'),
(5, 'JavaScript', 4, 1, 'system'),
(6, 'Vue', 4, 2, 'system'),
(7, 'React', 4, 3, 'system'),
(8, '数据结构', 5, 1, 'system'),
(9, '排序算法', 5, 2, 'system');

-- ========================================
-- 示例题目数据
-- ========================================

-- 单选题示例
INSERT INTO `subject` (`id`, `subject_name`, `subject_type`, `subject_difficult`, `subject_score`, `subject_parse`, `created_by`) VALUES
(1, 'Redis默认端口号是多少？', 1, 1, 5, 'Redis默认端口是6379，这是Redis作者Antirez选择的数字，没有特殊含义。', 'system');

INSERT INTO `subject_radio` (`subject_id`, `option_type`, `option_content`, `is_correct`) VALUES
(1, 1, '3306', 0),
(1, 2, '6379', 1),
(1, 3, '8080', 0),
(1, 4, '27017', 0);

INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`) VALUES
(1, 2, 1, 'system');

-- 多选题示例
INSERT INTO `subject` (`id`, `subject_name`, `subject_type`, `subject_difficult`, `subject_score`, `subject_parse`, `created_by`) VALUES
(2, 'Redis支持的数据类型有哪些？', 2, 1, 5, 'Redis支持String、List、Hash、Set、Sorted Set五种基本数据类型。', 'system');

INSERT INTO `subject_multiple` (`subject_id`, `option_type`, `option_content`, `is_correct`) VALUES
(2, 1, 'String', 1),
(2, 2, 'List', 1),
(2, 3, 'HashMap', 0),
(2, 4, 'Set', 1),
(2, 5, 'Sorted Set', 1),
(2, 6, 'LinkedList', 0);

INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`) VALUES
(2, 2, 1, 'system');

-- 判断题示例
INSERT INTO `subject` (`id`, `subject_name`, `subject_type`, `subject_difficult`, `subject_score`, `subject_parse`, `created_by`) VALUES
(3, 'Redis是单线程的。', 3, 1, 5, 'Redis的核心命令执行是单线程的，但Redis 6.0之后引入了多线程IO来处理网络读写。', 'system');

INSERT INTO `subject_judge` (`subject_id`, `is_correct`) VALUES
(3, 1);

INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`) VALUES
(3, 2, 1, 'system');

-- 简答题示例
INSERT INTO `subject` (`id`, `subject_name`, `subject_type`, `subject_difficult`, `subject_score`, `subject_parse`, `created_by`) VALUES
(4, '请简述Redis的持久化机制。', 4, 2, 10, 'Redis提供两种持久化机制：RDB和AOF。RDB是在指定时间间隔内将内存中的数据集快照写入磁盘；AOF是记录服务器接收的所有写操作命令，并在服务器启动时重新执行这些命令来还原数据。', 'system');

INSERT INTO `subject_brief` (`subject_id`, `subject_answer`) VALUES
(4, 'Redis提供两种持久化机制：\n1. RDB（Redis Database）：在指定时间间隔内将内存中的数据集快照写入磁盘，适合备份和灾难恢复，但可能会有数据丢失。\n2. AOF（Append Only File）：记录服务器接收的所有写操作命令，并在服务器启动时重新执行这些命令来还原数据，数据更安全但文件体积更大。\n\n两者可以同时使用，Redis重启时会优先加载AOF文件来还原数据。');

INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`) VALUES
(4, 2, 1, 'system');

-- 更多示例题目
INSERT INTO `subject` (`id`, `subject_name`, `subject_type`, `subject_difficult`, `subject_score`, `subject_parse`, `created_by`) VALUES
(5, '什么是缓存穿透？如何解决？', 4, 2, 10, '缓存穿透是指查询一个不存在的数据，由于缓存中没有数据，每次都会查询数据库，导致数据库压力过大。', 'system');

INSERT INTO `subject_brief` (`subject_id`, `subject_answer`) VALUES
(5, '缓存穿透是指查询一个根本不存在的数据，缓存和数据库中都没有，导致每次请求都会穿透缓存直接查询数据库。\n\n解决方案：\n1. 接口层增加校验，如用户鉴权校验，id做基础校验\n2. 从缓存取不到的数据，在数据库中也没有取到，这时也可以将key-value对写为key-null，缓存有效时间可以设置短点\n3. 使用布隆过滤器，在请求到达缓存和数据库之前，先通过布隆过滤器判断数据是否存在');

INSERT INTO `subject_mapping` (`subject_id`, `category_id`, `label_id`, `created_by`) VALUES
(5, 2, 1, 'system');
