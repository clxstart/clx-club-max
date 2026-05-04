package com.clx.analytics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clx.analytics.dto.DailyReportResponse;
import com.clx.analytics.dto.HotPostResponse;
import com.clx.analytics.dto.TrendResponse;
import com.clx.analytics.entity.AnalyticsReport;
import com.clx.analytics.mapper.AnalyticsReportMapper;
import com.clx.analytics.service.AnalyticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 分析报表服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    @Override
    public DailyReportResponse getDailyReport(LocalDate date) {
        DailyReportResponse response = new DailyReportResponse();
        response.setDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // 查询各项指标
        response.setDau(getMetricValue(date, "dau"));
        response.setWau(getMetricValue(date, "wau"));
        response.setMau(getMetricValue(date, "mau"));
        response.setNewUsers(getMetricValue(date, "new_users"));
        response.setNewPosts(getMetricValue(date, "new_posts"));
        response.setNewComments(getMetricValue(date, "new_comments"));
        response.setRetention1d(getMetricPercent(date, "retention_1d"));
        response.setRetention7d(getMetricPercent(date, "retention_7d"));
        response.setRetention30d(getMetricPercent(date, "retention_30d"));

        return response;
    }

    @Override
    public List<HotPostResponse> getHotPosts(LocalDate date, String type, int limit) {
        List<HotPostResponse> result = new ArrayList<>();

        LambdaQueryWrapper<AnalyticsReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalyticsReport::getReportDate, date)
                .eq(AnalyticsReport::getReportType, "hot_posts")
                .likeRight(AnalyticsReport::getDimension, "{\"rank_type\":\"" + type + "\"")
                .last("LIMIT " + limit);

        List<AnalyticsReport> reports = reportMapper.selectList(wrapper);
        for (AnalyticsReport report : reports) {
            HotPostResponse post = new HotPostResponse();
            // metricName 格式为 post_101，提取数字部分
            String postIdStr = report.getMetricName().replace("post_", "");
            post.setPostId(Long.parseLong(postIdStr));
            // metricValue 是浏览数
            post.setViewCount(report.getMetricValue() != null ? report.getMetricValue().longValue() : 0L);
            // 从 dimension 字段解析详细信息
            parsePostDimension(report.getDimension(), post);
            result.add(post);
        }

        return result;
    }

    @Override
    public TrendResponse getTrend(LocalDate startDate, LocalDate endDate, String metric) {
        TrendResponse response = new TrendResponse();
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        LambdaQueryWrapper<AnalyticsReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalyticsReport::getReportType, "daily")
                .eq(AnalyticsReport::getMetricName, metric)
                .between(AnalyticsReport::getReportDate, startDate, endDate)
                .orderByAsc(AnalyticsReport::getReportDate);

        List<AnalyticsReport> reports = reportMapper.selectList(wrapper);
        for (AnalyticsReport report : reports) {
            dates.add(report.getReportDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            values.add(report.getMetricValue() != null ? report.getMetricValue().longValue() : 0L);
        }

        response.setDates(dates);
        response.setValues(values);
        return response;
    }

    /** 获取指标值 */
    private Long getMetricValue(LocalDate date, String metricName) {
        LambdaQueryWrapper<AnalyticsReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalyticsReport::getReportDate, date)
                .eq(AnalyticsReport::getReportType, "daily")
                .eq(AnalyticsReport::getMetricName, metricName);

        AnalyticsReport report = reportMapper.selectOne(wrapper);
        if (report != null && report.getMetricValue() != null) {
            return report.getMetricValue().longValue();
        }
        return 0L;
    }

    /** 获取百分比指标 */
    private Double getMetricPercent(LocalDate date, String metricName) {
        LambdaQueryWrapper<AnalyticsReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnalyticsReport::getReportDate, date)
                .eq(AnalyticsReport::getReportType, "daily")
                .eq(AnalyticsReport::getMetricName, metricName);

        AnalyticsReport report = reportMapper.selectOne(wrapper);
        if (report != null && report.getMetricValue() != null) {
            return report.getMetricValue().doubleValue();
        }
        return 0.0;
    }

    /** 解析帖子维度信息 */
    private void parsePostDimension(String dimension, HotPostResponse post) {
        try {
            if (dimension != null && !dimension.isEmpty()) {
                // 解析 {"rank_type":"view","title":"xxx"} 格式
                var map = objectMapper.readValue(dimension, java.util.Map.class);
                post.setTitle((String) map.get("title"));
                post.setAuthorName((String) map.get("authorName"));
                if (map.get("viewCount") != null) {
                    post.setViewCount(((Number) map.get("viewCount")).longValue());
                }
                if (map.get("likeCount") != null) {
                    post.setLikeCount(((Number) map.get("likeCount")).longValue());
                }
                if (map.get("commentCount") != null) {
                    post.setCommentCount(((Number) map.get("commentCount")).longValue());
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("解析维度信息失败: {}", dimension, e);
        }
    }
}
