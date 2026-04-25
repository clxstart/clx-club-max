package com.clx.quiz.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本 VO。
 */
@Data
public class WrongBookVO {

    /** 题目ID */
    private Long subjectId;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型 */
    private Integer subjectType;

    /** 累计错误次数 */
    private Integer wrongCount;

    /** 最后答错时间 */
    private LocalDateTime lastWrongTime;
}