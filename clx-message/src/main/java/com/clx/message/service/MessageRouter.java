package com.clx.message.service;

import com.clx.message.dto.WsEnvelope;
import com.clx.message.ws.WsSessionManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息路由器。
 *
 * 根据用户在线状态决定消息投递策略：
 * - 在线：Redis Pub/Sub 广播 + WebSocket 推送
 * - 离线：写入 Redis 离线队列，等待上线补发
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageRouter {

    private final WsSessionManager wsSessionManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BROADCAST_CHANNEL = "ws:broadcast";
    private static final String OFFLINE_QUEUE_KEY = "msg:offline:";
    private static final int MAX_OFFLINE_MESSAGES = 50;

    /**
     * 路由消息。
     *
     * @param toUserId 目标用户ID
     * @param envelope 消息信封
     */
    public void route(Long toUserId, WsEnvelope envelope) {
        try {
            String payload = objectMapper.writeValueAsString(envelope);

            if (wsSessionManager.isOnline(toUserId)) {
                // 在线：通过 Redis Pub/Sub 广播（支持集群）
                broadcastToRedis(toUserId, payload);
            } else {
                // 离线：写入离线队列
                queueOffline(toUserId, payload);
            }
        } catch (JsonProcessingException e) {
            log.error("消息序列化失败: {}", e.getMessage());
        }
    }

    /**
     * 广播到 Redis Pub/Sub。
     */
    private void broadcastToRedis(Long userId, String payload) {
        try {
            String message = objectMapper.writeValueAsString(new BroadcastMessage(userId, payload));
            redisTemplate.convertAndSend(BROADCAST_CHANNEL, message);
            log.debug("Redis广播消息: userId={}", userId);
        } catch (JsonProcessingException e) {
            log.error("Redis广播消息序列化失败: {}", e.getMessage());
        }
    }

    /**
     * 写入离线队列。
     */
    private void queueOffline(Long userId, String payload) {
        String key = OFFLINE_QUEUE_KEY + userId;
        redisTemplate.opsForList().rightPush(key, payload);
        // 只保留最近 N 条
        redisTemplate.opsForList().trim(key, -MAX_OFFLINE_MESSAGES, -1);
        log.debug("离线消息入队: userId={}", userId);
    }

    /**
     * 广播消息结构。
     */
    @lombok.AllArgsConstructor
    @lombok.Data
    private static class BroadcastMessage {
        private Long userId;
        private String payload;
    }

}