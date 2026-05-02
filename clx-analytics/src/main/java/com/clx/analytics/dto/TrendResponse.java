package com.clx.analytics.dto;

import lombok.Data;

import java.util.List;

/**
 * 趋势数据响应
 */
@Data
public class TrendResponse {

    /** 日期列表 */
    private List<String> dates;

    /** 值列表 */
    private List<Long> values;
}
