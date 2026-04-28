package com.clx.search.service;

import com.clx.search.vo.HotKeywordVO;

import java.util.List;

/**
 * 搜索热词服务接口。
 */
public interface HotKeywordService {

    /**
     * 记录搜索关键词（Redis 计数）。
     */
    void recordKeyword(String keyword);

    /**
     * 获取今日热词。
     */
    List<HotKeywordVO> getTodayHotKeywords(int limit);

    /**
     * 定时同步到数据库。
     */
    void syncToDatabase();
}