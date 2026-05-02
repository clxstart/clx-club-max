package com.clx.analytics.service.impl;

import com.clx.analytics.dto.BehaviorLogRequest;
import com.clx.analytics.entity.BehaviorLog;
import com.clx.analytics.mapper.BehaviorLogMapper;
import com.clx.analytics.service.BehaviorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 行为日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorLogServiceImpl implements BehaviorLogService {

    private final BehaviorLogMapper behaviorLogMapper;

    @Override
    public void record(BehaviorLogRequest request) {
        BehaviorLog behaviorLog = new BehaviorLog();
        behaviorLog.setUserId(request.getUserId());
        behaviorLog.setBehaviorType(request.getBehaviorType());
        behaviorLog.setTargetId(request.getTargetId());
        behaviorLog.setTargetType(request.getTargetType());
        behaviorLog.setExtra(request.getExtra());
        behaviorLog.setCreateTime(LocalDateTime.now());

        behaviorLogMapper.insert(behaviorLog);
        log.debug("记录行为日志: userId={}, type={}", request.getUserId(), request.getBehaviorType());
    }
}
