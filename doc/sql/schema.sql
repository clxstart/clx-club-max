-- ========================================
-- 超大型社区项目 - 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `clx_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `clx_auth` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `clx_user`;

-- ========================================
-- 用户表（后续分表：sys_user_0 ~ sys_user_15）
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user` (
    `user_id` bigint NOT NULL COMMENT '用户ID(雪花算法)',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码(BCrypt)',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `gender` char(1) DEFAULT '0' COMMENT '性别:0未知,1男,2女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `signature` varchar(255) DEFAULT NULL COMMENT '个性签名',
    `org_id` bigint DEFAULT NULL COMMENT '组织ID',
    `status` char(1) DEFAULT '0' COMMENT '状态:0正常,1禁用,2锁定',
    `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除:0否,1是',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `login_count` int DEFAULT '0' COMMENT '登录次数',
    `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_email` (`email`),
    KEY `idx_org_id` (`org_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 角色表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(255) DEFAULT NULL COMMENT '描述',
    `sort` int DEFAULT '0' COMMENT '排序',
    `status` char(1) DEFAULT '0' COMMENT '状态:0正常,1禁用',
    `is_deleted` tinyint DEFAULT '0' COMMENT '是否删除',
    `create_by` varchar(64) DEFAULT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_by` varchar(64) DEFAULT NULL,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ========================================
-- 权限表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `permission_id` bigint NOT NULL COMMENT '权限ID',
    `permission_name` varchar(50) NOT NULL COMMENT '权限名称',
    `permission_code` varchar(100) NOT NULL COMMENT '权限编码',
    `resource_type` char(1) NOT NULL COMMENT '资源类型:1菜单,2按钮,3接口',
    `parent_id` bigint DEFAULT '0' COMMENT '父ID',
    `path` varchar(255) DEFAULT NULL COMMENT '路由路径',
    `api_path` varchar(255) DEFAULT NULL COMMENT 'API路径',
    `method` varchar(10) DEFAULT NULL COMMENT 'HTTP方法',
    `icon` varchar(100) DEFAULT NULL COMMENT '图标',
    `sort` int DEFAULT '0' COMMENT '排序',
    `visible` tinyint DEFAULT '1' COMMENT '是否可见',
    `status` char(1) DEFAULT '0' COMMENT '状态',
    `is_deleted` tinyint DEFAULT '0',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`permission_id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ========================================
-- 用户角色关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ========================================
-- 角色权限关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `role_id` bigint NOT NULL,
    `permission_id` bigint NOT NULL,
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ========================================
-- 组织机构表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_organization` (
    `org_id` bigint NOT NULL COMMENT '组织ID',
    `org_name` varchar(100) NOT NULL COMMENT '组织名称',
    `org_code` varchar(50) NOT NULL COMMENT '组织编码',
    `parent_id` bigint DEFAULT '0' COMMENT '父组织ID',
    `ancestors` varchar(500) DEFAULT NULL COMMENT '祖级路径(逗号分隔)',
    `sort` int DEFAULT '0' COMMENT '排序',
    `leader` varchar(50) DEFAULT NULL COMMENT '负责人',
    `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `status` char(1) DEFAULT '0' COMMENT '状态',
    `is_deleted` tinyint DEFAULT '0',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`org_id`),
    UNIQUE KEY `uk_org_code` (`org_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构表';

-- ========================================
-- 社交账号绑定表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_social_bind` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `social_type` varchar(20) NOT NULL COMMENT '社交类型:wecom/dingtalk/wechat/github',
    `social_id` varchar(100) NOT NULL COMMENT '社交账号ID',
    `social_name` varchar(100) DEFAULT NULL COMMENT '社交账号名称',
    `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `is_deleted` tinyint DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_social` (`social_type`, `social_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社交账号绑定表';

USE `clx_auth`;

-- ========================================
-- 登录日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `username` varchar(50) DEFAULT NULL COMMENT '用户名',
    `login_type` varchar(20) DEFAULT 'password' COMMENT '登录类型:password/sms/wecom/dingtalk',
    `login_ip` varchar(50) DEFAULT NULL COMMENT '登录IP',
    `login_location` varchar(100) DEFAULT NULL COMMENT '登录地点',
    `browser` varchar(50) DEFAULT NULL COMMENT '浏览器',
    `os` varchar(50) DEFAULT NULL COMMENT '操作系统',
    `device` varchar(100) DEFAULT NULL COMMENT '设备信息',
    `status` char(1) DEFAULT '0' COMMENT '状态:0成功,1失败',
    `msg` varchar(255) DEFAULT NULL COMMENT '消息',
    `login_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ========================================
-- 操作日志表
-- ========================================
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `username` varchar(50) DEFAULT NULL COMMENT '用户名',
    `module` varchar(100) DEFAULT NULL COMMENT '模块',
    `action` varchar(100) DEFAULT NULL COMMENT '操作',
    `method` varchar(255) DEFAULT NULL COMMENT '方法',
    `request_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
    `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
    `request_params` text COMMENT '请求参数',
    `response_result` text COMMENT '响应结果',
    `status` char(1) DEFAULT '0' COMMENT '状态:0成功,1失败',
    `error_msg` text COMMENT '错误信息',
    `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `cost_time` bigint DEFAULT NULL COMMENT '耗时(毫秒)',
    `oper_ip` varchar(50) DEFAULT NULL COMMENT '操作IP',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';