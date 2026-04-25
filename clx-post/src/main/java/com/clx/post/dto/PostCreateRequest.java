package com.clx.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建帖子请求。
 */
@Data
public class PostCreateRequest {

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度1-200字符")
    private String title;

    /** 内容 */
    @NotBlank(message = "内容不能为空")
    private String content;

    /** 分类ID */
    private Long categoryId;

    /** 标签ID列表 */
    private List<Long> tagIds;
}