package com.clx.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论请求 - 接收前端提交的评论数据。
 *
 * 支持树形评论：parentId 实现层级，replyToId 实现 @回复。
 */
@Data
public class CommentCreateRequest {

    /** 评论内容（必填，1-2000字符） */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 2000, message = "评论长度1-2000字符")
    private String content;

    /** 父评论ID（不传表示一级评论，传值表示回复） */
    private Long parentId;

    /** 回复的评论ID（用于显示 @某用户） */
    private Long replyToId;
}