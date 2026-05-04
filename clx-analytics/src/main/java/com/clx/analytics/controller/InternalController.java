package com.clx.analytics.controller;

import com.clx.analytics.entity.AnalyticsReport;
import com.clx.analytics.mapper.AnalyticsReportMapper;
import com.clx.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内部 API 控制器 - 供后台管理调用。
 */
@RestController
@RequestMapping("/internal/analytics")
@RequiredArgsConstructor
public class InternalController {

    private final AnalyticsReportMapper reportMapper;

    /** 获取概览统计 */
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        LocalDate today = LocalDate.now();

        Map<String, Object> result = new HashMap<>();
        result.put("dau", getMetricValue(today, "dau"));
        result.put("mau", getMetricValue(today, "mau"));
        result.put("newUsers", getMetricValue(today, "new_users"));
        result.put("newPosts", getMetricValue(today, "new_posts"));
        result.put("newComments", getMetricValue(today, "new_comments"));

        return R.ok(result);
    }

    /** 获取趋势数据 */
    @GetMapping("/trend")
    public R<Map<String, Object>> getTrend(@RequestParam(defaultValue = "7") Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<String> dates = new ArrayList<>();
        List<Long> dauList = new ArrayList<>();
        List<Long> postsList = new ArrayList<>();
        List<Long> commentsList = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dates.add(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            dauList.add(getMetricValue(date, "dau"));
            postsList.add(getMetricValue(date, "new_posts"));
            commentsList.add(getMetricValue(date, "new_comments"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("dau", dauList);
        result.put("posts", postsList);
        result.put("comments", commentsList);

        return R.ok(result);
    }

    private Long getMetricValue(LocalDate date, String metricName) {
        try {
            AnalyticsReport report = reportMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AnalyticsReport>()
                    .eq(AnalyticsReport::getReportDate, date)
                    .eq(AnalyticsReport::getReportType, "daily")
                    .eq(AnalyticsReport::getMetricName, metricName)
            );
            if (report != null && report.getMetricValue() != null) {
                return report.getMetricValue().longValue();
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}