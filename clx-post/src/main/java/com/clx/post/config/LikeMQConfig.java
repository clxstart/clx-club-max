package com.clx.post.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 点赞 MQ 配置。
 *
 * 包含 Exchange、Queue、DLQ（死信队列）及重试策略。
 */
@Configuration
public class LikeMQConfig {

    // Exchange
    public static final String LIKE_EXCHANGE = "like.exchange";

    // Queue
    public static final String LIKE_QUEUE = "like.queue";
    public static final String LIKE_DLQ = "like.dlq";  // 死信队列

    // Routing Key
    public static final String LIKE_ROUTING_KEY = "like.sync";

    // DLQ Routing Key
    public static final String LIKE_DLQ_ROUTING_KEY = "like.dlq";

    // ========== 消息转换器 ==========

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        // 重试配置在 application.yml 中设置
        return factory;
    }

    // ========== Exchange ==========

    @Bean
    public DirectExchange likeExchange() {
        return new DirectExchange(LIKE_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange likeDlqExchange() {
        return new DirectExchange(LIKE_DLQ, true, false);
    }

    // ========== Queue ==========

    /**
     * 点赞队列（带死信配置）。
     */
    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE)
                .deadLetterExchange(LIKE_DLQ)
                .deadLetterRoutingKey(LIKE_DLQ_ROUTING_KEY)
                .build();
    }

    /**
     * 死信队列。
     */
    @Bean
    public Queue likeDlqQueue() {
        return new Queue(LIKE_DLQ, true);
    }

    // ========== Binding ==========

    @Bean
    public Binding likeBinding() {
        return BindingBuilder.bind(likeQueue())
                .to(likeExchange())
                .with(LIKE_ROUTING_KEY);
    }

    @Bean
    public Binding likeDlqBinding() {
        return BindingBuilder.bind(likeDlqQueue())
                .to(likeDlqExchange())
                .with(LIKE_DLQ_ROUTING_KEY);
    }
}