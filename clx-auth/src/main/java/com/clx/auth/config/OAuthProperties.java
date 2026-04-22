package com.clx.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OAuth 第三方登录配置类
 * <p>
 * 功能：统一管理 GitHub、QQ、微信等第三方平台的 OAuth 配置
 * <p>
 * 配置来源：从 application.yml 中读取，支持环境变量覆盖
 * <p>
 * 使用方式：
 * <pre>
 * // 注入使用
 * @Autowired
 * private OAuthProperties oauthProperties;
 *
 * // 获取 GitHub 配置
 * String clientId = oauthProperties.getGithub().getClientId();
 * </pre>
 *
 * @author CLX
 * @since 2026-04-22
 * @see com.clx.auth.service.oauth.GithubOAuthService GitHub登录服务
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    /**
     * GitHub OAuth 配置
     * <p>
     * 申请地址：https://github.com/settings/developers
     * 需要配置：
     * 1. Homepage URL: http://localhost:5173
     * 2. Authorization callback URL: http://localhost:9100/auth/oauth/github/callback
     */
    private Github github = new Github();

    /**
     * QQ OAuth 配置
     * <p>
     * 申请地址：https://connect.qq.com/
     * 需要企业资质或个人开发者认证
     */
    private QQ qq = new QQ();

    /**
     * 微信 OAuth 配置
     * <p>
     * 申请地址：https://open.weixin.qq.com/
     * 需要企业资质
     */
    private Wechat wechat = new Wechat();

    /**
     * GitHub 配置内部类
     */
    @Data
    public static class Github {
        /**
         * GitHub OAuth App 的 Client ID
         * <p>
         * 获取方式：创建 OAuth App 后自动生成
         * 示例值：Iv1.abc123def456
         */
        private String clientId = "";

        /**
         * GitHub OAuth App 的 Client Secret
         * <p>
         * 获取方式：创建 OAuth App 后生成，只显示一次
         * 安全注意：这是敏感信息，不要提交到 Git！
         */
        private String clientSecret = "";

        /**
         * 授权回调地址
         * <p>
         * 必须与 GitHub 上配置的 Authorization callback URL 完全一致
         * 包括协议（http/https）、域名、端口、路径
         * <p>
         * 默认值：后端回调接口地址
         */
        private String redirectUri = "http://localhost:9100/auth/oauth/github/callback";
    }

    /**
     * QQ 配置内部类
     */
    @Data
    public static class QQ {
        /**
         * QQ 互联应用的 App ID
         */
        private String appId = "";

        /**
         * QQ 互联应用的 App Key
         */
        private String appKey = "";

        /**
         * 授权回调地址
         */
        private String redirectUri = "http://localhost:9100/auth/oauth/qq/callback";
    }

    /**
     * 微信配置内部类
     */
    @Data
    public static class Wechat {
        /**
         * 微信开放平台的 App ID
         */
        private String appId = "";

        /**
         * 微信开放平台的 App Secret
         */
        private String appSecret = "";

        /**
         * 授权回调地址
         */
        private String redirectUri = "http://localhost:9100/auth/oauth/wechat/callback";
    }
}
