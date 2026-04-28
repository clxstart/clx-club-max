package com.clx.api.auth.dto;

import lombok.Data;

/**
 * 角色 VO。
 */
@Data
public class RoleVO {

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 描述 */
    private String description;
}