package com.clx.quiz.dto;

import lombok.Data;

/**
 * 查询题目请求。
 */
@Data
public class SubjectQueryRequest {

    /** 分类ID */
    private Long categoryId;

    /** 标签ID */
    private Long labelId;

    /** 关键词 */
    private String keyword;

    /** 页码 */
    private Integer pageNo = 1;

    /** 每页数量 */
    private Integer pageSize = 10;
}