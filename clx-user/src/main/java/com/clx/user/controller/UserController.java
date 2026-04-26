package com.clx.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.user.dto.ProfileUpdateDTO;
import com.clx.user.service.UserService;
import com.clx.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器。
 */
@Tag(name = "用户管理", description = "用户资料相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户资料。
     */
    @Operation(summary = "获取用户资料")
    @GetMapping("/{userId}")
    public R<UserProfileVO> getProfile(@PathVariable Long userId) {
        Long currentUserId = null;
        if (StpUtil.isLogin()) {
            currentUserId = StpUtil.getLoginIdAsLong();
        }
        UserProfileVO profile = userService.getProfile(userId, currentUserId);
        return R.ok(profile);
    }

    /**
     * 获取当前用户资料。
     */
    @Operation(summary = "获取当前用户资料")
    @GetMapping("/me")
    public R<UserProfileVO> getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        UserProfileVO profile = userService.getCurrentUserProfile(userId);
        return R.ok(profile);
    }

    /**
     * 更新当前用户资料。
     */
    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody ProfileUpdateDTO dto) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updateProfile(userId, dto);
        return R.ok();
    }
}