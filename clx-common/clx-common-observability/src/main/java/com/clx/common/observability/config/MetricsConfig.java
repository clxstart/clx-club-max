package com.clx.common.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Micrometer 指标配置。
 *
 * <p>配置自定义监控指标，如线程池、业务计数器等。
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class MetricsConfig {

    /**
     * 监控通用线程池（示例）。
     *
     * <p>自动注册线程池指标：活跃线程数、队列大小、完成任务数等。
     */
    @Bean
    public ExecutorService monitoredThreadPool(MeterRegistry registry) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 注册线程池监控指标
        registry.gauge("executor.pool.size", executor,
                e -> ((ThreadPoolExecutor) e).getPoolSize());
        registry.gauge("executor.active.count", executor,
                e -> ((ThreadPoolExecutor) e).getActiveCount());
        registry.gauge("executor.queue.size", executor,
                e -> ((ThreadPoolExecutor) e).getQueue().size());
        registry.gauge("executor.completed.count", executor,
                e -> ((ThreadPoolExecutor) e).getCompletedTaskCount());

        return executor;
    }
}