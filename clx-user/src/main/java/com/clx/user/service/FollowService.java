package com.clx.user.service;

import com.clx.user.vo.UserSimpleVO;

import java.util.List;

/**
 * 关注服务接口。
 */
public interface FollowService {

    /**
     * 关注用户。
     * @return 新的关注数
     */
    int follow(Long userId, Long targetId);

    /**
     * 取消关注。
     * @return 新的关注数
     */
    int unfollow(Long userId, Long targetId);

    /**
     * 是否已关注。
     */
    boolean isFollowed(Long userId, Long targetId);

    /**
     * 获取关注列表。
     */
    List<UserSimpleVO> getFollowing(Long userId, int page, int size);

    /**
     * 获取粉丝列表。
     */
    List<UserSimpleVO> getFans(Long userId, int page, int size);

    /**
     * 统计关注数。
     */
    int countFollowing(Long userId);

    /**
     * 统计粉丝数。
     */
    int countFans(Long userId);
}