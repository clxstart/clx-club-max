package com.clx.auth.controller;

import com.clx.auth.dto.LoginRequest;
import com.clx.auth.service.AuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.auth.vo.UserInfoVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 *
 * <p>提供三个核心接口：
 * <ul>
 *   <li>POST /auth/login - 用户登录，返回Token</li>
 *   <li>POST /auth/logout - 用户登出，清除会话</li>
 *   <li>GET /auth/me - 获取当前登录用户信息</li>
 * </ul>
 *
 * <p>使用说明：
 * <ol>
 *   <li>登录成功后返回Token，前端需要在后续请求的Header中携带：<br>
 *       Authorization: Bearer {token}</li>
 *   <li>Token有效期4小时，临时有效期2小时（无操作2小时后自动过期）</li>
 *   <li>登出后Token立即失效</li>
 * </ol>
 *
 * @see AuthService 认证服务
 * @see LoginRequest 登录请求DTO
 * @see LoginVO 登录结果VO
 */
@Slf4j
@Tag(name = "认证接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 认证服务 */
    private final AuthService authService;

    /**
     * 用户登录。
     *
     * <p>请求示例：
     * <pre>
     * POST /auth/login
     * Content-Type: application/json
     * {"username":"admin","password":"admin123"}
     * </pre>
     *
     * <p>响应示例：
     * <pre>
     * {"code":200,"data":{"token":"xxx","tokenName":"Authorization"}}
     * </pre>
     *
     * @param request 登录请求（用户名 + 密码）
     * @param servletRequest HTTP请求（用于获取客户端IP）
     * @return 登录结果（Token和Token名称）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        // 去除用户名首尾空格
        String username = request.username() == null ? "" : request.username().trim();
        String clientIp = resolveClientIp(servletRequest);

        log.info("用户登录请求: username={}, ip={}", username, clientIp);
        return R.ok(authService.login(username, request.password(), clientIp));
    }

    /**
     * 用户登出。
     *
     * <p>请求示例：
     * <pre>
     * POST /auth/logout
     * Authorization: Bearer {token}
     * </pre>
     *
     * <p>登出后Token立即失效，需要重新登录。
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    /**
     * 获取当前登录用户信息。
     *
     * <p>请求示例：
     * <pre>
     * GET /auth/me
     * Authorization: Bearer {token}
     * </pre>
     *
     * <p>响应示例：
     * <pre>
     * {"code":200,"data":{"userId":1,"username":"admin","tokenInfo":{...}}}
     * </pre>
     *
     * <p>如果未登录或Token已过期，返回401错误。
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户")
    public R<UserInfoVO> me() {
        return R.ok(authService.getCurrentUser());
    }

    /**
     * 解析客户端真实IP地址。
     *
     * <p>支持以下场景：
     * <ul>
     *   <li>直接访问：使用 request.getRemoteAddr()</li>
     *   <li>反向代理（Nginx）：读取 X-Real-IP 头</li>
     *   <li>多层代理：读取 X-Forwarded-For 头的第一个IP</li>
     * </ul>
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String resolveClientIp(HttpServletRequest request) {
        // 优先检查X-Forwarded-For（多层代理场景，取第一个IP）
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        // 其次检查X-Real-IP（单层反向代理场景）
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        // 最后使用直接连接的远程地址
        return request.getRemoteAddr();
    }
}