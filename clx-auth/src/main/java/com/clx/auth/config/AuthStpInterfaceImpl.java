package com.clx.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.clx.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * sa-Token 权限接口实现。
 *
 * <p>功能说明：
 * sa-Token通过此接口获取用户的角色和权限列表。
 * 当使用 @SaCheckRole、@SaCheckPermission 注解时，
 * sa-Token会调用此类的 getRoleList 和 getPermissionList 方法。
 *
 * <p>实现原理：
 * <ul>
 *   <li>getRoleList：根据用户ID从数据库查询用户拥有的角色编码列表</li>
 *   <li>getPermissionList：根据用户ID从数据库查询用户拥有的权限编码列表</li>
 * </ul>
 *
 * <p>数据来源：
 * 用户 -> 用户角色关联表(sys_user_role) -> 角色表(sys_role) -> 角色权限关联表(sys_role_permission) -> 权限表(sys_permission)
 *
 * @see StpInterface sa-Token权限接口
 * @see UserMapper 用户数据访问
 */
@Component
@RequiredArgsConstructor
public class AuthStpInterfaceImpl implements StpInterface {

    /** 用户Mapper，用于查询角色和权限 */
    private final UserMapper userMapper;

    /**
     * 获取用户的权限列表。
     *
     * <p>查询路径：用户 -> 用户角色 -> 角色 -> 角色权限 -> 权限
     * 只查询状态正常(status='0')且未删除(is_deleted=0)的角色和权限。
     *
     * @param loginId   登录用户ID（sa-Token登录时存储的值）
     * @param loginType 登录类型（多账号体系时区分，当前默认为空）
     * @return 权限编码列表，如 ["user:add", "user:delete", "post:edit"]
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 将Object类型转为Long类型的用户ID
        Long userId = Long.parseLong(String.valueOf(loginId));
        return userMapper.selectPermissionCodesByUserId(userId);
    }

    /**
     * 获取用户的角色列表。
     *
     * <p>查询路径：用户 -> 用户角色 -> 角色
     * 只查询状态正常(status='0')且未删除(is_deleted=0)的角色。
     *
     * @param loginId   登录用户ID
     * @param loginType 登录类型
     * @return 角色编码列表，如 ["admin", "user", "moderator"]
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        return userMapper.selectRoleCodesByUserId(userId);
    }
}