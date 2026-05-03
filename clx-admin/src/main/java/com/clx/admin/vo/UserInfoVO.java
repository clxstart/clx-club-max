package com.clx.admin.vo;

import lombok.Data;

import java.util.List;

/**
 * 当前用户信息 VO（仅 admin 服务使用）。
 */
@Data
public class UserInfoVO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色列表 */
    private List<String> roles;
}
