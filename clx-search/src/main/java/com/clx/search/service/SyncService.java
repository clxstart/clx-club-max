package com.clx.search.service;

import com.clx.search.es.CategoryDocument;
import com.clx.search.es.PostDocument;
import com.clx.search.es.TagDocument;
import com.clx.search.es.UserDocument;

/**
 * 数据同步服务接口。
 */
public interface SyncService {

    /**
     * 同步帖子到 ES。
     */
    void syncPost(PostDocument post);

    /**
     * 同步用户到 ES。
     */
    void syncUser(UserDocument user);

    /**
     * 同步分类到 ES。
     */
    void syncCategory(CategoryDocument category);

    /**
     * 同步标签到 ES。
     */
    void syncTag(TagDocument tag);
}