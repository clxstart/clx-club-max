-- ========================================
-- 第三方登录功能 - 数据库变更脚本
-- 日期: 2026-04-22
-- ========================================

USE `clx_user`;

-- ----------------------------------------
-- 1. 社交账号绑定表
-- 用途：存储用户绑定的第三方平台账号（GitHub、QQ、微信等）
-- ----------------------------------------
CREATE TABLE IF NOT EXISTS `sys_social_bind` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 关联本地用户
    -- 为什么要关联？知道这是哪个本地用户的第三方账号
    `user_id` BIGINT NOT NULL COMMENT '本地用户ID，关联sys_user表的user_id',

    -- 第三方平台信息
    -- social_type 定义平台类型，方便区分是GitHub还是QQ
    -- social_id 是第三方平台的唯一标识，如GitHub的id
    `social_type` VARCHAR(20) NOT NULL COMMENT '平台类型：github-码云/phone-手机号/qq-QQ/wechat-微信',
    `social_id` VARCHAR(100) NOT NULL COMMENT '第三方平台的唯一标识，如GitHub的用户ID',

    -- 用户信息（冗余存储，用于展示）
    -- 为什么冗余？避免每次都要调第三方API获取用户名和头像
    `social_name` VARCHAR(100) DEFAULT NULL COMMENT '第三方平台的昵称，如GitHub用户名',
    `social_avatar` VARCHAR(500) DEFAULT NULL COMMENT '第三方平台的头像URL',

    -- Token信息（可选，用于后续操作）
    -- access_token 用于调用第三方API
    -- refresh_token 用于access_token过期后刷新
    `access_token` VARCHAR(500) DEFAULT NULL COMMENT '访问令牌，调用第三方API时用',
    `refresh_token` VARCHAR(500) DEFAULT NULL COMMENT '刷新令牌，access_token过期后用于刷新',
    `token_expire_time` DATETIME DEFAULT NULL COMMENT '令牌过期时间',

    -- 元数据
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',

    -- 主键
    PRIMARY KEY (`id`),

    -- 唯一约束：同一个平台的同一个账号只能绑定一次
    -- 为什么？防止重复绑定
    UNIQUE KEY `uk_social_bind` (`social_type`, `social_id`),

    -- 普通索引：方便根据用户ID查询绑定的所有第三方账号
    KEY `idx_user_id` (`user_id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='第三方社交账号绑定表：存储用户绑定的GitHub、QQ、微信等账号';


-- ----------------------------------------
-- 2. 修改用户表，添加手机号字段
-- 用途：支持手机号登录
-- ----------------------------------------

-- 先检查 phone 字段是否存在，不存在才添加
-- 为什么要检查？避免重复执行报错
SET @exist_phone := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'clx_user'
    AND table_name = 'sys_user'
    AND column_name = 'phone'
);

SET @sql := IF(@exist_phone = 0,
    'ALTER TABLE sys_user ADD COLUMN phone VARCHAR(20) COMMENT "手机号，用于手机号登录" AFTER email',
    'SELECT "phone字段已存在，跳过添加" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加手机号唯一索引
-- 为什么要唯一？一个手机号只能对应一个账号
SET @exist_phone_index := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = 'clx_user'
    AND table_name = 'sys_user'
    AND index_name = 'uk_phone'
);

SET @sql_index := IF(@exist_phone_index = 0,
    'ALTER TABLE sys_user ADD UNIQUE KEY uk_phone (phone)',
    'SELECT "uk_phone索引已存在，跳过添加" as message'
);

PREPARE stmt2 FROM @sql_index;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;


-- ----------------------------------------
-- 3. 添加测试数据（可选）
-- ----------------------------------------

-- 给现有 admin 用户添加一个模拟的GitHub绑定
-- 用于测试第三方登录功能
-- INSERT INTO sys_social_bind (user_id, social_type, social_id, social_name)
-- VALUES (1, 'github', '12345678', 'admin-github');

-- 验证表结构
SELECT 'sys_social_bind 表创建成功' as result;
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT
FROM information_schema.columns
WHERE table_schema = 'clx_user' AND table_name = 'sys_social_bind';
