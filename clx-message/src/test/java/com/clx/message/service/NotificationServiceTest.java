package com.clx.message.service;

import com.clx.common.redis.service.RedisService;
import com.clx.message.dto.NotificationTriggerRequest;
import com.clx.message.entity.Notification;
import com.clx.message.mapper.NotificationMapper;
import com.clx.message.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 通知服务测试。
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MessageRouter messageRouter;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Nested
    @DisplayName("触发通知")
    class Trigger {

        @Test
        @DisplayName("C9: 触发通知写入 DB")
        void trigger_shouldInsertNotification() {
            when(notificationMapper.selectByAggregateKey(anyString())).thenReturn(null);
            when(notificationMapper.insert(any(Notification.class))).thenAnswer(inv -> {
                Notification n = inv.getArgument(0);
                n.setId(1L);
                return 1;
            });

            NotificationTriggerRequest request = new NotificationTriggerRequest();
            request.setUserId(1L);
            request.setType("like");
            request.setTitle("赞了你的帖子");

            Long id = notificationService.trigger(request);

            assertNotNull(id);
            verify(notificationMapper).insert(any(Notification.class));
        }

        @Test
        @DisplayName("C10: 1小时内同类通知聚合")
        void trigger_duplicate_shouldAggregate() {
            Notification existing = new Notification();
            existing.setId(1L);
            existing.setAggregateCount(1);

            when(notificationMapper.selectByAggregateKey(anyString())).thenReturn(existing);

            NotificationTriggerRequest request = new NotificationTriggerRequest();
            request.setUserId(1L);
            request.setType("like");
            request.setTitle("赞了你的帖子");
            request.setSourceId(100L);
            request.setSourceType("post");

            Long id = notificationService.trigger(request);

            assertEquals(1L, id);
            verify(notificationMapper).updateAggregateCount(eq(1L), eq(2));
            verify(notificationMapper, never()).insert(any());
        }

        @Test
        @DisplayName("C11: 标记已读后未读数-1")
        void read_shouldDecrementUnread() {
            Notification notification = new Notification();
            notification.setId(1L);
            notification.setUserId(1L);
            notification.setType("like");
            notification.setIsRead(false);

            when(notificationMapper.selectById(1L)).thenReturn(notification);
            when(notificationMapper.markRead(1L)).thenReturn(1);
            when(redisService.hGet("unread:1", "like")).thenReturn(5);

            notificationService.markRead(1L, 1L);

            verify(notificationMapper).markRead(1L);
            verify(redisService).hSet("unread:1", "like", 4);
        }
    }

}