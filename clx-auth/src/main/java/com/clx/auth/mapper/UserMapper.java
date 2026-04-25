package com.clx.auth.mapper;

import com.clx.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问 Mapper
 * <p>
 * XML 映射文件：resources/mapper/UserMapper.xml
 *
 * @author CLX
 */
@Mapper
public interface UserMapper {

    /** 根据用户名查询用户（登录用，只查未删除） */
    User selectByUsername(@Param("username") String username);

    /** 查询用户角色编码列表（关联 sys_user_role -> sys_role） */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /** 查询用户权限编码列表（关联 sys_user_role -> sys_role -> sys_role_permission -> sys_permission） */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /** 登录成功后更新登录信息（IP、时间、次数） */
    int updateLoginSuccess(@Param("userId") Long userId, @Param("loginIp") String loginIp);

    /** 插入新用户（注册用） */
    int insert(User user);

    /** 用户名是否已存在（注册唯一性校验） */
    boolean existsByUsername(@Param("username") String username);

    /** 邮箱是否已存在（注册唯一性校验） */
    boolean existsByEmail(@Param("email") String email);

    /** 根据邮箱更新密码（密码重置用） */
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);

    /** 根据用户ID查询用户 */
    User selectById(@Param("userId") Long userId);

    /** 根据手机号查询用户（手机号登录用） */
    User selectByPhone(@Param("phone") String phone);

    /** 手机号是否已存在 */
    boolean existsByPhone(@Param("phone") String phone);
}
