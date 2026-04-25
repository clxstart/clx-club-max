package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;

import java.util.List;

/**
 * 题型处理策略接口。
 */
public interface SubjectTypeHandler {

    /**
     * 获取题型：1单选 2多选 3判断 4简答。
     */
    int getType();

    /**
     * 保存题目选项/答案。
     *
     * @param subjectId  题目id
     * @param optionList 选项列表
     * @param createdBy  创建人
     */
    void save(Long subjectId, List<SubjectOptionDTO> optionList, String createdBy);

    /**
     * 查询题目选项/答案（含正确答案）。
     *
     * @param subjectId 题目id
     * @return 选项列表（含isCorrect）
     */
    List<SubjectOptionDTO> getWithOptions(Long subjectId);

    /**
     * 查询题目选项（不含正确答案，用于答题）。
     *
     * @param subjectId 题目id
     * @return 选项列表（不含isCorrect）
     */
    List<SubjectOptionDTO> getWithoutAnswer(Long subjectId);

    /**
     * 判断用户答案是否正确。
     *
     * @param subjectId     题目id
     * @param answerContent 用户答案
     * @return 0错 1对 2部分对
     */
    int judge(Long subjectId, String answerContent);

    /**
     * 获取正确答案。
     *
     * @param subjectId 题目id
     * @return 正确答案
     */
    String getCorrectAnswer(Long subjectId);

    /**
     * 删除题目选项/答案。
     *
     * @param subjectId 题目id
     */
    void delete(Long subjectId);
}