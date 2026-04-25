package com.clx.quiz.service;

import com.clx.quiz.dto.SubjectCreateRequest;
import com.clx.quiz.dto.SubjectQueryRequest;
import com.clx.quiz.vo.SubjectDetailVO;
import com.clx.quiz.vo.SubjectVO;

import java.util.List;

/**
 * 题目服务接口。
 */
public interface SubjectService {

    /**
     * 新增题目。
     */
    boolean add(SubjectCreateRequest request, String createdBy);

    /**
     * 删除题目。
     */
    boolean delete(Long id);

    /**
     * 分页查询题目。
     */
    List<SubjectVO> queryPage(SubjectQueryRequest request);

    /**
     * 统计题目数量。
     */
    int count(SubjectQueryRequest request);

    /**
     * 获取题目详情（含答案）。
     */
    SubjectDetailVO getDetail(Long id);

    /**
     * 根据标签随机获取题目ID列表。
     */
    List<Long> getRandomSubjectIds(List<Long> labelIds, int limit);
}