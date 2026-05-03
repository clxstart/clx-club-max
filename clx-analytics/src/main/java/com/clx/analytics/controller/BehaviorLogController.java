package com.clx.analytics.controller;

import com.clx.analytics.dto.BehaviorLogRequest;
import com.clx.analytics.service.BehaviorLogService;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 行为日志控制器
 */
@Tag(name = "行为日志接口")
@RestController
@RequestMapping("/analytics/behavior")
@RequiredArgsConstructor
public class BehaviorLogController {

    private final BehaviorLogService behaviorLogService;

    @Operation(summary = "记录行为日志")
    @PostMapping
    public R<Void> record(@Valid @RequestBody BehaviorLogRequest request,
                          HttpServletRequest httpRequest) {
        // 获取 IP 和 User-Agent
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        behaviorLogService.record(request);
        return R.ok();
    }

    /** 获取客户端 IP */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
