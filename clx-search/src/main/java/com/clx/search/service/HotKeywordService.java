package com.clx.search.service;

import com.clx.search.entity.HotKeyword;
import com.clx.search.mapper.HotKeywordMapper;
import lombok.Data;
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
 * 搜索热词服务。
 *
 * 使用 Redis 计数 + 定时入库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotKeywordService {

    private final StringRedisTemplate redisTemplate;
    private final HotKeywordMapper hotKeywordMapper;

    private static final String REDIS_KEY_PREFIX = "search:keyword:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 记录搜索关键词（Redis 计数）。
     */
    public void recordKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;
        redisTemplate.opsForZSet().incrementScore(key, keyword.trim().toLowerCase(), 1);
    }

    /**
     * 获取今日热词。
     */
    public List<HotKeywordVO> getTodayHotKeywords(int limit) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;

        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
        List<HotKeywordVO> result = new ArrayList<>();

        if (keywords != null) {
            for (String keyword : keywords) {
                Double score = redisTemplate.opsForZSet().score(key, keyword);
                HotKeywordVO vo = new HotKeywordVO();
                vo.setKeyword(keyword);
                vo.setCount(score != null ? score.longValue() : 0);
                // 计算增长率（对比昨日）
                String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
                Double yesterdayScore = redisTemplate.opsForZSet().score(REDIS_KEY_PREFIX + yesterday, keyword);
                if (yesterdayScore != null && yesterdayScore > 0) {
                    double growth = ((score != null ? score : 0) - yesterdayScore) / yesterdayScore * 100;
                    vo.setGrowth(String.format("%.0f%%", growth));
                } else {
                    vo.setGrowth("+100%");
                }
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 定时任务：每小时将 Redis 数据同步到数据库。
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncToDatabase() {
        log.info("开始同步热词数据到数据库...");
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = REDIS_KEY_PREFIX + today;

        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, 99);
        if (keywords != null) {
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
        }
        log.info("热词数据同步完成");
    }

    /**
     * 热词 VO。
     */
    @Data
    public static class HotKeywordVO {
        private String keyword;
        private Long count;
        private String growth;
    }
}