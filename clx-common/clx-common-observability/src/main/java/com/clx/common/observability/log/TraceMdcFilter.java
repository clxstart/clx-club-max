package com.clx.common.observability.log;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 日志 MDC 过滤器。
 *
 * <p>将 TraceId、SpanId 放入 MDC，便于日志中输出链路信息。
 *
 * <p>日志配置示例（logback-spring.xml）：
 * <pre>
 * &lt;pattern&gt;%d{yyyy-MM-dd HH:mm:ss} [%X{traceId}] [%thread] %-5level %logger{36} - %msg%n&lt;/pattern&gt;
 * </pre>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(Tracer.class)
public class TraceMdcFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";

    private final Tracer tracer;

    public TraceMdcFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                MDC.put(TRACE_ID_KEY, currentSpan.context().traceId());
                MDC.put(SPAN_ID_KEY, currentSpan.context().spanId());
            }
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
            MDC.remove(SPAN_ID_KEY);
        }
    }
}
