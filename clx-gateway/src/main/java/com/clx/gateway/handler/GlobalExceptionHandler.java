package com.clx.gateway.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 *
 * <p>捕获 Gateway 层的异常，统一返回 JSON 格式的错误信息。
 */
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        // 已经提交的响应无法修改
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应类型
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 根据异常类型设置状态码和消息
        int code;
        String message;

        if (ex instanceof NotLoginException) {
            code = 401;
            message = "未登录或登录已过期";
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
        } else if (ex instanceof NotRoleException) {
            code = 403;
            message = "无此角色权限";
            response.setStatusCode(HttpStatus.FORBIDDEN);
        } else if (ex instanceof NotPermissionException) {
            code = 403;
            message = "无此操作权限";
            response.setStatusCode(HttpStatus.FORBIDDEN);
        } else if (ex instanceof ResponseStatusException rse) {
            code = rse.getStatusCode().value();
            message = rse.getReason();
            response.setStatusCode(rse.getStatusCode());
        } else {
            code = 500;
            message = "服务器内部错误";
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            log.error("Gateway 异常", ex);
        }

        // 构建响应体
        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", message);
        result.put("data", null);

        try {
            String body = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            return Mono.error(e);
        }
    }
}
