package com.clx.search.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置。
 */
@Configuration
public class RabbitMQConfig {

    // 数据同步 Exchange
    public static final String SEARCH_SYNC_EXCHANGE = "search.sync.exchange";

    // 数据同步 Queue
    public static final String POST_SYNC_QUEUE = "search.post.sync";
    public static final String USER_SYNC_QUEUE = "search.user.sync";
    public static final String CATEGORY_SYNC_QUEUE = "search.category.sync";
    public static final String TAG_SYNC_QUEUE = "search.tag.sync";

    // Routing Key
    public static final String POST_SYNC_KEY = "search.post.sync";
    public static final String USER_SYNC_KEY = "search.user.sync";
    public static final String CATEGORY_SYNC_KEY = "search.category.sync";
    public static final String TAG_SYNC_KEY = "search.tag.sync";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange searchSyncExchange() {
        return new TopicExchange(SEARCH_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue postSyncQueue() {
        return new Queue(POST_SYNC_QUEUE, true);
    }

    @Bean
    public Queue userSyncQueue() {
        return new Queue(USER_SYNC_QUEUE, true);
    }

    @Bean
    public Queue categorySyncQueue() {
        return new Queue(CATEGORY_SYNC_QUEUE, true);
    }

    @Bean
    public Queue tagSyncQueue() {
        return new Queue(TAG_SYNC_QUEUE, true);
    }

    @Bean
    public Binding postSyncBinding() {
        return BindingBuilder.bind(postSyncQueue())
                .to(searchSyncExchange())
                .with(POST_SYNC_KEY);
    }

    @Bean
    public Binding userSyncBinding() {
        return BindingBuilder.bind(userSyncQueue())
                .to(searchSyncExchange())
                .with(USER_SYNC_KEY);
    }

    @Bean
    public Binding categorySyncBinding() {
        return BindingBuilder.bind(categorySyncQueue())
                .to(searchSyncExchange())
                .with(CATEGORY_SYNC_KEY);
    }

    @Bean
    public Binding tagSyncBinding() {
        return BindingBuilder.bind(tagSyncQueue())
                .to(searchSyncExchange())
                .with(TAG_SYNC_KEY);
    }
}