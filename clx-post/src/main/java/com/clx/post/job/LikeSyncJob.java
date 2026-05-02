package com.clx.post.job;

import com.clx.common.redis.service.RedisService;
import com.clx.post.entity.Post;
import com.clx.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 点赞计数校准定时任务。
 *
 * 定时对比 Redis 与 DB 计数，修正偏差。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeSyncJob {

    private final RedisService redisService;
    private final PostMapper postMapper;

    private static final String LIKE_COUNT_KEY = "post:like_count:";

    /**
     * 每 5 分钟校准一次。
     *
     * 规则：
     * - Redis > DB：更新 DB（Redis 是实时计数，更准确）
     * - Redis < DB：更新 Redis（可能 Redis key 丢失）
     * - Redis key 不存在：从 DB 初始化
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void sync() {
        log.info("开始校准点赞计数...");

        // 查询热门帖子（like_count > 0）
        List<Post> posts = postMapper.selectHot(1000);

        int synced = 0;
        for (Post post : posts) {
            Long postId = post.getId();
            int dbCount = post.getLikeCount();

            String key = LIKE_COUNT_KEY + postId;
            Object redisValue = redisService.get(key);

            if (redisValue == null) {
                // Redis key 不存在，从 DB 初始化
                redisService.set(key, dbCount);
                log.debug("初始化 Redis 计数: postId={}, count={}", postId, dbCount);
                synced++;
            } else {
                int redisCount = ((Number) redisValue).intValue();

                if (redisCount > dbCount) {
                    // Redis 更大，更新 DB
                    postMapper.updateLikeCount(postId, redisCount);
                    log.debug("更新 DB 计数: postId={}, redis={}, db={}", postId, redisCount, dbCount);
                    synced++;
                } else if (redisCount < dbCount) {
                    // Redis 更小，更新 Redis
                    redisService.set(key, dbCount);
                    log.debug("更新 Redis 计数: postId={}, redis={}, db={}", postId, redisCount, dbCount);
                    synced++;
                }
            }
        }

        log.info("校准完成，共修正 {} 条", synced);
    }
}
