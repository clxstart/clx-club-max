package com.clx.user.service.impl;

import com.clx.user.dto.ProfileUpdateDTO;
import com.clx.user.entity.User;
import com.clx.user.mapper.ActiveMapper;
import com.clx.user.mapper.UserMapper;
import com.clx.user.service.UserService;
import com.clx.user.vo.ActiveUserVO;
import com.clx.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ActiveMapper activeMapper;

    @Override
    public UserProfileVO getProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // isFollowed 后续由 FollowService 提供，暂时默认 false
        return toProfileVO(user, false);
    }

    @Override
    public UserProfileVO getCurrentUserProfile(Long currentUserId) {
        User user = userMapper.selectById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toProfileVO(user, false);
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = new User();
        user.setUserId(userId);
        if (dto.nickname() != null) {
            user.setNickname(dto.nickname());
        }
        if (dto.avatar() != null) {
            user.setAvatar(dto.avatar());
        }
        if (dto.signature() != null) {
            user.setSignature(dto.signature());
        }
        if (dto.gender() != null) {
            user.setGender(dto.gender());
        }
        userMapper.updateProfile(user);
    }

    @Override
    public void incrLikeTotalCount(Long userId, int delta) {
        userMapper.incrLikeTotalCount(userId, delta);
    }

    @Override
    public List<ActiveUserVO> getActiveUsers(int limit) {
        return activeMapper.selectActiveUsers(limit);
    }

    private UserProfileVO toProfileVO(User user, boolean isFollowed) {
        return new UserProfileVO(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                user.getSignature(),
                user.getGender(),
                user.getFollowCount() != null ? user.getFollowCount() : 0,
                user.getFansCount() != null ? user.getFansCount() : 0,
                user.getLikeTotalCount() != null ? user.getLikeTotalCount() : 0,
                isFollowed
        );
    }
}