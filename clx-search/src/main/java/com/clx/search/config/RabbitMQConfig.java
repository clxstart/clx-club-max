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
 * RabbitMQ 配置 - 用于搜索数据同步。
 *
 * 其他服务（帖子/用户等）数据变更时，发消息到 MQ，搜索服务消费消息同步到 ES。
 */
@Configuration
public class RabbitMQConfig {

    // ==================== Exchange ====================
    // 主题交换机，所有同步消息的入口（决定路由到那个队列）
    public static final String SEARCH_SYNC_EXCHANGE = "search.sync.exchange";

    // ==================== Queue ====================
    // 四种数据各自的同步队列（存储消息，消费者从这里获取消息）
    public static final String POST_SYNC_QUEUE = "search.post.sync";       // 帖子同步
    public static final String USER_SYNC_QUEUE = "search.user.sync";       // 用户同步
    public static final String CATEGORY_SYNC_QUEUE = "search.category.sync"; // 分类同步
    public static final String TAG_SYNC_QUEUE = "search.tag.sync";         // 标签同步

    // ==================== Routing Key ====================
    // 路由键，决定消息发到哪个队列
    public static final String POST_SYNC_KEY = "search.post.sync";
    public static final String USER_SYNC_KEY = "search.user.sync";
    public static final String CATEGORY_SYNC_KEY = "search.category.sync";
    public static final String TAG_SYNC_KEY = "search.tag.sync";

    /** JSON 序列化器，消息自动转 JSON */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** 主题交换机，消息路由入口（持久化） */
    @Bean
    public TopicExchange searchSyncExchange() {
        return new TopicExchange(SEARCH_SYNC_EXCHANGE, true, false);
    }

    /** 帖子同步队列（持久化） */
    @Bean
    public Queue postSyncQueue() {
        return new Queue(POST_SYNC_QUEUE, true);
    }

    /** 用户同步队列（持久化） */
    @Bean
    public Queue userSyncQueue() {
        return new Queue(USER_SYNC_QUEUE, true);
    }

    /** 分类同步队列（持久化） */
    @Bean
    public Queue categorySyncQueue() {
        return new Queue(CATEGORY_SYNC_QUEUE, true);
    }

    /** 标签同步队列（持久化） */
    @Bean
    public Queue tagSyncQueue() {
        return new Queue(TAG_SYNC_QUEUE, true);
    }

    /** 帖子队列绑定：路由键匹配时消息进入帖子队列 */
    @Bean
    public Binding postSyncBinding() {
        return BindingBuilder.bind(postSyncQueue())
                .to(searchSyncExchange())
                .with(POST_SYNC_KEY);
    }

    /** 用户队列绑定：路由键匹配时消息进入用户队列 */
    @Bean
    public Binding userSyncBinding() {
        return BindingBuilder.bind(userSyncQueue())
                .to(searchSyncExchange())
                .with(USER_SYNC_KEY);
    }

    /** 分类队列绑定：路由键匹配时消息进入分类队列 */
    @Bean
    public Binding categorySyncBinding() {
        return BindingBuilder.bind(categorySyncQueue())
                .to(searchSyncExchange())
                .with(CATEGORY_SYNC_KEY);
    }

    /** 标签队列绑定：路由键匹配时消息进入标签队列 */
    @Bean
    public Binding tagSyncBinding() {
        return BindingBuilder.bind(tagSyncQueue())
                .to(searchSyncExchange())
                .with(TAG_SYNC_KEY);
    }
}