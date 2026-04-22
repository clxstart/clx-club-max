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
 *   <li>insert：插入新用户（注册时使用）</li>
 *   <li>existsByUsername：检查用户名是否存在</li>
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

    /**
     * 插入新用户。
     *
     * <p>用于用户注册，插入用户基本信息。
     *
     * @param user 用户对象（包含 userId, username, password, nickname）
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 检查用户名是否已存在。
     *
     * <p>用于注册时唯一性校验，避免重复注册。
     *
     * @param username 用户名
     * @return true 如果用户名已存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否已存在。
     *
     * <p>用于注册时唯一性校验。
     *
     * @param email 邮箱地址
     * @return true 如果邮箱已存在
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根据邮箱更新密码。
     *
     * <p>用于密码重置功能。
     *
     * @param email 邮箱地址
     * @param newPassword 新密码（已加密）
     * @return 影响行数
     */
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);
}