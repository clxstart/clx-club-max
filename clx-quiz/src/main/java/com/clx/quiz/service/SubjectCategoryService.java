package com.clx.quiz.service;

import com.clx.quiz.entity.SubjectCategory;

import java.util.List;

/**
 * 题目分类服务接口。
 */
public interface SubjectCategoryService {

    /**
     * 获取所有分类。
     */
    List<SubjectCategory> getAll();

    /**
     * 根据id获取分类。
     */
    SubjectCategory getById(Long id);

    /**
     * 新增分类。
     */
    boolean add(SubjectCategory category);

    /**
     * 更新分类。
     */
    boolean update(SubjectCategory category);

    /**
     * 删除分类。
     */
    boolean delete(Long id);
}