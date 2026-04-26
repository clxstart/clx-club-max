package com.clx.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 活跃用户排行视图。
 */
@Schema(description = "活跃用户排行")
public record ActiveUserVO(
        @Schema(description = "排名")
        Integer rank,
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "活跃度分数")
        Integer score
) {}
