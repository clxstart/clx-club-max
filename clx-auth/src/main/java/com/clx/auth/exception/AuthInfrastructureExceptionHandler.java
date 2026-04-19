package com.clx.auth.exception;

import com.clx.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * auth 模块基础设施异常处理。
 */
@Slf4j
@RestControllerAdvice
@Order(-2)
public class AuthInfrastructureExceptionHandler {

    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<R<Void>> handleRedisConnectionFailure(RedisConnectionFailureException e) {
        log.error("Redis 连接失败", e);
        return ResponseEntity.status(503).body(R.fail(503, "认证缓存不可用，请稍后重试"));
    }
}
