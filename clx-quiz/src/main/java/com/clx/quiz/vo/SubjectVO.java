package com.clx.quiz.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目列表 VO。
 */
@Data
public class SubjectVO {

    /** 题目ID */
    private Long id;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型：1单选 2多选 3判断 4简答 */
    private Integer subjectType;

    /** 难度等级 */
    private Integer subjectDifficult;

    /** 分类名称 */
    private String categoryName;

    /** 标签名称列表 */
    private List<String> labelNames;
}