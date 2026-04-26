package com.clx.search.datasource;

import java.util.List;

/**
 * 数据源接口 - 定义统一的搜索方法。
 *
 * 不管你是 ES 搜索还是爬虫，都要实现这个接口，让 SearchFacade 能统一调用。
 */
public interface DataSource<T> {

    /** 执行搜索，返回结果列表 */
    List<T> doSearch(String keyword, int page, int size);

    /** 数据源名字（如 post、user、picture），供 Registry 注册查找 */
    String getName();

    /** 统计总数（可选实现） */
    default long count(String keyword) {
        return 0;
    }
}