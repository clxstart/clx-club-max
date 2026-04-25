package com.clx.quiz.vo;

import lombok.Data;

import java.util.List;

/**
 * 练习结果 VO。
 */
@Data
public class PracticeResultVO {

    /** 题目总数 */
    private Integer totalCount;

    /** 正确数 */
    private Integer correctCount;

    /** 正确率 */
    private Double correctRate;

    /** 用时 */
    private String timeUsed;

    /** 错题ID列表 */
    private List<Long> wrongSubjectIds;
}