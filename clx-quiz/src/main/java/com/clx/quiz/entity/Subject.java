package com.clx.quiz.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目主表实体。
 */
@Data
public class Subject {

    /** 主键 */
    private Long id;

    /** 题目名称/题干 */
    private String subjectName;

    /** 题目类型：1单选 2多选 3判断 4简答 */
    private Integer subjectType;

    /** 难度等级：1简单 2中等 3较难 4困难 5专家 */
    private Integer subjectDifficult;

    /** 题目分数 */
    private Integer subjectScore;

    /** 题目解析 */
    private String subjectParse;

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