package com.clx.post.service;

import com.clx.common.redis.service.RedisService;
import com.clx.post.dto.LikeMessage;
import com.clx.post.entity.Comment;
import com.clx.post.entity.Post;
import com.clx.post.mapper.CommentMapper;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import com.clx.post.mq.LikeProducer;
import com.clx.post.service.impl.LikeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 点赞服务单元测试。
 *
 * likePost：异步流程（Redis + MQ）
 * unlikePost：同步流程（DB + 清 Redis）
 * likeComment/unlikeComment：同步流程（不变）
 */
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private LikeProducer likeProducer;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Nested
    @DisplayName("点赞帖子（异步）")
    class LikePost {

        @Test
        @DisplayName("C1: 首次点赞 - Redis 计数 +1，发送 MQ 消息")
        void likePost_shouldIncrementRedisCount() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(false);
            when(redisService.increment("post:like_count:1")).thenReturn(6L);

            int count = likeService.likePost(1L, 100L);

            assertEquals(6, count);
            verify(redisService).increment("post:like_count:1");
            verify(likeProducer).send(any(LikeMessage.class));
            // 不调用 DB（异步由消费者处理）
            verify(postMapper, never()).incrementLikeCount(any());
        }

        @Test
        @DisplayName("C2: 点赞后 MQ 消息包含正确信息")
        void likePost_shouldSendMessage() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(false);
            when(redisService.increment(anyString())).thenReturn(1L);

            likeService.likePost(1L, 100L);

            verify(likeProducer).send(argThat(msg ->
                    msg.getPostId().equals(1L) &&
                    msg.getUserId().equals(100L) &&
                    msg.getAction().equals("like")
            ));
        }

        @Test
        @DisplayName("C5: 重复点赞 - 抛异常，不发 MQ")
        void likePost_again_shouldThrow() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> likeService.likePost(1L, 100L));
            assertEquals("已点赞", ex.getMessage());

            verify(redisService, never()).increment(any());
            verify(likeProducer, never()).send(any());
        }
    }

    @Nested
    @DisplayName("取消点赞帖子（同步）")
    class UnlikePost {

        @Test
        @DisplayName("C6: 取消点赞 - DB 计数 -1")
        void unlikePost_shouldDecrementDbCount() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(true);
            when(likeRecordMapper.delete(100L, "1", 1L)).thenReturn(1);
            when(postMapper.decrementLikeCount(1L)).thenReturn(1);

            Post post = new Post();
            post.setId(1L);
            post.setLikeCount(4);
            when(postMapper.selectById(1L)).thenReturn(post);

            int count = likeService.unlikePost(1L, 100L);

            assertEquals(4, count);
            verify(postMapper).decrementLikeCount(1L);
        }

        @Test
        @DisplayName("C7: 取消点赞 - 删除 Redis key")
        void unlikePost_shouldDeleteRedisKey() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(true);
            when(likeRecordMapper.delete(100L, "1", 1L)).thenReturn(1);
            when(postMapper.decrementLikeCount(1L)).thenReturn(1);

            Post post = new Post();
            post.setId(1L);
            post.setLikeCount(3);
            when(postMapper.selectById(1L)).thenReturn(post);

            likeService.unlikePost(1L, 100L);

            verify(redisService).delete("post:like_count:1");
        }

        @Test
        @DisplayName("C8: 未点赞时取消 - 抛异常")
        void unlikePost_notLiked_shouldThrow() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> likeService.unlikePost(1L, 100L));
            assertEquals("未点赞", ex.getMessage());

            verify(postMapper, never()).decrementLikeCount(any());
            verify(redisService, never()).delete(anyString());
        }
    }

    @Nested
    @DisplayName("点赞评论（同步）")
    class LikeComment {

        @Test
        @DisplayName("首次点赞评论 - 点赞数+1")
        void likeComment_firstTime() {
            when(likeRecordMapper.exists(100L, "2", 10L)).thenReturn(false);
            when(likeRecordMapper.insert(any())).thenReturn(1);

            Comment comment = new Comment();
            comment.setId(10L);
            comment.setLikeCount(3);
            when(commentMapper.incrementLikeCount(10L)).thenReturn(1);
            when(commentMapper.selectById(10L)).thenReturn(comment);

            int count = likeService.likeComment(10L, 100L);

            assertEquals(3, count);
            verify(commentMapper).incrementLikeCount(10L);
        }

        @Test
        @DisplayName("重复点赞评论 - 抛异常")
        void likeComment_duplicate() {
            when(likeRecordMapper.exists(100L, "2", 10L)).thenReturn(true);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> likeService.likeComment(10L, 100L));
            assertEquals("已点赞", ex.getMessage());

            verify(commentMapper, never()).incrementLikeCount(any());
        }
    }

    @Nested
    @DisplayName("取消点赞评论（同步）")
    class UnlikeComment {

        @Test
        @DisplayName("取消点赞评论 - 点赞数-1")
        void unlikeComment_success() {
            when(likeRecordMapper.exists(100L, "2", 10L)).thenReturn(true);
            when(likeRecordMapper.delete(100L, "2", 10L)).thenReturn(1);

            Comment comment = new Comment();
            comment.setId(10L);
            comment.setLikeCount(2);
            when(commentMapper.decrementLikeCount(10L)).thenReturn(1);
            when(commentMapper.selectById(10L)).thenReturn(comment);

            int count = likeService.unlikeComment(10L, 100L);

            assertEquals(2, count);
            verify(commentMapper).decrementLikeCount(10L);
        }

        @Test
        @DisplayName("未点赞评论时取消 - 抛异常")
        void unlikeComment_notLiked() {
            when(likeRecordMapper.exists(100L, "2", 10L)).thenReturn(false);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> likeService.unlikeComment(10L, 100L));
            assertEquals("未点赞", ex.getMessage());

            verify(commentMapper, never()).decrementLikeCount(any());
        }
    }
}