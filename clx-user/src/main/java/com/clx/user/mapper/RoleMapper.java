package com.clx.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper（管理后台用）。
 */
@Mapper
public interface RoleMapper {

    /**
     * 查询用户角色ID列表。
     */
    List<Long> selectUserRoleIds(@Param("userId") Long userId);

    /**
     * 删除用户角色关联。
     */
    int deleteUserRoles(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联。
     */
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}