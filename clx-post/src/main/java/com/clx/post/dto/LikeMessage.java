package com.clx.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 点赞 MQ 消息实体 - 用于异步写 DB。
 *
 * 字段说明：
 * - postId/userId：业务核心数据
 * - uuid：幂等保障（防止重复消费）
 * - timestamp：便于问题排查
 * - action：预留扩展（取消点赞目前不走 MQ）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeMessage {

    private Long postId;      // 帖子ID
    private Long userId;      // 用户ID
    private String action;    // 操作类型（固定 "like"）
    private Long timestamp;   // 消息时间戳
    private String uuid;      // 幂等标识 userId:postId:timestamp

    /** 创建点赞消息（自动生成 uuid） */
    public static LikeMessage of(Long postId, Long userId) {
        long ts = Instant.now().toEpochMilli();
        String uuid = userId + ":" + postId + ":" + ts;
        return new LikeMessage(postId, userId, "like", ts, uuid);
    }
}