package com.clx.search.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 搜索热词实体。
 */
@Data
public class HotKeyword {

    /** ID */
    private Long id;

    /** 关键词 */
    private String keyword;

    /** 搜索次数 */
    private Long searchCount;

    /** 统计周期：day/week/month */
    private String periodType;

    /** 周期日期（如2026-04-23） */
    private String periodDate;

    /** 增长率（%） */
    private BigDecimal growthRate;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}