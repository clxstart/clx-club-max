package com.clx.analytics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 分析报表实体
 */
@Data
@TableName("analytics_report")
public class AnalyticsReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 报表日期 */
    private LocalDate reportDate;

    /** 报表类型: daily/hot_posts/trend */
    private String reportType;

    /** 指标名称 */
    private String metricName;

    /** 指标值 */
    private BigDecimal metricValue;

    /** 维度(JSON) */
    private String dimension;

    /** 创建时间 */
    private LocalDateTime createTime;
}
