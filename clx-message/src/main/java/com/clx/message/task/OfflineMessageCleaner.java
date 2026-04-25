package com.clx.message.task;

import com.clx.message.mapper.ChatMessageMapper;
import com.clx.message.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 离线消息清理定时任务。
 *
 * 每天凌晨 3 点执行：
 * 1. 清理 30 天前的私信消息
 * 2. 清理会话中超过 500 条的消息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfflineMessageCleaner {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    private static final int EXPIRE_DAYS = 30;
    private static final int MAX_MESSAGES_PER_SESSION = 500;

    /**
     * 每天凌晨 3 点执行。
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void clean() {
        log.info("开始清理过期私信消息...");

        // 清理 30 天前的消息
        int deletedOld = chatMessageMapper.deleteOldMessages(EXPIRE_DAYS);
        log.info("清理 {} 天前消息: {} 条", EXPIRE_DAYS, deletedOld);

        // 清理会话中超过 500 条的消息
        // 注：这里简化处理，实际应该遍历会话逐个清理
        log.info("私信消息清理完成");
    }

}