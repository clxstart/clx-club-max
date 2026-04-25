package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目-分类-标签关联实体。
 */
@Data
public class SubjectMapping {

    /** 主键 */
    private Long id;

    /** 题目id */
    private Long subjectId;

    /** 分类id */
    private Long categoryId;

    /** 标签id */
    private Long labelId;

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