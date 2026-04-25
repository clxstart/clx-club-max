package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简答题实体。
 */
@Data
public class SubjectBrief {

    /** 主键 */
    private Long id;

    /** 题目id */
    private Long subjectId;

    /** 参考答案 */
    private String subjectAnswer;

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