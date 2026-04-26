package com.clx.user.service.impl;

import com.clx.user.entity.User;
import com.clx.user.entity.UserFollow;
import com.clx.user.mapper.UserFollowMapper;
import com.clx.user.mapper.UserMapper;
import com.clx.user.service.FollowService;
import com.clx.user.vo.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 关注服务实现。
 */
@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper followMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public int follow(Long userId, Long targetId) {
        // 不能关注自己
        if (userId.equals(targetId)) {
            throw new RuntimeException("不能关注自己");
        }
        // 检查是否已关注
        UserFollow existing = followMapper.selectByUserAndTarget(userId, targetId);
        if (existing != null) {
            throw new RuntimeException("已关注该用户");
        }
        // 插入关注关系
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setTargetId(targetId);
        followMapper.insert(follow);
        // 同步更新计数
        userMapper.incrFollowCount(userId, 1);
        userMapper.incrFansCount(targetId, 1);
        return followMapper.countFollowing(userId);
    }

    @Override
    @Transactional
    public int unfollow(Long userId, Long targetId) {
        // 检查是否已关注
        UserFollow existing = followMapper.selectByUserAndTarget(userId, targetId);
        if (existing == null) {
            throw new RuntimeException("未关注该用户");
        }
        // 删除关注关系
        followMapper.deleteByUserAndTarget(userId, targetId);
        // 同步更新计数
        userMapper.incrFollowCount(userId, -1);
        userMapper.incrFansCount(targetId, -1);
        return followMapper.countFollowing(userId);
    }

    @Override
    public boolean isFollowed(Long userId, Long targetId) {
        if (userId == null) {
            return false;
        }
        return followMapper.selectByUserAndTarget(userId, targetId) != null;
    }

    @Override
    public List<UserSimpleVO> getFollowing(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<UserFollow> follows = followMapper.selectFollowing(userId, offset, size);
        List<UserSimpleVO> result = new ArrayList<>();
        for (UserFollow follow : follows) {
            User user = userMapper.selectById(follow.getTargetId());
            if (user != null) {
                result.add(new UserSimpleVO(user.getUserId(), user.getNickname(), user.getAvatar(), user.getSignature()));
            }
        }
        return result;
    }

    @Override
    public List<UserSimpleVO> getFans(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<UserFollow> follows = followMapper.selectFans(userId, offset, size);
        List<UserSimpleVO> result = new ArrayList<>();
        for (UserFollow follow : follows) {
            User user = userMapper.selectById(follow.getUserId());
            if (user != null) {
                result.add(new UserSimpleVO(user.getUserId(), user.getNickname(), user.getAvatar(), user.getSignature()));
            }
        }
        return result;
    }

    @Override
    public int countFollowing(Long userId) {
        return followMapper.countFollowing(userId);
    }

    @Override
    public int countFans(Long userId) {
        return followMapper.countFans(userId);
    }
}