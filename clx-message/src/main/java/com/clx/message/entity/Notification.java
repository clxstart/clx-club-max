package com.clx.message.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体。
 */
@Data
public class Notification {

    /** 通知ID */
    private Long id;

    /** 接收通知的用户ID */
    private Long userId;

    /** 通知类型: comment_reply/like/follow/system */
    private String type;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 来源ID */
    private Long sourceId;

    /** 来源类型 */
    private String sourceType;

    /** 是否已读 */
    private Boolean isRead;

    /** 聚合数量 */
    private Integer aggregateCount;

    /** 聚合键 */
    private String aggregateKey;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}