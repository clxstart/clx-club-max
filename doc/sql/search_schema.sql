-- ========================================
-- 搜索服务数据库
-- ========================================
CREATE DATABASE IF NOT EXISTS `clx_search` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `clx_search`;

-- ========================================
-- 搜索日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `search_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `keyword` varchar(200) NOT NULL COMMENT '搜索关键词',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `search_types` varchar(100) DEFAULT NULL COMMENT '搜索类型（逗号分隔）',
    `result_count` int DEFAULT 0 COMMENT '结果总数',
    `cost_time` int DEFAULT 0 COMMENT '耗时（毫秒）',
    `click_results` varchar(500) DEFAULT NULL COMMENT '点击的结果ID（逗号分隔）',
    `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (`id`),
    KEY `idx_keyword` (`keyword`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索日志表';

-- ========================================
-- 搜索热词表
-- ========================================
CREATE TABLE IF NOT EXISTS `hot_keyword` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `keyword` varchar(200) NOT NULL COMMENT '关键词',
    `search_count` bigint DEFAULT 0 COMMENT '搜索次数',
    `period_type` varchar(20) NOT NULL COMMENT '统计周期：day/week/month',
    `period_date` varchar(20) NOT NULL COMMENT '周期日期（如2026-04-23）',
    `growth_rate` decimal(10,2) DEFAULT 0 COMMENT '增长率（%）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword_period` (`keyword`, `period_type`, `period_date`),
    KEY `idx_period_date` (`period_date`),
    KEY `idx_search_count` (`search_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索热词表';