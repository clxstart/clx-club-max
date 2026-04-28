package com.clx.user.service.impl;

import com.clx.api.user.dto.PageResultDTO;
import com.clx.api.user.dto.UserPageVO;
import com.clx.api.user.dto.UserQueryDTO;
import com.clx.api.user.dto.UserUpdateDTO;
import com.clx.user.dto.ProfileUpdateDTO;
import com.clx.user.entity.User;
import com.clx.user.mapper.ActiveMapper;
import com.clx.user.mapper.RoleMapper;
import com.clx.user.mapper.UserMapper;
import com.clx.user.service.UserService;
import com.clx.user.vo.ActiveUserVO;
import com.clx.user.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final ActiveMapper activeMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserProfileVO getProfile(Long userId, Long currentUserId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
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

    // ========== 管理后台接口实现 ==========

    @Override
    public PageResultDTO<UserPageVO> getUserPage(UserQueryDTO query) {
        // 参数校验
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        if (query.getSize() > 100) {
            query.setSize(100);
        }

        List<UserPageVO> records = userMapper.selectUserPage(query);
        long total = userMapper.countUsers(query);

        PageResultDTO<UserPageVO> result = new PageResultDTO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setCurrent(query.getPage());
        result.setSize(query.getSize());
        return result;
    }

    @Override
    public UserPageVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        UserPageVO vo = new UserPageVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        vo.setLastLoginTime(user.getLastLoginTime());
        // 角色列表需要单独查询
        vo.setRoles(Collections.emptyList());
        return vo;
    }

    @Override
    @Transactional
    public void adminUpdateUser(Long userId, UserUpdateDTO dto) {
        User user = new User();
        user.setUserId(userId);
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setSignature(dto.getSignature());
        user.setGender(dto.getGender());
        if (dto.getBirthday() != null) {
            user.setBirthday(LocalDate.parse(dto.getBirthday()));
        }
        userMapper.adminUpdateUser(userId, user);
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        userMapper.updateStatus(userId, "1");
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        userMapper.updateStatus(userId, "0");
    }

    @Override
    public List<Long> getUserRoleIds(Long userId) {
        return roleMapper.selectUserRoleIds(userId);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        // 先删除原有关联
        roleMapper.deleteUserRoles(userId);
        // 再插入新关联
        if (roleIds != null && !roleIds.isEmpty()) {
            roleMapper.insertUserRoles(userId, roleIds);
        }
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