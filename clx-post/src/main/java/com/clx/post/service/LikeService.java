package com.clx.post.service;

/**
 * 点赞服务接口。
 */
public interface LikeService {

    /**
     * 点赞帖子。
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 当前点赞数
     */
    int likePost(Long postId, Long userId);

    /**
     * 取消点赞帖子。
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 当前点赞数
     */
    int unlikePost(Long postId, Long userId);

    /**
     * 点赞评论。
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 当前点赞数
     */
    int likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论。
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 当前点赞数
     */
    int unlikeComment(Long commentId, Long userId);
}