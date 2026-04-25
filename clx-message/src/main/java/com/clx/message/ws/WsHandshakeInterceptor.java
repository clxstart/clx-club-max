package com.clx.message.ws;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器。
 *
 * 验证 sa-Token，提取用户 ID 存入 WebSocket Session attributes。
 */
@Slf4j
@Component
public class WsHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_KEY = "userId";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从 URL 参数获取 token
        String query = request.getURI().getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("WebSocket握手失败: 缺少token参数");
            return false;
        }

        String token = extractToken(query);
        if (token == null) {
            log.warn("WebSocket握手失败: token参数格式错误");
            return false;
        }

        // 验证 token
        try {
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null) {
                log.warn("WebSocket握手失败: token无效");
                return false;
            }
            Long userId = Long.parseLong(loginId.toString());
            attributes.put(USER_ID_KEY, userId);
            log.info("WebSocket握手成功: userId={}", userId);
            return true;
        } catch (Exception e) {
            log.warn("WebSocket握手失败: token验证异常 - {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理
    }

    private String extractToken(String query) {
        // 解析 token=xxx 格式
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

}