package com.clx.message.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.message.dto.SendMessageRequest;
import com.clx.message.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 私信控制器。
 */
@Tag(name = "私信管理", description = "私信相关接口")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final ChatService chatService;

    /**
     * 发送私信。
     */
    @Operation(summary = "发送私信")
    @PostMapping("/message/send")
    public R<Map<String, Object>> sendMessage(@RequestBody SendMessageRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = chatService.sendMessage(userId, request);
        return R.ok(result);
    }

    /**
     * 获取会话列表。
     */
    @Operation(summary = "获取会话列表")
    @GetMapping("/message/sessions")
    public R<Map<String, Object>> getSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = chatService.getSessions(userId, page, size);
        return R.ok(result);
    }

    /**
     * 获取会话消息历史。
     */
    @Operation(summary = "获取会话消息历史")
    @GetMapping("/message/sessions/{sessionId}/messages")
    public R<Map<String, Object>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = StpUtil.getLoginIdAsLong();
        Map<String, Object> result = chatService.getMessages(userId, sessionId, cursor, size);
        return R.ok(result);
    }

    /**
     * 标记会话已读。
     */
    @Operation(summary = "标记会话已读")
    @PutMapping("/message/sessions/{sessionId}/read")
    public R<Void> markRead(@PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        chatService.markRead(userId, sessionId);
        return R.ok(null);
    }

}