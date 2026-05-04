package com.clx.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 VO。
 */
@Data
public class OperLogVO {

    /** 日志ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 模块名称 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 请求URL */
    private String requestUrl;

    /** 请求方法 */
    private String requestMethod;

    /** 请求参数 */
    private String requestParams;

    /** 响应结果 */
    private String responseResult;

    /** 状态：0成功，1失败 */
    private String status;

    /** 错误信息 */
    private String errorMsg;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作IP */
    private String operIp;

    /** 操作时间 */
    private LocalDateTime operTime;
}
