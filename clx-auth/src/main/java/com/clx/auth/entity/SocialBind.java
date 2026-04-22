package com.clx.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交账号绑定实体类
 * <p>
 * 用途：存储用户绑定的第三方平台账号（GitHub、QQ、微信等）
 * <p>
 * 对应数据库表：sys_social_bind
 * <p>
 * 设计说明：
 * <ul>
 *   <li>一个本地用户可以绑定多个第三方账号</li>
 *   <li>同一个第三方平台的同一个账号只能绑定一个本地用户</li>
 *   <li>通过 social_type + social_id 唯一确定一个第三方账号</li>
 * </ul>
 *
 * @author CLX
 * @since 2026-04-22
 * @see com.clx.auth.service.oauth.GithubOAuthService
 */
@Data
public class SocialBind {

    /**
     * 主键ID
     * <p>
     * 数据库自增，无业务含义
     */
    private Long id;

    /**
     * 本地用户ID
     * <p>
     * 关联 sys_user 表的 user_id
     * 用途：知道这是哪个本地用户的第三方账号
     * <p>
     * 示例值：1713765432000123
     */
    private Long userId;

    /**
     * 平台类型
     * <p>
     * 用途：区分是哪个第三方平台
     * <p>
     * 可选值：
     * <ul>
     *   <li>github - GitHub</li>
     *   <li>phone - 手机号</li>
     *   <li>qq - QQ</li>
     *   <li>wechat - 微信</li>
     * </ul>
     * <p>
     * 示例值："github"
     */
    private String socialType;

    /**
     * 第三方平台的唯一标识
     * <p>
     * 用途：在第三方平台中唯一标识这个用户
     * <p>
     * 不同平台的示例值：
     * <ul>
     *   <li>GitHub：12345678（数字ID）</li>
     *   <li>QQ：123456789（数字ID）</li>
     *   <li>微信：openid 字符串</li>
     *   <li>手机号：13800138000</li>
     * </ul>
     */
    private String socialId;

    /**
     * 第三方平台的昵称
     * <p>
     * 用途：显示在界面上，让用户知道绑定了哪个账号
     * <p>
     * 示例值："zhangsan"（GitHub用户名）
     */
    private String socialName;

    /**
     * 第三方平台的头像URL
     * <p>
     * 用途：显示用户头像
     * <p>
     * 示例值："https://avatars.githubusercontent.com/u/12345678?v=4"
     */
    private String socialAvatar;

    /**
     * 访问令牌
     * <p>
     * 用途：调用第三方API时使用
     * 注意：敏感信息，数据库中可加密存储
     * <p>
     * 示例值："gho_xxxxxxxxxxxx"
     */
    private String accessToken;

    /**
     * 刷新令牌
     * <p>
     * 用途：access_token 过期后用于获取新的 token
     * 注意：敏感信息，数据库中可加密存储
     */
    private String refreshToken;

    /**
     * 令牌过期时间
     * <p>
     * 用途：判断 access_token 是否过期，过期前需要刷新
     */
    private LocalDateTime tokenExpireTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime bindTime;

    /**
     * 删除标记：0未删除，1已删除
     */
    private Integer isDeleted;

    /**
     * 记录更新时间
     */
    private LocalDateTime updateTime;
}
