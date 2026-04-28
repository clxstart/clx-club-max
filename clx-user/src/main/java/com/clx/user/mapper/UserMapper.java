package com.clx.user.mapper;

import com.clx.api.user.dto.UserPageVO;
import com.clx.api.user.dto.UserQueryDTO;
import com.clx.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    // ========== 管理后台接口 ==========

    /**
     * 分页查询用户列表。
     */
    List<UserPageVO> selectUserPage(@Param("query") UserQueryDTO query);

    /**
     * 统计用户总数。
     */
    long countUsers(@Param("query") UserQueryDTO query);

    /**
     * 更新用户状态。
     */
    int updateStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 管理员更新用户资料。
     */
    int adminUpdateUser(@Param("userId") Long userId, @Param("user") User user);
}
