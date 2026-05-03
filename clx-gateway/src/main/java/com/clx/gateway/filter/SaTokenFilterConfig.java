package com.clx.gateway.filter;

import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * sa-Token 路由拦截器配置。
 *
 * <p>在 Gateway 层进行统一认证，验证通过后请求转发到下游服务。
 */
@Configuration
public class SaTokenFilterConfig {

    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                // 拦截所有路由
                .addInclude("/**")
                // 排除不需要认证的路径
                .addExclude(
                        // 认证相关
                        "/auth/login",
                        "/auth/register",
                        "/auth/captcha",
                        // 静态资源
                        "/*.html",
                        "/*.css",
                        "/*.js",
                        "/favicon.ico",
                        // 健康检查
                        "/actuator/**",
                        // Knife4j 接口文档
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**"
                )
                // 认证函数
                .setAuth(obj -> {
                    // 登录校验
                    SaRouter.match("/**")
                            .notMatch(excludePaths())
                            .check(r -> StpUtil.checkLogin());
                })
                // 异常处理
                .setError(e -> SaResult
                        .code(401)
                        .setMsg("未登录或登录已过期，请重新登录")
                );
    }

    /**
     * 不需要登录的路径。
     */
    private String[] excludePaths() {
        return new String[]{
                "/auth/login",
                "/auth/register",
                "/auth/captcha",
                "/auth/sms-code",
                "/auth/oauth/**",
                // 搜索服务公开接口
                "/search/hot",
                "/search/suggest",
                // 帖子公开接口（浏览帖子不需要登录）
                "/post/list",
                "/post/detail/**",
                "/category/**",
                "/tag/**",
                // 用户公开信息
                "/user/profile/**"
        };
    }
}
