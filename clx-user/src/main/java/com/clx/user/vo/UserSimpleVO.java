package com.clx.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户简要视图（用于关注/粉丝列表）。
 */
@Schema(description = "用户简要信息")
public record UserSimpleVO(
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "昵称")
        String nickname,
        @Schema(description = "头像URL")
        String avatar,
        @Schema(description = "个性签名")
        String signature
) {}
