package com.clx.search.manager;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.search.datasource.DataSource;
import com.clx.search.datasource.DataSourceRegistry;
import com.clx.search.dto.SearchRequest;
import com.clx.search.enums.SearchTypeEnum;
import com.clx.search.service.HotKeywordService;
import com.clx.search.service.SearchLogService;
import com.clx.search.vo.SearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 搜索门面 - 业务聚合层。
 *
 * 职责：解析请求、并发搜索、合并结果、记录日志和热词。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchFacade {

    private final DataSourceRegistry registry;
    private final HotKeywordService hotKeywordService;
    private final SearchLogService searchLogService;

    /** 爬虫超时时间（毫秒） */
    private static final long CRAWLER_TIMEOUT = 3000;

    /** 执行聚合搜索 */
    public SearchVO searchAll(SearchRequest request, String ip) {
        String keyword = request.getKeyword();
        int page = request.getPage() != null ? request.getPage() : 1;
        int size = request.getSize() != null ? request.getSize() : 10;

        long startTime = System.currentTimeMillis();

        // 记录热词
        hotKeywordService.recordKeyword(keyword);

        SearchVO searchVO = new SearchVO();
        searchVO.setKeyword(keyword);

        // 确定要搜索的类型（过滤无效类型）
        List<String> types = request.getTypes();
        if (types == null || types.isEmpty()) {
            types = Arrays.asList("post", "user", "category", "tag", "picture", "web");
        }
        // 使用枚举校验并过滤无效类型
        final List<String> validTypes = types.stream()
                .filter(SearchTypeEnum::isValid)
                .collect(Collectors.toList());

        if (validTypes.isEmpty()) {
            log.warn("没有有效的搜索类型: {}", types);
            return searchVO;
        }

        // 并发搜索
        Map<String, CompletableFuture<SearchVO.SearchResult>> futures = new HashMap<>();

        for (String type : validTypes) {
            DataSource<?> dataSource = registry.getDataSource(type);
            if (dataSource == null) {
                log.warn("数据源不存在: {}", type);
                continue;
            }

            CompletableFuture<SearchVO.SearchResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    List<?> items = dataSource.doSearch(keyword, page, size);
                    SearchVO.SearchResult result = new SearchVO.SearchResult();
                    result.setTotal((long) items.size());
                    result.setItems(items);
                    return result;
                } catch (Exception e) {
                    log.error("搜索 {} 失败: {}", type, e.getMessage());
                    SearchVO.SearchResult result = new SearchVO.SearchResult();
                    result.setError("搜索失败: " + e.getMessage());
                    return result;
                }
            });

            // 爬虫数据源设置超时
            if ("picture".equals(type) || "web".equals(type)) {
                future = future.orTimeout(CRAWLER_TIMEOUT, TimeUnit.MILLISECONDS)
                        .exceptionally(e -> {
                            SearchVO.SearchResult result = new SearchVO.SearchResult();
                            result.setError("搜索超时");
                            return result;
                        });
            }

            futures.put(type, future);
        }

        // 等待所有搜索完成
        CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0])).join();

        // 收集结果
        Map<String, SearchVO.SearchResult> results = new HashMap<>();
        boolean hasError = false;
        int totalResultCount = 0;
        for (Map.Entry<String, CompletableFuture<SearchVO.SearchResult>> entry : futures.entrySet()) {
            try {
                SearchVO.SearchResult result = entry.getValue().get();
                results.put(entry.getKey(), result);
                if (result.getError() != null) {
                    hasError = true;
                } else {
                    totalResultCount += result.getTotal() != null ? result.getTotal().intValue() : 0;
                }
            } catch (Exception e) {
                log.error("获取 {} 结果失败: {}", entry.getKey(), e.getMessage());
                hasError = true;
            }
        }

        searchVO.setResults(results);
        searchVO.setPartialSuccess(hasError);
        long totalTime = System.currentTimeMillis() - startTime;
        searchVO.setTotalTime(totalTime);

        // 异步记录搜索日志
        Long userId = null;
        try {
            if (StpUtil.isLogin()) {
                userId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception ignored) {}
        final Long finalUserId = userId;
        final int finalTotalResultCount = totalResultCount;
        final long finalTotalTime = totalTime;
        CompletableFuture.runAsync(() -> {
            searchLogService.recordLog(keyword, finalUserId, String.join(",", validTypes), finalTotalResultCount, (int) finalTotalTime, ip);
        });

        return searchVO;
    }

    /** 单类型搜索 */
    public SearchVO.SearchResult searchSingle(String type, String keyword, int page, int size) {
        // 校验类型
        if (!SearchTypeEnum.isValid(type)) {
            SearchVO.SearchResult result = new SearchVO.SearchResult();
            result.setError("无效的搜索类型: " + type);
            return result;
        }

        DataSource<?> dataSource = registry.getDataSource(type);
        if (dataSource == null) {
            SearchVO.SearchResult result = new SearchVO.SearchResult();
            result.setError("数据源不存在: " + type);
            return result;
        }

        try {
            List<?> items = dataSource.doSearch(keyword, page, size);
            SearchVO.SearchResult result = new SearchVO.SearchResult();
            result.setTotal((long) items.size());
            result.setItems(items);
            return result;
        } catch (Exception e) {
            log.error("搜索 {} 失败: {}", type, e.getMessage());
            SearchVO.SearchResult result = new SearchVO.SearchResult();
            result.setError("搜索失败: " + e.getMessage());
            return result;
        }
    }
}