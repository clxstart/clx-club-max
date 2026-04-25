package com.clx.post.mq;

import com.clx.post.dto.LikeMessage;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 点赞消息消费者测试。
 */
@ExtendWith(MockitoExtension.class)
class LikeConsumerTest {

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private LikeConsumer likeConsumer;

    @Nested
    @DisplayName("消费点赞消息")
    class Consume {

        @Test
        @DisplayName("C3: 首次点赞 - 写入 like_record")
        void consume_shouldInsertLikeRecord() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(false);
            when(likeRecordMapper.insert(any())).thenReturn(1);
            when(postMapper.incrementLikeCount(1L)).thenReturn(1);

            LikeMessage message = LikeMessage.of(1L, 100L);
            likeConsumer.consume(message);

            verify(likeRecordMapper).insert(any());
        }

        @Test
        @DisplayName("C4: 首次点赞 - 更新 post.like_count")
        void consume_shouldUpdatePostCount() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(false);
            when(likeRecordMapper.insert(any())).thenReturn(1);
            when(postMapper.incrementLikeCount(1L)).thenReturn(1);

            LikeMessage message = LikeMessage.of(1L, 100L);
            likeConsumer.consume(message);

            verify(postMapper).incrementLikeCount(1L);
        }

        @Test
        @DisplayName("C9: 重复消息 - 幂等跳过")
        void consume_duplicate_shouldSkip() {
            when(likeRecordMapper.exists(100L, "1", 1L)).thenReturn(true);

            LikeMessage message = LikeMessage.of(1L, 100L);
            likeConsumer.consume(message);

            verify(likeRecordMapper, never()).insert(any());
            verify(postMapper, never()).incrementLikeCount(any());
        }
    }
}