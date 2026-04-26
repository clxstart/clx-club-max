package com.clx.auth.controller;

import com.clx.auth.enums.OAuthPlatform;
import com.clx.auth.service.OAuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.domain.R;
import com.clx.common.core.exception.ServiceException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OAuth 登录控制器
 * <p>
 * 通用第三方登录接口，支持 GitHub/微信/钉钉等多平台
 * <p>
 * API 格式：/auth/oauth/{platform}/xxx
 *
 * @author CLX
 */
@Slf4j
@RestController
@RequestMapping("/auth/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth 登录")
public class OAuthController {

    /** 各平台 OAuth 服务实现，按平台类型映射 */
    private final List<OAuthService> oAuthServices;
    private Map<OAuthPlatform, OAuthService> serviceMap;

    /** 初始化服务映射（延迟加载） */
    private Map<OAuthPlatform, OAuthService> getServiceMap() {
        if (serviceMap == null) {
            serviceMap = oAuthServices.stream()
                    .collect(Collectors.toMap(OAuthService::getPlatform, Function.identity()));
        }
        return serviceMap;
    }

    /** 获取指定平台的服务 */
    private OAuthService getService(OAuthPlatform platform) {
        OAuthService service = getServiceMap().get(platform);
        if (service == null) {
            throw ServiceException.validationFailed("暂不支持 " + platform.getName() + " 登录");
        }
        return service;
    }

    /**
     * 获取授权 URL
     * <p>
     * 前端拿到 URL 后跳转，用户在第三方平台授权
     *
     * @param platform  平台标识（github/wechat/dingtalk）
     * @param redirect  是否直接 302 重定向
     */
    @GetMapping("/{platform}/authorize")
    @Operation(summary = "获取授权 URL")
    public R<String> getAuthorizeUrl(
            @Parameter(description = "平台：github/wechat/dingtalk", required = true)
            @PathVariable String platform,
            @Parameter(description = "是否直接重定向")
            @RequestParam(defaultValue = "false") boolean redirect,
            HttpServletResponse response) throws IOException {

        OAuthPlatform oauthPlatform = OAuthPlatform.fromCode(platform);
        if (oauthPlatform == null) {
            throw ServiceException.validationFailed("不支持的登录平台: " + platform);
        }

        log.info("获取 {} 授权 URL", oauthPlatform.getName());

        String authorizeUrl = getService(oauthPlatform).getAuthorizeUrl();

        if (redirect) {
            response.sendRedirect(authorizeUrl);
            return null;
        }

        return R.ok(authorizeUrl);
    }

    /**
     * OAuth 回调处理
     * <p>
     * 用户授权后，第三方平台重定向到这里，后端完成登录并跳转前端
     *
     * @param platform  平台标识
     * @param code      授权码
     * @param state     状态码（防 CSRF）
     */
    @GetMapping("/{platform}/callback")
    @Operation(summary = "OAuth 回调")
    public void handleCallback(
            @Parameter(description = "平台标识", required = true)
            @PathVariable String platform,
            @Parameter(description = "授权码", required = true)
            @RequestParam String code,
            @Parameter(description = "状态码", required = true)
            @RequestParam String state,
            HttpServletResponse response) throws IOException {

        OAuthPlatform oauthPlatform = OAuthPlatform.fromCode(platform);
        if (oauthPlatform == null) {
            response.sendRedirect(getErrorUrl("不支持的登录平台"));
            return;
        }

        log.info("收到 {} OAuth 回调: code={}, state={}", oauthPlatform.getName(), code, state);

        // 参数校验
        if (code == null || code.isBlank()) {
            response.sendRedirect(getErrorUrl("授权码不能为空"));
            return;
        }
        if (state == null || state.isBlank()) {
            response.sendRedirect(getErrorUrl("状态码不能为空"));
            return;
        }

        try {
            LoginVO loginVO = getService(oauthPlatform).handleCallback(code, state);

            log.info("{} 登录成功, token={}...", oauthPlatform.getName(), loginVO.token().substring(0, 10));

            // 重定向到前端，带上 token
            String redirectUrl = String.format(
                "http://localhost:5173?oauth_token=%s",
                loginVO.token()
            );
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("{} 登录失败: {}", oauthPlatform.getName(), e.getMessage(), e);
            response.sendRedirect(getErrorUrl(e.getMessage()));
        }
    }

    /** 构造错误跳转 URL */
    private String getErrorUrl(String message) throws IOException {
        return "http://localhost:5173?error=" + java.net.URLEncoder.encode(message, "UTF-8");
    }
}