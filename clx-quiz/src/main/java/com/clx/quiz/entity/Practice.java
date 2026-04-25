package com.clx.quiz.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 练习记录实体。
 */
@Data
public class Practice {

    /** 主键 */
    private Long id;

    /** 用户id */
    private Long userId;

    /** 题目总数 */
    private Integer totalCount;

    /** 正确数 */
    private Integer correctCount;

    /** 正确率 */
    private BigDecimal correctRate;

    /** 用时（秒） */
    private Integer timeUsed;

    /** 状态：0进行中 1已完成 */
    private Integer status;

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