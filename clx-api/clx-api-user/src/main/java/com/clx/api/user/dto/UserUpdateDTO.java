package com.clx.api.user.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新DTO
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别:0未知,1男,2女
     */
    private String gender;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 生日（字符串格式：yyyy-MM-dd）
     */
    private String birthday;

    /**
     * 状态:0正常,1禁用,2锁定
     */
    private String status;
}
