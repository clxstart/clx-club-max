package com.clx.common.observability.constant;

/**
 * 可观测性相关常量。
 */
public final class ObservabilityConstants {

    /** TraceId 请求头 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** SpanId 请求头 */
    public static final String SPAN_ID_HEADER = "X-Span-Id";

    /** 服务名称标签 */
    public static final String TAG_SERVICE = "service";

    /** 方法名称标签 */
    public static final String TAG_METHOD = "method";

    /** 错误类型标签 */
    public static final String TAG_ERROR = "error";

    /** HTTP 请求标签 */
    public static final String TAG_HTTP_METHOD = "http.method";

    /** HTTP URL 标签 */
    public static final String TAG_HTTP_URL = "http.url";

    /** HTTP 状态码标签 */
    public static final String TAG_HTTP_STATUS = "http.status_code";

    private ObservabilityConstants() {
    }
}
