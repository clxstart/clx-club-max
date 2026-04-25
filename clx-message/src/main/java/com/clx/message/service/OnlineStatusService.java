package com.clx.message.service;

import java.util.Map;

/**
 * 在线状态服务接口。
 */
public interface OnlineStatusService {

    /**
     * 获取多个用户的在线状态。
     *
     * @param userIds 用户ID列表
     * @return Map<userId, {online, lastActiveTime}>
     */
    Map<Long, OnlineStatus> getOnlineStatus(java.util.List<Long> userIds);

    /**
     * 获取单个用户的在线状态。
     */
    OnlineStatus getOnlineStatus(Long userId);

    /**
     * 更新用户最后活跃时间。
     */
    void updateLastActiveTime(Long userId);

    /**
     * 用户在线状态。
     */
    record OnlineStatus(boolean online, Long lastActiveTime) {}

}