package com.clx.quiz.service;

import com.clx.quiz.entity.Subject;
import com.clx.quiz.entity.WrongBook;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.mapper.WrongBookMapper;
import com.clx.quiz.service.impl.WrongBookServiceImpl;
import com.clx.quiz.vo.WrongBookVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 错题本服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class WrongBookServiceTest {

    @Mock
    private WrongBookMapper wrongBookMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private WrongBookServiceImpl wrongBookService;

    private Subject buildSubject(Long id) {
        Subject subject = new Subject();
        subject.setId(id);
        subject.setSubjectName("题目" + id);
        subject.setSubjectType(1);
        return subject;
    }

    private WrongBook buildWrongBook(Long id, Long subjectId, int wrongCount) {
        WrongBook wb = new WrongBook();
        wb.setId(id);
        wb.setUserId(100L);
        wb.setSubjectId(subjectId);
        wb.setWrongCount(wrongCount);
        wb.setLastWrongTime(LocalDateTime.now());
        return wb;
    }

    @Nested
    @DisplayName("添加错题")
    class Add {

        @Test
        @DisplayName("首次添加错题 - 新增记录")
        void add_newRecord() {
            when(wrongBookMapper.selectByUserAndSubject(100L, 1L)).thenReturn(null);

            wrongBookService.add(100L, 1L);

            verify(wrongBookMapper).insert(argThat(wb ->
                    wb.getUserId().equals(100L) &&
                    wb.getSubjectId().equals(1L) &&
                    wb.getWrongCount().equals(1)
            ));
            verify(wrongBookMapper, never()).update(any());
        }

        @Test
        @DisplayName("重复添加错题 - 累加次数")
        void add_existingRecord() {
            WrongBook existing = buildWrongBook(1L, 10L, 2);
            when(wrongBookMapper.selectByUserAndSubject(100L, 10L)).thenReturn(existing);

            wrongBookService.add(100L, 10L);

            verify(wrongBookMapper).update(argThat(wb ->
                    wb.getId().equals(1L) &&
                    wb.getWrongCount().equals(3) && // 2 + 1 = 3
                    wb.getLastWrongTime() != null
            ));
            verify(wrongBookMapper, never()).insert(any());
        }

        @Test
        @DisplayName("多次累加 - 次数递增")
        void add_multipleTimes() {
            WrongBook existing = buildWrongBook(1L, 10L, 5);
            when(wrongBookMapper.selectByUserAndSubject(100L, 10L)).thenReturn(existing);

            // 第一次累加
            wrongBookService.add(100L, 10L);
            // 更新后再次查询返回更新后的记录
            when(wrongBookMapper.selectByUserAndSubject(100L, 10L))
                    .thenReturn(buildWrongBook(1L, 10L, 6));

            // 第二次累加
            wrongBookService.add(100L, 10L);

            verify(wrongBookMapper, times(2)).update(any());
        }
    }

    @Nested
    @DisplayName("查询错题本列表")
    class QueryList {

        @Test
        @DisplayName("正常查询")
        void list_success() {
            WrongBook wb1 = buildWrongBook(1L, 10L, 2);
            WrongBook wb2 = buildWrongBook(2L, 20L, 1);

            when(wrongBookMapper.selectPage(100L, 0, 10))
                    .thenReturn(Arrays.asList(wb1, wb2));
            when(wrongBookMapper.count(100L)).thenReturn(2);
            when(subjectMapper.selectById(10L)).thenReturn(buildSubject(10L));
            when(subjectMapper.selectById(20L)).thenReturn(buildSubject(20L));

            Map<String, Object> result = wrongBookService.list(100L, 1, 10);

            assertEquals(2, result.get("total"));
            java.util.List<WrongBookVO> voList = (java.util.List<WrongBookVO>) result.get("list");
            assertEquals(2, voList.size());
            assertEquals("题目10", voList.get(0).getSubjectName());
        }

        @Test
        @DisplayName("空列表返回正确结构")
        void list_empty() {
            when(wrongBookMapper.selectPage(100L, 0, 10))
                    .thenReturn(Collections.emptyList());
            when(wrongBookMapper.count(100L)).thenReturn(0);

            Map<String, Object> result = wrongBookService.list(100L, 1, 10);

            assertEquals(0, result.get("total"));
            java.util.List<WrongBookVO> voList = (java.util.List<WrongBookVO>) result.get("list");
            assertTrue(voList.isEmpty());
        }

        @Test
        @DisplayName("题目被删除后仍显示（题目名为空）")
        void list_subjectDeleted() {
            WrongBook wb = buildWrongBook(1L, 999L, 1);
            when(wrongBookMapper.selectPage(100L, 0, 10))
                    .thenReturn(Arrays.asList(wb));
            when(wrongBookMapper.count(100L)).thenReturn(1);
            when(subjectMapper.selectById(999L)).thenReturn(null); // 题目已删除

            Map<String, Object> result = wrongBookService.list(100L, 1, 10);

            assertEquals(1, result.get("total"));
            java.util.List<WrongBookVO> voList = (java.util.List<WrongBookVO>) result.get("list");
            assertEquals(1, voList.size());
            assertNull(voList.get(0).getSubjectName()); // 题目名为空
        }

        @Test
        @DisplayName("分页参数正确传递")
        void list_pagination() {
            when(wrongBookMapper.selectPage(100L, 20, 10))
                    .thenReturn(Collections.emptyList());
            when(wrongBookMapper.count(100L)).thenReturn(25);

            Map<String, Object> result = wrongBookService.list(100L, 3, 10);

            verify(wrongBookMapper).selectPage(100L, 20, 10); // offset = (3-1)*10
            assertEquals(25, result.get("total"));
        }
    }

    @Nested
    @DisplayName("移除错题")
    class Remove {

        @Test
        @DisplayName("正常移除")
        void remove_success() {
            WrongBook wb = buildWrongBook(1L, 10L, 2);
            when(wrongBookMapper.selectByUserAndSubject(100L, 10L)).thenReturn(wb);
            when(wrongBookMapper.deleteById(1L)).thenReturn(1);

            boolean result = wrongBookService.remove(100L, 10L);

            assertTrue(result);
            verify(wrongBookMapper).deleteById(1L);
        }

        @Test
        @DisplayName("错题不存在 - 返回 false")
        void remove_notExist() {
            when(wrongBookMapper.selectByUserAndSubject(100L, 999L)).thenReturn(null);

            boolean result = wrongBookService.remove(100L, 999L);

            assertFalse(result);
            verify(wrongBookMapper, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("错题本自动写入测试")
    class AutoAddWrongBook {

        @Test
        @DisplayName("练习结束自动写入错题本")
        void autoAddOnPracticeFinish() {
            // 模拟练习结束有错题的情况
            when(wrongBookMapper.selectByUserAndSubject(100L, 1L)).thenReturn(null);

            wrongBookService.add(100L, 1L);

            verify(wrongBookMapper).insert(any(WrongBook.class));
        }

        @Test
        @DisplayName("同一题多次答错只增加次数不重复记录")
        void autoAddTwice() {
            WrongBook existing = buildWrongBook(1L, 10L, 2);
            when(wrongBookMapper.selectByUserAndSubject(100L, 10L))
                    .thenReturn(existing);

            wrongBookService.add(100L, 10L);
            wrongBookService.add(100L, 10L);

            // 应该调用两次 update，不调用 insert
            verify(wrongBookMapper, times(2)).update(any());
            verify(wrongBookMapper, never()).insert(any());
        }
    }
}