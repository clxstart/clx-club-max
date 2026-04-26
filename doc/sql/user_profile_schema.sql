-- ========================================
-- 用户模块数据库变更脚本
-- ========================================

USE `clx_user`;

-- ========================================
-- 关注关系表
-- ========================================
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '关注者ID',
    `target_id` bigint NOT NULL COMMENT '被关注者ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
    KEY `idx_target_id` (`target_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

-- ========================================
-- 帖子收藏表
-- ========================================
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';

-- ========================================
-- 用户表新增获赞数字段
-- ========================================
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `like_total_count` int DEFAULT '0' COMMENT '获赞总数' AFTER `login_count`;

-- ========================================
-- 用户表将关注数、粉丝数字段从冗余改为真实统计
-- （如果已有这些字段则跳过）
-- ========================================
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `follow_count` int DEFAULT '0' COMMENT '关注数' AFTER `like_total_count`;
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `fans_count` int DEFAULT '0' COMMENT '粉丝数' AFTER `follow_count`;
