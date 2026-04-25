package com.clx.post.mapper;

import com.clx.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论 Mapper。
 */
@Mapper
public interface CommentMapper {

    /**
     * 插入评论。
     */
    int insert(Comment comment);

    /**
     * 根据ID查询评论。
     */
    Comment selectById(@Param("id") Long id);

    /**
     * 删除评论（软删除）。
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询帖子的评论列表。
     */
    List<Comment> selectByPostId(
            @Param("postId") Long postId,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询帖子评论总数。
     */
    int selectCountByPostId(@Param("postId") Long postId);

    /**
     * 增加点赞数。
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞数。
     */
    int decrementLikeCount(@Param("id") Long id);
}