package com.clx.api.auth.dto;

import lombok.Data;

/**
 * 角色 VO。
 */
@Data
public class RoleVO {
    private Long roleId;
    private String roleName;
    private String roleCode;
    private String description;
}