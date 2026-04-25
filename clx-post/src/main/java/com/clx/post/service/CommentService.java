package com.clx.post.service;

import com.clx.post.dto.CommentCreateRequest;
import com.clx.post.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口。
 */
public interface CommentService {

    /**
     * 创建评论。
     */
    Long create(Long postId, CommentCreateRequest request, Long userId);

    /**
     * 删除评论。
     */
    void delete(Long commentId, Long userId);

    /**
     * 获取帖子的评论列表。
     */
    List<CommentVO> getList(Long postId, Long userId);
}