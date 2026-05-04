package com.clx.admin.feign;

import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据分析服务 Feign 客户端。
 */
@FeignClient(name = "clx-analytics", contextId = "analyticsFeignClient", url = "${feign.client.config.clx-analytics.url:}")
public interface AnalyticsFeignClient {

    /** 获取概览统计 */
    @GetMapping("/internal/analytics/overview")
    R<Map<String, Object>> getOverview();

    /** 获取趋势数据 */
    @GetMapping("/internal/analytics/trend")
    R<Map<String, Object>> getTrend(@RequestParam(required = false, defaultValue = "7") Integer days);
}