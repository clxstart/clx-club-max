package com.clx.message.service.impl;

import com.clx.common.redis.service.RedisService;
import com.clx.message.config.RedisPubSubConfig;
import com.clx.message.dto.SendMessageRequest;
import com.clx.message.dto.WsEnvelope;
import com.clx.message.entity.ChatMessage;
import com.clx.message.entity.ChatSession;
import com.clx.message.mapper.ChatMessageMapper;
import com.clx.message.mapper.ChatSessionMapper;
import com.clx.message.service.ChatService;
import com.clx.message.service.MessageRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 私信服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;
    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRouter messageRouter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String UNREAD_KEY = "unread:";
    private static final int MAX_MESSAGES_PER_SESSION = 500;

    @Override
    @Transactional
    public Map<String, Object> sendMessage(Long fromUserId, SendMessageRequest request) {
        Long toUserId = request.getToUserId();
        String content = request.getContent().trim();

        // 校验
        if (fromUserId.equals(toUserId)) {
            throw new RuntimeException("不能给自己发私信");
        }
        if (content.isEmpty()) {
            throw new RuntimeException("消息内容不能为空");
        }

        // 获取或创建会话
        ChatSession session = getOrCreateSession(fromUserId, toUserId);

        // 写入消息
        ChatMessage message = new ChatMessage();
        message.setSessionId(session.getId());
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);

        // 更新会话
        sessionMapper.updateLastMessage(session.getId(), content.length() > 100 ? content.substring(0, 100) : content);

        // 增加未读数
        boolean isUser1 = toUserId.equals(session.getUser1Id());
        sessionMapper.incrementUnread(session.getId(), isUser1);

        // 更新 Redis 未读计数
        String unreadKey = UNREAD_KEY + toUserId;
        redisService.hSet(unreadKey, "chat", (redisService.hGet(unreadKey, "chat") != null
                ? ((Number) redisService.hGet(unreadKey, "chat")).intValue() + 1 : 1));

        // 路由推送
        messageRouter.route(toUserId, WsEnvelope.chat(
                fromUserId,
                null, // fromName 后续从用户服务获取
                content,
                session.getId(),
                System.currentTimeMillis()
        ));

        log.info("私信发送成功: from={}, to={}, sessionId={}", fromUserId, toUserId, session.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("messageId", message.getId());
        result.put("sessionId", session.getId());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @Override
    public Map<String, Object> getSessions(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        List<ChatSession> sessions = sessionMapper.selectByUserId(userId, offset, size);
        int total = sessionMapper.countByUserId(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ChatSession session : sessions) {
            Map<String, Object> item = new HashMap<>();
            item.put("sessionId", session.getId());

            // 确定对方是谁
            Long targetUserId = session.getUser1Id().equals(userId) ? session.getUser2Id() : session.getUser1Id();
            item.put("targetUserId", targetUserId);
            // targetNickname 和 targetAvatar 后续从用户服务获取
            item.put("targetNickname", "用户" + targetUserId);
            item.put("targetAvatar", null);

            item.put("lastMessage", session.getLastMessage());
            item.put("lastTime", session.getLastTime() != null ?
                    session.getLastTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);

            // 未读数
            int unread = session.getUser1Id().equals(userId) ? session.getUser1Unread() : session.getUser2Unread();
            item.put("unreadCount", unread);

            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public Map<String, Object> getMessages(Long userId, Long sessionId, Long cursor, int size) {
        // 验证会话归属
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || (!session.getUser1Id().equals(userId) && !session.getUser2Id().equals(userId))) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        List<ChatMessage> messages = messageMapper.selectBySessionId(sessionId, cursor, size);

        List<Map<String, Object>> list = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, Object> item = new HashMap<>();
            item.put("messageId", msg.getId());
            item.put("fromUserId", msg.getFromUserId());
            item.put("content", msg.getContent());
            item.put("timestamp", msg.getCreateTime() != null ?
                    msg.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
            item.put("direction", msg.getFromUserId().equals(userId) ? "sent" : "received");
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("hasMore", messages.size() == size);
        return result;
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || (!session.getUser1Id().equals(userId) && !session.getUser2Id().equals(userId))) {
            throw new RuntimeException("会话不存在或无权访问");
        }

        boolean isUser1 = session.getUser1Id().equals(userId);
        sessionMapper.clearUnread(sessionId, isUser1);

        // 更新 Redis 未读计数
        String unreadKey = UNREAD_KEY + userId;
        Object chatUnread = redisService.hGet(unreadKey, "chat");
        if (chatUnread != null) {
            int current = ((Number) chatUnread).intValue();
            int sessionUnread = isUser1 ? session.getUser1Unread() : session.getUser2Unread();
            redisService.hSet(unreadKey, "chat", Math.max(0, current - sessionUnread));
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        Object value = redisService.hGet(UNREAD_KEY + userId, "chat");
        return value != null ? ((Number) value).intValue() : 0;
    }

    /**
     * 获取或创建会话。
     */
    private ChatSession getOrCreateSession(Long user1, Long user2) {
        // 保证 user1 < user2
        Long minId = Math.min(user1, user2);
        Long maxId = Math.max(user1, user2);

        ChatSession session = sessionMapper.selectByUsers(minId, maxId);
        if (session != null) {
            return session;
        }

        // 创建新会话
        session = new ChatSession();
        session.setUser1Id(minId);
        session.setUser2Id(maxId);
        session.setUser1Unread(0);
        session.setUser2Unread(0);
        session.setCreateTime(LocalDateTime.now());
        sessionMapper.insert(session);

        log.info("创建新会话: user1={}, user2={}, sessionId={}", minId, maxId, session.getId());
        return session;
    }

}