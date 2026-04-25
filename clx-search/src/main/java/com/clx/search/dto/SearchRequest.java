package com.clx.search.dto;

import lombok.Data;

import java.util.List;

/**
 * 聚合搜索请求。
 */
@Data
public class SearchRequest {

    /** 搜索关键词 */
    private String keyword;

    /** 搜索类型列表（为空则搜索所有） */
    private List<String> types;

    /** 页码（从 1 开始） */
    private Integer page = 1;

    /** 每页数量 */
    private Integer size = 10;

    /** 是否启用高亮 */
    private Boolean enableHighlight = true;

    /** 是否启用搜索建议 */
    private Boolean enableSuggest = true;
}