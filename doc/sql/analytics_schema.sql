-- 用户行为分析数据库表结构
-- 数据库：clx_analytics

CREATE DATABASE IF NOT EXISTS `clx_analytics` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `clx_analytics`;

-- 行为日志表
CREATE TABLE IF NOT EXISTS `behavior_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `behavior_type` varchar(30) NOT NULL COMMENT '行为类型: login/view_post/like_post/like_comment/comment/favorite/follow',
    `target_id` bigint DEFAULT NULL COMMENT '目标ID',
    `target_type` varchar(20) DEFAULT NULL COMMENT '目标类型: post/comment/user',
    `extra` json DEFAULT NULL COMMENT '扩展信息(JSON)',
    `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_behavior_type` (`behavior_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为日志表';

-- 分析报表表
CREATE TABLE IF NOT EXISTS `analytics_report` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `report_date` date NOT NULL COMMENT '报表日期',
    `report_type` varchar(20) NOT NULL COMMENT '报表类型: daily/hot_posts/trend',
    `metric_name` varchar(50) NOT NULL COMMENT '指标名称',
    `metric_value` decimal(20,4) DEFAULT NULL COMMENT '指标值',
    `dimension` varchar(500) DEFAULT NULL COMMENT '维度(JSON)',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date_type_metric` (`report_date`, `report_type`, `metric_name`),
    KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析报表表';
