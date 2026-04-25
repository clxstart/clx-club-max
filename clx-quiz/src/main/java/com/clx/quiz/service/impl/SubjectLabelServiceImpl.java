package com.clx.quiz.service.impl;

import com.clx.quiz.entity.SubjectLabel;
import com.clx.quiz.mapper.SubjectLabelMapper;
import com.clx.quiz.service.SubjectLabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目标签服务实现。
 */
@Service
@RequiredArgsConstructor
public class SubjectLabelServiceImpl implements SubjectLabelService {

    private final SubjectLabelMapper labelMapper;

    @Override
    public List<SubjectLabel> getAll() {
        return labelMapper.selectAll();
    }

    @Override
    public List<SubjectLabel> getByCategoryId(Long categoryId) {
        return labelMapper.selectByCategoryId(categoryId);
    }

    @Override
    public SubjectLabel getById(Long id) {
        return labelMapper.selectById(id);
    }

    @Override
    public boolean add(SubjectLabel label) {
        return labelMapper.insert(label) > 0;
    }

    @Override
    public boolean update(SubjectLabel label) {
        return labelMapper.update(label) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return labelMapper.deleteById(id) > 0;
    }
}