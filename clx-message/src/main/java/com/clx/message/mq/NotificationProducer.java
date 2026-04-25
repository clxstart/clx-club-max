package com.clx.message.mq;

import com.clx.message.config.RabbitMQConfig;
import com.clx.message.dto.NotificationTriggerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 通知 MQ 生产者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送通知消息到 MQ。
     */
    public void send(NotificationTriggerRequest request) {
        log.info("发送通知消息: userId={}, type={}", request.getUserId(), request.getType());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                request
        );
    }

}