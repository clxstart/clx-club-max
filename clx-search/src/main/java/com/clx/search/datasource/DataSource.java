package com.clx.search.datasource;

import java.util.List;

/**
 * 数据源接口（定义统一的搜索方法）。
 *
 * 所有数据源必须实现此接口，提供统一的搜索能力。
 *
 * @param <T> 搜索结果的数据类型
 */
public interface DataSource<T> {

    /**
     * 执行搜索。
     *
     * @param keyword 搜索关键词
     * @param page    页码（从 1 开始）
     * @param size    每页数量
     * @return 搜索结果列表
     */
    List<T> doSearch(String keyword, int page, int size);

    /**
     * 获取数据源名称。
     *
     * @return 数据源名称（如 post、user、picture）
     */
    String getName();

    /**
     * 获取总数量（用于分页）。
     *
     * @param keyword 搜索关键词
     * @return 总数量
     */
    default long count(String keyword) {
        return 0;
    }
}