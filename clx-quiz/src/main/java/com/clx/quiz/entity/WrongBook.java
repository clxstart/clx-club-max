package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题本实体。
 */
@Data
public class WrongBook {

    /** 主键 */
    private Long id;

    /** 用户id */
    private Long userId;

    /** 题目id */
    private Long subjectId;

    /** 累计错误次数 */
    private Integer wrongCount;

    /** 最后答错时间 */
    private LocalDateTime lastWrongTime;

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