package com.clx.analytics.service;

import com.clx.analytics.dto.BehaviorLogRequest;

/**
 * 行为日志服务接口
 */
public interface BehaviorLogService {

    /**
     * 记录行为日志
     *
     * @param request 行为日志请求
     */
    void record(BehaviorLogRequest request);
}
