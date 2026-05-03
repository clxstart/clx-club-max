package com.clx.common.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 雪花算法 ID 生成器
 *
 * <p>生成 64 位 Long 类型唯一 ID，结构：
 * <pre>
 * 0 - 41位时间戳 - 10位机器ID - 12位序列号
 * </pre>
 *
 * @author CLX
 * @since 2026-04-25
 */
public class SnowflakeIdWorker {

    private static final Logger log = LoggerFactory.getLogger(SnowflakeIdWorker.class);

    /** 起始时间戳 (2026-01-01) */
    private static final long TW_EPOCH = 1735689600000L;

    /** 机器 ID 占用位数 */
    private static final long WORKER_ID_BITS = 5L;

    /** 数据中心 ID 占用位数 */
    private static final long DATACENTER_ID_BITS = 5L;

    /** 序列号占用位数 */
    private static final long SEQUENCE_BITS = 12L;

    /** 机器 ID 最大值 */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /** 数据中心 ID 最大值 */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 序列号掩码 */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /** 机器 ID 左移位数 */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /** 数据中心 ID 左移位数 */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /** 时间戳左移位数 */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    /** 时钟回拨容忍阈值（毫秒）- 超过此阈值抛出异常 */
    private static final long CLOCK_BACKWARDS_TOLERANCE = 5L;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数
     *
     * @param workerId     机器 ID (0-31)
     * @param datacenterId 数据中心 ID (0-31)
     */
    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId must be between 0 and 31");
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId must be between 0 and 31");
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 生成下一个 ID（线程安全）
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            long offset = lastTimestamp - timestamp;

            // 小幅回拨，等待追上
            if (offset <= CLOCK_BACKWARDS_TOLERANCE) {
                log.warn("时钟小幅回拨 {}ms，等待恢复", offset);
                try {
                    Thread.sleep(offset);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during clock backwards wait", e);
                }
                timestamp = System.currentTimeMillis();
                if (timestamp < lastTimestamp) {
                    // 等待后仍然回拨
                    throw new RuntimeException("Clock moved backwards, refusing to generate id");
                }
            } else {
                // 大幅回拨，拒绝生成
                log.error("时钟大幅回拨 {}ms，拒绝生成ID", offset);
                throw new RuntimeException("Clock moved backwards " + offset + "ms, refusing to generate id");
            }
        }

        // 同一毫秒内
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - TW_EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /** 阻塞到下一毫秒 */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /** 默认实例（workerId=0, datacenterId=0） */
    private static final SnowflakeIdWorker DEFAULT = new SnowflakeIdWorker(0, 0);

    /** 获取默认实例 */
    public static SnowflakeIdWorker getDefault() {
        return DEFAULT;
    }

    /** 快捷方法：生成 ID */
    public static long genId() {
        return DEFAULT.nextId();
    }
}
