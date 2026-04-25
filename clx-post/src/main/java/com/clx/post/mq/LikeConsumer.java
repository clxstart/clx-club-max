package com.clx.post.mq;

import com.clx.post.config.LikeMQConfig;
import com.clx.post.dto.LikeMessage;
import com.clx.post.entity.LikeRecord;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 点赞消息消费者。
 *
 * 消费点赞消息，写入 like_record + 更新 post.like_count。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeConsumer {

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;

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

        log.info("点赞消费成功: postId={}, userId={}", postId, userId);
    }
}