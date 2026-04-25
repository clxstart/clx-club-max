package com.clx.quiz.service.impl;

import com.clx.quiz.dto.SubjectCreateRequest;
import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.dto.SubjectQueryRequest;
import com.clx.quiz.entity.Subject;
import com.clx.quiz.entity.SubjectCategory;
import com.clx.quiz.entity.SubjectLabel;
import com.clx.quiz.entity.SubjectMapping;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.mapper.SubjectMappingMapper;
import com.clx.quiz.service.SubjectCategoryService;
import com.clx.quiz.service.SubjectLabelService;
import com.clx.quiz.service.SubjectService;
import com.clx.quiz.service.handler.SubjectTypeHandler;
import com.clx.quiz.service.handler.SubjectTypeHandlerFactory;
import com.clx.quiz.vo.SubjectDetailVO;
import com.clx.quiz.vo.SubjectVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目服务实现。
 */
@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectMapper subjectMapper;
    private final SubjectMappingMapper mappingMapper;
    private final SubjectTypeHandlerFactory handlerFactory;
    private final SubjectCategoryService categoryService;
    private final SubjectLabelService labelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean add(SubjectCreateRequest request, String createdBy) {
        // 插入题目主表
        Subject subject = new Subject();
        subject.setSubjectName(request.getSubjectName());
        subject.setSubjectType(request.getSubjectType());
        subject.setSubjectDifficult(request.getSubjectDifficult());
        subject.setSubjectScore(request.getSubjectScore());
        subject.setSubjectParse(request.getSubjectParse());
        subject.setCreatedBy(createdBy);
        subjectMapper.insert(subject);

        Long subjectId = subject.getId();

        // 使用策略保存选项/答案
        SubjectTypeHandler handler = handlerFactory.getHandler(request.getSubjectType());
        handler.save(subjectId, request.getOptionList(), createdBy);

        // 保存分类标签关联
        List<SubjectMapping> mappings = new ArrayList<>();
        if (request.getCategoryIds() != null) {
            for (Long categoryId : request.getCategoryIds()) {
                SubjectMapping mapping = new SubjectMapping();
                mapping.setSubjectId(subjectId);
                mapping.setCategoryId(categoryId);
                mapping.setCreatedBy(createdBy);
                mappings.add(mapping);
            }
        }
        if (request.getLabelIds() != null) {
            for (Long labelId : request.getLabelIds()) {
                SubjectMapping mapping = new SubjectMapping();
                mapping.setSubjectId(subjectId);
                mapping.setLabelId(labelId);
                mapping.setCreatedBy(createdBy);
                mappings.add(mapping);
            }
        }
        if (!mappings.isEmpty()) {
            mappingMapper.batchInsert(mappings);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        // 删除题目选项/答案
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            return false;
        }
        SubjectTypeHandler handler = handlerFactory.getHandler(subject.getSubjectType());
        handler.delete(id);

        // 删除关联
        mappingMapper.deleteBySubjectId(id);

        // 删除题目主表
        return subjectMapper.deleteById(id) > 0;
    }

    @Override
    public List<SubjectVO> queryPage(SubjectQueryRequest request) {
        int offset = (request.getPageNo() - 1) * request.getPageSize();
        List<Subject> subjects = subjectMapper.selectPage(
                request.getCategoryId(),
                request.getLabelId(),
                request.getKeyword(),
                offset,
                request.getPageSize()
        );

        // 获取分类和标签名称
        List<SubjectCategory> categories = categoryService.getAll();
        List<SubjectLabel> labels = labelService.getAll();
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(SubjectCategory::getId, SubjectCategory::getCategoryName));
        Map<Long, String> labelMap = labels.stream()
                .collect(Collectors.toMap(SubjectLabel::getId, SubjectLabel::getLabelName));

        return subjects.stream().map(s -> {
            SubjectVO vo = new SubjectVO();
            vo.setId(s.getId());
            vo.setSubjectName(s.getSubjectName());
            vo.setSubjectType(s.getSubjectType());
            vo.setSubjectDifficult(s.getSubjectDifficult());

            // 查询关联
            List<SubjectMapping> mappings = mappingMapper.selectBySubjectId(s.getId());
            List<String> labelNames = new ArrayList<>();
            String categoryName = null;
            for (SubjectMapping m : mappings) {
                if (m.getCategoryId() != null && categoryName == null) {
                    categoryName = categoryMap.get(m.getCategoryId());
                }
                if (m.getLabelId() != null) {
                    String labelName = labelMap.get(m.getLabelId());
                    if (labelName != null) {
                        labelNames.add(labelName);
                    }
                }
            }
            vo.setCategoryName(categoryName);
            vo.setLabelNames(labelNames);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public int count(SubjectQueryRequest request) {
        return subjectMapper.count(
                request.getCategoryId(),
                request.getLabelId(),
                request.getKeyword()
        );
    }

    @Override
    public SubjectDetailVO getDetail(Long id) {
        Subject subject = subjectMapper.selectById(id);
        if (subject == null) {
            return null;
        }

        SubjectDetailVO vo = new SubjectDetailVO();
        vo.setId(subject.getId());
        vo.setSubjectName(subject.getSubjectName());
        vo.setSubjectType(subject.getSubjectType());
        vo.setSubjectDifficult(subject.getSubjectDifficult());
        vo.setSubjectScore(subject.getSubjectScore());
        vo.setSubjectParse(subject.getSubjectParse());

        // 查询关联
        List<SubjectMapping> mappings = mappingMapper.selectBySubjectId(id);
        List<Long> categoryIds = new ArrayList<>();
        List<Long> labelIds = new ArrayList<>();
        for (SubjectMapping m : mappings) {
            if (m.getCategoryId() != null) {
                categoryIds.add(m.getCategoryId());
            }
            if (m.getLabelId() != null) {
                labelIds.add(m.getLabelId());
            }
        }
        vo.setCategoryIds(categoryIds);
        vo.setLabelIds(labelIds);

        // 查询选项
        SubjectTypeHandler handler = handlerFactory.getHandler(subject.getSubjectType());
        vo.setOptionList(handler.getWithOptions(id));

        return vo;
    }

    @Override
    public List<Long> getRandomSubjectIds(List<Long> labelIds, int limit) {
        return subjectMapper.selectRandomIdsByLabels(labelIds, limit);
    }
}