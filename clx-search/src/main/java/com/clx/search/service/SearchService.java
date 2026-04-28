package com.clx.search.service;

import com.clx.search.es.CategoryDocument;
import com.clx.search.es.PostDocument;
import com.clx.search.es.TagDocument;
import com.clx.search.es.UserDocument;

import java.util.List;

/**
 * ES 搜索服务接口。
 */
public interface SearchService {

    /**
     * 搜索帖子 - 多字段匹配 + 高亮。
     */
    List<PostDocument> searchPosts(String keyword, int page, int size, boolean enableHighlight);

    /**
     * 搜索用户。
     */
    List<UserDocument> searchUsers(String keyword, int page, int size);

    /**
     * 搜索分类。
     */
    List<CategoryDocument> searchCategories(String keyword, int page, int size);

    /**
     * 搜索标签。
     */
    List<TagDocument> searchTags(String keyword, int page, int size);
}