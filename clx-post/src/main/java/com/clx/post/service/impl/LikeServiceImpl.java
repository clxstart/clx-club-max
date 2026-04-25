package com.clx.post.service.impl;

import com.clx.common.redis.service.RedisService;
import com.clx.post.dto.LikeMessage;
import com.clx.post.entity.Comment;
import com.clx.post.entity.LikeRecord;
import com.clx.post.entity.Post;
import com.clx.post.mapper.CommentMapper;
import com.clx.post.mapper.LikeRecordMapper;
import com.clx.post.mapper.PostMapper;
import com.clx.post.mq.LikeProducer;
import com.clx.post.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现。
 *
 * likePost：异步化，Redis 计数 + MQ 消息
 * unlikePost：同步，写 DB + 清 Redis key
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final RedisService redisService;
    private final LikeProducer likeProducer;

    // Redis key 格式
    private static final String LIKE_COUNT_KEY = "post:like_count:";

    @Override
    public int likePost(Long postId, Long userId) {
        // 检查是否已点赞（避免无效 MQ 消息）
        if (likeRecordMapper.exists(userId, "1", postId)) {
            throw new RuntimeException("已点赞");
        }

        // Redis 计数 +1
        String key = LIKE_COUNT_KEY + postId;
        Long count = redisService.increment(key);

        // 发送 MQ 消息（消费者写入 like_record + 更新 DB）
        LikeMessage message = LikeMessage.of(postId, userId);
        likeProducer.send(message);

        // 返回 Redis 计数
        return count.intValue();
    }

    @Override
    @Transactional
    public int unlikePost(Long postId, Long userId) {
        // 检查是否已点赞
        if (!likeRecordMapper.exists(userId, "1", postId)) {
            throw new RuntimeException("未点赞");
        }

        // 删除点赞记录
        likeRecordMapper.delete(userId, "1", postId);

        // 减少点赞数
        postMapper.decrementLikeCount(postId);

        // 清除 Redis key（下次点赞从 DB 初始化）
        String key = LIKE_COUNT_KEY + postId;
        redisService.delete(key);

        // 返回 DB 计数
        Post post = postMapper.selectById(postId);
        return post != null ? post.getLikeCount() : 0;
    }

    @Override
    @Transactional
    public int likeComment(Long commentId, Long userId) {
        // 评论点赞保持同步
        if (likeRecordMapper.exists(userId, "2", commentId)) {
            throw new RuntimeException("已点赞");
        }

        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setTargetType("2");
        record.setTargetId(commentId);
        likeRecordMapper.insert(record);

        commentMapper.incrementLikeCount(commentId);

        Comment comment = commentMapper.selectById(commentId);
        return comment != null ? comment.getLikeCount() : 0;
    }

    @Override
    @Transactional
    public int unlikeComment(Long commentId, Long userId) {
        // 评论取消点赞保持同步
        if (!likeRecordMapper.exists(userId, "2", commentId)) {
            throw new RuntimeException("未点赞");
        }

        likeRecordMapper.delete(userId, "2", commentId);

        commentMapper.decrementLikeCount(commentId);

        Comment comment = commentMapper.selectById(commentId);
        return comment != null ? comment.getLikeCount() : 0;
    }
}