package com.clx.api.user.dto;

import lombok.Data;

/**
 * 用户更新 DTO（管理员修改用户资料）。
 */
@Data
public class UserUpdateDTO {

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 个性签名 */
    private String signature;

    /** 性别:0未知,1男,2女 */
    private String gender;

    /** 生日 */
    private String birthday;
}