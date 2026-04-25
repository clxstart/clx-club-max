package com.clx.search.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聚合搜索结果。
 */
@Data
public class SearchVO {

    /** 搜索关键词 */
    private String keyword;

    /** 总耗时（毫秒） */
    private Long totalTime;

    /** 各数据源搜索结果 */
    private Map<String, SearchResult> results;

    /** 搜索建议 */
    private List<String> suggest;

    /** 是否部分成功 */
    private Boolean partialSuccess = false;

    /**
     * 单个数据源搜索结果。
     */
    @Data
    public static class SearchResult {
        /** 总数量 */
        private Long total;
        /** 结果列表 */
        private List<?> items;
        /** 错误信息（如有） */
        private String error;
    }
}