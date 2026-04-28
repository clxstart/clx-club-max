package com.clx.auth.controller;

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
}