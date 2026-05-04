package com.clx.admin.dto;

import lombok.Data;

/**
 * 日志查询条件。
 */
@Data
public class LogQueryDTO {

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 模块名称 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 操作用户名 */
    private String username;

    /** 状态：0成功，1失败 */
    private String status;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;
}
