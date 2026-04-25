package com.clx.quiz.service;

import com.clx.quiz.dto.SubjectCreateRequest;
import com.clx.quiz.dto.SubjectOptionDTO;
import com.clx.quiz.dto.SubjectQueryRequest;
import com.clx.quiz.entity.Subject;
import com.clx.quiz.entity.SubjectCategory;
import com.clx.quiz.entity.SubjectLabel;
import com.clx.quiz.entity.SubjectMapping;
import com.clx.quiz.mapper.SubjectMapper;
import com.clx.quiz.mapper.SubjectMappingMapper;
import com.clx.quiz.service.handler.SubjectTypeHandler;
import com.clx.quiz.service.handler.SubjectTypeHandlerFactory;
import com.clx.quiz.service.impl.SubjectServiceImpl;
import com.clx.quiz.vo.SubjectDetailVO;
import com.clx.quiz.vo.SubjectVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 题目服务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class SubjectServiceTest {

    @Mock
    private SubjectMapper subjectMapper;

    @Mock
    private SubjectMappingMapper mappingMapper;

    @Mock
    private SubjectTypeHandlerFactory handlerFactory;

    @Mock
    private SubjectCategoryService categoryService;

    @Mock
    private SubjectLabelService labelService;

    @Mock
    private SubjectTypeHandler handler;

    @InjectMocks
    private SubjectServiceImpl subjectService;

    private Subject buildSubject() {
        Subject subject = new Subject();
        subject.setId(1L);
        subject.setSubjectName("测试题目");
        subject.setSubjectType(1);
        subject.setSubjectDifficult(1);
        subject.setSubjectScore(5);
        subject.setSubjectParse("这是解析");
        return subject;
    }

    @Nested
    @DisplayName("新增题目")
    class Create {

        @Test
        @DisplayName("新增单选题 - 成功")
        void createRadio_success() {
            SubjectCreateRequest request = new SubjectCreateRequest();
            request.setSubjectName("单选题测试");
            request.setSubjectType(1);
            request.setSubjectDifficult(1);
            request.setSubjectScore(5);
            request.setSubjectParse("解析内容");
            request.setCategoryIds(Arrays.asList(1L));
            request.setLabelIds(Arrays.asList(1L, 2L));

            SubjectOptionDTO opt1 = new SubjectOptionDTO();
            opt1.setOptionType(1);
            opt1.setOptionContent("选项A");
            opt1.setIsCorrect(1);
            SubjectOptionDTO opt2 = new SubjectOptionDTO();
            opt2.setOptionType(2);
            opt2.setOptionContent("选项B");
            opt2.setIsCorrect(0);
            request.setOptionList(Arrays.asList(opt1, opt2));

            when(handlerFactory.getHandler(1)).thenReturn(handler);

            boolean result = subjectService.add(request, "admin");

            assertTrue(result);
            verify(subjectMapper).insert(any(Subject.class));
            verify(handler).save(isNull(), eq(request.getOptionList()), eq("admin"));
            verify(mappingMapper).batchInsert(anyList());
        }

        @Test
        @DisplayName("新增多选题 - 成功")
        void createMultiple_success() {
            SubjectCreateRequest request = new SubjectCreateRequest();
            request.setSubjectName("多选题测试");
            request.setSubjectType(2);
            request.setSubjectDifficult(2);
            request.setSubjectScore(10);
            request.setOptionList(Arrays.asList(
                    createOption(1, "选项A", 1),
                    createOption(2, "选项B", 1),
                    createOption(3, "选项C", 0)
            ));

            when(handlerFactory.getHandler(2)).thenReturn(handler);

            boolean result = subjectService.add(request, "admin");

            assertTrue(result);
            verify(handler).save(isNull(), eq(request.getOptionList()), eq("admin"));
        }

        @Test
        @DisplayName("新增判断题 - 成功")
        void createJudge_success() {
            SubjectCreateRequest request = new SubjectCreateRequest();
            request.setSubjectName("判断题测试");
            request.setSubjectType(3);
            request.setSubjectDifficult(1);
            request.setSubjectScore(5);
            request.setOptionList(Arrays.asList(
                    createOption(1, "", 1) // 判断题只有一条答案记录
            ));

            when(handlerFactory.getHandler(3)).thenReturn(handler);

            boolean result = subjectService.add(request, "admin");

            assertTrue(result);
            verify(handler).save(isNull(), eq(request.getOptionList()), eq("admin"));
        }

        @Test
        @DisplayName("新增简答题 - 成功")
        void createBrief_success() {
            SubjectCreateRequest request = new SubjectCreateRequest();
            request.setSubjectName("简答题测试");
            request.setSubjectType(4);
            request.setSubjectDifficult(3);
            request.setSubjectScore(20);
            request.setSubjectParse("简答题解析");
            request.setOptionList(Arrays.asList(
                    createOption(1, "这是参考答案内容", 1)
            ));

            when(handlerFactory.getHandler(4)).thenReturn(handler);

            boolean result = subjectService.add(request, "admin");

            assertTrue(result);
            verify(handler).save(isNull(), eq(request.getOptionList()), eq("admin"));
        }

        @Test
        @DisplayName("无分类标签 - 不保存关联")
        void create_withoutCategoryAndLabel() {
            SubjectCreateRequest request = new SubjectCreateRequest();
            request.setSubjectName("无分类题目");
            request.setSubjectType(1);
            request.setOptionList(Arrays.asList(createOption(1, "A", 1)));

            when(handlerFactory.getHandler(1)).thenReturn(handler);

            boolean result = subjectService.add(request, "admin");

            assertTrue(result);
            verify(mappingMapper, never()).batchInsert(anyList());
        }
    }

    @Nested
    @DisplayName("删除题目")
    class Delete {

        @Test
        @DisplayName("正常删除")
        void delete_success() {
            Subject subject = buildSubject();
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(subjectMapper.deleteById(1L)).thenReturn(1);

            boolean result = subjectService.delete(1L);

            assertTrue(result);
            verify(handler).delete(1L);
            verify(mappingMapper).deleteBySubjectId(1L);
        }

        @Test
        @DisplayName("题目不存在 - 返回 false")
        void delete_notExist() {
            when(subjectMapper.selectById(999L)).thenReturn(null);

            boolean result = subjectService.delete(999L);

            assertFalse(result);
            verify(subjectMapper, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("分页查询")
    class QueryPage {

        @Test
        @DisplayName("正常分页查询")
        void queryPage_success() {
            Subject subject = buildSubject();
            SubjectCategory category = new SubjectCategory();
            category.setId(1L);
            category.setCategoryName("后端");
            SubjectLabel label = new SubjectLabel();
            label.setId(1L);
            label.setLabelName("Java");

            when(subjectMapper.selectPage(isNull(), isNull(), isNull(), eq(0), eq(10)))
                    .thenReturn(Arrays.asList(subject));
            when(categoryService.getAll()).thenReturn(Arrays.asList(category));
            when(labelService.getAll()).thenReturn(Arrays.asList(label));
            when(mappingMapper.selectBySubjectId(1L)).thenReturn(Arrays.asList(
                    createMapping(1L, 1L, 1L)
            ));

            SubjectQueryRequest request = new SubjectQueryRequest();
            request.setPageNo(1);
            request.setPageSize(10);

            List<SubjectVO> result = subjectService.queryPage(request);

            assertEquals(1, result.size());
            assertEquals("后端", result.get(0).getCategoryName());
        }

        @Test
        @DisplayName("空结果返回空列表")
        void queryPage_empty() {
            when(subjectMapper.selectPage(isNull(), isNull(), isNull(), eq(0), eq(10)))
                    .thenReturn(Collections.emptyList());
            when(categoryService.getAll()).thenReturn(Collections.emptyList());
            when(labelService.getAll()).thenReturn(Collections.emptyList());

            SubjectQueryRequest request = new SubjectQueryRequest();
            request.setPageNo(1);
            request.setPageSize(10);

            List<SubjectVO> result = subjectService.queryPage(request);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("获取详情")
    class GetDetail {

        @Test
        @DisplayName("正常获取详情")
        void getDetail_success() {
            Subject subject = buildSubject();
            when(subjectMapper.selectById(1L)).thenReturn(subject);
            when(mappingMapper.selectBySubjectId(1L)).thenReturn(Collections.emptyList());
            when(handlerFactory.getHandler(1)).thenReturn(handler);
            when(handler.getWithOptions(1L)).thenReturn(Collections.emptyList());

            SubjectDetailVO vo = subjectService.getDetail(1L);

            assertNotNull(vo);
            assertEquals(1L, vo.getId());
            assertEquals("测试题目", vo.getSubjectName());
        }

        @Test
        @DisplayName("题目不存在 - 返回 null")
        void getDetail_notExist() {
            when(subjectMapper.selectById(999L)).thenReturn(null);

            SubjectDetailVO vo = subjectService.getDetail(999L);

            assertNull(vo);
        }
    }

    private SubjectOptionDTO createOption(int type, String content, int isCorrect) {
        SubjectOptionDTO dto = new SubjectOptionDTO();
        dto.setOptionType(type);
        dto.setOptionContent(content);
        dto.setIsCorrect(isCorrect);
        return dto;
    }

    private SubjectMapping createMapping(Long subjectId, Long categoryId, Long labelId) {
        SubjectMapping mapping = new SubjectMapping();
        mapping.setSubjectId(subjectId);
        mapping.setCategoryId(categoryId);
        mapping.setLabelId(labelId);
        return mapping;
    }
}