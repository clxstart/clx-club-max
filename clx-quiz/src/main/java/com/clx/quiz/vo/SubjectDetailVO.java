package com.clx.quiz.vo;

import com.clx.quiz.dto.SubjectOptionDTO;
import lombok.Data;

import java.util.List;

/**
 * 题目详情 VO。
 */
@Data
public class SubjectDetailVO {

    /** 题目ID */
    private Long id;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型：1单选 2多选 3判断 4简答 */
    private Integer subjectType;

    /** 难度等级 */
    private Integer subjectDifficult;

    /** 题目分数 */
    private Integer subjectScore;

    /** 题目解析 */
    private String subjectParse;

    /** 分类ID列表 */
    private List<Long> categoryIds;

    /** 标签ID列表 */
    private List<Long> labelIds;

    /** 选项列表（含答案） */
    private List<SubjectOptionDTO> optionList;
}