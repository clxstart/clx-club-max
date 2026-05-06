package com.clx.api.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 */
@Data
public class UserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态:0正常,1禁用,2锁定
     */
    private String status;

    /**
     * 当前页
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;
}
