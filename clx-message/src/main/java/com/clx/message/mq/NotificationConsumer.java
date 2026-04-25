package com.clx.message.mq;

import com.clx.message.config.RabbitMQConfig;
import com.clx.message.dto.NotificationTriggerRequest;
import com.clx.message.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知 MQ 消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    /**
     * 消费通知消息。
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consume(NotificationTriggerRequest request) {
        log.info("消费通知消息: userId={}, type={}", request.getUserId(), request.getType());
        try {
            notificationService.trigger(request);
            log.info("通知消费成功: userId={}, type={}", request.getUserId(), request.getType());
        } catch (Exception e) {
            log.error("通知消费失败: userId={}, type={}, error={}",
                    request.getUserId(), request.getType(), e.getMessage());
            throw e; // 重新抛出触发重试
        }
    }

}