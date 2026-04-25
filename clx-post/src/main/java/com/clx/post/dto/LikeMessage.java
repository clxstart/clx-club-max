package com.clx.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 点赞 MQ 消息实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeMessage {

    /** 帖子ID */
    private Long postId;

    /** 用户ID */
    private Long userId;

    /** 操作类型：固定为 "like"（unlike 不走 MQ） */
    private String action;

    /** 消息时间戳 */
    private Long timestamp;

    /** 幂等唯一标识（userId:postId:timestamp） */
    private String uuid;

    /**
     * 创建点赞消息。
     */
    public static LikeMessage of(Long postId, Long userId) {
        long ts = Instant.now().toEpochMilli();
        String uuid = userId + ":" + postId + ":" + ts;
        return new LikeMessage(postId, userId, "like", ts, uuid);
    }
}