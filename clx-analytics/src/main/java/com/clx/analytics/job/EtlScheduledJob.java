package com.clx.analytics.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ETL 定时任务
 * 每日凌晨 02:00 执行数据同步
 * 每日凌晨 03:00 执行 Hive ETL
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EtlScheduledJob {

    /**
     * 每日凌晨 02:00 执行数据同步
     * Cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void exportBehaviorLog() {
        log.info("===== 开始执行数据同步 =====");
        // 实际部署时调用 export_behavior.sh 脚本
        // 这里仅作占位，生产环境需要通过 ProcessBuilder 或 SSH 调用远程脚本
        log.info("数据同步任务已触发，请确保 export_behavior.sh 脚本已配置");
    }

    /**
     * 每日凌晨 03:00 执行 Hive ETL
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void runHiveEtl() {
        log.info("===== 开始执行 Hive ETL =====");
        // 实际部署时调用 run_etl.sh 脚本
        log.info("Hive ETL 任务已触发，请确保 run_etl.sh 脚本已配置");
    }
}
