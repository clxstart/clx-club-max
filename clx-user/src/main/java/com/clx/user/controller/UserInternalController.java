package com.clx.user.controller;

import com.clx.api.user.dto.PageResultDTO;
import com.clx.api.user.dto.UserPageVO;
import com.clx.api.user.dto.UserQueryDTO;
import com.clx.api.user.dto.UserUpdateDTO;
import com.clx.common.core.domain.R;
import com.clx.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户内部接口（供 Feign 调用）。
 */
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    /**
     * 分页查询用户列表。
     */
    @PostMapping("/page")
    public R<PageResultDTO<UserPageVO>> getUserPage(@RequestBody UserQueryDTO query) {
        return R.ok(userService.getUserPage(query));
    }

    /**
     * 获取用户详情。
     */
    @GetMapping("/{userId}")
    public R<UserPageVO> getUserById(@PathVariable Long userId) {
        return R.ok(userService.getUserById(userId));
    }

    /**
     * 更新用户资料。
     */
    @PutMapping("/{userId}")
    public R<Void> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO dto) {
        userService.adminUpdateUser(userId, dto);
        return R.ok();
    }

    /**
     * 封禁用户。
     */
    @PutMapping("/{userId}/ban")
    public R<Void> banUser(@PathVariable Long userId) {
        userService.banUser(userId);
        return R.ok();
    }

    /**
     * 解封用户。
     */
    @PutMapping("/{userId}/unban")
    public R<Void> unbanUser(@PathVariable Long userId) {
        userService.unbanUser(userId);
        return R.ok();
    }

    /**
     * 获取用户角色列表。
     */
    @GetMapping("/{userId}/roles")
    public R<List<Long>> getUserRoles(@PathVariable Long userId) {
        return R.ok(userService.getUserRoleIds(userId));
    }

    /**
     * 更新用户角色。
     */
    @PutMapping("/{userId}/roles")
    public R<Void> updateUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.updateUserRoles(userId, roleIds);
        return R.ok();
    }
}