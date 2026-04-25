package com.clx.quiz.dto;

import lombok.Data;

/**
 * 题目选项 DTO。
 */
@Data
public class SubjectOptionDTO {

    /** 选项类型：1A 2B 3C 4D 5E 6F */
    private Integer optionType;

    /** 选项内容 */
    private String optionContent;

    /** 是否正确：0否 1是 */
    private Integer isCorrect;
}