package com.clx.post.mq;

import com.clx.post.config.LikeMQConfig;
import com.clx.post.dto.LikeMessage;
import com.clx.post.entity.LikeRecord;
import com.clx.post.entity.Post;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 点赞消息消费者。
 *
 * 消费点赞消息，写入 like_record + 更新 post.like_count + 同步获赞数到 clx-user。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeConsumer {

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final RestTemplate restTemplate;

    /** clx-user 服务地址 */
    private static final String USER_SERVICE_URL = "http://localhost:9200/internal/user/like/incr";

    /**
     * 消费点赞消息。
     *
     * 幂等处理：检查 like_record 是否已存在。
     */
    @RabbitListener(queues = LikeMQConfig.LIKE_QUEUE)
    public void consume(LikeMessage message) {
        log.info("消费点赞消息: postId={}, userId={}, uuid={}",
                message.getPostId(), message.getUserId(), message.getUuid());

        Long postId = message.getPostId();
        Long userId = message.getUserId();

        // 幂等检查：是否已点赞
        if (likeRecordMapper.exists(userId, "1", postId)) {
            log.warn("重复点赞消息，跳过: postId={}, userId={}", postId, userId);
            return;
        }

        // 写入 like_record
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setTargetType("1");
        record.setTargetId(postId);
        record.setCreateTime(LocalDateTime.now());
        likeRecordMapper.insert(record);

        // 更新 post.like_count
        postMapper.incrementLikeCount(postId);

        // 同步获赞数到 clx-user（获取帖子作者ID）
        try {
            Post post = postMapper.selectById(postId);
            if (post != null && post.getAuthorId() != null) {
                Map<String, Object> body = Map.of(
                        "userId", post.getAuthorId(),
                        "delta", 1
                );
                restTemplate.postForEntity(USER_SERVICE_URL, body, String.class);
                log.info("同步获赞数成功: authorId={}", post.getAuthorId());
            }
        } catch (Exception e) {
            log.error("同步获赞数失败: {}", e.getMessage());
            // 不抛出异常，避免消息重试
        }

        log.info("点赞消费成功: postId={}, userId={}", postId, userId);
    }
}