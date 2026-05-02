package com.clx.analytics.dto;

import lombok.Data;

/**
 * 日报表响应
 */
@Data
public class DailyReportResponse {

    /** 日期 */
    private String date;

    /** 日活用户数 */
    private Long dau;

    /** 周活用户数 */
    private Long wau;

    /** 月活用户数 */
    private Long mau;

    /** 新增用户数 */
    private Long newUsers;

    /** 新增帖子数 */
    private Long newPosts;

    /** 新增评论数 */
    private Long newComments;

    /** 次日留存率 */
    private Double retention1d;

    /** 7日留存率 */
    private Double retention7d;

    /** 30日留存率 */
    private Double retention30d;
}