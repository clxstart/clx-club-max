package com.clx.user.mapper;

import com.clx.user.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 关注关系 Mapper。
 */
@Mapper
public interface UserFollowMapper {

    /**
     * 查询是否已关注。
     */
    UserFollow selectByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId);

    /**
     * 插入关注关系。
     */
    int insert(UserFollow follow);

    /**
     * 删除关注关系。
     */
    int deleteByUserAndTarget(@Param("userId") Long userId, @Param("targetId") Long targetId);

    /**
     * 查询关注列表。
     */
    List<UserFollow> selectFollowing(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询粉丝列表。
     */
    List<UserFollow> selectFans(@Param("targetId") Long targetId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计关注数。
     */
    int countFollowing(@Param("userId") Long userId);

    /**
     * 统计粉丝数。
     */
    int countFans(@Param("targetId") Long targetId);
}