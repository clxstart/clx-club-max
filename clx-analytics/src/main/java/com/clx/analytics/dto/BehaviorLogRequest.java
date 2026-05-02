package com.clx.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 行为日志创建请求
 */
@Data
public class BehaviorLogRequest {

    /** 用户ID */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /** 行为类型 */
    @NotBlank(message = "行为类型不能为空")
    private String behaviorType;

    /** 目标ID */
    private Long targetId;

    /** 目标类型 */
    private String targetType;

    /** 扩展信息(JSON) */
    private String extra;
}
