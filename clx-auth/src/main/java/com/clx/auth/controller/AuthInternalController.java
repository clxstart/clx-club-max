package com.clx.auth.controller;

import com.clx.api.auth.dto.PermissionVO;
import com.clx.api.auth.dto.RoleVO;
import com.clx.auth.mapper.AuthMapper;
import com.clx.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证内部接口（供 Feign 调用）。
 */
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
public class AuthInternalController {

    private final AuthMapper authMapper;

    /**
     * 获取所有角色列表。
     */
    @GetMapping("/roles")
    public R<List<RoleVO>> getRoleList() {
        return R.ok(authMapper.selectAllRoles());
    }

    /**
     * 获取用户角色编码列表。
     */
    @GetMapping("/user/{userId}/roles")
    public R<List<String>> getUserRoleCodes(@PathVariable Long userId) {
        return R.ok(authMapper.selectRoleCodesByUserId(userId));
    }

    /**
     * 获取所有权限列表。
     */
    @GetMapping("/permissions")
    public R<List<PermissionVO>> getPermissionList() {
        return R.ok(authMapper.selectAllPermissions());
    }

    /**
     * 获取角色的权限ID列表。
     */
    @GetMapping("/role/{roleId}/permissions")
    public R<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return R.ok(authMapper.selectPermissionIdsByRoleId(roleId));
    }

    /**
     * 新增角色。
     */
    @PostMapping("/role")
    public R<Void> addRole(@RequestBody RoleVO role) {
        role.setRoleId(System.currentTimeMillis());
        authMapper.insertRole(role);
        return R.ok();
    }

    /**
     * 更新角色。
     */
    @PutMapping("/role")
    public R<Void> updateRole(@RequestBody RoleVO role) {
        authMapper.updateRole(role);
        return R.ok();
    }

    /**
     * 删除角色。
     */
    @DeleteMapping("/role/{roleId}")
    public R<Void> deleteRole(@PathVariable Long roleId) {
        authMapper.deleteRole(roleId);
        authMapper.deleteRolePermissions(roleId);
        return R.ok();
    }

    /**
     * 新增权限。
     */
    @PostMapping("/permission")
    public R<Void> addPermission(@RequestBody PermissionVO permission) {
        permission.setPermissionId(System.currentTimeMillis());
        authMapper.insertPermission(permission);
        return R.ok();
    }

    /**
     * 更新权限。
     */
    @PutMapping("/permission")
    public R<Void> updatePermission(@RequestBody PermissionVO permission) {
        authMapper.updatePermission(permission);
        return R.ok();
    }

    /**
     * 删除权限。
     */
    @DeleteMapping("/permission/{permissionId}")
    public R<Void> deletePermission(@PathVariable Long permissionId) {
        authMapper.deletePermission(permissionId);
        return R.ok();
    }

    /**
     * 分配权限给角色。
     */
    @PutMapping("/role/{roleId}/permissions")
    public R<Void> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        // 先删除旧的关联
        authMapper.deleteRolePermissions(roleId);
        // 再添加新的关联
        for (Long permissionId : permissionIds) {
            authMapper.insertRolePermission(roleId, permissionId);
        }
        return R.ok();
    }
}