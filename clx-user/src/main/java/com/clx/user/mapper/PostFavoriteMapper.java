package com.clx.user.mapper;

import com.clx.user.entity.PostFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏 Mapper。
 */
@Mapper
public interface PostFavoriteMapper {

    /**
     * 查询是否已收藏。
     */
    PostFavorite selectByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 插入收藏。
     */
    int insert(PostFavorite favorite);

    /**
     * 删除收藏。
     */
    int deleteByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 查询用户收藏列表（返回帖子ID）。
     */
    List<Long> selectPostIds(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计收藏数。
     */
    int count(@Param("userId") Long userId);
}