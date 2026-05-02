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
 * 点赞 MQ 配置 - 定义交换机、队列、死信队列及绑定关系。
 */
@Configuration
public class LikeMQConfig {

    // ========== 常量定义 ==========

    public static final String LIKE_EXCHANGE = "like.exchange";        // 主交换机
    public static final String LIKE_QUEUE = "like.queue";              // 主队列
    public static final String LIKE_DLQ = "like.dlq";                  // 死信队列
    public static final String LIKE_ROUTING_KEY = "like.sync";          // 主路由键
    public static final String LIKE_DLQ_ROUTING_KEY = "like.dlq";       // 死信路由键

    // ========== 消息转换器（JSON 序列化） ==========

    // 消息转json
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
        return factory;  // 重试策略在 application.yml 配置
    }

    // ========== 交换机（durable=true 持久化） ==========

    @Bean
    public DirectExchange likeExchange() {
        return new DirectExchange(LIKE_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange likeDlqExchange() {
        return new DirectExchange(LIKE_DLQ, true, false);
    }

    // ========== 队列 ==========

    /** 主队列：消费失败后进入死信队列 */
    @Bean
    public Queue likeQueue() {
        return QueueBuilder.durable(LIKE_QUEUE)
                .deadLetterExchange(LIKE_DLQ)
                .deadLetterRoutingKey(LIKE_DLQ_ROUTING_KEY)
                .build();
    }

    /** 死信队列：存储消费失败的消息，供人工处理 */
    @Bean
    public Queue likeDlqQueue() {
        return new Queue(LIKE_DLQ, true);
    }

    // ========== 绑定关系 ==========

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