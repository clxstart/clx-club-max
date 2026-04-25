package com.clx.search.datasource;

import com.clx.search.es.TagDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标签数据源（ES 搜索）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagDataSource implements DataSource<TagDocument> {

    private final SearchService searchService;

    @Override
    public List<TagDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchTags(keyword, page, size);
    }

    @Override
    public String getName() {
        return "tag";
    }
}