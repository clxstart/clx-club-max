package com.clx.auth.entity;

import com.clx.common.core.constant.StatusConstants;
import lombok.Data;

/**
 * 用户实体
 * <p>
 * 对应数据库表 sys_user，存储用户基本信息。
 *
 * @author CLX
 */
@Data
public class User {

    /** 用户ID，主键 */
    private Long userId;

    /** 用户名，唯一，用于登录 */
    private String username;

    /** 密码，BCrypt加密存储 */
    private String password;

    /** 昵称，显示名称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 状态：0正常，1禁用，2锁定 */
    private String status;

    /** 删除标记：0未删除，1已删除 */
    private Integer isDeleted;

    /** 用户是否被禁用 */
    public boolean isDisabled() {
        return StatusConstants.DISABLED.equals(status);
    }

    /** 用户是否被锁定 */
    public boolean isLocked() {
        return StatusConstants.LOCKED.equals(status);
    }

    /** 用户是否已删除 */
    public boolean isDeleted() {
        return Integer.valueOf(StatusConstants.DELETED).equals(isDeleted);
    }
}