package com.clx.message.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话实体。
 */
@Data
public class ChatSession {

    /** 会话ID */
    private Long id;

    /** 用户1 ID（较小值） */
    private Long user1Id;

    /** 用户2 ID（较大值） */
    private Long user2Id;

    /** 最后一条消息内容 */
    private String lastMessage;

    /** 最后消息时间 */
    private LocalDateTime lastTime;

    /** 用户1未读数 */
    private Integer user1Unread;

    /** 用户2未读数 */
    private Integer user2Unread;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

}