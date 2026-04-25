package com.clx.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建评论请求。
 */
@Data
public class CommentCreateRequest {

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 2000, message = "评论长度1-2000字符")
    private String content;

    /** 父评论ID（0或不传表示一级评论） */
    private Long parentId;

    /** 回复的评论ID */
    private Long replyToId;
}