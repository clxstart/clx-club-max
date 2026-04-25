package com.clx.quiz.dto;

import lombok.Data;

import java.util.List;

/**
 * 开始练习请求。
 */
@Data
public class PracticeStartRequest {

    /** 标签ID列表 */
    private List<Long> labelIds;

    /** 题目数量 */
    private Integer count = 10;
}