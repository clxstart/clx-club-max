package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.clx.admin.feign.AuthFeignClient;
import com.clx.admin.service.OperLogService;
import com.clx.admin.vo.PermissionVO;
import com.clx.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器。
 */
@RestController
@RequestMapping("/admin/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final AuthFeignClient authFeignClient;
    private final OperLogService operLogService;

    /** 获取权限列表 */
    @SaCheckRole("admin")
    @GetMapping("/list")
    public R<List<PermissionVO>> getPermissionList() {
        return authFeignClient.getPermissionList();
    }

    /** 新增权限 */
    @SaCheckRole("admin")
    @PostMapping
    public R<Void> addPermission(@RequestBody PermissionVO permission, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.addPermission(permission);
        operLogService.logAsync("权限管理", "新增权限", request.getRequestURI(), "POST",
                JSON.toJSONString(permission), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 更新权限 */
    @SaCheckRole("admin")
    @PutMapping
    public R<Void> updatePermission(@RequestBody PermissionVO permission, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.updatePermission(permission);
        operLogService.logAsync("权限管理", "更新权限", request.getRequestURI(), "PUT",
                JSON.toJSONString(permission), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 删除权限 */
    @SaCheckRole("admin")
    @DeleteMapping("/{permissionId}")
    public R<Void> deletePermission(@PathVariable Long permissionId, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = authFeignClient.deletePermission(permissionId);
        operLogService.logAsync("权限管理", "删除权限", request.getRequestURI(), "DELETE",
                String.valueOf(permissionId), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }
}