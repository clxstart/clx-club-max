package com.clx.message.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.message.dto.WsEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * WebSocket 消息处理器。
 *
 * 处理客户端发送的消息：
 * - ping: 心跳请求，返回 pong
 * - chat: 私信消息，转发给目标用户
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final WsSessionManager wsSessionManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            wsSessionManager.onConnect(userId, session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = getUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        // 更新心跳（刷新在线状态 TTL）
        wsSessionManager.refreshHeartbeat(userId);

        // 解析消息
        String payload = message.getPayload();
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
        String type = (String) msgMap.get("type");

        if ("ping".equals(type)) {
            // 心跳响应
            WsEnvelope pong = WsEnvelope.pong();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
        } else if ("chat".equals(type)) {
            // 私信消息（暂不处理，后续 step-4 实现）
            // TODO: 调用 ChatService 处理
            log.debug("收到chat消息: userId={}, payload={}", userId, payload);
        } else {
            log.warn("未知消息类型: type={}, userId={}", type, userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserId(session);
        if (userId != null) {
            wsSessionManager.onDisconnect(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误: {}", exception.getMessage());
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(WsHandshakeInterceptor.USER_ID_KEY);
        return userId != null ? (Long) userId : null;
    }

}