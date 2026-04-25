package com.clx.quiz.dto;

import lombok.Data;

import java.util.List;

/**
 * 新增题目请求。
 */
@Data
public class SubjectCreateRequest {

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

    /** 分类ID列表 */
    private List<Long> categoryIds;

    /** 标签ID列表 */
    private List<Long> labelIds;

    /** 选项列表 */
    private List<SubjectOptionDTO> optionList;
}