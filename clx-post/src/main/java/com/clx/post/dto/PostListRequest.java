package com.clx.post.dto;

import lombok.Data;

/**
 * 帖子列表查询请求 - 接收分页、排序、筛选参数。
 *
 * 使用方式：GET /post/list?page=1&size=20&sort=hot&categoryId=1&tagId=1
 */
@Data
public class PostListRequest {

    /** 页码（默认第1页） */
    private Integer page = 1;

    /** 每页数量（默认20条） */
    private Integer size = 20;

    /** 排序方式：latest(最新) / hot(热门) / recommend(推荐) */
    private String sort = "latest";

    /** 分类ID（可选，筛选指定分类） */
    private Long categoryId;

    /** 标签ID（可选，筛选指定标签） */
    private Long tagId;
}