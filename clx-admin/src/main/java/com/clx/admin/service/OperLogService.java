package com.clx.admin.service;

import com.clx.admin.mapper.OperLogMapper;

/**
 * 操作日志服务接口。
 */
public interface OperLogService {

    /**
     * 记录操作日志。
     */
    void log(String module, String action, String requestUrl, String requestMethod,
             String requestParams, String responseResult, String status, String errorMsg,
             Long costTime, Long userId, String username, String operIp);

    /**
     * 异步记录操作日志。
     */
    void logAsync(String module, String action, String requestUrl, String requestMethod,
                  String requestParams, String responseResult, String status, String errorMsg,
                  Long costTime, Long userId, String username, String operIp);
}