package com.clx.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket 消息信封。
 *
 * 所有 WebSocket 推送消息的统一格式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsEnvelope {

    /** 消息类型: pong/chat/notification/offline/unread */
    private String type;

    /** Pong（心跳响应） */
    public static WsEnvelope pong() {
        return new WsEnvelope("pong", null, null, null, null, null, null, null, null, null);
    }

    // 以下字段用于不同消息类型，按需填充

    /** 私信来源用户ID */
    private Long from;

    /** 私信来源用户昵称 */
    private String fromName;

    /** 私信内容 */
    private String content;

    /** 私信会话ID */
    private Long sessionId;

    /** 消息时间戳 */
    private Long timestamp;

    /** 通知分类 */
    private String category;

    /** 通知标题 */
    private String title;

    /** 离线消息批次 */
    private Object offline;

    /** 未读计数 */
    private Object unread;

    /**
     * 创建私信推送信封。
     */
    public static WsEnvelope chat(Long from, String fromName, String content, Long sessionId, Long timestamp) {
        WsEnvelope envelope = new WsEnvelope();
        envelope.setType("chat");
        envelope.setFrom(from);
        envelope.setFromName(fromName);
        envelope.setContent(content);
        envelope.setSessionId(sessionId);
        envelope.setTimestamp(timestamp);
        return envelope;
    }

    /**
     * 创建通知推送信封。
     */
    public static WsEnvelope notification(String category, String title, String content, Long timestamp) {
        WsEnvelope envelope = new WsEnvelope();
        envelope.setType("notification");
        envelope.setCategory(category);
        envelope.setTitle(title);
        envelope.setContent(content);
        envelope.setTimestamp(timestamp);
        return envelope;
    }

}