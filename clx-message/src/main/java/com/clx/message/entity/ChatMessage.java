package com.clx.message.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息实体。
 */
@Data
public class ChatMessage {

    /** 消息ID */
    private Long id;

    /** 会话ID */
    private Long sessionId;

    /** 发送者ID */
    private Long fromUserId;

    /** 接收者ID */
    private Long toUserId;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;

}