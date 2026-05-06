package com.clx.message.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知服务 MQ 配置。
 *
 * 定义通知消息的交换机、队列、死信队列及绑定关系。
 * 支持 JSON 序列化和消费失败重试（3次后进入死信队列）。
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 常量定义 ====================

    /** 主交换机 */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    /** 主队列 */
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    /** 死信队列 */
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    /** 主路由键 */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.create";
    /** 死信路由键 */
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.dlq";

    // ==================== 消息转换器 ====================

    /** JSON 序列化，使消息在 MQ 中可读 */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** RabbitTemplate 使用 JSON 转换器 */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /** 监听器容器使用 JSON 转换器 */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // ==================== 交换机 ====================

    /** 主交换机（durable=true 持久化） */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /** 死信交换机 */
    @Bean
    public DirectExchange notificationDlqExchange() {
        return new DirectExchange(NOTIFICATION_DLQ, true, false);
    }

    // ==================== 队列 ====================

    /**
     * 主队列。
     * 消费失败后进入死信队列（通过 x-dead-letter-exchange 和 x-dead-letter-routing-key 配置）。
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLQ)
                .deadLetterRoutingKey(NOTIFICATION_DLQ_ROUTING_KEY)
                .build();
    }

    /** 死信队列：存储消费失败的消息，供人工处理 */
    @Bean
    public Queue notificationDlqQueue() {
        return new Queue(NOTIFICATION_DLQ, true);
    }

    // ==================== 绑定关系 ====================

    /** 主队列绑定：交换机 + 路由键 → 主队列 */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /** 死信队列绑定：死信交换机 + 死信路由键 → 死信队列 */
    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlqQueue())
                .to(notificationDlqExchange())
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }
}