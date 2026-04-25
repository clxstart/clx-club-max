package com.clx.message.ws;

import com.clx.common.redis.service.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WebSocket 会话管理器测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WsSessionManagerTest {

    @Mock
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @InjectMocks
    private WsSessionManager wsSessionManager;

    @Nested
    @DisplayName("连接管理")
    class Connection {

        @Test
        @DisplayName("C7: 连接时设置 Redis 在线状态")
        void connect_shouldSetOnline() throws Exception {
            WebSocketSession session = mock(WebSocketSession.class);
            when(session.isOpen()).thenReturn(true);
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.range(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());

            wsSessionManager.onConnect(1L, session);

            verify(redisService).set(eq("user:online:1"), anyLong(), eq(120L));
        }

        @Test
        @DisplayName("单设备：新连接踢旧连接")
        void newConnection_shouldKickOld() throws Exception {
            WebSocketSession oldSession = mock(WebSocketSession.class);
            when(oldSession.isOpen()).thenReturn(true);
            WebSocketSession newSession = mock(WebSocketSession.class);
            when(newSession.isOpen()).thenReturn(true);
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.range(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());

            wsSessionManager.onConnect(1L, oldSession);
            wsSessionManager.onConnect(1L, newSession);

            verify(oldSession).close(any(org.springframework.web.socket.CloseStatus.class));
        }
    }

    @Nested
    @DisplayName("消息推送")
    class Push {

        @Test
        @DisplayName("C3: 接收者在线时 WebSocket 推送")
        void push_online_shouldSend() throws Exception {
            WebSocketSession session = mock(WebSocketSession.class);
            when(session.isOpen()).thenReturn(true);
            when(redisTemplate.opsForList()).thenReturn(listOperations);
            when(listOperations.range(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptyList());

            wsSessionManager.onConnect(1L, session);

            boolean result = wsSessionManager.pushToUser(1L, "{\"type\":\"chat\"}");

            assertTrue(result);
            verify(session).sendMessage(any(org.springframework.web.socket.TextMessage.class));
        }

        @Test
        @DisplayName("C4: 接收者离线时返回 false")
        void push_offline_shouldReturnFalse() {
            boolean result = wsSessionManager.pushToUser(99L, "{\"type\":\"chat\"}");
            assertFalse(result);
        }
    }

}