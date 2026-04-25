package com.clx.post.dto;

import lombok.Data;

/**
 * 帖子列表查询请求。
 */
@Data
public class PostListRequest {

    /** 页码（从1开始） */
    private Integer page = 1;

    /** 每页数量 */
    private Integer size = 20;

    /** 排序方式：latest/hot/recommend */
    private String sort = "latest";

    /** 分类ID */
    private Long categoryId;

    /** 标签ID */
    private Long tagId;
}