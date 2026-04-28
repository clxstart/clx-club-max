package com.clx.search.service;

/**
 * 搜索日志服务接口。
 */
public interface SearchLogService {

    /**
     * 记录搜索日志。
     */
    void recordLog(String keyword, Long userId, String types, int resultCount, int costTime, String ip);
}