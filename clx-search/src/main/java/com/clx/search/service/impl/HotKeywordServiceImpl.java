package com.clx.search.service.impl;

import com.clx.search.mapper.HotKeywordMapper;
import com.clx.search.service.HotKeywordService;
import com.clx.search.vo.HotKeywordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 搜索热词服务实现。
 *
 * 职责：Redis 实时计数 + 定时同步 MySQL。
 * 存储：ZSet（按日期分 Key），Score 为搜索次数。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotKeywordServiceImpl implements HotKeywordService {

    private final StringRedisTemplate redisTemplate;
    private final HotKeywordMapper hotKeywordMapper;

    /** Redis Key 前缀 */
    private static final String REDIS_KEY_PREFIX = "search:keyword:";
    /** 日期格式化器 */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 记录一次搜索词。
     *
     * @param keyword 搜索关键词（自动转小写、去空格）
     */
    @Override
    public void recordKeyword(String keyword) {
        // 1. 空值过滤
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        // 2. ZSet 计数 +1
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;
        redisTemplate.opsForZSet().incrementScore(key, keyword.trim().toLowerCase(), 1);
    }

    /**
     * 获取今日热搜榜。
     *
     * @param limit 返回数量
     * @return 按热度降序的热词列表（含增长率）
     */
    @Override
    public List<HotKeywordVO> getTodayHotKeywords(int limit) {
        // 1. 取 Top N（ZSet 按分数降序）
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);

        List<HotKeywordVO> result = new ArrayList<>();
        if (keywords == null) {
            return result;
        }

        // 2. 遍历构建 VO（含增长率计算）
        for (String keyword : keywords) {
            Double score = redisTemplate.opsForZSet().score(key, keyword);

            HotKeywordVO vo = new HotKeywordVO();
            vo.setKeyword(keyword);
            vo.setCount(score != null ? score.longValue() : 0);

            // 计算增长率：对比昨日分数
            String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            Double yesterdayScore = redisTemplate.opsForZSet().score(REDIS_KEY_PREFIX + yesterday, keyword);

            if (yesterdayScore != null && yesterdayScore > 0) {
                // 有历史数据：计算增长率
                double growth = ((score != null ? score : 0) - yesterdayScore) / yesterdayScore * 100;
                vo.setGrowth(String.format("%.0f%%", growth));
            } else {
                // 新词：标记 +100%
                vo.setGrowth("+100%");
            }

            result.add(vo);
        }

        return result;
    }

    /**
     * 定时同步 Redis 数据到 MySQL（每小时执行）。
     *
     * 目的：持久化存储，支持历史查询。
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
    public void syncToDatabase() {
        log.info("开始同步热词数据到数据库...");

        // 1. 取 Top 100
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, 99);

        if (keywords == null) {
            log.info("热词数据同步完成（无数据）");
            return;
        }

        // 2. 逐条写入数据库（增量更新）
        for (String keyword : keywords) {
            Double score = redisTemplate.opsForZSet().score(key, keyword);
            if (score != null && score > 0) {
                try {
                    hotKeywordMapper.incrementCount(keyword, "day", today);
                } catch (Exception e) {
                    log.warn("同步热词失败: {}", keyword);
                }
            }
        }

        log.info("热词数据同步完成");
    }
}
