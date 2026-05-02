package com.clx.analytics.controller;

import com.clx.analytics.dto.DailyReportResponse;
import com.clx.analytics.dto.HotPostResponse;
import com.clx.analytics.dto.TrendResponse;
import com.clx.analytics.service.AnalyticsService;
import com.clx.common.core.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 分析报表控制器
 */
@Tag(name = "分析报表接口")
@RestController
@RequestMapping("/analytics/report")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "获取日报表")
    @GetMapping("/daily")
    public R<DailyReportResponse> getDailyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return R.ok(analyticsService.getDailyReport(date));
    }

    @Operation(summary = "获取热门帖子")
    @GetMapping("/hot-posts")
    public R<List<HotPostResponse>> getHotPosts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "view") String type,
            @RequestParam(defaultValue = "10") int limit) {
        return R.ok(analyticsService.getHotPosts(date, type, limit));
    }

    @Operation(summary = "获取趋势数据")
    @GetMapping("/trend")
    public R<TrendResponse> getTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "dau") String metric) {
        return R.ok(analyticsService.getTrend(startDate, endDate, metric));
    }
}
