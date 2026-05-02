package com.clx.api.auth.feign;

import com.clx.api.auth.dto.PermissionVO;
import com.clx.api.auth.dto.RoleVO;
import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证服务 Feign 客户端。
 */
@FeignClient(name = "clx-auth", contextId = "authFeignClient", url = "${feign.client.config.clx-auth.url:}")
public interface AuthFeignClient {

    /**
     * 获取所有角色列表。
     */
    @GetMapping("/internal/auth/roles")
    R<List<RoleVO>> getRoleList();

    /**
     * 获取用户角色编码列表。
     */
    @GetMapping("/internal/auth/user/{userId}/roles")
    R<List<String>> getUserRoleCodes(@PathVariable("userId") Long userId);

    /**
     * 获取所有权限列表。
     */
    @GetMapping("/internal/auth/permissions")
    R<List<PermissionVO>> getPermissionList();

    /**
     * 获取角色的权限ID列表。
     */
    @GetMapping("/internal/auth/role/{roleId}/permissions")
    R<List<Long>> getRolePermissions(@PathVariable("roleId") Long roleId);

    /**
     * 新增角色。
     */
    @PostMapping("/internal/auth/role")
    R<Void> addRole(@RequestBody RoleVO role);

    /**
     * 更新角色。
     */
    @PutMapping("/internal/auth/role")
    R<Void> updateRole(@RequestBody RoleVO role);

    /**
     * 删除角色。
     */
    @DeleteMapping("/internal/auth/role/{roleId}")
    R<Void> deleteRole(@PathVariable("roleId") Long roleId);

    /**
     * 新增权限。
     */
    @PostMapping("/internal/auth/permission")
    R<Void> addPermission(@RequestBody PermissionVO permission);

    /**
     * 更新权限。
     */
    @PutMapping("/internal/auth/permission")
    R<Void> updatePermission(@RequestBody PermissionVO permission);

    /**
     * 删除权限。
     */
    @DeleteMapping("/internal/auth/permission/{permissionId}")
    R<Void> deletePermission(@PathVariable("permissionId") Long permissionId);

    /**
     * 分配权限给角色。
     */
    @PutMapping("/internal/auth/role/{roleId}/permissions")
    R<Void> assignPermissions(@PathVariable("roleId") Long roleId, @RequestBody List<Long> permissionIds);
}