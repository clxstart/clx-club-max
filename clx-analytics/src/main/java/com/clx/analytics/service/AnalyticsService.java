package com.clx.analytics.service;

import com.clx.analytics.dto.DailyReportResponse;
import com.clx.analytics.dto.HotPostResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 分析报表服务接口
 */
public interface AnalyticsService {

    /**
     * 获取日报表
     *
     * @param date 日期
     * @return 日报表数据
     */
    DailyReportResponse getDailyReport(LocalDate date);

    /**
     * 获取热门帖子
     *
     * @param date 日期
     * @param type 类型: view/like/comment
     * @param limit 数量限制
     * @return 热门帖子列表
     */
    List<HotPostResponse> getHotPosts(LocalDate date, String type, int limit);

    /**
     * 获取趋势数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param metric 指标: dau/mau/newUsers
     * @return 趋势数据（日期和值列表）
     */
    TrendResponse getTrend(LocalDate startDate, LocalDate endDate, String metric);
}
