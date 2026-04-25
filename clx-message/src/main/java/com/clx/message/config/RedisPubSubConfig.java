package com.clx.message.config;

import com.clx.message.ws.WsSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub 配置。
 *
 * 用于 WebSocket 集群广播。
 * 当消息需要推送给用户时，发布到 Redis channel，所有实例订阅并尝试推送。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPubSubConfig {

    public static final String BROADCAST_CHANNEL = "ws:broadcast";

    private final WsSessionManager wsSessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Redis 消息监听容器。
     */
    @org.springframework.context.annotation.Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            org.springframework.data.redis.connection.RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListener(), new PatternTopic(BROADCAST_CHANNEL));
        return container;
    }

    /**
     * 消息监听器。
     */
    @org.springframework.context.annotation.Bean
    public MessageListener messageListener() {
        return new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                try {
                    String json = new String(message.getBody());
                    BroadcastMessage broadcast = objectMapper.readValue(json, BroadcastMessage.class);

                    // 尝试推送给本地连接
                    boolean success = wsSessionManager.pushToUser(broadcast.getUserId(), broadcast.getPayload());
                    if (success) {
                        log.debug("Redis广播推送成功: userId={}", broadcast.getUserId());
                    }
                } catch (Exception e) {
                    log.error("Redis广播消息处理失败: {}", e.getMessage());
                }
            }
        };
    }

    /**
     * 广播消息结构。
     */
    @lombok.Data
    public static class BroadcastMessage {
        private Long userId;
        private String payload;
    }

}