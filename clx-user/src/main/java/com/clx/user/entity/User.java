package com.clx.user.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体（完整字段）。
 * <p>
 * 对应数据库表 sys_user，用于用户服务展示完整的用户资料。
 */
@Data
public class User {

    /** 用户ID */
    private Long userId;

    /** 用户名，唯一 */
    private String username;

    /** 密码 */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 个性签名 */
    private String signature;

    /** 性别:0未知,1男,2女 */
    private String gender;

    /** 生日 */
    private LocalDate birthday;

    /** 状态:0正常,1禁用,2锁定 */
    private String status;

    /** 删除标记 */
    private Integer isDeleted;

    /** 获赞总数 */
    private Integer likeTotalCount;

    /** 关注数 */
    private Integer followCount;

    /** 粉丝数 */
    private Integer fansCount;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
