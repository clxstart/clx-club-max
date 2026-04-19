package com.clx.auth.entity;

import com.clx.common.core.constant.StatusConstants;

/**
 * 用户实体类。
 *
 * <p>对应数据库表 sys_user，存储用户基本信息。
 *
 * <p>字段说明：
 * <ul>
 *   <li>userId：用户ID，主键</li>
 *   <li>username：用户名，唯一，用于登录</li>
 *   <li>password：密码，BCrypt加密存储</li>
 *   <li>nickname：昵称，显示名称</li>
 *   <li>status：状态（0正常，1禁用，2锁定）</li>
 *   <li>isDeleted：删除标记（0未删除，1已删除）</li>
 * </ul>
 *
 * <p>注意：此实体不使用Lombok，保持纯Java实现，
 * 方便理解字段映射关系。
 */
public class User {

    /** 用户ID，主键自增 */
    private Long userId;

    /** 用户名，唯一索引，用于登录认证 */
    private String username;

    /** 密码，BCrypt加密存储（长度60字符） */
    private String password;

    /** 昵称，用户显示名称 */
    private String nickname;

    /** 状态：0正常，1禁用，2锁定 */
    private String status;

    /** 删除标记：0未删除，1已删除 */
    private Integer isDeleted;

    // ========== Getter/Setter ==========

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    // ========== 状态判断方法 ==========

    /**
     * 判断用户是否被禁用。
     *
     * @return true如果状态为"1禁用"
     */
    public boolean isDisabled() {
        return StatusConstants.DISABLED.equals(status);
    }

    /**
     * 判断用户是否被锁定。
     *
     * @return true如果状态为"2锁定"
     */
    public boolean isLocked() {
        return StatusConstants.LOCKED.equals(status);
    }

    /**
     * 判断用户是否已被删除。
     *
     * @return true如果isDeleted为1
     */
    public boolean isDeleted() {
        return Integer.valueOf(StatusConstants.DELETED).equals(isDeleted);
    }
}