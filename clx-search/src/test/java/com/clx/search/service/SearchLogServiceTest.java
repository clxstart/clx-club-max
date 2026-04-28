package com.clx.search.service;

import com.clx.search.entity.SearchLog;
import com.clx.search.mapper.SearchLogMapper;
import com.clx.search.service.impl.SearchLogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SearchLogService 单元测试。
 *
 * 验证点：
 * 1. 关键信息完整记录
 * 2. 记录失败不影响主流程
 */
@ExtendWith(MockitoExtension.class)
class SearchLogServiceTest {

    @Mock
    private SearchLogMapper searchLogMapper;

    private SearchLogService searchLogService;

    @BeforeEach
    void setUp() {
        searchLogService = new SearchLogServiceImpl(searchLogMapper);
    }

    @Test
    @DisplayName("正常记录搜索日志")
    void testRecordLog_Success() {
        searchLogService.recordLog(
                "Spring Boot",
                100L,
                "post,user",
                156,
                320,
                "192.168.1.1"
        );

        // 验证 mapper 被调用
        ArgumentCaptor<SearchLog> captor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogMapper, times(1)).insert(captor.capture());

        SearchLog log = captor.getValue();
        assertEquals("Spring Boot", log.getKeyword(), "关键词应为 Spring Boot");
        assertEquals(100L, log.getUserId(), "用户ID应为 100");
        assertEquals("post,user", log.getSearchTypes(), "搜索类型应为 post,user");
        assertEquals(156, log.getResultCount(), "结果数量应为 156");
        assertEquals(320, log.getCostTime(), "耗时应为 320ms");
        assertEquals("192.168.1.1", log.getIp(), "IP 应为 192.168.1.1");
        assertNotNull(log.getCreateTime(), "创建时间不应为空");
    }

    @Test
    @DisplayName("未登录用户 userId 为 null")
    void testRecordLog_AnonymousUser() {
        searchLogService.recordLog(
                "Java",
                null,  // 未登录
                "post",
                50,
                200,
                "10.0.0.1"
        );

        ArgumentCaptor<SearchLog> captor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogMapper, times(1)).insert(captor.capture());

        SearchLog log = captor.getValue();
        assertNull(log.getUserId(), "未登录用户 ID 应为 null");
        assertEquals("Java", log.getKeyword(), "关键词应为 Java");
    }

    @Test
    @DisplayName("记录失败不抛异常")
    void testRecordLog_Failure() {
        // 模拟数据库插入失败
        doThrow(new RuntimeException("数据库连接失败"))
                .when(searchLogMapper).insert(any(SearchLog.class));

        // 调用不应抛异常
        assertDoesNotThrow(() -> {
            searchLogService.recordLog("test", 1L, "post", 10, 100, "127.0.0.1");
        }, "记录失败不应抛异常");
    }

    @Test
    @DisplayName("完整搜索日志信息")
    void testRecordLog_CompleteInfo() {
        String keyword = "Elasticsearch";
        Long userId = 999L;
        String types = "post,user,category";
        int resultCount = 256;
        int costTime = 450;
        String ip = "172.16.0.100";

        searchLogService.recordLog(keyword, userId, types, resultCount, costTime, ip);

        ArgumentCaptor<SearchLog> captor = ArgumentCaptor.forClass(SearchLog.class);
        verify(searchLogMapper).insert(captor.capture());

        SearchLog log = captor.getValue();

        // 验证所有字段
        assertEquals(keyword, log.getKeyword(), "关键词匹配");
        assertEquals(userId, log.getUserId(), "用户ID匹配");
        assertEquals(types, log.getSearchTypes(), "搜索类型匹配");
        assertEquals(resultCount, log.getResultCount(), "结果数量匹配");
        assertEquals(costTime, log.getCostTime(), "耗时匹配");
        assertEquals(ip, log.getIp(), "IP匹配");
        assertNotNull(log.getCreateTime(), "创建时间已设置");
    }
}