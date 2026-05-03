package com.clx.common.observability.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Actuator 端点 - 应用运行信息。
 *
 * <p>访问路径：/actuator/app-info
 *
 * <p>需要在配置文件中暴露端点：
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: app-info,health,info
 * </pre>
 */
@Component
@Endpoint(id = "app-info")
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
public class AppInfoEndpoint {

    /**
     * 获取应用运行信息。
     */
    @ReadOperation
    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();

        // JVM 内存信息
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heap_used", memoryMXBean.getHeapMemoryUsage().getUsed() / 1024 / 1024 + " MB");
        memory.put("heap_max", memoryMXBean.getHeapMemoryUsage().getMax() / 1024 / 1024 + " MB");
        memory.put("non_heap_used", memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1024 / 1024 + " MB");
        info.put("memory", memory);

        // 线程信息
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", threadMXBean.getThreadCount());
        threads.put("peak", threadMXBean.getPeakThreadCount());
        threads.put("daemon", threadMXBean.getDaemonThreadCount());
        info.put("threads", threads);

        // 运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        info.put("uptime_ms", uptime);
        info.put("uptime_human", formatUptime(uptime));

        return info;
    }

    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        }
        if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        }
        return seconds + "s";
    }
}