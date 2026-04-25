package com.clx.message.service.impl;

import com.clx.common.redis.service.RedisService;
import com.clx.message.service.OnlineStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线状态服务实现。
 *
 * Redis Key: user:online:{userId} = lastActiveTime (Timestamp)
 * TTL: 120 秒
 */
@Service
@RequiredArgsConstructor
public class OnlineStatusServiceImpl implements OnlineStatusService {

    private final RedisService redisService;

    private static final String USER_ONLINE_KEY = "user:online:";
    private static final int ONLINE_TTL_SECONDS = 120;

    @Override
    public Map<Long, OnlineStatus> getOnlineStatus(List<Long> userIds) {
        Map<Long, OnlineStatus> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, getOnlineStatus(userId));
        }
        return result;
    }

    @Override
    public OnlineStatus getOnlineStatus(Long userId) {
        Object value = redisService.get(USER_ONLINE_KEY + userId);
        if (value == null) {
            // Redis key 不存在，用户离线
            return new OnlineStatus(false, null);
        }

        Long lastActiveTime = ((Number) value).longValue();
        // 如果 TTL 过期前 key 还在，认为在线
        return new OnlineStatus(true, lastActiveTime);
    }

    @Override
    public void updateLastActiveTime(Long userId) {
        long now = System.currentTimeMillis();
        redisService.set(USER_ONLINE_KEY + userId, now, ONLINE_TTL_SECONDS);
    }

}