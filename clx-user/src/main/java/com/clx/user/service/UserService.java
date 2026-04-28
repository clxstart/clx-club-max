package com.clx.user.service;

import com.clx.api.user.dto.PageResultDTO;
import com.clx.api.user.dto.UserPageVO;
import com.clx.api.user.dto.UserQueryDTO;
import com.clx.api.user.dto.UserUpdateDTO;
import com.clx.user.dto.ProfileUpdateDTO;
import com.clx.user.vo.ActiveUserVO;
import com.clx.user.vo.UserProfileVO;

import java.util.List;

/**
 * 用户服务接口。
 */
public interface UserService {

    /**
     * 获取用户资料。
     */
    UserProfileVO getProfile(Long userId, Long currentUserId);

    /**
     * 获取当前用户资料。
     */
    UserProfileVO getCurrentUserProfile(Long currentUserId);

    /**
     * 更新用户资料。
     */
    void updateProfile(Long userId, ProfileUpdateDTO dto);

    /**
     * 增加获赞数。
     */
    void incrLikeTotalCount(Long userId, int delta);

    /**
     * 获取活跃用户排行。
     */
    List<ActiveUserVO> getActiveUsers(int limit);

    // ========== 管理后台接口 ==========

    /**
     * 分页查询用户列表。
     */
    PageResultDTO<UserPageVO> getUserPage(UserQueryDTO query);

    /**
     * 获取用户详情（管理员用）。
     */
    UserPageVO getUserById(Long userId);

    /**
     * 管理员更新用户资料。
     */
    void adminUpdateUser(Long userId, UserUpdateDTO dto);

    /**
     * 封禁用户。
     */
    void banUser(Long userId);

    /**
     * 解封用户。
     */
    void unbanUser(Long userId);

    /**
     * 获取用户角色ID列表。
     */
    List<Long> getUserRoleIds(Long userId);

    /**
     * 更新用户角色。
     */
    void updateUserRoles(Long userId, List<Long> roleIds);
}