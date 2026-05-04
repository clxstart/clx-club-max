package com.clx.api.auth.dto;

import lombok.Data;

/**
 * 权限 VO。
 */
@Data
public class PermissionVO {
    private Long permissionId;
    private String permissionName;
    private String permissionCode;
    private Integer permissionType;
    private Long parentId;
    private String path;
}