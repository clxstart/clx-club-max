package com.clx.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.message.dto.NotificationTriggerRequest;
import com.clx.message.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器。
 */
@Tag(name = "通知管理", description = "通知相关接口")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 触发通知（内部调用）。
     */
    @Operation(summary = "触发通知（内部调用）")
    @PostMapping("/notification/trigger")
    public R<Map<String, Object>> trigger(@RequestBody NotificationTriggerRequest request) {
        Long id = notificationService.trigger(request);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("notificationId", id);
        return R.ok(result);
    }

    /**
     * 获取通知列表。
     */
    @SaCheckLogin
    @Operation(summary = "获取通知列表")
    @GetMapping("/notification/list")
    public R<Map<String, Object>> getList(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = notificationService.getList(userId, type, page, size);
        return R.ok(result);
    }

    /**
     * 标记单条已读。
     */
    @SaCheckLogin
    @Operation(summary = "标记单条已读")
    @PutMapping("/notification/read/{id}")
    public R<Void> markRead(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markRead(userId, id);
        return R.ok(null);
    }

    /**
     * 按类型标记全部已读。
     */
    @SaCheckLogin
    @Operation(summary = "按类型标记全部已读")
    @PutMapping("/notification/read-all")
    public R<Void> markAllRead(@RequestParam(required = false) String type) {
        Long userId = StpUtil.getLoginIdAsLong();
        notificationService.markAllRead(userId, type);
        return R.ok(null);
    }

    /**
     * 获取未读数。
     */
    @SaCheckLogin
    @Operation(summary = "获取未读数")
    @GetMapping("/notification/unread-count")
    public R<Map<String, Integer>> getUnreadCount() {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Integer> result = notificationService.getUnreadCount(userId);
        return R.ok(result);
    }

}