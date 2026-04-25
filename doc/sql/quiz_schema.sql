-- ========================================
-- 刷题模块 - 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `clx_quiz` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `clx_quiz`;

-- ========================================
-- 题目分类表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_name` varchar(32) NOT NULL COMMENT '分类名称',
    `parent_id` bigint DEFAULT 0 COMMENT '父级id，0表示顶级分类',
    `sort_num` int DEFAULT 0 COMMENT '排序号',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- ========================================
-- 题目标签表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_label` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `label_name` varchar(32) NOT NULL COMMENT '标签名称',
    `category_id` bigint DEFAULT NULL COMMENT '所属分类id',
    `sort_num` int DEFAULT 0 COMMENT '排序号',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目标签表';

-- ========================================
-- 题目主表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_name` varchar(256) NOT NULL COMMENT '题目名称/题干',
    `subject_type` tinyint NOT NULL COMMENT '题目类型：1单选 2多选 3判断 4简答',
    `subject_difficult` tinyint DEFAULT 1 COMMENT '难度等级：1简单 2中等 3较难 4困难 5专家',
    `subject_score` int DEFAULT 5 COMMENT '题目分数',
    `subject_parse` text COMMENT '题目解析',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_type` (`subject_type`),
    KEY `idx_subject_difficult` (`subject_difficult`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目主表';

-- ========================================
-- 题目-分类-标签关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_mapping` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `category_id` bigint DEFAULT NULL COMMENT '分类id',
    `label_id` bigint DEFAULT NULL COMMENT '标签id',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_label_id` (`label_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目-分类-标签关联表';

-- ========================================
-- 单选题选项表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_radio` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `option_type` tinyint NOT NULL COMMENT '选项类型：1A 2B 3C 4D 5E 6F',
    `option_content` varchar(512) DEFAULT NULL COMMENT '选项内容',
    `is_correct` tinyint DEFAULT 0 COMMENT '是否正确：0否 1是',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单选题选项表';

-- ========================================
-- 多选题选项表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_multiple` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `option_type` tinyint NOT NULL COMMENT '选项类型：1A 2B 3C 4D 5E 6F',
    `option_content` varchar(512) DEFAULT NULL COMMENT '选项内容',
    `is_correct` tinyint DEFAULT 0 COMMENT '是否正确：0否 1是',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多选题选项表';

-- ========================================
-- 判断题表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_judge` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `is_correct` tinyint NOT NULL COMMENT '是否正确：0错 1对',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='判断题表';

-- ========================================
-- 简答题表
-- ========================================
CREATE TABLE IF NOT EXISTS `subject_brief` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `subject_answer` text COMMENT '参考答案',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='简答题表';

-- ========================================
-- 练习记录表
-- ========================================
CREATE TABLE IF NOT EXISTS `practice` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户id',
    `total_count` int DEFAULT 0 COMMENT '题目总数',
    `correct_count` int DEFAULT 0 COMMENT '正确数',
    `correct_rate` decimal(5,2) DEFAULT 0.00 COMMENT '正确率',
    `time_used` int DEFAULT 0 COMMENT '用时（秒）',
    `status` tinyint DEFAULT 0 COMMENT '状态：0进行中 1已完成',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习记录表';

-- ========================================
-- 练习详情表
-- ========================================
CREATE TABLE IF NOT EXISTS `practice_detail` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `practice_id` bigint NOT NULL COMMENT '练习id',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `subject_type` tinyint NOT NULL COMMENT '题目类型',
    `answer_content` varchar(1024) DEFAULT NULL COMMENT '用户答案',
    `is_correct` tinyint DEFAULT 0 COMMENT '是否正确：0错 1对 2部分对',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    KEY `idx_practice_id` (`practice_id`),
    KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='练习详情表';

-- ========================================
-- 错题本表
-- ========================================
CREATE TABLE IF NOT EXISTS `wrong_book` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户id',
    `subject_id` bigint NOT NULL COMMENT '题目id',
    `wrong_count` int DEFAULT 1 COMMENT '累计错误次数',
    `last_wrong_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后答错时间',
    `created_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` tinyint DEFAULT 0 COMMENT '是否删除：0否 1是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_subject` (`user_id`, `subject_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';
