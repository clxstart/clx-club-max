-- ========================================
-- 初始化数据
-- ========================================

USE `clx_user`;

-- 默认管理员：admin / admin123
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `email`, `phone`, `nickname`, `real_name`, `gender`, `status`)
VALUES
    (1, 'admin', '$2a$10$cX1Bgw3VdxwApyokYRF3B.iYYKD5IOu/8siinuC.M6NkQSIW7A4we', 'admin@clx.com', '13800000000', '超级管理员', '管理员', '1', '0');

-- 测试用户：test / test123
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `email`, `phone`, `nickname`, `gender`, `status`)
VALUES
    (2, 'test', '$2a$10$.sa0pD3UOFSlEj94ijMxVO3Q4mRLc.7v.6ZVIYGnZJCDF.5YAzZPS', 'test@clx.com', '13800000001', '测试用户', '0', '0');

-- 默认角色
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_code`, `description`, `sort`) VALUES
    (1, '超级管理员', 'admin', '拥有所有权限', 1),
    (2, '普通用户', 'user', '普通用户权限', 2),
    (3, '访客', 'guest', '访客权限', 3);

-- 用户角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
    (1, 1),
    (2, 2);

-- 菜单权限
INSERT INTO `sys_permission` (`permission_id`, `permission_name`, `permission_code`, `resource_type`, `parent_id`, `path`, `sort`) VALUES
    (1, '系统管理', 'system', '1', 0, '/system', 1),
    (2, '用户管理', 'system:user', '1', 1, '/system/user', 1),
    (3, '角色管理', 'system:role', '1', 1, '/system/role', 2),
    (4, '权限管理', 'system:permission', '1', 1, '/system/permission', 3),
    (5, '组织管理', 'system:org', '1', 1, '/system/org', 4);

-- 接口权限
INSERT INTO `sys_permission` (`permission_id`, `permission_name`, `permission_code`, `resource_type`, `parent_id`, `api_path`, `method`) VALUES
    (100, '用户查询', 'system:user:list', '3', 2, '/user/list', 'GET'),
    (101, '用户新增', 'system:user:add', '3', 2, '/user', 'POST'),
    (102, '用户修改', 'system:user:edit', '3', 2, '/user', 'PUT'),
    (103, '用户删除', 'system:user:delete', '3', 2, '/user/*', 'DELETE'),
    (200, '角色查询', 'system:role:list', '3', 3, '/role/list', 'GET'),
    (201, '角色新增', 'system:role:add', '3', 3, '/role', 'POST'),
    (202, '角色修改', 'system:role:edit', '3', 3, '/role', 'PUT'),
    (203, '角色删除', 'system:role:delete', '3', 3, '/role/*', 'DELETE');

-- admin 拥有全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, permission_id FROM `sys_permission`;

-- user 拥有基础查看权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
    (2, 1), (2, 2), (2, 3), (2, 4), (2, 5),
    (2, 100), (2, 200);

-- 默认组织
INSERT INTO `sys_organization` (`org_id`, `org_name`, `org_code`, `parent_id`, `ancestors`, `sort`) VALUES
    (1, 'CLX社区', 'clx', 0, '0', 1),
    (2, '技术部', 'tech', 1, '0,1', 1),
    (3, '运营部', 'operation', 1, '0,1', 2),
    (4, '产品部', 'product', 1, '0,1', 3);
