package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录实体。
 */
@Data
public class LikeRecord {

    /** ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 目标类型:1帖子,2评论 */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 创建时间 */
    private LocalDateTime createTime;
}