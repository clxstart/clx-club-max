package com.clx.admin.vo;

import lombok.Data;

/**
 * 权限 VO。
 */
@Data
public class PermissionVO {

    /** 权限ID */
    private Long permissionId;

    /** 权限名称 */
    private String permissionName;

    /** 权限编码 */
    private String permissionCode;

    /** 资源类型：1菜单，2按钮，3接口 */
    private String resourceType;

    /** 父ID */
    private Long parentId;

    /** 路由路径 */
    private String path;

    /** API路径 */
    private String apiPath;

    /** HTTP方法 */
    private String method;

    /** 排序 */
    private Integer sort;
}