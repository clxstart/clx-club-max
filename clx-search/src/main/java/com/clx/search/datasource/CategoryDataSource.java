package com.clx.search.datasource;

import com.clx.search.es.CategoryDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 分类数据源（ES 搜索）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryDataSource implements DataSource<CategoryDocument> {

    private final SearchService searchService;

    @Override
    public List<CategoryDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchCategories(keyword, page, size);
    }

    @Override
    public String getName() {
        return "category";
    }
}