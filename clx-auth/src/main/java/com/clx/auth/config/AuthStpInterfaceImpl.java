package com.clx.auth.config;

import cn.dev33.satoken.stp.StpInterface;
import com.clx.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * sa-Token 权限适配器。
 * 供 @SaCheckRole、@SaCheckPermission 注解校验时调用，
 * 查询路径：用户 -> sys_user_role -> sys_role -> sys_role_permission -> sys_permission
 */
// sa-token 通过它获取用户的角色和权限列表
@Component
@RequiredArgsConstructor
public class AuthStpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;

    /** 获取用户角色编码列表，如 ["admin", "moderator"] */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        return userMapper.selectRoleCodesByUserId(userId);
    }

    /** 获取用户权限编码列表，如 ["user:add", "post:edit"] */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.parseLong(String.valueOf(loginId));
        return userMapper.selectPermissionCodesByUserId(userId);
    }

}