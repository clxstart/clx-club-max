package com.clx.search.datasource;

import com.clx.search.es.UserDocument;
import com.clx.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户数据源（ES 搜索）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataSource implements DataSource<UserDocument> {

    private final SearchService searchService;

    @Override
    public List<UserDocument> doSearch(String keyword, int page, int size) {
        return searchService.searchUsers(keyword, page, size);
    }

    @Override
    public String getName() {
        return "user";
    }
}