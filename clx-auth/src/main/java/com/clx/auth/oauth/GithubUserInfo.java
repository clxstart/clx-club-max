package com.clx.auth.oauth;

import lombok.Data;

/**
 * GitHub 用户信息（OAuth 回调返回）
 */
@Data
public class GithubUserInfo {

    /** GitHub 用户唯一 ID（绑定用） */
    private Long id;

    /** GitHub 登录名 */
    private String login;

    /** 昵称（可能为空） */
    private String name;

    /** 头像 URL */
    private String avatarUrl;

    /** 邮箱（可能为空） */
    private String email;

    /** 个人主页 */
    private String htmlUrl;
}