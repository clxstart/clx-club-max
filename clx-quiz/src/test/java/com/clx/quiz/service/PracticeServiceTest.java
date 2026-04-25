package com.clx.quiz.service;

import com.clx.quiz.dto.PracticeStartRequest;
import com.clx.quiz.dto.PracticeSubmitRequest;
import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.entity.Practice;
import com.clx.quiz.entity.PracticeDetail;
import com.clx.quiz.entity.Subject;
import com.clx.quiz.mapper.PracticeDetailMapper;
import com.clx.quiz.mapper.PracticeMapper;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.service.handler.SubjectTypeHandler;
import com.clx.quiz.service.handler.SubjectTypeHandlerFactory;
import com.clx.quiz.service.impl.PracticeServiceImpl;
import com.clx.quiz.vo.PracticeResultVO;
import com.clx.quiz.vo.PracticeSubjectVO;
import com.clx.quiz.vo.SubmitResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 练习服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PracticeServiceTest {

    @Mock
    private PracticeMapper practiceMapper;

    @Mock
    private PracticeDetailMapper detailMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectService subjectService;

    @Mock
    private SubjectTypeHandlerFactory handlerFactory;

    @Mock
    private WrongBookService wrongBookService;

    @Mock
    private SubjectTypeHandler handler;

    @InjectMocks
    private PracticeServiceImpl practiceService;

    private Subject buildSubject(Long id, int type) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setSubjectName("题目" + id);
        subject.setSubjectType(type);
        subject.setSubjectDifficult(1);
        subject.setSubjectParse("解析内容");
        return subject;
    }

    @Nested
    @DisplayName("开始练习")
    class Start {

        @Test
        @DisplayName("正常开始练习")
        void start_success() {
            PracticeStartRequest request = new PracticeStartRequest();
            request.setLabelIds(Arrays.asList(1L, 2L));
            request.setCount(5);

            List<Long> subjectIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
            when(subjectService.getRandomSubjectIds(Arrays.asList(1L, 2L), 5))
                    .thenReturn(subjectIds);

            for (Long id : subjectIds) {
                when(subjectMapper.selectById(id)).thenReturn(buildSubject(id, 1));
            }

            Map<String, Object> result = practiceService.start(request, 100L);

            assertNotNull(result);
            assertEquals(5, result.get("totalCount"));
            verify(practiceMapper).insert(any(Practice.class));
            verify(detailMapper).batchInsert(anyList());
        }

        @Test
        @DisplayName("无符合条件题目 - 抛异常")
        void start_noSubjects() {
            PracticeStartRequest request = new PracticeStartRequest();
            request.setLabelIds(Arrays.asList(1L));
            request.setCount(10);

            when(subjectService.getRandomSubjectIds(Arrays.asList(1L), 10))
                    .thenReturn(Collections.emptyList());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> practiceService.start(request, 100L));

            assertEquals("暂无符合条件的题目", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("获取练习题目")
    class GetSubject {

        @Test
        @DisplayName("正常获取题目（不含答案）")
        void getSubject_success() {
            Subject subject = buildSubject(1L, 1);
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(handler.getWithoutAnswer(1L)).thenReturn(Collections.emptyList());

            PracticeSubjectVO vo = practiceService.getSubject(100L, 1L, 1);

            assertNotNull(vo);
            assertEquals(1L, vo.getSubjectId());
            assertEquals("题目1", vo.getSubjectName());
            verify(handler).getWithoutAnswer(1L); // 确保调用的是不含答案的方法
        }

        @Test
        @DisplayName("题目不存在 - 返回 null")
        void getSubject_notExist() {
            when(subjectMapper.selectById(999L)).thenReturn(null);

            PracticeSubjectVO vo = practiceService.getSubject(100L, 999L, 1);

            assertNull(vo);
        }
    }

    @Nested
    @DisplayName("提交答案")
    class Submit {

        @Test
        @DisplayName("提交单选题答案 - 正确")
        void submit_correct() {
            Subject subject = buildSubject(1L, 1);
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(handler.judge(1L, "1")).thenReturn(1);
            when(handler.getCorrectAnswer(1L)).thenReturn("1");
            when(detailMapper.selectByPracticeIdAndSubjectId(100L, 1L))
                    .thenReturn(new PracticeDetail());

            PracticeSubmitRequest request = new PracticeSubmitRequest();
            request.setPracticeId(100L);
            request.setSubjectId(1L);
            request.setSubjectType(1);
            request.setAnswerContent("1");

            SubmitResultVO result = practiceService.submit(request, 100L);

            assertEquals(1, result.getIsCorrect());
            assertEquals("1", result.getCorrectAnswer());
            assertFalse(result.getNeedSelfJudge());
        }

        @Test
        @DisplayName("提交单选题答案 - 错误")
        void submit_wrong() {
            Subject subject = buildSubject(1L, 1);
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(handler.judge(1L, "2")).thenReturn(0);
            when(handler.getCorrectAnswer(1L)).thenReturn("1");
            when(detailMapper.selectByPracticeIdAndSubjectId(100L, 1L))
                    .thenReturn(new PracticeDetail());

            PracticeSubmitRequest request = new PracticeSubmitRequest();
            request.setPracticeId(100L);
            request.setSubjectId(1L);
            request.setSubjectType(1);
            request.setAnswerContent("2");

            SubmitResultVO result = practiceService.submit(request, 100L);

            assertEquals(0, result.getIsCorrect());
        }

        @Test
        @DisplayName("提交简答题答案 - 需自评")
        void submit_briefNeedSelfJudge() {
            Subject subject = buildSubject(1L, 4);
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(handlerFactory.getHandler(4)).thenReturn(handler);
            when(handler.judge(1L, "用户答案")).thenReturn(2); // 简答题返回2
            when(handler.getCorrectAnswer(1L)).thenReturn("参考答案");
            when(detailMapper.selectByPracticeIdAndSubjectId(100L, 1L))
                    .thenReturn(new PracticeDetail());

            PracticeSubmitRequest request = new PracticeSubmitRequest();
            request.setPracticeId(100L);
            request.setSubjectId(1L);
            request.setSubjectType(4);
            request.setAnswerContent("用户答案");

            SubmitResultVO result = practiceService.submit(request, 100L);

            assertEquals(2, result.getIsCorrect());
            assertTrue(result.getNeedSelfJudge());
        }

        @Test
        @DisplayName("题目不存在 - 抛异常")
        void submit_subjectNotExist() {
            when(subjectMapper.selectById(999L)).thenReturn(null);

            PracticeSubmitRequest request = new PracticeSubmitRequest();
            request.setPracticeId(100L);
            request.setSubjectId(999L);
            request.setSubjectType(1);
            request.setAnswerContent("1");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> practiceService.submit(request, 100L));

            assertEquals("题目不存在", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("结束练习")
    class Finish {

        @Test
        @DisplayName("正常结束练习 - 全部正确")
        void finish_allCorrect() {
            List<PracticeDetail> details = Arrays.asList(
                    createDetail(1L, 1, 1),
                    createDetail(2L, 1, 1),
                    createDetail(3L, 1, 1)
            );
            when(detailMapper.selectByPracticeId(100L)).thenReturn(details);
            when(practiceMapper.selectById(100L)).thenReturn(new Practice());

            PracticeResultVO result = practiceService.finish(100L, 100L);

            assertEquals(3, result.getTotalCount());
            assertEquals(3, result.getCorrectCount());
            assertEquals(1.0, result.getCorrectRate());
            assertTrue(result.getWrongSubjectIds().isEmpty());
            verify(wrongBookService, never()).add(anyLong(), anyLong());
        }

        @Test
        @DisplayName("正常结束练习 - 部分正确，写入错题本")
        void finish_partialCorrect() {
            List<PracticeDetail> details = Arrays.asList(
                    createDetail(1L, 1, 1),  // 正确
                    createDetail(2L, 1, 0),  // 错误
                    createDetail(3L, 1, 0)   // 错误
            );
            when(detailMapper.selectByPracticeId(100L)).thenReturn(details);
            when(practiceMapper.selectById(100L)).thenReturn(new Practice());

            PracticeResultVO result = practiceService.finish(100L, 100L);

            assertEquals(3, result.getTotalCount());
            assertEquals(1, result.getCorrectCount());
            assertEquals(2, result.getWrongSubjectIds().size());
            assertTrue(result.getWrongSubjectIds().contains(2L));
            assertTrue(result.getWrongSubjectIds().contains(3L));
            // 验证错题本写入
            verify(wrongBookService).add(100L, 2L);
            verify(wrongBookService).add(100L, 3L);
        }

        @Test
        @DisplayName("跳过未作答题目 - 不计入错题本也不计入正确")
        void finish_unanswered() {
            List<PracticeDetail> details = Arrays.asList(
                    createDetail(1L, 1, 1),   // 正确
                    createDetail(2L, 1, null) // 未作答
            );
            when(detailMapper.selectByPracticeId(100L)).thenReturn(details);
            when(practiceMapper.selectById(100L)).thenReturn(new Practice());

            PracticeResultVO result = practiceService.finish(100L, 100L);

            assertEquals(2, result.getTotalCount());
            assertEquals(1, result.getCorrectCount());
            assertTrue(result.getWrongSubjectIds().isEmpty()); // 未作答不计入错题本
            verify(wrongBookService, never()).add(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("完整练习流程")
    class FullPracticeFlow {

        @Test
        @DisplayName("端到端练习流程测试")
        void testFullPracticeFlow() {
            // 1. 开始练习
            PracticeStartRequest startRequest = new PracticeStartRequest();
            startRequest.setLabelIds(Arrays.asList(1L));
            startRequest.setCount(2);

            when(subjectService.getRandomSubjectIds(Arrays.asList(1L), 2))
                    .thenReturn(Arrays.asList(1L, 2L));
            when(subjectMapper.selectById(1L)).thenReturn(buildSubject(1L, 1));
            when(subjectMapper.selectById(2L)).thenReturn(buildSubject(2L, 1));
            // mock insert 设置 ID
            when(practiceMapper.insert(any(Practice.class))).thenAnswer((Answer<Integer>) invocation -> {
                Practice practice = invocation.getArgument(0);
                practice.setId(100L);
                return 1;
            });

            Map<String, Object> startResult = practiceService.start(startRequest, 100L);
            assertNotNull(startResult.get("practiceId"));

            // 2. 获取题目
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(handler.getWithoutAnswer(1L)).thenReturn(Collections.emptyList());

            PracticeSubjectVO subjectVO = practiceService.getSubject(
                    (Long) startResult.get("practiceId"), 1L, 1);
            assertNotNull(subjectVO);
            // 确保不含答案（空列表）
            assertTrue(subjectVO.getOptionList().isEmpty());
        }
    }

    private PracticeDetail createDetail(Long subjectId, Integer subjectType, Integer isCorrect) {
        PracticeDetail detail = new PracticeDetail();
        detail.setSubjectId(subjectId);
        detail.setSubjectType(subjectType);
        detail.setIsCorrect(isCorrect);
        return detail;
    }
}