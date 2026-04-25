package com.clx.quiz.vo;

import lombok.Data;

/**
 * 提交答案结果 VO。
 */
@Data
public class SubmitResultVO {

    /** 是否正确：0错 1对 2部分对（简答题为2，需用户自评） */
    private Integer isCorrect;

    /** 正确答案 */
    private String correctAnswer;

    /** 题目解析 */
    private String subjectParse;

    /** 是否需要用户自评（简答题为true） */
    private Boolean needSelfJudge;
}