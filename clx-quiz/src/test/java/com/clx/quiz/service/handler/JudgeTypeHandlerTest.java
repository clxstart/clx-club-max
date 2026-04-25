package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectJudge;
import com.clx.quiz.mapper.SubjectJudgeMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 判断题处理策略测试。
 */
@ExtendWith(MockitoExtension.class)
class JudgeTypeHandlerTest {

    @Mock
    private SubjectJudgeMapper judgeMapper;

    @InjectMocks
    private JudgeTypeHandler handler;

    @Nested
    @DisplayName("题型识别")
    class GetType {
        @Test
        @DisplayName("判断题类型为 3")
        void getType_returnsThree() {
            assertEquals(3, handler.getType());
        }
    }

    @Nested
    @DisplayName("判题")
    class Judge {
        @Test
        @DisplayName("答正确 - 返回正确")
        void judge_correctTrue() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(1); // 正确答案是"正确"

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            int result = handler.judge(100L, "1");

            assertEquals(1, result);
        }

        @Test
        @DisplayName("答错误 - 返回正确")
        void judge_correctFalse() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(0); // 正确答案是"错误"

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            int result = handler.judge(100L, "0");

            assertEquals(1, result);
        }

        @Test
        @DisplayName("答错 - 返回错误")
        void judge_wrong() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(1); // 正确答案是"正确"

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            int result = handler.judge(100L, "0"); // 用户答"错误"

            assertEquals(0, result);
        }

        @Test
        @DisplayName("题目不存在 - 返回错误")
        void judge_notExist() {
            when(judgeMapper.selectBySubjectId(100L)).thenReturn(null);

            int result = handler.judge(100L, "1");

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("获取正确答案")
    class GetCorrectAnswer {
        @Test
        @DisplayName("正确答案为正确")
        void getCorrectAnswer_true() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(1);

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("正确", answer);
        }

        @Test
        @DisplayName("正确答案为错误")
        void getCorrectAnswer_false() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(0);

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("错误", answer);
        }

        @Test
        @DisplayName("题目不存在 - 返回空串")
        void getCorrectAnswer_notExist() {
            when(judgeMapper.selectBySubjectId(100L)).thenReturn(null);

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("", answer);
        }
    }

    @Nested
    @DisplayName("获取选项")
    class GetOptions {
        @Test
        @DisplayName("含答案获取 - 返回正确/错误选项")
        void getWithOptions_returnsResult() {
            SubjectJudge judge = new SubjectJudge();
            judge.setIsCorrect(1);

            when(judgeMapper.selectBySubjectId(100L)).thenReturn(judge);

            List<SubjectOptionDTO> options = handler.getWithOptions(100L);

            assertEquals(1, options.size());
            assertEquals("正确", options.get(0).getOptionContent());
            assertEquals(1, options.get(0).getIsCorrect());
        }

        @Test
        @DisplayName("不含答案获取 - 返回正确/错误两个选项但不告诉用户哪个对")
        void getWithoutAnswer_returnsBothOptions() {
            // 不需要 mock，因为 getWithoutAnswer 不查询数据库判断答案

            List<SubjectOptionDTO> options = handler.getWithoutAnswer(100L);

            assertEquals(2, options.size());
            assertEquals("错误", options.get(0).getOptionContent());
            assertEquals("正确", options.get(1).getOptionContent());
            // 都没有设置 isCorrect
            assertNull(options.get(0).getIsCorrect());
            assertNull(options.get(1).getIsCorrect());
        }
    }
}