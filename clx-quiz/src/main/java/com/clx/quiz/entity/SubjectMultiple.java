package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 多选题选项实体。
 */
@Data
public class SubjectMultiple {

    /** 主键 */
    private Long id;

    /** 题目id */
    private Long subjectId;

    /** 选项类型：1A 2B 3C 4D 5E 6F */
    private Integer optionType;

    /** 选项内容 */
    private String optionContent;

    /** 是否正确：0否 1是 */
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