package com.clx.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * "记住我"功能配置类。
 *
 * <p>当用户登录时勾选"记住我"选项，Token 有效期会延长，
 * 用户在一段时间内无需重新登录。
 *
 * <p>配置项：
 * <ul>
 *   <li>timeout：绝对有效期（默认30天），Token 的最大存活时间</li>
 *   <li>activeTimeout：活跃有效期（默认7天），无操作后 Token 过期时间</li>
 * </ul>
 *
 * <p>配置示例（application.yml）：
 * <pre>
 * clx:
 *   auth:
 *     remember-me:
 *       timeout: 2592000  # 30天（秒）
 *       active-timeout: 604800  # 7天（秒）
 * </pre>
 *
 * <p>注意：rememberMe 模式下，Token 有效期比普通登录更长，
 * 但仍依赖 Redis 存储，因此仍支持踢人下线。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "clx.auth.remember-me")
public class RememberMeProperties {

    /**
     * 勾选"记住我"后的 Token 绝对有效期（秒）。
     *
     * <p>默认值：30天 = 2592000秒
     *
     * <p>含义：从登录时刻开始计算，30天后 Token 必定过期，
     * 无论用户是否活跃。
     */
    private long timeout = TimeUnit.DAYS.toSeconds(30);

    /**
     * 勾选"记住我"后的 Token 活跃有效期（秒）。
     *
     * <p>默认值：7天 = 604800秒
     *
     * <p>含义：用户无任何操作7天后，Token 过期。
     * 每次有新请求时，活跃有效期会重新计算。
     *
     * <p>注意：活跃有效期不能超过绝对有效期。
     */
    private long activeTimeout = TimeUnit.DAYS.toSeconds(7);
}