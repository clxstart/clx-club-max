package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目标签实体。
 */
@Data
public class SubjectLabel {

    /** 主键 */
    private Long id;

    /** 标签名称 */
    private String labelName;

    /** 所属分类id */
    private Long categoryId;

    /** 排序号 */
    private Integer sortNum;

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
