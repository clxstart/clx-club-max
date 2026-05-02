package com.clx.auth.mapper;

import com.clx.api.auth.dto.PermissionVO;
import com.clx.api.auth.dto.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证相关 Mapper。
 */
@Mapper
public interface AuthMapper {

    /**
     * 查询所有角色列表。
     */
    List<RoleVO> selectAllRoles();

    /**
     * 查询用户角色编码列表。
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有权限列表。
     */
    List<PermissionVO> selectAllPermissions();

    /**
     * 查询角色的权限ID列表。
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 新增角色。
     */
    int insertRole(RoleVO role);

    /**
     * 更新角色。
     */
    int updateRole(RoleVO role);

    /**
     * 删除角色（逻辑删除）。
     */
    int deleteRole(@Param("roleId") Long roleId);

    /**
     * 新增权限。
     */
    int insertPermission(PermissionVO permission);

    /**
     * 更新权限。
     */
    int updatePermission(PermissionVO permission);

    /**
     * 删除权限（逻辑删除）。
     */
    int deletePermission(@Param("permissionId") Long permissionId);

    /**
     * 删除角色的所有权限关联。
     */
    int deleteRolePermissions(@Param("roleId") Long roleId);

    /**
     * 新增角色权限关联。
     */
    int insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}