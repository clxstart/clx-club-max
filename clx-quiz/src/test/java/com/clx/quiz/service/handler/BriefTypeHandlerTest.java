package com.clx.quiz.service.handler;

import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.SubjectBrief;
import com.clx.quiz.mapper.SubjectBriefMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 简答题处理策略测试。
 * 简答题无法自动判题，需用户自评。
 */
@ExtendWith(MockitoExtension.class)
class BriefTypeHandlerTest {

    @Mock
    private SubjectBriefMapper briefMapper;

    @InjectMocks
    private BriefTypeHandler handler;

    @Nested
    @DisplayName("题型识别")
    class GetType {
        @Test
        @DisplayName("简答题类型为 4")
        void getType_returnsFour() {
            assertEquals(4, handler.getType());
        }
    }

    @Nested
    @DisplayName("判题")
    class Judge {
        @Test
        @DisplayName("简答题无法自动判题 - 返回 2 表示需要自评")
        void judge_returnsTwo() {
            // 简答题不管答案是什么，都返回 2 表示需要用户自评
            int result = handler.judge(100L, "用户写的答案");

            assertEquals(2, result);
            // 不调用 mapper，直接返回
            verify(briefMapper, never()).selectBySubjectId(anyLong());
        }
    }

    @Nested
    @DisplayName("获取正确答案")
    class GetCorrectAnswer {
        @Test
        @DisplayName("返回参考答案")
        void getCorrectAnswer_success() {
            SubjectBrief brief = new SubjectBrief();
            brief.setSubjectAnswer("这是参考答案内容");

            when(briefMapper.selectBySubjectId(100L)).thenReturn(brief);

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("这是参考答案内容", answer);
        }

        @Test
        @DisplayName("题目不存在 - 返回空串")
        void getCorrectAnswer_notExist() {
            when(briefMapper.selectBySubjectId(100L)).thenReturn(null);

            String answer = handler.getCorrectAnswer(100L);

            assertEquals("", answer);
        }
    }

    @Nested
    @DisplayName("获取选项")
    class GetOptions {
        @Test
        @DisplayName("含答案获取 - 返回参考答案")
        void getWithOptions_returnsAnswer() {
            SubjectBrief brief = new SubjectBrief();
            brief.setSubjectAnswer("参考答案");

            when(briefMapper.selectBySubjectId(100L)).thenReturn(brief);

            List<SubjectOptionDTO> options = handler.getWithOptions(100L);

            assertEquals(1, options.size());
            assertEquals("参考答案", options.get(0).getOptionContent());
        }

        @Test
        @DisplayName("不含答案获取 - 返回空列表（前端显示输入框）")
        void getWithoutAnswer_returnsEmpty() {
            // 简答题答题时不显示答案，让用户自己写
            List<SubjectOptionDTO> options = handler.getWithoutAnswer(100L);

            assertTrue(options.isEmpty());
            // 不调用 mapper
            verify(briefMapper, never()).selectBySubjectId(anyLong());
        }
    }

    @Nested
    @DisplayName("保存")
    class Save {
        @Test
        @DisplayName("保存参考答案")
        void save_success() {
            SubjectOptionDTO dto = new SubjectOptionDTO();
            dto.setOptionContent("这是参考答案内容");

            List<SubjectOptionDTO> options = Collections.singletonList(dto);

            handler.save(100L, options, "admin");

            verify(briefMapper).insert(argThat(brief ->
                    brief.getSubjectId().equals(100L) &&
                    "这是参考答案内容".equals(brief.getSubjectAnswer()) &&
                    "admin".equals(brief.getCreatedBy())
            ));
        }

        @Test
        @DisplayName("选项列表为空 - 保存空答案")
        void save_emptyOptions() {
            handler.save(100L, null, "admin");

            verify(briefMapper).insert(argThat(brief ->
                    brief.getSubjectId().equals(100L) &&
                    brief.getSubjectAnswer() == null
            ));
        }
    }
}