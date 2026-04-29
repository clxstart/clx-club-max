package com.clx.admin.mapper;

import com.clx.common.core.domain.LoginUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface OperLogMapper {

    /**
     * 插入操作日志。
     */
    int insert(OperLogDTO log);

    /**
     * 操作日志 DTO。
     */
    @lombok.Data
    class OperLogDTO {
        private Long id;
        private Long userId;
        private String username;
        private String module;
        private String action;
        private String method;
        private String requestUrl;
        private String requestMethod;
        private String requestParams;
        private String responseResult;
        private String status;
        private String errorMsg;
        private Long costTime;
        private String operIp;
    }
}