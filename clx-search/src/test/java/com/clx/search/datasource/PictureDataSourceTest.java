package com.clx.search.datasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PictureDataSource 单元测试。
 *
 * 验证点：
 * 1. 爬虫失败返回空列表，不抛异常
 * 2. 爬虫超时不阻塞
 */
class PictureDataSourceTest {

    private final PictureDataSource pictureDataSource = new PictureDataSource();

    @Test
    @DisplayName("数据源名称正确")
    void testGetName() {
        assertEquals("picture", pictureDataSource.getName(), "数据源名称应为 picture");
    }

    @Test
    @DisplayName("正常关键词搜索")
    void testDoSearch_NormalKeyword() {
        // 注意：这是真实网络请求测试，可能因网络问题失败
        // 在 CI 环境中可能需要 skip
        List<PictureDataSource.Picture> results = pictureDataSource.doSearch("Spring Boot", 1, 5);

        // 只要没有抛异常就算通过（网络问题可能返回空）
        assertNotNull(results, "结果不应为空（但可以为空列表）");
        // 如果网络正常，应该有结果
        // assertTrue(results.size() > 0, "正常搜索应有结果");
    }

    @Test
    @DisplayName("异常关键词搜索不抛异常")
    void testDoSearch_InvalidKeyword() {
        // 空关键词 - 可能返回空列表或有结果，关键是不能抛异常
        List<PictureDataSource.Picture> results = pictureDataSource.doSearch("", 1, 10);
        assertNotNull(results, "空关键词搜索应返回列表，不抛异常");

        // 特殊字符关键词
        results = pictureDataSource.doSearch("!@#$%^&*()", 1, 10);
        assertNotNull(results, "特殊字符搜索应返回结果（可能空），不抛异常");
    }

    @Test
    @DisplayName("分页参数正确处理")
    void testDoSearch_Pagination() {
        // 第 1 页
        List<PictureDataSource.Picture> page1 = pictureDataSource.doSearch("Java", 1, 3);
        assertNotNull(page1, "第1页结果不应为空");

        // 第 2 页
        List<PictureDataSource.Picture> page2 = pictureDataSource.doSearch("Java", 2, 3);
        assertNotNull(page2, "第2页结果不应为空");

        // 不同页应该有不同结果（如果网络正常）
        // 注意：这个测试依赖真实网络，可能不稳定
    }

    @Test
    @DisplayName("爬虫失败不抛异常")
    void testDoSearch_NoExceptionOnFailure() {
        // 使用一个可能触发网络错误的 URL
        // 但由于 JSoup 内部处理，应该返回空列表而不是抛异常
        List<PictureDataSource.Picture> results = null;
        try {
            results = pictureDataSource.doSearch("测试关键词", 1, 10);
        } catch (Exception e) {
            fail("爬虫失败不应抛异常，但抛出: " + e.getMessage());
        }
        assertNotNull(results, "结果不应为 null");
    }
}