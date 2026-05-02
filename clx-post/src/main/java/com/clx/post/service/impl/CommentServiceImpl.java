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
 * 评论服务实现 - 支持递归多级评论。
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
        // 一次性查询帖子所有评论
        List<Comment> comments = commentMapper.selectByPostId(postId, 0, 1000);

        // 构建 parentId -> 子评论列表 的映射
        Map<Long, List<Comment>> childrenMap = comments.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 构建评论ID -> 评论 的映射（用于查找 replyTo 目标）
        Map<Long, Comment> commentMap = comments.stream()
                .collect(Collectors.toMap(Comment::getId, c -> c));

        // 找出所有一级评论，递归构建树
        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                CommentVO vo = buildCommentVO(comment, userId, childrenMap, commentMap);
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 递归构建评论VO（包含所有子评论）。
     */
    private CommentVO buildCommentVO(Comment comment, Long userId,
                                      Map<Long, List<Comment>> childrenMap,
                                      Map<Long, Comment> commentMap) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setCreatedAt(comment.getCreateTime());

        // 点赞状态
        boolean isLiked = userId != null && likeRecordMapper.exists(userId, "2", comment.getId());
        vo.setIsLiked(isLiked);

        // 作者信息
        CommentVO.AuthorVO authorVO = new CommentVO.AuthorVO();
        authorVO.setId(comment.getAuthorId());
        authorVO.setName(comment.getAuthorName());
        vo.setAuthor(authorVO);

        // 回复目标（@某人）
        if (comment.getReplyToId() != null && comment.getReplyToId() > 0) {
            Comment replyToComment = commentMap.get(comment.getReplyToId());
            if (replyToComment != null) {
                CommentVO.ReplyToVO replyToVO = new CommentVO.ReplyToVO();
                replyToVO.setCommentId(replyToComment.getId());
                replyToVO.setAuthorId(replyToComment.getAuthorId());
                replyToVO.setAuthorName(replyToComment.getAuthorName());
                vo.setReplyTo(replyToVO);
            }
        }

        // 递归处理子评论
        List<Comment> children = childrenMap.getOrDefault(comment.getId(), new ArrayList<>());
        for (Comment child : children) {
            CommentVO childVO = buildCommentVO(child, userId, childrenMap, commentMap);
            vo.getChildren().add(childVO);
        }

        return vo;
    }
}