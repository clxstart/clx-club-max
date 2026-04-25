package com.clx.auth.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交账号绑定实体
 * <p>
 * 用户绑定的第三方平台账号（GitHub、微信等），一个用户可绑定多个平台。
 *
 * @author CLX
 * @since 2026-04-22
 */
@Data
public class SocialBind {

    /** 主键ID */
    private Long id;

    /** 本地用户ID，关联 sys_user.user_id */
    private Long userId;

    /** 平台类型：github/wechat/dingtalk */
    private String socialType;

    /** 第三方平台用户唯一标识 */
    private String socialId;

    /** 第三方平台昵称 */
    private String socialName;

    /** 第三方平台头像URL */
    private String socialAvatar;

    /** 访问令牌（敏感信息，可加密存储） */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 令牌过期时间 */
    private LocalDateTime tokenExpireTime;

    /** 绑定时间 */
    private LocalDateTime bindTime;

    /** 删除标记：0未删除，1已删除 */
    private Integer isDeleted;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
