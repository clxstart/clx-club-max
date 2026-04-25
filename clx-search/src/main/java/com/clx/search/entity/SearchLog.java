package com.clx.search.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索日志实体。
 */
@Data
public class SearchLog {

    /** 日志ID */
    private Long id;

    /** 搜索关键词 */
    private String keyword;

    /** 用户ID */
    private Long userId;

    /** 搜索类型（逗号分隔） */
    private String searchTypes;

    /** 结果总数 */
    private Integer resultCount;

    /** 耗时（毫秒） */
    private Integer costTime;

    /** 点击的结果ID（逗号分隔） */
    private String clickResults;

    /** IP地址 */
    private String ip;

    /** 用户代理 */
    private String userAgent;

    /** 搜索时间 */
    private LocalDateTime createTime;
}