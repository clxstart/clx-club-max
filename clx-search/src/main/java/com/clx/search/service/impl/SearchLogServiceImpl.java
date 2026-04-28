package com.clx.search.service.impl;

import com.clx.search.entity.SearchLog;
import com.clx.search.mapper.SearchLogMapper;
import com.clx.search.service.SearchLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 搜索日志服务实现。
 *
 * 职责：记录用户搜索行为到数据库，用于数据分析。
 * 特点：异常静默处理，不影响主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchLogServiceImpl implements SearchLogService {

    private final SearchLogMapper searchLogMapper;

    /**
     * 记录一条搜索日志。
     *
     * @param keyword    搜索关键词
     * @param userId     用户ID（未登录为null）
     * @param types      搜索类型（逗号分隔，如"post,user"）
     * @param resultCount 返回结果总数
     * @param costTime   搜索耗时（毫秒）
     * @param ip         用户IP地址
     */
    @Override
    public void recordLog(String keyword, Long userId, String types, int resultCount, int costTime, String ip) {
        // 1. 构建日志实体
        SearchLog logEntity = new SearchLog();
        logEntity.setKeyword(keyword);
        logEntity.setUserId(userId);
        logEntity.setSearchTypes(types);
        logEntity.setResultCount(resultCount);
        logEntity.setCostTime(costTime);
        logEntity.setIp(ip);
        logEntity.setCreateTime(LocalDateTime.now());

        // 2. 写入数据库（失败静默，不抛异常）
        try {
            searchLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("记录搜索日志失败: {}", e.getMessage());
        }
    }
}