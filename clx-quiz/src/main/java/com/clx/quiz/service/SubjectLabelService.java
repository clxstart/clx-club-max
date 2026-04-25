package com.clx.quiz.service;

import com.clx.quiz.entity.SubjectLabel;

import java.util.List;

/**
 * 题目标签服务接口。
 */
public interface SubjectLabelService {

    /**
     * 获取所有标签。
     */
    List<SubjectLabel> getAll();

    /**
     * 获取某分类下的标签。
     */
    List<SubjectLabel> getByCategoryId(Long categoryId);

    /**
     * 根据id获取标签。
     */
    SubjectLabel getById(Long id);

    /**
     * 新增标签。
     */
    boolean add(SubjectLabel label);

    /**
     * 更新标签。
     */
    boolean update(SubjectLabel label);

    /**
     * 删除标签。
     */
    boolean delete(Long id);
}