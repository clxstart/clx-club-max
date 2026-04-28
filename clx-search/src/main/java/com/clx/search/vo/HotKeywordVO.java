package com.clx.search.vo;

import lombok.Data;

/**
 * 热词 VO。
 */
@Data
public class HotKeywordVO {
    /** 关键词 */
    private String keyword;
    /** 搜索次数 */
    private Long count;
    /** 增长率 */
    private String growth;
}