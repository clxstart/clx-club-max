package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论实体。
 */
@Data
public class Comment {

    /** 评论ID */
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 父评论ID（0表示一级评论） */
    private Long parentId;

    /** 回复的评论ID */
    private Long replyToId;

    /** 评论者ID */
    private Long authorId;

    /** 评论者名称（冗余） */
    private String authorName;

    /** 评论内容 */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /** 状态:0正常,1禁用 */
    private String status;

    /** 是否删除 */
    private Boolean isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}