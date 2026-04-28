package com.clx.auth.mapper;

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
}