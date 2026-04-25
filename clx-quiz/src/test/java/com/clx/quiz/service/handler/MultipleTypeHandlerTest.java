package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectMultiple;
import com.clx.quiz.mapper.SubjectMultipleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 多选题处理策略测试。
 * 多选题必须全部选对才算正确，不支持部分得分。
 */
@ExtendWith(MockitoExtension.class)
class MultipleTypeHandlerTest {

    @Mock
    private SubjectMultipleMapper multipleMapper;

    @InjectMocks
    private MultipleTypeHandler handler;

    private List<SubjectMultiple> buildMultipleOptions() {
        // 创建多选题：A(1)和B(2)是正确答案，C(3)和D(4)是错误答案
        SubjectMultiple optA = new SubjectMultiple();
        optA.setOptionType(1);
        optA.setOptionContent("选项A");
        optA.setIsCorrect(1);

        SubjectMultiple optB = new SubjectMultiple();
        optB.setOptionType(2);
        optB.setOptionContent("选项B");
        optB.setIsCorrect(1);

        SubjectMultiple optC = new SubjectMultiple();
        optC.setOptionType(3);
        optC.setOptionContent("选项C");
        optC.setIsCorrect(0);

        SubjectMultiple optD = new SubjectMultiple();
        optD.setOptionType(4);
        optD.setOptionContent("选项D");
        optD.setIsCorrect(0);

        return Arrays.asList(optA, optB, optC, optD);
    }

    @Nested
    @DisplayName("题型识别")
    class GetType {
        @Test
        @DisplayName("多选题类型为 2")
        void getType_returnsTwo() {
            assertEquals(2, handler.getType());
        }
    }

    @Nested
    @DisplayName("判题")
    class Judge {
        @Test
        @DisplayName("全对 - 返回正确")
        void judge_allCorrect() {
            // 正确答案是 A(1) 和 B(2)
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户选择 A,B → 全对
            int result = handler.judge(100L, "1,2");

            assertEquals(1, result);
        }

        @Test
        @DisplayName("少选 - 返回错误")
        void judge_partialCorrect() {
            // 正确答案是 A(1) 和 B(2)
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户只选 A → 少选，算错
            int result = handler.judge(100L, "1");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("错选 - 返回错误")
        void judge_wrongSelection() {
            // 正确答案是 A(1) 和 B(2)
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户选 A,C → C是错的，算错
            int result = handler.judge(100L, "1,3");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("全错 - 返回错误")
        void judge_allWrong() {
            // 正确答案是 A(1) 和 B(2)
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户选 C,D → 全错
            int result = handler.judge(100L, "3,4");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("答案格式带空格 - 正常解析")
        void judge_withSpaces() {
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户选择 "1, 2" 带空格
            int result = handler.judge(100L, "1, 2");

            assertEquals(1, result);
        }

        @Test
        @DisplayName("单选题当多选做 - 返回错误")
        void judge_singleAnswerForMultiple() {
            // 正确答案是 A(1) 和 B(2)，两个选项
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            // 用户只选了一个正确选项，但题目要求两个都选
            int result = handler.judge(100L, "1");

            assertEquals(0, result); // 少选算错
        }
    }

    @Nested
    @DisplayName("获取正确答案")
    class GetCorrectAnswer {
        @Test
        @DisplayName("返回所有正确选项（逗号分隔，排序）")
        void getCorrectAnswer_success() {
            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(buildMultipleOptions());

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("1,2", answer);
        }

        @Test
        @DisplayName("无正确选项 - 返回空串")
        void getCorrectAnswer_noCorrect() {
            SubjectMultiple optA = new SubjectMultiple();
            optA.setOptionType(1);
            optA.setIsCorrect(0);

            SubjectMultiple optB = new SubjectMultiple();
            optB.setOptionType(2);
            optB.setIsCorrect(0);

            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(optA, optB));

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("", answer);
        }
    }

    @Nested
    @DisplayName("获取选项")
    class GetOptions {
        @Test
        @DisplayName("含答案获取 - 包含 isCorrect")
        void getWithOptions_includesCorrect() {
            SubjectMultiple opt = new SubjectMultiple();
            opt.setOptionType(1);
            opt.setOptionContent("内容");
            opt.setIsCorrect(1);

            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(opt));

            List<SubjectOptionDTO> options = handler.getWithOptions(100L);

            assertEquals(1, options.size());
            assertEquals(1, options.get(0).getIsCorrect());
        }

        @Test
        @DisplayName("不含答案获取 - 不包含 isCorrect")
        void getWithoutAnswer_excludesCorrect() {
            SubjectMultiple opt = new SubjectMultiple();
            opt.setOptionType(1);
            opt.setOptionContent("内容");
            opt.setIsCorrect(1);

            when(multipleMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(opt));

            List<SubjectOptionDTO> options = handler.getWithoutAnswer(100L);

            assertEquals(1, options.size());
            assertNull(options.get(0).getIsCorrect());
        }
    }
}