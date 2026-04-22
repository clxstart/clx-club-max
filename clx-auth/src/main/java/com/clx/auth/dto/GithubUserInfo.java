package com.clx.auth.dto;

import lombok.Data;

/**
 * GitHub 用户信息 DTO
 * <p>
 * 用途：存储从 GitHub API 获取的用户信息
 * <p>
 * 数据来源：调用 GitHub API https://api.github.com/user
 * <p>
 * 字段说明：只保留我们需要的字段，GitHub 返回的字段很多，这里只映射关键字段
 *
 * @author CLX
 * @since 2026-04-22
 * @see com.clx.auth.service.oauth.GithubOAuthService#fetchUserInfo 获取用户信息方法
 */
@Data
public class GithubUserInfo {

    /**
     * GitHub 用户唯一 ID
     * <p>
     * 重要：这是 GitHub 用户的唯一标识，不会变
     * 用途：与本地用户绑定，存储在 sys_social_bind.social_id 字段
     * <p>
     * 示例值：12345678
     */
    private Long id;

    /**
     * GitHub 登录名（用户名）
     * <p>
     * 用途：显示在界面上，让用户知道是哪个 GitHub 账号
     * <p>
     * 示例值："zhangsan"
     */
    private String login;

    /**
     * GitHub 昵称
     * <p>
     * 用途：如果用户设置了昵称，显示昵称更友好
     * 注意：可能为空，为空时用 login
     * <p>
     * 示例值："张三"
     */
    private String name;

    /**
     * 头像 URL
     * <p>
     * 用途：显示用户头像
     * <p>
     * 示例值："https://avatars.githubusercontent.com/u/12345678?v=4"
     */
    private String avatarUrl;

    /**
     * 用户邮箱
     * <p>
     * 注意：只有当用户在 GitHub 上设置了公开邮箱才会有值
     * 可能为空，为空时需要用户额外填写
     * <p>
     * 示例值："zhangsan@example.com"
     */
    private String email;

    /**
     * 个人主页
     * <p>
     * 用途：可选，展示用户的 GitHub 主页链接
     */
    private String htmlUrl;
}
