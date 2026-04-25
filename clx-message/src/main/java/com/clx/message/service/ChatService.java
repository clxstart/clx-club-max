package com.clx.message.service;

import com.clx.message.dto.SendMessageRequest;
import com.clx.message.entity.ChatMessage;
import com.clx.message.entity.ChatSession;

import java.util.List;
import java.util.Map;

/**
 * 私信服务接口。
 */
public interface ChatService {

    /**
     * 发送私信。
     *
     * @param fromUserId 发送者ID
     * @param request    发送请求
     * @return 消息ID和会话ID
     */
    Map<String, Object> sendMessage(Long fromUserId, SendMessageRequest request);

    /**
     * 获取会话列表。
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 会话列表
     */
    Map<String, Object> getSessions(Long userId, int page, int size);

    /**
     * 获取会话消息历史。
     *
     * @param userId    当前用户ID
     * @param sessionId 会话ID
     * @param cursor    游标（上一页最后一条消息ID）
     * @param size      每页数量
     * @return 消息列表
     */
    Map<String, Object> getMessages(Long userId, Long sessionId, Long cursor, int size);

    /**
     * 标记会话已读。
     *
     * @param userId    当前用户ID
     * @param sessionId 会话ID
     */
    void markRead(Long userId, Long sessionId);

    /**
     * 获取私信未读数。
     *
     * @param userId 用户ID
     * @return 未读数
     */
    int getUnreadCount(Long userId);

}