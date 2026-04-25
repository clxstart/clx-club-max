package com.clx.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO。
 */
@Data
public class CommentVO {

    /** 评论ID */
    private Long id;

    /** 评论内容 */
    private String content;

    /** 作者信息 */
    private AuthorVO author;

    /** 点赞数 */
    private Integer likeCount;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 子评论列表 */
    private List<CommentVO> children;

    /**
     * 作者信息。
     */
    @Data
    public static class AuthorVO {
        private Long id;
        private String name;
        private String avatar;
    }
}