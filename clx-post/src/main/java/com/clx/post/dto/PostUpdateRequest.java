package com.clx.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新帖子请求 - 接收前端提交的帖子修改数据。
 */
@Data
public class PostUpdateRequest {

    /** 标题（必填，1-200字符） */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度1-200字符")
    private String title;

    /** 内容（必填，支持 Markdown） */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类ID（单选） */
    private Long categoryId;

    /** 标签ID列表（多选） */
    private List<Long> tagIds;
}