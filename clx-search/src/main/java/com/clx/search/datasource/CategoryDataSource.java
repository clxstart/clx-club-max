package com.clx.search.datasource;

import com.clx.search.es.CategoryDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类数据源 - 从 ES 搜索分类。
 *
 * 用户搜"技术"时，搜索分类名称和描述，返回匹配的分类列表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataSource implements DataSource<CategoryDocument> {

    private final SearchService searchService;

    @Override
    public List<CategoryDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchCategories(keyword, page, size);  // 调用 ES 搜索分类
    }

    @Override
    public String getName() {
        return "category";  // 数据源名称
    }
}