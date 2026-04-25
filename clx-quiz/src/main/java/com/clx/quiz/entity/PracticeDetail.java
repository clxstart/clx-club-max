package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 练习详情实体。
 */
@Data
public class PracticeDetail {

    /** 主键 */
    private Long id;

    /** 练习id */
    private Long practiceId;

    /** 题目id */
    private Long subjectId;

    /** 题目类型 */
    private Integer subjectType;

    /** 用户答案 */
    private String answerContent;

    /** 是否正确：0错 1对 2部分对 */
    private Integer isCorrect;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新人 */
    private String updateBy;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 是否删除：0否 1是 */
    private Integer isDeleted;
}