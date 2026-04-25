package com.clx.search.es;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ES 用户文档。
 */
@Data
public class UserDocument {

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

    /** 个性签名 */
    private String signature;

    /** 头像 */
    private String avatar;

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}