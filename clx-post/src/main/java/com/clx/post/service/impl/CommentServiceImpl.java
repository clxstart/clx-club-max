package com.clx.post.service.impl;

import com.clx.post.dto.CommentCreateRequest;
import com.clx.post.entity.Comment;
import com.clx.post.mapper.CommentMapper;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import com.clx.post.service.CommentService;
import com.clx.post.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现。
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final LikeRecordMapper likeRecordMapper;

    @Override
    @Transactional
    public Long create(Long postId, CommentCreateRequest request, Long userId) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToId(request.getReplyToId());
        comment.setAuthorId(userId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus("0");
        comment.setIsDeleted(false);

        commentMapper.insert(comment);
        postMapper.incrementCommentCount(postId);

        return comment.getId();
    }

    @Override
    @Transactional
    public void delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        if (!comment.getAuthorId().equals(userId)) {
            throw new RuntimeException("无权删除此评论");
        }

        commentMapper.deleteById(commentId);
        postMapper.decrementCommentCount(comment.getPostId());
    }

    @Override
    public List<CommentVO> getList(Long postId, Long userId) {
        // 查询所有一级和二级评论
        List<Comment> comments = commentMapper.selectByPostId(postId, 0, 1000);

        // 转换为VO
        List<CommentVO> allVOs = comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setCreatedAt(comment.getCreateTime());

            // 检查是否点赞
            boolean isLiked = false;
            if (userId != null) {
                isLiked = likeRecordMapper.exists(userId, "2", comment.getId());
            }
            vo.setIsLiked(isLiked);

            // 作者信息
            CommentVO.AuthorVO authorVO = new CommentVO.AuthorVO();
            authorVO.setId(comment.getAuthorId());
            authorVO.setName(comment.getAuthorName());
            vo.setAuthor(authorVO);

            return vo;
        }).collect(Collectors.toList());

        // 构建树形结构
        Map<Long, List<CommentVO>> childrenMap = allVOs.stream()
                .filter(vo -> {
                    Comment c = comments.stream()
                            .filter(cm -> cm.getId().equals(vo.getId()))
                            .findFirst().orElse(null);
                    return c != null && c.getParentId() != null && c.getParentId() > 0;
                })
                .collect(Collectors.groupingBy(vo -> {
                    Comment c = comments.stream()
                            .filter(cm -> cm.getId().equals(vo.getId()))
                            .findFirst().orElse(null);
                    return c != null ? c.getParentId() : 0L;
                }));

        List<CommentVO> rootVOs = allVOs.stream()
                .filter(vo -> {
                    Comment c = comments.stream()
                            .filter(cm -> cm.getId().equals(vo.getId()))
                            .findFirst().orElse(null);
                    return c != null && (c.getParentId() == null || c.getParentId() == 0);
                })
                .collect(Collectors.toList());

        rootVOs.forEach(vo -> vo.setChildren(childrenMap.getOrDefault(vo.getId(), new ArrayList<>())));

        return rootVOs;
    }
}