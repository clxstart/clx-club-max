package com.clx.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.clx.common.core.constant.StatusConstants;
import lombok.Data;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId;
    private String username;
    private String password;
    private String nickname;
    private String status;

    /**
     * 判断用户是否正常
     */
    public boolean isNormal() {
        return StatusConstants.NORMAL.equals(status);
    }

    /**
     * 判断用户是否被禁用
     */
    public boolean isDisabled() {
        return StatusConstants.DISABLED.equals(status);
    }

    /**
     * 判断用户是否被锁定
     */
    public boolean isLocked() {
        return StatusConstants.LOCKED.equals(status);
    }

}