package com.clx.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通知触发请求（内部调用）。
 */
@Data
public class NotificationTriggerRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "通知类型不能为空")
    private String type;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    private String content;

    private Long sourceId;

    private String sourceType;

}