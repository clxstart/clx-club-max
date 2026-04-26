package com.clx.user.service;

import com.clx.user.dto.ProfileUpdateDTO;
import com.clx.user.vo.UserProfileVO;

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
}