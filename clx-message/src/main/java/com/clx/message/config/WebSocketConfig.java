package com.clx.message.config;

import com.clx.message.ws.MessageWebSocketHandler;
import com.clx.message.ws.WsHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置。
 *
 * 路径：ws://localhost:9500/ws/message?token={sa-Token}
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    // 消息处理器
    private final MessageWebSocketHandler messageWebSocketHandler;
    // 握手拦截器
    private final WsHandshakeInterceptor wsHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(messageWebSocketHandler, "/ws/message") // 注册处理器
                .addInterceptors(wsHandshakeInterceptor) // 添加栏截器
                .setAllowedOrigins("*"); // 允许跨域
    }

}