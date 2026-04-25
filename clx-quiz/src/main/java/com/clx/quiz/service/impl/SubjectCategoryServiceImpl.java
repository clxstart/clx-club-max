package com.clx.quiz.service.impl;

import com.clx.quiz.entity.SubjectCategory;
import com.clx.quiz.mapper.SubjectCategoryMapper;
import com.clx.quiz.service.SubjectCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目分类服务实现。
 */
@Service
@RequiredArgsConstructor
public class SubjectCategoryServiceImpl implements SubjectCategoryService {

    private final SubjectCategoryMapper categoryMapper;

    @Override
    public List<SubjectCategory> getAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public SubjectCategory getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public boolean add(SubjectCategory category) {
        return categoryMapper.insert(category) > 0;
    }

    @Override
    public boolean update(SubjectCategory category) {
        return categoryMapper.update(category) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
}