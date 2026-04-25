package com.clx.message.service.impl;

import com.clx.common.redis.service.RedisService;
import com.clx.message.dto.NotificationTriggerRequest;
import com.clx.message.dto.WsEnvelope;
import com.clx.message.entity.Notification;
import com.clx.message.mapper.NotificationMapper;
import com.clx.message.service.MessageRouter;
import com.clx.message.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final RedisService redisService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRouter messageRouter;

    private static final String UNREAD_KEY = "unread:";
    private static final String AGGREGATE_LOCK_PREFIX = "notify:aggregate:";
    private static final int AGGREGATE_WINDOW_SECONDS = 3600; // 1 小时

    @Override
    public Long trigger(NotificationTriggerRequest request) {
        Long userId = request.getUserId();
        String type = request.getType();

        // 生成聚合键
        String aggregateKey = generateAggregateKey(userId, type, request.getSourceId(), request.getSourceType());

        // 检查聚合
        Notification existing = notificationMapper.selectByAggregateKey(aggregateKey);
        if (existing != null) {
            // 聚合：更新数量
            int newCount = existing.getAggregateCount() + 1;
            notificationMapper.updateAggregateCount(existing.getId(), newCount);
            log.info("通知聚合: userId={}, type={}, count={}", userId, type, newCount);
            return existing.getId();
        }

        // 创建新通知
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setSourceId(request.getSourceId());
        notification.setSourceType(request.getSourceType());
        notification.setIsRead(false);
        notification.setAggregateKey(aggregateKey);
        notification.setAggregateCount(1);
        notification.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(notification);

        // 更新 Redis 未读计数
        String unreadKey = UNREAD_KEY + userId;
        redisService.hSet(unreadKey, type, (redisService.hGet(unreadKey, type) != null
                ? ((Number) redisService.hGet(unreadKey, type)).intValue() + 1 : 1));

        // 设置聚合锁（1 小时 TTL）
        redisService.set(AGGREGATE_LOCK_PREFIX + aggregateKey, "1", AGGREGATE_WINDOW_SECONDS);

        log.info("通知创建: userId={}, type={}, id={}", userId, type, notification.getId());
        return notification.getId();
    }

    @Override
    public Map<String, Object> getList(Long userId, String type, int page, int size) {
        int offset = (page - 1) * size;
        List<Notification> notifications = notificationMapper.selectByUserId(userId, type, offset, size);
        int total = notificationMapper.countByUserId(userId, type);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Notification n : notifications) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", n.getId());
            item.put("type", n.getType());
            item.put("title", n.getTitle());
            item.put("content", n.getContent());
            item.put("isRead", n.getIsRead());
            item.put("aggregateCount", n.getAggregateCount());
            item.put("createTime", n.getCreateTime() != null ?
                    n.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
            list.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            throw new RuntimeException("通知不存在或无权访问");
        }

        if (!notification.getIsRead()) {
            notificationMapper.markRead(notificationId);

            // 更新 Redis 未读计数
            String unreadKey = UNREAD_KEY + userId;
            Object value = redisService.hGet(unreadKey, notification.getType());
            if (value != null) {
                int current = ((Number) value).intValue();
                redisService.hSet(unreadKey, notification.getType(), Math.max(0, current - 1));
            }
        }
    }

    @Override
    public void markAllRead(Long userId, String type) {
        notificationMapper.markAllRead(userId, type);

        // 更新 Redis 未读计数
        String unreadKey = UNREAD_KEY + userId;
        if (type != null && !type.isEmpty()) {
            redisService.hSet(unreadKey, type, 0);
        } else {
            // 全部类型清零
            redisService.hSet(unreadKey, "comment", 0);
            redisService.hSet(unreadKey, "like", 0);
            redisService.hSet(unreadKey, "follow", 0);
            redisService.hSet(unreadKey, "system", 0);
        }
    }

    @Override
    public Map<String, Integer> getUnreadCount(Long userId) {
        Map<String, Integer> result = new HashMap<>();
        result.put("comment", 0);
        result.put("like", 0);
        result.put("follow", 0);
        result.put("system", 0);
        result.put("chat", 0);

        String unreadKey = UNREAD_KEY + userId;
        Map<Object, Object> redisData = redisService.hGetAll(unreadKey);

        for (Map.Entry<Object, Object> entry : redisData.entrySet()) {
            String key = entry.getKey().toString();
            int value = ((Number) entry.getValue()).intValue();
            result.put(key, value);
        }

        return result;
    }

    private String generateAggregateKey(Long userId, String type, Long sourceId, String sourceType) {
        return userId + ":" + type + ":" + (sourceId != null ? sourceId : "none") + ":" +
                (sourceType != null ? sourceType : "none");
    }

}