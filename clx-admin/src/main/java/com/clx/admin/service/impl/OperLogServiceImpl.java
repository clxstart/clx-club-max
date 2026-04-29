package com.clx.admin.service.impl;

import com.alibaba.fastjson2.JSON;
import com.clx.admin.mapper.OperLogMapper;
import com.clx.admin.mapper.OperLogMapper.OperLogDTO;
import com.clx.admin.service.OperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl implements OperLogService {

    private final OperLogMapper operLogMapper;

    @Override
    public void log(String module, String action, String requestUrl, String requestMethod,
                    String requestParams, String responseResult, String status, String errorMsg,
                    Long costTime, Long userId, String username, String operIp) {
        OperLogDTO dto = new OperLogDTO();
        dto.setUserId(userId);
        dto.setUsername(username);
        dto.setModule(module);
        dto.setAction(action);
        dto.setMethod("");
        dto.setRequestUrl(requestUrl);
        dto.setRequestMethod(requestMethod);
        dto.setRequestParams(requestParams);
        dto.setResponseResult(responseResult);
        dto.setStatus(status);
        dto.setErrorMsg(errorMsg);
        dto.setCostTime(costTime);
        dto.setOperIp(operIp);
        operLogMapper.insert(dto);
    }

    @Async
    @Override
    public void logAsync(String module, String action, String requestUrl, String requestMethod,
                         String requestParams, String responseResult, String status, String errorMsg,
                         Long costTime, Long userId, String username, String operIp) {
        try {
            log(module, action, requestUrl, requestMethod, requestParams, responseResult,
                status, errorMsg, costTime, userId, username, operIp);
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage());
        }
    }
}