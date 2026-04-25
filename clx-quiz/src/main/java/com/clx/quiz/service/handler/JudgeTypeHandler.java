package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectJudge;
import com.clx.quiz.mapper.SubjectJudgeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 判断题处理策略。
 */
@Component
@RequiredArgsConstructor
public class JudgeTypeHandler implements SubjectTypeHandler {

    private final SubjectJudgeMapper judgeMapper;

    @Override
    public int getType() {
        return 3;
    }

    @Override
    public void save(Long subjectId, List<SubjectOptionDTO> optionList, String createdBy) {
        // 判断题只有一条答案记录
        SubjectOptionDTO dto = optionList.get(0);
        SubjectJudge judge = new SubjectJudge();
        judge.setSubjectId(subjectId);
        judge.setIsCorrect(dto.getIsCorrect());
        judge.setCreatedBy(createdBy);
        judgeMapper.insert(judge);
    }

    @Override
    public List<SubjectOptionDTO> getWithOptions(Long subjectId) {
        SubjectJudge judge = judgeMapper.selectBySubjectId(subjectId);
        if (judge == null) {
            return Collections.emptyList();
        }
        SubjectOptionDTO dto = new SubjectOptionDTO();
        dto.setOptionType(1); // 判断题只有一个选项
        dto.setOptionContent(judge.getIsCorrect() == 1 ? "正确" : "错误");
        dto.setIsCorrect(judge.getIsCorrect());
        return Collections.singletonList(dto);
    }

    @Override
    public List<SubjectOptionDTO> getWithoutAnswer(Long subjectId) {
        // 判断题答题时显示"正确/错误"两个选项，但不告诉用户哪个是对的
        SubjectOptionDTO dto1 = new SubjectOptionDTO();
        dto1.setOptionType(0);
        dto1.setOptionContent("错误");
        SubjectOptionDTO dto2 = new SubjectOptionDTO();
        dto2.setOptionType(1);
        dto2.setOptionContent("正确");
        return List.of(dto1, dto2);
    }

    @Override
    public int judge(Long subjectId, String answerContent) {
        // 判断题答案格式：0错 1对
        SubjectJudge judge = judgeMapper.selectBySubjectId(subjectId);
        if (judge == null) {
            return 0;
        }
        int userAnswer = Integer.parseInt(answerContent);
        return judge.getIsCorrect().equals(userAnswer) ? 1 : 0;
    }

    @Override
    public String getCorrectAnswer(Long subjectId) {
        SubjectJudge judge = judgeMapper.selectBySubjectId(subjectId);
        if (judge == null) {
            return "";
        }
        return judge.getIsCorrect() == 1 ? "正确" : "错误";
    }

    @Override
    public void delete(Long subjectId) {
        judgeMapper.deleteBySubjectId(subjectId);
    }
}