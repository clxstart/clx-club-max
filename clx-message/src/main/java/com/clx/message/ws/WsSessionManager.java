package com.clx.message.ws;

import com.clx.common.redis.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 会话管理器。
 *
 * 职责：
 * 1. 维护用户 WebSocket 连接（单设备，新连接踢旧连接）
 * 2. 推送消息给在线用户
 * 3. 更新 Redis 在线状态
 * 4. 连接/断开事件处理
 * 5. 上线时补发离线消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WsSessionManager {

    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 用户 WebSocket Session 映射 */
    private final Map<Long, org.springframework.web.socket.WebSocketSession> sessions = new ConcurrentHashMap<>();

    /** Redis Key */
    private static final String USER_ONLINE_KEY = "user:online:";
    private static final String WS_SESSION_KEY = "ws:session:";
    private static final String OFFLINE_QUEUE_KEY = "msg:offline:";
    private static final int ONLINE_TTL_SECONDS = 120;
    private static final int MAX_OFFLINE_PUSH = 50;

    /**
     * 用户连接建立。
     *
     * 单设备策略：踢掉旧连接，新连接接管。
     * 上线补发：推送离线期间积攒的消息。
     */
    public void onConnect(Long userId, org.springframework.web.socket.WebSocketSession session) {
        // 检查是否有旧连接
        org.springframework.web.socket.WebSocketSession oldSession = sessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                // 推送被踢消息后关闭
                oldSession.close(new org.springframework.web.socket.CloseStatus(1008, "kicked"));
                log.info("踢掉旧连接: userId={}", userId);
            } catch (Exception e) {
                log.warn("关闭旧连接失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        // 注册新连接
        sessions.put(userId, session);

        // 更新 Redis 在线状态
        long now = System.currentTimeMillis();
        redisService.set(USER_ONLINE_KEY + userId, now, ONLINE_TTL_SECONDS);
        redisService.set(WS_SESSION_KEY + userId, "clx-message", ONLINE_TTL_SECONDS);

        // 上线补发离线消息
        pushOfflineMessages(userId, session);

        log.info("WebSocket连接建立: userId={}, 当前在线用户数={}", userId, sessions.size());
    }

    /**
     * 推送离线消息。
     */
    private void pushOfflineMessages(Long userId, org.springframework.web.socket.WebSocketSession session) {
        String key = OFFLINE_QUEUE_KEY + userId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, MAX_OFFLINE_PUSH - 1);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        try {
            // 批量推送
            for (Object msg : messages) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(msg.toString()));
            }

            // 推送完成后清空队列
            redisTemplate.delete(key);
            log.info("离线消息补发完成: userId={}, count={}", userId, messages.size());
        } catch (Exception e) {
            log.error("离线消息补发失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 用户断开连接。
     */
    public void onDisconnect(Long userId, org.springframework.web.socket.WebSocketSession session) {
        sessions.remove(userId);
        log.info("WebSocket连接断开: userId={}, 当前在线用户数={}", userId, sessions.size());
    }

    /**
     * 刷新心跳（更新 Redis TTL）。
     */
    public void refreshHeartbeat(Long userId) {
        long now = System.currentTimeMillis();
        redisService.set(USER_ONLINE_KEY + userId, now, ONLINE_TTL_SECONDS);
        redisService.set(WS_SESSION_KEY + userId, "clx-message", ONLINE_TTL_SECONDS);
    }

    /**
     * 推送消息给指定用户。
     *
     * @param userId 目标用户ID
     * @param message JSON 格式消息
     * @return 是否推送成功（用户在线）
     */
    public boolean pushToUser(Long userId, String message) {
        org.springframework.web.socket.WebSocketSession session = sessions.get(userId);
        if (session == null || !session.isOpen()) {
            log.debug("用户不在线，无法推送: userId={}", userId);
            return false;
        }

        try {
            session.sendMessage(new org.springframework.web.socket.TextMessage(message));
            log.debug("消息推送成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("消息推送失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }

    /**
     * 检查用户是否在线。
     */
    public boolean isOnline(Long userId) {
        org.springframework.web.socket.WebSocketSession session = sessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 获取在线用户数。
     */
    public int getOnlineCount() {
        return sessions.size();
    }

}