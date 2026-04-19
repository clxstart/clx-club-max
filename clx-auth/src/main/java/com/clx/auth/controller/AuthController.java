package com.clx.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.dto.LoginRequest;
import com.clx.auth.service.AuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.UserInfoVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@Tag(name = "认证接口", description = "登录、登出")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());

        String token = authService.login(request.getUsername(), request.getPassword());

        LoginVO vo = LoginVO.builder()
                .token(token)
                .tokenName("Authorization")
                .build();

        return R.ok(vo);
    }

    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    @GetMapping("/me")
    public R<UserInfoVO> getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return R.fail("未登录");
        }

        UserInfoVO vo = UserInfoVO.builder()
                .userId(authService.getLoginUserId())
                .username(authService.getLoginUsername())
                .tokenInfo(StpUtil.getTokenInfo())
                .build();

        return R.ok(vo);
    }

}
