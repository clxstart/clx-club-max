package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.clx.admin.feign.AuthFeignClient;
import com.clx.admin.vo.RoleVO;
import com.clx.admin.service.OperLogService;
import com.clx.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器。
 */
@RestController
@RequestMapping("/admin/role")
@RequiredArgsConstructor
public class RoleController {

    private final AuthFeignClient authFeignClient;
    private final OperLogService operLogService;

    /** 获取角色列表 */
    @SaCheckRole("admin")
    @GetMapping("/list")
    public R<List<RoleVO>> getRoleList() {
        return authFeignClient.getRoleList();
    }

    /** 新增角色 */
    @SaCheckRole("admin")
    @PostMapping
    public R<Void> addRole(@RequestBody RoleVO role, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.addRole(role);
        operLogService.logAsync("角色管理", "新增角色", request.getRequestURI(), "POST",
                JSON.toJSONString(role), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 更新角色 */
    @SaCheckRole("admin")
    @PutMapping
    public R<Void> updateRole(@RequestBody RoleVO role, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.updateRole(role);
        operLogService.logAsync("角色管理", "更新角色", request.getRequestURI(), "PUT",
                JSON.toJSONString(role), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 删除角色 */
    @SaCheckRole("admin")
    @DeleteMapping("/{roleId}")
    public R<Void> deleteRole(@PathVariable Long roleId, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.deleteRole(roleId);
        operLogService.logAsync("角色管理", "删除角色", request.getRequestURI(), "DELETE",
                String.valueOf(roleId), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 获取角色的权限ID列表 */
    @SaCheckRole("admin")
    @GetMapping("/{roleId}/permissions")
    public R<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        return authFeignClient.getRolePermissions(roleId);
    }

    /** 分配权限给角色 */
    @SaCheckRole("admin")
    @PutMapping("/{roleId}/permissions")
    public R<Void> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds,
                                      HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.assignPermissions(roleId, permissionIds);
        operLogService.logAsync("角色管理", "分配权限", request.getRequestURI(), "PUT",
                JSON.toJSONString(permissionIds), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }
}