package com.clx.auth.mapper;

import com.clx.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问 Mapper。
 *
 * <p>对应的XML映射文件：resources/mapper/UserMapper.xml
 *
 * <p>提供以下查询：
 * <ul>
 *   <li>selectByUsername：根据用户名查询用户（登录时使用）</li>
 *   <li>selectRoleCodesByUserId：查询用户拥有的角色编码列表</li>
 *   <li>selectPermissionCodesByUserId：查询用户拥有的权限编码列表</li>
 *   <li>updateLoginSuccess：登录成功后更新登录信息</li>
 * </ul>
 *
 * @see User 用户实体
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户。
     *
     * <p>只查询未删除的用户（is_deleted=0），用于登录认证。
     *
     * @param username 用户名
     * @return 用户对象，不存在时返回null
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询用户拥有的角色编码列表。
     *
     * <p>关联路径：sys_user_role -> sys_role
     * 只查询状态正常且未删除的角色。
     *
     * @param userId 用户ID
     * @return 角色编码列表，如 ["admin", "user"]
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 查询用户拥有的权限编码列表。
     *
     * <p>关联路径：sys_user_role -> sys_role -> sys_role_permission -> sys_permission
     * 只查询状态正常且未删除的角色和权限。
     *
     * @param userId 用户ID
     * @return 权限编码列表，如 ["user:add", "user:delete"]
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 登录成功后更新用户登录信息。
     *
     * <p>更新内容：最后登录IP、最后登录时间、登录次数+1
     *
     * @param userId  用户ID
     * @param loginIp 登录IP地址
     * @return 影响行数
     */
    int updateLoginSuccess(@Param("userId") Long userId, @Param("loginIp") String loginIp);
}