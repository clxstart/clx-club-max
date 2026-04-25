package com.clx.message.service;

import com.clx.message.dto.NotificationTriggerRequest;
import com.clx.message.entity.Notification;

import java.util.Map;

/**
 * 通知服务接口。
 */
public interface NotificationService {

    /**
     * 触发通知（内部调用）。
     *
     * @param request 通知触发请求
     * @return 通知ID
     */
    Long trigger(NotificationTriggerRequest request);

    /**
     * 获取通知列表。
     *
     * @param userId 用户ID
     * @param type   通知类型（可选）
     * @param page   页码
     * @param size   每页数量
     * @return 通知列表
     */
    Map<String, Object> getList(Long userId, String type, int page, int size);

    /**
     * 标记单条已读。
     */
    void markRead(Long userId, Long notificationId);

    /**
     * 按类型标记全部已读。
     */
    void markAllRead(Long userId, String type);

    /**
     * 获取未读数（按类型分组）。
     */
    Map<String, Integer> getUnreadCount(Long userId);

}