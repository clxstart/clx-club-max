package com.clx.message.controller;

import com.clx.common.core.domain.R;
import com.clx.message.service.OnlineStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 在线状态控制器。
 * 批量获取用户在线/离线状态
 */
@Tag(name = "在线状态", description = "用户在线状态查询接口")
@RestController
@RequiredArgsConstructor
public class OnlineStatusController {

    private final OnlineStatusService onlineStatusService;

    /**
     * 批量获取用户在线状态。
     */
    @Operation(summary = "批量获取在线状态")
    @GetMapping("/message/online-status")
    public R<Map<String, Map<String, Object>>> getOnlineStatus(@RequestParam String userIds) {
        List<Long> ids = parseUserIds(userIds);
        Map<Long, OnlineStatusService.OnlineStatus> statusMap = onlineStatusService.getOnlineStatus(ids);

        // 转换为前端友好格式
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map.Entry<Long, OnlineStatusService.OnlineStatus> entry : statusMap.entrySet()) {
            Map<String, Object> status = new HashMap<>();
            status.put("online", entry.getValue().online());
            status.put("lastActiveTime", entry.getValue().lastActiveTime());
            result.put(entry.getKey().toString(), status);
        }

        return R.ok(result);
    }

    /**
     * 解析逗号分隔的用户ID字符串。
     *
     * @param userIds 逗号分隔的用户ID字符串，如 "1,5,10"
     * @return 用户ID列表，如 [1L, 5L, 10L]
     */
    private List<Long> parseUserIds(String userIds) {
        return java.util.Arrays.stream(userIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

}