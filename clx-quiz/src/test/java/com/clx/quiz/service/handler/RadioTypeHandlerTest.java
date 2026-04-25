package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectRadio;
import com.clx.quiz.mapper.SubjectRadioMapper;
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
 * 单选题处理策略测试。
 */
@ExtendWith(MockitoExtension.class)
class RadioTypeHandlerTest {

    @Mock
    private SubjectRadioMapper radioMapper;

    @InjectMocks
    private RadioTypeHandler handler;

    @BeforeEach
    void setUp() {
        // 公共初始化（如有需要）
    }

    @Nested
    @DisplayName("题型识别")
    class GetType {
        @Test
        @DisplayName("单选题类型为 1")
        void getType_returnsOne() {
            assertEquals(1, handler.getType());
        }
    }

    @Nested
    @DisplayName("保存选项")
    class Save {
        @Test
        @DisplayName("正常保存单选题选项")
        void save_success() {
            SubjectOptionDTO opt1 = new SubjectOptionDTO();
            opt1.setOptionType(1);
            opt1.setOptionContent("选项A");
            opt1.setIsCorrect(1);

            SubjectOptionDTO opt2 = new SubjectOptionDTO();
            opt2.setOptionType(2);
            opt2.setOptionContent("选项B");
            opt2.setIsCorrect(0);

            List<SubjectOptionDTO> options = Arrays.asList(opt1, opt2);

            handler.save(100L, options, "admin");

            verify(radioMapper).batchInsert(argThat(list -> {
                // 验证传入的列表包含两个选项
                return list.size() == 2 &&
                        list.get(0).getSubjectId().equals(100L) &&
                        list.get(0).getOptionType().equals(1) &&
                        list.get(0).getIsCorrect().equals(1) &&
                        list.get(1).getOptionType().equals(2);
            }));
        }
    }

    @Nested
    @DisplayName("判题")
    class Judge {
        @Test
        @DisplayName("选对 - 返回正确")
        void judge_correct() {
            SubjectRadio radio1 = new SubjectRadio();
            radio1.setOptionType(1);
            radio1.setOptionContent("正确答案");
            radio1.setIsCorrect(1);

            SubjectRadio radio2 = new SubjectRadio();
            radio2.setOptionType(2);
            radio2.setOptionContent("错误答案");
            radio2.setIsCorrect(0);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio1, radio2));

            int result = handler.judge(100L, "1");

            assertEquals(1, result);
        }

        @Test
        @DisplayName("选错 - 返回错误")
        void judge_wrong() {
            SubjectRadio radio1 = new SubjectRadio();
            radio1.setOptionType(1);
            radio1.setIsCorrect(1);

            SubjectRadio radio2 = new SubjectRadio();
            radio2.setOptionType(2);
            radio2.setIsCorrect(0);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio1, radio2));

            int result = handler.judge(100L, "2");

            assertEquals(0, result);
        }

        @Test
        @DisplayName("答案不存在 - 返回错误")
        void judge_answerNotExist() {
            SubjectRadio radio = new SubjectRadio();
            radio.setOptionType(1);
            radio.setIsCorrect(1);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio));

            int result = handler.judge(100L, "3"); // 不存在的选项

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("获取正确答案")
    class GetCorrectAnswer {
        @Test
        @DisplayName("返回正确选项类型")
        void getCorrectAnswer_success() {
            SubjectRadio radio1 = new SubjectRadio();
            radio1.setOptionType(1);
            radio1.setIsCorrect(1);

            SubjectRadio radio2 = new SubjectRadio();
            radio2.setOptionType(2);
            radio2.setIsCorrect(0);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio1, radio2));

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("1", answer);
        }

        @Test
        @DisplayName("无正确选项 - 返回空串")
        void getCorrectAnswer_noCorrect() {
            SubjectRadio radio1 = new SubjectRadio();
            radio1.setOptionType(1);
            radio1.setIsCorrect(0);

            SubjectRadio radio2 = new SubjectRadio();
            radio2.setOptionType(2);
            radio2.setIsCorrect(0);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio1, radio2));

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
            SubjectRadio radio = new SubjectRadio();
            radio.setOptionType(1);
            radio.setOptionContent("内容");
            radio.setIsCorrect(1);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio));

            List<SubjectOptionDTO> options = handler.getWithOptions(100L);

            assertEquals(1, options.size());
            assertEquals(1, options.get(0).getIsCorrect());
        }

        @Test
        @DisplayName("不含答案获取 - 不包含 isCorrect")
        void getWithoutAnswer_excludesCorrect() {
            SubjectRadio radio = new SubjectRadio();
            radio.setOptionType(1);
            radio.setOptionContent("内容");
            radio.setIsCorrect(1);

            when(radioMapper.selectBySubjectId(100L))
                    .thenReturn(Arrays.asList(radio));

            List<SubjectOptionDTO> options = handler.getWithoutAnswer(100L);

            assertEquals(1, options.size());
            assertNull(options.get(0).getIsCorrect());
        }
    }
}