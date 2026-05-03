package com.clx.common.observability.context;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 链路追踪上下文工具类。
 *
 * <p>提供获取当前 TraceId、SpanId 等信息的便捷方法。
 */
@Component
@ConditionalOnClass(Tracer.class)
public class TraceContext {

    private static Tracer tracer;

    public TraceContext(Tracer tracer) {
        TraceContext.tracer = tracer;
    }

    /**
     * 获取当前 TraceId。
     *
     * @return TraceId，不存在返回 "N/A"
     */
    public static String getTraceId() {
        return Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .map(Span::context)
                .map(ctx -> ctx.traceId())
                .orElse("N/A");
    }

    /**
     * 获取当前 SpanId。
     *
     * @return SpanId，不存在返回 "N/A"
     */
    public static String getSpanId() {
        return Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .map(Span::context)
                .map(ctx -> ctx.spanId())
                .orElse("N/A");
    }

    /**
     * 判断是否在链路追踪中。
     */
    public static boolean isInTrace() {
        return tracer != null && tracer.currentSpan() != null;
    }

    /**
     * 添加标签到当前 Span（用于记录业务信息）。
     *
     * @param key   标签名
     * @param value 标签值
     */
    public static void tag(String key, String value) {
        Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .ifPresent(span -> span.tag(key, value));
    }

    /**
     * 记录异常到当前 Span。
     *
     * @param e 异常
     */
    public static void error(Throwable e) {
        Optional.ofNullable(tracer)
                .map(Tracer::currentSpan)
                .ifPresent(span -> {
                    span.tag("error", e.getClass().getSimpleName());
                    span.error(e);
                });
    }
}
