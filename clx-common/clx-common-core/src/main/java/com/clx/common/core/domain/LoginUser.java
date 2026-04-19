package com.clx.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 登录用户上下文
 * 存储在Token中的用户信息
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /** 头像 */
    private String avatar;

    /** 组织ID */
    private Long orgId;

    /** 角色编码列表 */
    private Set<String> roles;

    /** 权限编码列表 */
    private Set<String> permissions;

    /** 用户状态：0正常 1禁用 2锁定 */
    private String status;

    /** 登录时间 */
    private Long loginTime;

    /** 登录IP */
    private String loginIp;

    /** Token过期时间 */
    private Long expireTime;

    // ========== 便捷方法 ==========

    public boolean isAdmin() {
        return roles != null && roles.contains("admin");
    }

    public boolean isNormal() {
        return "0".equals(status);
    }

    public boolean isLocked() {
        return "2".equals(status);
    }

    public boolean isDisabled() {
        return "1".equals(status);
    }

}
