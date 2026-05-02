package com.clx.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论VO - 树形结构展示多级评论。
 */
@Data
public class CommentVO {

    /** 评论ID */
    private Long id;

    /** 评论内容 */
    private String content;

    /** 作者信息 */
    private AuthorVO author;

    /** 回复目标（@某人） */
    private ReplyToVO replyTo;

    /** 点赞数 */
    private Integer likeCount;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 子评论列表（递归） */
    private List<CommentVO> children = new ArrayList<>();

    /** 作者信息 */
    @Data
    public static class AuthorVO {
        private Long id;
        private String name;
        private String avatar;
    }

    /** 回复目标信息 */
    @Data
    public static class ReplyToVO {
        private Long commentId;
        private Long authorId;
        private String authorName;
    }
}