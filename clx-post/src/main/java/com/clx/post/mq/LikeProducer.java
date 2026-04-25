package com.clx.post.mq;

import com.clx.post.config.LikeMQConfig;
import com.clx.post.dto.LikeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 点赞消息生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送点赞消息到 MQ。
     *
     * @param message 点赞消息
     */
    public void send(LikeMessage message) {
        log.info("发送点赞消息: postId={}, userId={}, uuid={}",
                message.getPostId(), message.getUserId(), message.getUuid());
        rabbitTemplate.convertAndSend(
                LikeMQConfig.LIKE_EXCHANGE,
                LikeMQConfig.LIKE_ROUTING_KEY,
                message
        );
    }
}