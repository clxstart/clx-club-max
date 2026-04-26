package com.clx.user.mapper;

import com.clx.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户。
     */
    User selectById(@Param("userId") Long userId);

    /**
     * 更新用户资料。
     */
    int updateProfile(@Param("user") User user);

    /**
     * 增加获赞数。
     */
    int incrLikeTotalCount(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 增加关注数。
     */
    int incrFollowCount(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 增加粉丝数。
     */
    int incrFansCount(@Param("userId") Long userId, @Param("delta") int delta);
}
