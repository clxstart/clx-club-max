package com.clx.quiz.vo;

import com.clx.quiz.dto.SubjectOptionDTO;
import lombok.Data;

import java.util.List;

/**
 * 练习题目 VO（不含答案）。
 */
@Data
public class PracticeSubjectVO {

    /** 题目ID */
    private Long subjectId;

    /** 题目名称 */
    private String subjectName;

    /** 题目类型 */
    private Integer subjectType;

    /** 难度等级 */
    private Integer subjectDifficult;

    /** 选项列表（不含答案） */
    private List<SubjectOptionDTO> optionList;
}