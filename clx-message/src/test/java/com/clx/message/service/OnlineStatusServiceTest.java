package com.clx.message.service;

import com.clx.common.redis.service.RedisService;
import com.clx.message.service.impl.OnlineStatusServiceImpl;
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
 * 在线状态服务测试。
 */
@ExtendWith(MockitoExtension.class)
class OnlineStatusServiceTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private OnlineStatusServiceImpl onlineStatusService;

    @Nested
    @DisplayName("在线状态查询")
    class GetStatus {

        @Test
        @DisplayName("C13: updateLastActiveTime 调用 Redis set")
        void updateLastActiveTime_shouldCallRedisSet() {
            onlineStatusService.updateLastActiveTime(1L);
            // set 被调用（void 方法，不做 stub）
            verify(redisService).set(eq("user:online:1"), anyLong(), eq(120L));
        }

        @Test
        @DisplayName("C14: 在线用户查询返回在线状态")
        void online_shouldReturnOnline() {
            when(redisService.get("user:online:1")).thenReturn(System.currentTimeMillis());

            OnlineStatusService.OnlineStatus status = onlineStatusService.getOnlineStatus(1L);

            assertTrue(status.online());
            assertNotNull(status.lastActiveTime());
        }

        @Test
        @DisplayName("C15: TTL过期后查询返回离线")
        void expired_shouldReturnOffline() {
            when(redisService.get("user:online:1")).thenReturn(null);

            OnlineStatusService.OnlineStatus status = onlineStatusService.getOnlineStatus(1L);

            assertFalse(status.online());
            assertNull(status.lastActiveTime());
        }
    }

}