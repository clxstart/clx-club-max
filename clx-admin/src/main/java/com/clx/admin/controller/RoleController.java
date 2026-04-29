package com.clx.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.admin.service.OperLogService;
import com.clx.api.auth.dto.RoleVO;
import com.clx.api.auth.feign.AuthFeignClient;
import com.clx.common.core.domain.R;
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

    /**
     * 获取角色列表。
     */
    @GetMapping("/list")
    public R<List<RoleVO>> getRoleList() {
        return authFeignClient.getRoleList();
    }
}