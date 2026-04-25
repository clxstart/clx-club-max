package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectBrief;
import com.clx.quiz.mapper.SubjectBriefMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 简答题处理策略。
 * 简答题无法自动判题，需用户自评。
 */
@Component
@RequiredArgsConstructor
public class BriefTypeHandler implements SubjectTypeHandler {

    private final SubjectBriefMapper briefMapper;

    @Override
    public int getType() {
        return 4;
    }

    @Override
    public void save(Long subjectId, List<SubjectOptionDTO> optionList, String createdBy) {
        // 简答题存储参考答案
        SubjectBrief brief = new SubjectBrief();
        brief.setSubjectId(subjectId);
        // 从第一个选项中取答案内容（简答题只有一条）
        if (optionList != null && !optionList.isEmpty()) {
            brief.setSubjectAnswer(optionList.get(0).getOptionContent());
        }
        brief.setCreatedBy(createdBy);
        briefMapper.insert(brief);
    }

    @Override
    public List<SubjectOptionDTO> getWithOptions(Long subjectId) {
        SubjectBrief brief = briefMapper.selectBySubjectId(subjectId);
        if (brief == null) {
            return Collections.emptyList();
        }
        SubjectOptionDTO dto = new SubjectOptionDTO();
        dto.setOptionType(1);
        dto.setOptionContent(brief.getSubjectAnswer());
        dto.setIsCorrect(1); // 简答题标记为正确答案展示
        return Collections.singletonList(dto);
    }

    @Override
    public List<SubjectOptionDTO> getWithoutAnswer(Long subjectId) {
        // 简答题答题时不显示答案，返回空列表让前端显示输入框
        return Collections.emptyList();
    }

    @Override
    public int judge(Long subjectId, String answerContent) {
        // 简答题无法自动判题，返回 2 表示"需要用户自评"
        return 2;
    }

    @Override
    public String getCorrectAnswer(Long subjectId) {
        SubjectBrief brief = briefMapper.selectBySubjectId(subjectId);
        if (brief == null) {
            return "";
        }
        return brief.getSubjectAnswer();
    }

    @Override
    public void delete(Long subjectId) {
        briefMapper.deleteBySubjectId(subjectId);
    }
}