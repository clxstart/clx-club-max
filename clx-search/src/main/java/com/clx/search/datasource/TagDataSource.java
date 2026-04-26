package com.clx.search.datasource;

import com.clx.search.es.TagDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标签数据源 - 从 ES 搜索标签。
 *
 * 用户搜"Java"时，搜索标签名称和描述，返回匹配的标签列表。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TagDataSource implements DataSource<TagDocument> {

    private final SearchService searchService;

    @Override
    public List<TagDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchTags(keyword, page, size);  // 调用 ES 搜索标签
    }

    @Override
    public String getName() {
        return "tag";  // 数据源名称
    }
}