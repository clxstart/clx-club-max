package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectRadio;
import com.clx.quiz.mapper.SubjectRadioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单选题处理策略。
 */
@Component
@RequiredArgsConstructor
public class RadioTypeHandler implements SubjectTypeHandler {

    private final SubjectRadioMapper radioMapper;

    @Override
    public int getType() {
        return 1;
    }

    @Override
    public void save(Long subjectId, List<SubjectOptionDTO> optionList, String createdBy) {
        List<SubjectRadio> radios = optionList.stream().map(dto -> {
            SubjectRadio radio = new SubjectRadio();
            radio.setSubjectId(subjectId);
            radio.setOptionType(dto.getOptionType());
            radio.setOptionContent(dto.getOptionContent());
            radio.setIsCorrect(dto.getIsCorrect());
            radio.setCreatedBy(createdBy);
            return radio;
        }).collect(Collectors.toList());
        radioMapper.batchInsert(radios);
    }

    @Override
    public List<SubjectOptionDTO> getWithOptions(Long subjectId) {
        List<SubjectRadio> radios = radioMapper.selectBySubjectId(subjectId);
        return radios.stream().map(r -> {
            SubjectOptionDTO dto = new SubjectOptionDTO();
            dto.setOptionType(r.getOptionType());
            dto.setOptionContent(r.getOptionContent());
            dto.setIsCorrect(r.getIsCorrect());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SubjectOptionDTO> getWithoutAnswer(Long subjectId) {
        List<SubjectRadio> radios = radioMapper.selectBySubjectId(subjectId);
        return radios.stream().map(r -> {
            SubjectOptionDTO dto = new SubjectOptionDTO();
            dto.setOptionType(r.getOptionType());
            dto.setOptionContent(r.getOptionContent());
            // 不设置 isCorrect
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public int judge(Long subjectId, String answerContent) {
        // 单选题答案格式：选项类型数字，如 "1" 表示选A
        List<SubjectRadio> radios = radioMapper.selectBySubjectId(subjectId);
        int userAnswer = Integer.parseInt(answerContent);
        for (SubjectRadio radio : radios) {
            if (radio.getOptionType().equals(userAnswer) && radio.getIsCorrect() == 1) {
                return 1; // 正确
            }
        }
        return 0; // 错误
    }

    @Override
    public String getCorrectAnswer(Long subjectId) {
        List<SubjectRadio> radios = radioMapper.selectBySubjectId(subjectId);
        for (SubjectRadio radio : radios) {
            if (radio.getIsCorrect() == 1) {
                return String.valueOf(radio.getOptionType());
            }
        }
        return "";
    }

    @Override
    public void delete(Long subjectId) {
        radioMapper.deleteBySubjectId(subjectId);
    }
}