package com.clx.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.clx.search.config.RabbitMQConfig;
import com.clx.search.es.PostDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 数据同步服务（MQ 消费者）。
 *
 * 消费 Canal 发送的数据变更消息，更新 ES 索引。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final ElasticsearchClient esClient;

    private static final String POST_INDEX = "clx_post";
    private static final String USER_INDEX = "clx_user";
    private static final String CATEGORY_INDEX = "clx_category";
    private static final String TAG_INDEX = "clx_tag";

    /**
     * 消费帖子同步消息。
     */
    @RabbitListener(queues = RabbitMQConfig.POST_SYNC_QUEUE)
    public void syncPost(PostDocument post) {
        log.info("收到帖子同步消息: {}", post.getId());
        try {
            IndexRequest<PostDocument> request = IndexRequest.of(i -> i
                    .index(POST_INDEX)
                    .id(String.valueOf(post.getId()))
                    .document(post));
            esClient.index(request);
            log.info("帖子同步成功: {}", post.getId());
        } catch (IOException e) {
            log.error("帖子同步失败: {}", e.getMessage());
        }
    }

    /**
     * 消费用户同步消息。
     */
    @RabbitListener(queues = RabbitMQConfig.USER_SYNC_QUEUE)
    public void syncUser(Object user) {
        log.info("收到用户同步消息");
        // TODO: 实现用户同步逻辑
    }

    /**
     * 消费分类同步消息。
     */
    @RabbitListener(queues = RabbitMQConfig.CATEGORY_SYNC_QUEUE)
    public void syncCategory(Object category) {
        log.info("收到分类同步消息");
        // TODO: 实现分类同步逻辑
    }

    /**
     * 消费标签同步消息。
     */
    @RabbitListener(queues = RabbitMQConfig.TAG_SYNC_QUEUE)
    public void syncTag(Object tag) {
        log.info("收到标签同步消息");
        // TODO: 实现标签同步逻辑
    }
}