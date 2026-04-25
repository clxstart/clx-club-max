-- ========================================
-- 消息服务数据库
-- ========================================
CREATE DATABASE IF NOT EXISTS `clx_message` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `clx_message`;

-- ========================================
-- 会话表
-- ========================================
CREATE TABLE IF NOT EXISTS `chat_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `user1_id` bigint NOT NULL COMMENT '用户1 ID（较小值）',
    `user2_id` bigint NOT NULL COMMENT '用户2 ID（较大值）',
    `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_time` datetime DEFAULT NULL COMMENT '最后消息时间',
    `user1_unread` int DEFAULT '0' COMMENT '用户1未读数',
    `user2_unread` int DEFAULT '0' COMMENT '用户2未读数',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
    KEY `idx_user1` (`user1_id`),
    KEY `idx_user2` (`user2_id`),
    KEY `idx_last_time` (`last_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

-- ========================================
-- 私信消息表
-- ========================================
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `session_id` bigint NOT NULL COMMENT '会话ID',
    `from_user_id` bigint NOT NULL COMMENT '发送者ID',
    `to_user_id` bigint NOT NULL COMMENT '接收者ID',
    `content` varchar(2000) NOT NULL COMMENT '消息内容',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_from_user` (`from_user_id`),
    KEY `idx_to_user` (`to_user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';

-- ========================================
-- 通知表
-- ========================================
CREATE TABLE IF NOT EXISTS `notification` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `user_id` bigint NOT NULL COMMENT '接收通知的用户ID',
    `type` varchar(20) NOT NULL COMMENT '通知类型: comment_reply/like/follow/system',
    `title` varchar(200) NOT NULL COMMENT '通知标题',
    `content` varchar(500) DEFAULT NULL COMMENT '通知内容',
    `source_id` bigint DEFAULT NULL COMMENT '来源ID（帖子/评论等）',
    `source_type` varchar(20) DEFAULT NULL COMMENT '来源类型: post/comment/user',
    `is_read` tinyint DEFAULT '0' COMMENT '是否已读:0未读,1已读',
    `aggregate_count` int DEFAULT '1' COMMENT '聚合数量（"3人赞了"中的3）',
    `aggregate_key` varchar(100) DEFAULT NULL COMMENT '聚合键（用于同类型同来源合并）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_type` (`user_id`, `type`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_aggregate_key` (`aggregate_key`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
