package com.clx.auth.controller;

import com.clx.auth.service.oauth.GithubOAuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * GitHub OAuth 登录控制器
 * <p>
 * 功能：提供 GitHub 第三方登录的 API 接口
 * <p>
 * 接口列表：
 * <ul>
 *   <li>GET /auth/oauth/github/authorize - 获取授权 URL（前端跳转用）</li>
 *   <li>GET /auth/oauth/github/callback - GitHub 回调接口（处理授权结果）</li>
 * </ul>
 * <p>
 * 使用流程：
 * <ol>
 *   <li>前端调用 /authorize 获取授权 URL</li>
 *   <li>前端跳转到授权 URL，用户在 GitHub 授权</li>
 *   <li>GitHub 重定向到 /callback，后端处理登录</li>
 *   <li>后端返回登录结果（Token），前端存储并跳转首页</li>
 * </ol>
 *
 * @author CLX
 * @since 2026-04-22
 * @see GithubOAuthService GitHub OAuth 服务实现
 */
@Slf4j
@RestController
@RequestMapping("/auth/oauth/github")
@RequiredArgsConstructor
@Tag(name = "GitHub OAuth 登录", description = "GitHub 第三方登录相关接口")
public class GithubOAuthController {

    /**
     * GitHub OAuth 服务
     * <p>
     * 用途：处理授权 URL 生成和回调处理
     */
    private final GithubOAuthService githubOAuthService;

    /**
     * 获取 GitHub 授权 URL
     * <p>
     * 调用时机：用户点击"GitHub 登录"按钮时
     * <p>
     * 前端使用方式：
     * <pre>
     * // 方式1：直接跳转（推荐）
     * const response = await fetch('/auth/oauth/github/authorize');
     * const data = await response.json();
     * window.location.href = data.data;  // 跳转到 GitHub 授权页
     *
     * // 方式2：后端直接重定向（需要浏览器访问）
     * // 浏览器直接访问 /auth/oauth/github/authorize?redirect=true
     * // 后端会返回 302 重定向到 GitHub
     * </pre>
     *
     * @param redirect 是否直接重定向（默认 false，返回 JSON）
     * @param response HTTP 响应对象（用于重定向）
     * @return 授权 URL，前端需要跳转到这个地址
     * @throws IOException 重定向失败时抛出
     */
    @GetMapping("/authorize")
    @Operation(summary = "获取 GitHub 授权 URL",
            description = "获取 GitHub OAuth 授权页面的 URL，前端需要跳转到这个地址让用户授权")
    public R<String> getAuthorizeUrl(
            @Parameter(description = "是否直接重定向到 GitHub，默认 false")
            @RequestParam(value = "redirect", defaultValue = "false") boolean redirect,
            HttpServletResponse response) throws IOException {

        log.debug("获取 GitHub 授权 URL, redirect={}", redirect);

        // 调用服务生成授权 URL
        String authorizeUrl = githubOAuthService.getAuthorizeUrl();

        if (redirect) {
            // 直接重定向模式：返回 302，浏览器自动跳转
            // 用途：前端不方便处理跳转时，可以直接访问这个接口
            log.debug("直接重定向到: {}", authorizeUrl);
            response.sendRedirect(authorizeUrl);
            return null;  // 重定向后不需要返回数据
        }

        // 默认模式：返回 JSON，前端自己处理跳转
        // 优点：前端可以添加 loading 状态，用户体验更好
        return R.ok(authorizeUrl);
    }

    /**
     * GitHub OAuth 回调接口
     * <p>
     * 调用时机：用户在 GitHub 授权后，GitHub 自动重定向到这里
     * <p>
     * URL 格式：
     * <pre>
     * /auth/oauth/github/callback?code=xxx&state=yyy
     * </pre>
     * <p>
     * 处理流程：
     * <ol>
     *   <li>验证 state 防止 CSRF 攻击</li>
     *   <li>用 code 换取 access_token</li>
     *   <li>获取 GitHub 用户信息</li>
     *   <li>查找或创建本地用户</li>
     *   <li>生成 JWT Token</li>
     *   <li>重定向到前端页面，带上 token</li>
     * </ol>
     *
     * @param code  GitHub 返回的授权码（临时有效，只能用一次）
     * @param state 随机状态码（用于验证请求来源）
     * @return 登录结果，包含 JWT Token 和用户信息
     */
    @GetMapping("/callback")
    @Operation(summary = "GitHub OAuth 回调",
            description = "GitHub 授权后的回调地址，处理登录逻辑并重定向到前端")
    public void handleCallback(
            @Parameter(description = "GitHub 返回的授权码", required = true)
            @RequestParam(value = "code") String code,
            @Parameter(description = "随机状态码，用于验证", required = true)
            @RequestParam(value = "state") String state,
            HttpServletResponse response) throws IOException {

        log.info("收到 GitHub OAuth 回调: code={}, state={}", code, state);

        // 参数校验
        if (code == null || code.isBlank()) {
            log.warn("GitHub 回调缺少 code 参数");
            response.sendRedirect("http://localhost:5174/login?error=授权码不能为空");
            return;
        }
        if (state == null || state.isBlank()) {
            log.warn("GitHub 回调缺少 state 参数");
            response.sendRedirect("http://localhost:5174/login?error=状态码不能为空");
            return;
        }

        try {
            // 调用服务处理回调，完成登录
            LoginVO loginVO = githubOAuthService.handleCallback(code, state);

            log.info("GitHub 登录成功, token={}...", loginVO.token().substring(0, 10));

            // 重定向到前端页面，带上 token
            // TODO: 前端地址应该从配置读取
            // 使用 /oauth-callback 路径，避免被 Vite 代理到后端
            String redirectUrl = String.format(
                "http://localhost:5174/oauth-callback?token=%s&rememberMe=%s",
                loginVO.token(),
                loginVO.rememberMe()
            );
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("GitHub 登录失败: {}", e.getMessage(), e);
            response.sendRedirect("http://localhost:5174/login?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
        }
    }
}
