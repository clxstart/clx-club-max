package com.clx.search.manager;

import com.clx.search.datasource.DataSource;
import com.clx.search.datasource.DataSourceRegistry;
import com.clx.search.dto.SearchRequest;
import com.clx.search.service.HotKeywordService;
import com.clx.search.service.SearchLogService;
import com.clx.search.vo.SearchVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.quality.Strictness.*;

/**
 * SearchFacade 单元测试。
 *
 * 验证点：
 * 1. 并发查询多个数据源
 * 2. 部分数据源失败不影响其他
 * 3. 热词记录和日志记录
 *
 * 注意：使用 doReturn().when() 语法解决泛型通配符类型问题
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class SearchFacadeTest {

    @Mock
    private DataSourceRegistry registry;

    @Mock
    private HotKeywordService hotKeywordService;

    @Mock
    private SearchLogService searchLogService;

    private SearchFacade searchFacade;

    @BeforeEach
    void setUp() {
        searchFacade = new SearchFacade(registry, hotKeywordService, searchLogService);
    }

    @Test
    @DisplayName("并发查询多个数据源成功")
    void testSearchAll_Success() {
        // 准备 mock 数据源（使用 doReturn 绕过泛型问题）
        DataSource<?> postDataSource = createMockDataSource("post", Arrays.asList("post1", "post2"));
        DataSource<?> userDataSource = createMockDataSource("user", Arrays.asList("user1"));
        DataSource<?> categoryDataSource = createMockDataSource("category", Arrays.asList("cat1"));

        doReturn(postDataSource).when(registry).getDataSource("post");
        doReturn(userDataSource).when(registry).getDataSource("user");
        doReturn(categoryDataSource).when(registry).getDataSource("category");

        SearchRequest request = new SearchRequest();
        request.setKeyword("test");
        request.setTypes(Arrays.asList("post", "user", "category"));
        request.setPage(1);
        request.setSize(10);

        SearchVO result = searchFacade.searchAll(request, "127.0.0.1");

        assertNotNull(result, "结果不应为空");
        assertEquals("test", result.getKeyword(), "关键词应为 test");
        assertNotNull(result.getResults(), "结果列表不应为空");
        assertEquals(3, result.getResults().size(), "应返回3种数据源结果");
        assertFalse(result.getPartialSuccess(), "不应有部分失败");

        // 验证热词记录
        verify(hotKeywordService, times(1)).recordKeyword("test");
    }

    @Test
    @DisplayName("部分数据源失败不影响其他")
    void testSearchAll_PartialFailure() {
        // post 数据源正常
        DataSource<?> postDataSource = createMockDataSource("post", Arrays.asList("post1", "post2"));
        // picture 数据源模拟失败
        DataSource<?> pictureDataSource = mock(DataSource.class);
        when(pictureDataSource.getName()).thenReturn("picture");
        doThrow(new RuntimeException("爬取失败"))
                .when(pictureDataSource).doSearch(anyString(), anyInt(), anyInt());

        doReturn(postDataSource).when(registry).getDataSource("post");
        doReturn(pictureDataSource).when(registry).getDataSource("picture");

        SearchRequest request = new SearchRequest();
        request.setKeyword("test");
        request.setTypes(Arrays.asList("post", "picture"));
        request.setPage(1);
        request.setSize(10);

        SearchVO result = searchFacade.searchAll(request, "127.0.0.1");

        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.getResults().size(), "应返回2种数据源结果");

        // post 结果正常
        SearchVO.SearchResult postResult = result.getResults().get("post");
        assertNotNull(postResult, "post 结果不应为空");
        assertNull(postResult.getError(), "post 不应有错误");
        assertEquals(2, postResult.getTotal(), "post 应有2条结果");

        // picture 结果有错误
        SearchVO.SearchResult pictureResult = result.getResults().get("picture");
        assertNotNull(pictureResult, "picture 结果不应为空");
        assertNotNull(pictureResult.getError(), "picture 应有错误");

        assertTrue(result.getPartialSuccess(), "应标记为部分成功");
    }

    @Test
    @DisplayName("默认搜索5种数据源")
    void testSearchAll_DefaultTypes() {
        DataSource<?> mockDataSource = createMockDataSource("mock", Arrays.asList("item1"));

        doReturn(mockDataSource).when(registry).getDataSource("post");
        doReturn(mockDataSource).when(registry).getDataSource("user");
        doReturn(mockDataSource).when(registry).getDataSource("category");
        doReturn(mockDataSource).when(registry).getDataSource("picture");
        doReturn(mockDataSource).when(registry).getDataSource("web");

        SearchRequest request = new SearchRequest();
        request.setKeyword("test");
        // 不设置 types，应使用默认的5种
        request.setPage(1);
        request.setSize(10);

        SearchVO result = searchFacade.searchAll(request, "127.0.0.1");

        assertEquals(5, result.getResults().size(), "应返回5种数据源结果");
    }

    @Test
    @DisplayName("单类型搜索成功")
    void testSearchSingle_Success() {
        DataSource<?> postDataSource = createMockDataSource("post", Arrays.asList("post1", "post2"));
        doReturn(postDataSource).when(registry).getDataSource("post");

        SearchVO.SearchResult result = searchFacade.searchSingle("post", "test", 1, 10);

        assertNotNull(result, "结果不应为空");
        assertEquals(2, result.getTotal(), "应有2条结果");
        assertNull(result.getError(), "不应有错误");
    }

    @Test
    @DisplayName("单类型搜索数据源不存在")
    void testSearchSingle_DataSourceNotFound() {
        doReturn(null).when(registry).getDataSource("nonexistent");

        SearchVO.SearchResult result = searchFacade.searchSingle("nonexistent", "test", 1, 10);

        assertNotNull(result, "结果不应为空");
        assertNotNull(result.getError(), "应有错误信息");
        assertTrue(result.getError().contains("数据源不存在"), "错误信息应包含'数据源不存在'");
    }

    // ========== 辅助方法 ==========

    private DataSource<?> createMockDataSource(String name, List<?> results) {
        DataSource<?> dataSource = mock(DataSource.class);
        when(dataSource.getName()).thenReturn(name);
        doReturn(results).when(dataSource).doSearch(anyString(), anyInt(), anyInt());
        return dataSource;
    }
}