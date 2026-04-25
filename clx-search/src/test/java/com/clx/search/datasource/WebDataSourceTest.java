package com.clx.search.datasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebDataSource 单元测试。
 *
 * 验证点：
 * 1. 爬虫失败返回空列表，不抛异常
 * 2. 结果格式正确
 */
class WebDataSourceTest {

    private final WebDataSource webDataSource = new WebDataSource();

    @Test
    @DisplayName("数据源名称正确")
    void testGetName() {
        assertEquals("web", webDataSource.getName(), "数据源名称应为 web");
    }

    @Test
    @DisplayName("正常关键词搜索")
    void testDoSearch_NormalKeyword() {
        // 真实网络请求测试
        List<WebDataSource.WebResult> results = webDataSource.doSearch("Spring Boot", 1, 5);

        assertNotNull(results, "结果不应为空（但可以为空列表）");
        // 如果网络正常，应该有结果
        if (!results.isEmpty()) {
            WebDataSource.WebResult first = results.get(0);
            assertNotNull(first.getTitle(), "结果标题不应为空");
            assertNotNull(first.getUrl(), "结果 URL 不应为空");
            assertEquals("bing", first.getSource(), "来源应为 bing");
        }
    }

    @Test
    @DisplayName("异常关键词搜索返回空列表")
    void testDoSearch_InvalidKeyword() {
        // 空关键词
        List<WebDataSource.WebResult> results = webDataSource.doSearch("", 1, 10);
        assertNotNull(results, "空关键词搜索应返回空列表，不抛异常");
        assertTrue(results.isEmpty(), "空关键词搜索应返回空列表");
    }

    @Test
    @DisplayName("爬虫失败不抛异常")
    void testDoSearch_NoExceptionOnFailure() {
        List<WebDataSource.WebResult> results = null;
        try {
            results = webDataSource.doSearch("测试", 1, 10);
        } catch (Exception e) {
            fail("爬虫失败不应抛异常，但抛出: " + e.getMessage());
        }
        assertNotNull(results, "结果不应为 null");
    }

    @Test
    @DisplayName("结果数量限制")
    void testDoSearch_ResultSize() {
        int size = 3;
        List<WebDataSource.WebResult> results = webDataSource.doSearch("Java", 1, size);

        assertNotNull(results, "结果不应为空");
        assertTrue(results.size() <= size, "结果数量不应超过 " + size);
    }
}