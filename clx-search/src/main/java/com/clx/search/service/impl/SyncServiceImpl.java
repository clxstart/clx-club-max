package com.clx.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.clx.search.config.RabbitMQConfig;
import com.clx.search.constant.ESIndexConstants;
import com.clx.search.es.CategoryDocument;
import com.clx.search.es.PostDocument;
import com.clx.search.es.TagDocument;
import com.clx.search.es.UserDocument;
import com.clx.search.service.SyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 数据同步服务实现 - MQ 消费者。
 *
 * 消费其他服务发送的数据变更消息，更新 ES 索引。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final ElasticsearchClient esClient;

    @Override
    @RabbitListener(queues = RabbitMQConfig.POST_SYNC_QUEUE)
    public void syncPost(PostDocument post) {
        log.info("收到帖子同步消息: {}", post.getId());
        try {
            IndexRequest<PostDocument> request = IndexRequest.of(i -> i
                    .index(ESIndexConstants.POST_INDEX)
                    .id(String.valueOf(post.getId()))
                    .document(post));
            esClient.index(request);
            log.info("帖子同步成功: {}", post.getId());
        } catch (IOException e) {
            log.error("帖子同步失败: {}", e.getMessage());
        }
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.USER_SYNC_QUEUE)
    public void syncUser(UserDocument user) {
        log.info("收到用户同步消息: {}", user.getUserId());
        try {
            IndexRequest<UserDocument> request = IndexRequest.of(i -> i
                    .index(ESIndexConstants.USER_INDEX)
                    .id(String.valueOf(user.getUserId()))
                    .document(user));
            esClient.index(request);
            log.info("用户同步成功: {}", user.getUserId());
        } catch (IOException e) {
            log.error("用户同步失败: {}", e.getMessage());
        }
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.CATEGORY_SYNC_QUEUE)
    public void syncCategory(CategoryDocument category) {
        log.info("收到分类同步消息: {}", category.getId());
        try {
            IndexRequest<CategoryDocument> request = IndexRequest.of(i -> i
                    .index(ESIndexConstants.CATEGORY_INDEX)
                    .id(String.valueOf(category.getId()))
                    .document(category));
            esClient.index(request);
            log.info("分类同步成功: {}", category.getId());
        } catch (IOException e) {
            log.error("分类同步失败: {}", e.getMessage());
        }
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.TAG_SYNC_QUEUE)
    public void syncTag(TagDocument tag) {
        log.info("收到标签同步消息: {}", tag.getId());
        try {
            IndexRequest<TagDocument> request = IndexRequest.of(i -> i
                    .index(ESIndexConstants.TAG_INDEX)
                    .id(String.valueOf(tag.getId()))
                    .document(tag));
            esClient.index(request);
            log.info("标签同步成功: {}", tag.getId());
        } catch (IOException e) {
            log.error("标签同步失败: {}", e.getMessage());
        }
    }
}