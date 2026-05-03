package com.clx.common.observability.config;

import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 链路追踪配置。
 *
 * <p>基于 Micrometer Tracing，支持 Zipkin/Jaeger 等追踪系统。
 * 自动集成 Spring MVC、Feign、RestTemplate 等组件。
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingConfig {

    /**
     * 当前微服务名称，用于标识链路来源。
     * 需在配置文件中设置：spring.application.name=clx-auth
     */
    public static final String SERVICE_NAME_KEY = "spring.application.name";

}
