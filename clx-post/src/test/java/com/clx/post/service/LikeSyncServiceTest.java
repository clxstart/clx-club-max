package com.clx.post.service;

import com.clx.common.redis.service.RedisService;
import com.clx.post.entity.Post;
import com.clx.post.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 点赞计数校准服务测试。
 */
@ExtendWith(MockitoExtension.class)
class LikeSyncServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private LikeSyncService likeSyncService;

    private Post createPost(Long id, int likeCount) {
        Post post = new Post();
        post.setId(id);
        post.setLikeCount(likeCount);
        return post;
    }

    @Nested
    @DisplayName("校准逻辑")
    class Sync {

        @Test
        @DisplayName("C13: Redis > DB 时更新 DB")
        void sync_shouldUpdateDbWhenRedisHigher() {
            Post post = createPost(1L, 5);
            when(postMapper.selectHot(1000)).thenReturn(List.of(post));
            when(redisService.get("post:like_count:1")).thenReturn(10);

            likeSyncService.sync();

            verify(postMapper).updateLikeCount(1L, 10);
        }

        @Test
        @DisplayName("C14: Redis < DB 时更新 Redis")
        void sync_shouldUpdateRedisWhenDbHigher() {
            Post post = createPost(1L, 10);
            when(postMapper.selectHot(1000)).thenReturn(List.of(post));
            when(redisService.get("post:like_count:1")).thenReturn(5);

            likeSyncService.sync();

            verify(redisService).set("post:like_count:1", 10);
        }

        @Test
        @DisplayName("C15: Redis key 不存在时从 DB 初始化")
        void sync_shouldInitFromDb() {
            Post post = createPost(1L, 8);
            when(postMapper.selectHot(1000)).thenReturn(List.of(post));
            when(redisService.get("post:like_count:1")).thenReturn(null);

            likeSyncService.sync();

            verify(redisService).set("post:like_count:1", 8);
            verify(postMapper, never()).updateLikeCount(any(), anyInt());
        }

        @Test
        @DisplayName("Redis == DB 时不做操作")
        void sync_noChangeWhenEqual() {
            Post post = createPost(1L, 10);
            when(postMapper.selectHot(1000)).thenReturn(List.of(post));
            when(redisService.get("post:like_count:1")).thenReturn(10);

            likeSyncService.sync();

            verify(postMapper, never()).updateLikeCount(any(), anyInt());
            verify(redisService, never()).set(anyString(), anyInt());
        }
    }
}