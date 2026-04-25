package com.clx.search.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSourceRegistry 单元测试。
 *
 * 验证点：
 * 1. 数据源正确注册
 * 2. 根据类型获取数据源
 * 3. 判断数据源是否存在
 */
class DataSourceRegistryTest {

    private DataSourceRegistry registry;

    @BeforeEach
    void setUp() {
        // 创建 mock 数据源
        DataSource<String> postDataSource = new DataSource<>() {
            @Override
            public List<String> doSearch(String keyword, int page, int size) {
                return Arrays.asList("post1", "post2");
            }
            @Override
            public String getName() {
                return "post";
            }
        };

        DataSource<String> userDataSource = new DataSource<>() {
            @Override
            public List<String> doSearch(String keyword, int page, int size) {
                return Arrays.asList("user1");
            }
            @Override
            public String getName() {
                return "user";
            }
        };

        DataSource<String> pictureDataSource = new DataSource<>() {
            @Override
            public List<String> doSearch(String keyword, int page, int size) {
                return Arrays.asList("pic1");
            }
            @Override
            public String getName() {
                return "picture";
            }
        };

        // 构造 Registry
        List<DataSource<?>> dataSources = Arrays.asList(postDataSource, userDataSource, pictureDataSource);
        registry = new DataSourceRegistry(dataSources);
        registry.init();
    }

    @Test
    @DisplayName("数据源注册成功")
    void testInit() {
        Set<String> types = registry.getRegisteredTypes();
        assertEquals(3, types.size(), "应注册3个数据源");
        assertTrue(types.contains("post"), "应包含 post 数据源");
        assertTrue(types.contains("user"), "应包含 user 数据源");
        assertTrue(types.contains("picture"), "应包含 picture 数据源");
    }

    @Test
    @DisplayName("根据类型获取数据源")
    void testGetDataSource() {
        DataSource<?> postSource = registry.getDataSource("post");
        assertNotNull(postSource, "post 数据源不应为空");
        assertEquals("post", postSource.getName(), "数据源名称应为 post");

        DataSource<?> userSource = registry.getDataSource("user");
        assertNotNull(userSource, "user 数据源不应为空");
        assertEquals("user", userSource.getName(), "数据源名称应为 user");
    }

    @Test
    @DisplayName("获取不存在的数据源返回 null")
    void testGetNonExistentDataSource() {
        DataSource<?> source = registry.getDataSource("nonexistent");
        assertNull(source, "不存在的数据源应返回 null");
    }

    @Test
    @DisplayName("判断数据源是否存在")
    void testHasDataSource() {
        assertTrue(registry.hasDataSource("post"), "post 数据源应存在");
        assertTrue(registry.hasDataSource("user"), "user 数据源应存在");
        assertFalse(registry.hasDataSource("nonexistent"), "nonexistent 数据源不应存在");
    }

    @Test
    @DisplayName("数据源可正常搜索")
    void testDataSourceSearch() {
        DataSource<?> postSource = registry.getDataSource("post");
        List<?> results = postSource.doSearch("test", 1, 10);
        assertEquals(2, results.size(), "应返回2条结果");
    }
}
