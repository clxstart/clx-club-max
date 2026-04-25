package com.clx.message.service;

import com.clx.common.redis.service.RedisService;
import com.clx.message.dto.SendMessageRequest;
import com.clx.message.entity.ChatMessage;
import com.clx.message.entity.ChatSession;
import com.clx.message.mapper.ChatMessageMapper;
import com.clx.message.mapper.ChatSessionMapper;
import com.clx.message.service.impl.ChatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 私信服务测试。
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionMapper sessionMapper;

    @Mock
    private ChatMessageMapper messageMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private MessageRouter messageRouter;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Nested
    @DisplayName("发送私信")
    class SendMessage {

        @Test
        @DisplayName("C1: 发送私信后 DB 有记录")
        void send_shouldInsertMessage() {
            when(sessionMapper.selectByUsers(1L, 2L)).thenReturn(null);
            when(sessionMapper.insert(any(ChatSession.class))).thenAnswer(inv -> {
                ChatSession s = inv.getArgument(0);
                s.setId(1L);
                return 1;
            });
            when(messageMapper.insert(any(ChatMessage.class))).thenAnswer(inv -> {
                ChatMessage m = inv.getArgument(0);
                m.setId(100L);
                return 1;
            });

            SendMessageRequest request = new SendMessageRequest();
            request.setToUserId(2L);
            request.setContent("你好");

            Map<String, Object> result = chatService.sendMessage(1L, request);

            assertNotNull(result.get("messageId"));
            verify(messageMapper).insert(any(ChatMessage.class));
        }

        @Test
        @DisplayName("C5: 发送给自己报错")
        void send_self_shouldThrow() {
            SendMessageRequest request = new SendMessageRequest();
            request.setToUserId(1L);
            request.setContent("你好");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> chatService.sendMessage(1L, request));
            assertEquals("不能给自己发私信", ex.getMessage());
        }

        @Test
        @DisplayName("C6: 空内容报错")
        void send_empty_shouldThrow() {
            SendMessageRequest request = new SendMessageRequest();
            request.setToUserId(2L);
            request.setContent("   ");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> chatService.sendMessage(1L, request));
            assertEquals("消息内容不能为空", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("会话管理")
    class Session {

        @Test
        @DisplayName("C2: 发送私信后会话更新")
        void send_shouldUpdateSession() {
            ChatSession existing = new ChatSession();
            existing.setId(1L);
            existing.setUser1Id(1L);
            existing.setUser2Id(2L);

            when(sessionMapper.selectByUsers(1L, 2L)).thenReturn(existing);
            when(messageMapper.insert(any(ChatMessage.class))).thenAnswer(inv -> {
                ChatMessage m = inv.getArgument(0);
                m.setId(100L);
                return 1;
            });

            SendMessageRequest request = new SendMessageRequest();
            request.setToUserId(2L);
            request.setContent("你好");

            chatService.sendMessage(1L, request);

            verify(sessionMapper).updateLastMessage(eq(1L), anyString());
        }
    }

}