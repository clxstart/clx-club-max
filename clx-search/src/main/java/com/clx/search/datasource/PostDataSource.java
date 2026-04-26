package com.clx.search.datasource;

import com.clx.search.es.PostDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子数据源 - 从 ES 搜索帖子。
 *
 * 用户搜"Spring"时，搜索帖子标题、内容、作者名、分类、标签，返回匹配的帖子列表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostDataSource implements DataSource<PostDocument> {

    private final SearchService searchService;

    @Override
    public List<PostDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchPosts(keyword, page, size, true);  // 调用 ES 搜索帖子，开启高亮
    }

    @Override
    public String getName() {
        return "post";  // 数据源名称
    }
}