package com.clx.quiz.dto;

import lombok.Data;

/**
 * 提交答案请求。
 */
@Data
public class PracticeSubmitRequest {

    /** 练习id */
    private Long practiceId;

    /** 题目id */
    private Long subjectId;

    /** 题目类型 */
    private Integer subjectType;

    /** 用户答案 */
    private String answerContent;
}