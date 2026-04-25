package com.clx.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.clx.search.es.PostDocument;
import com.clx.search.es.UserDocument;
import com.clx.search.es.CategoryDocument;
import com.clx.search.es.TagDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ES 搜索服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    private static final String POST_INDEX = "clx_post";
    private static final String USER_INDEX = "clx_user";
    private static final String CATEGORY_INDEX = "clx_category";
    private static final String TAG_INDEX = "clx_tag";

    /**
     * 搜索帖子。
     */
    public List<PostDocument> searchPosts(String keyword, int page, int size, boolean enableHighlight) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("title^2", "content", "summary", "authorName", "categoryName", "tags.name")
            )._toQuery();

            SearchResponse<PostDocument> response = esClient.search(s -> {
                s.index(POST_INDEX)
                        .query(query)
                        .from((page - 1) * size)
                        .size(size);
                if (enableHighlight) {
                    s.highlight(h -> h
                            .fields("title", f -> f.preTags("<em>").postTags("</em>"))
                            .fields("content", f -> f.preTags("<em>").postTags("</em>"))
                    );
                }
                return s;
            }, PostDocument.class);

            List<PostDocument> results = new ArrayList<>();
            for (Hit<PostDocument> hit : response.hits().hits()) {
                PostDocument doc = hit.source();
                if (doc != null) {
                    // 处理高亮
                    if (enableHighlight && hit.highlight() != null) {
                        List<String> titleHighlights = hit.highlight().get("title");
                        if (titleHighlights != null && !titleHighlights.isEmpty()) {
                            doc.setTitle(titleHighlights.get(0));
                        }
                    }
                    results.add(doc);
                }
            }
            return results;
        } catch (IOException e) {
            log.error("搜索帖子失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 搜索用户。
     */
    public List<UserDocument> searchUsers(String keyword, int page, int size) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("username^2", "nickname^2", "signature", "email")
            )._toQuery();

            SearchResponse<UserDocument> response = esClient.search(s -> s
                    .index(USER_INDEX)
                    .query(query)
                    .from((page - 1) * size)
                    .size(size),
                    UserDocument.class);

            List<UserDocument> results = new ArrayList<>();
            for (Hit<UserDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;
        } catch (IOException e) {
            log.error("搜索用户失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 搜索分类。
     */
    public List<CategoryDocument> searchCategories(String keyword, int page, int size) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("name^2", "code", "description")
            )._toQuery();

            SearchResponse<CategoryDocument> response = esClient.search(s -> s
                    .index(CATEGORY_INDEX)
                    .query(query)
                    .from((page - 1) * size)
                    .size(size),
                    CategoryDocument.class);

            List<CategoryDocument> results = new ArrayList<>();
            for (Hit<CategoryDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;
        } catch (IOException e) {
            log.error("搜索分类失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 搜索标签。
     */
    public List<TagDocument> searchTags(String keyword, int page, int size) {
        try {
            Query query = MultiMatchQuery.of(m -> m
                    .query(keyword)
                    .fields("name^2", "description")
            )._toQuery();

            SearchResponse<TagDocument> response = esClient.search(s -> s
                    .index(TAG_INDEX)
                    .query(query)
                    .from((page - 1) * size)
                    .size(size),
                    TagDocument.class);

            List<TagDocument> results = new ArrayList<>();
            for (Hit<TagDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }
            return results;
        } catch (IOException e) {
            log.error("搜索标签失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}