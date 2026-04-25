package com.clx.search.service;

import com.clx.search.entity.SearchLog;
import com.clx.search.mapper.SearchLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 搜索日志服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogService {

    private final SearchLogMapper searchLogMapper;

    /**
     * 记录搜索日志。
     */
    public void recordLog(String keyword, Long userId, String types, int resultCount, int costTime, String ip) {
        SearchLog logEntity = new SearchLog();
        logEntity.setKeyword(keyword);
        logEntity.setUserId(userId);
        logEntity.setSearchTypes(types);
        logEntity.setResultCount(resultCount);
        logEntity.setCostTime(costTime);
        logEntity.setIp(ip);
        logEntity.setCreateTime(LocalDateTime.now());

        try {
            searchLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("记录搜索日志失败: {}", e.getMessage());
        }
    }
}