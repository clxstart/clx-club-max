package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectMultiple;
import com.clx.quiz.mapper.SubjectMultipleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 多选题处理策略。
 */
@Component
@RequiredArgsConstructor
public class MultipleTypeHandler implements SubjectTypeHandler {

    private final SubjectMultipleMapper multipleMapper;

    @Override
    public int getType() {
        return 2;
    }

    @Override
    public void save(Long subjectId, List<SubjectOptionDTO> optionList, String createdBy) {
        List<SubjectMultiple> multiples = optionList.stream().map(dto -> {
            SubjectMultiple multiple = new SubjectMultiple();
            multiple.setSubjectId(subjectId);
            multiple.setOptionType(dto.getOptionType());
            multiple.setOptionContent(dto.getOptionContent());
            multiple.setIsCorrect(dto.getIsCorrect());
            multiple.setCreatedBy(createdBy);
            return multiple;
        }).collect(Collectors.toList());
        multipleMapper.batchInsert(multiples);
    }

    @Override
    public List<SubjectOptionDTO> getWithOptions(Long subjectId) {
        List<SubjectMultiple> multiples = multipleMapper.selectBySubjectId(subjectId);
        return multiples.stream().map(m -> {
            SubjectOptionDTO dto = new SubjectOptionDTO();
            dto.setOptionType(m.getOptionType());
            dto.setOptionContent(m.getOptionContent());
            dto.setIsCorrect(m.getIsCorrect());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SubjectOptionDTO> getWithoutAnswer(Long subjectId) {
        List<SubjectMultiple> multiples = multipleMapper.selectBySubjectId(subjectId);
        return multiples.stream().map(m -> {
            SubjectOptionDTO dto = new SubjectOptionDTO();
            dto.setOptionType(m.getOptionType());
            dto.setOptionContent(m.getOptionContent());
            // 不设置 isCorrect
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public int judge(Long subjectId, String answerContent) {
        // 多选题答案格式：逗号分隔的选项类型，如 "1,2,4" 表示选A,B,D
        List<SubjectMultiple> multiples = multipleMapper.selectBySubjectId(subjectId);
        // 获取正确答案集合
        Set<Integer> correctOptions = new HashSet<>();
        for (SubjectMultiple multiple : multiples) {
            if (multiple.getIsCorrect() == 1) {
                correctOptions.add(multiple.getOptionType());
            }
        }
        // 解析用户答案
        Set<Integer> userOptions = new HashSet<>();
        String[] parts = answerContent.split(",");
        for (String part : parts) {
            userOptions.add(Integer.parseInt(part.trim()));
        }
        // 必须完全匹配才算正确
        return correctOptions.equals(userOptions) ? 1 : 0;
    }

    @Override
    public String getCorrectAnswer(Long subjectId) {
        List<SubjectMultiple> multiples = multipleMapper.selectBySubjectId(subjectId);
        List<Integer> correctOptions = multiples.stream()
                .filter(m -> m.getIsCorrect() == 1)
                .map(SubjectMultiple::getOptionType)
                .sorted()
                .collect(Collectors.toList());
        return correctOptions.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    @Override
    public void delete(Long subjectId) {
        multipleMapper.deleteBySubjectId(subjectId);
    }
}