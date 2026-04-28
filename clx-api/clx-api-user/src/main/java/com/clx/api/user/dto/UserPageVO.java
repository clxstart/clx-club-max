package com.clx.api.user.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户分页列表项 VO。
 */
@Data
public class UserPageVO {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态:0正常,1封禁 */
    private String status;

    /** 角色列表 */
    private List<String> roles;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}