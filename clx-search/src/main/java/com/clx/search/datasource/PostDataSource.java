package com.clx.search.datasource;

import com.clx.search.es.PostDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子数据源（ES 搜索）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDataSource implements DataSource<PostDocument> {

    private final SearchService searchService;

    @Override
    public List<PostDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchPosts(keyword, page, size, true);
    }

    @Override
    public String getName() {
        return "post";
    }
}