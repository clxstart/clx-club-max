package com.clx.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户资料视图。
 */
@Schema(description = "用户资料")
public record UserProfileVO(
        @Schema(description = "用户ID")
        Long userId,
        @Schema(description = "用户名")
        String username,
        @Schema(description = "昵称")
        String nickname,
        @Schema(description = "头像URL")
        String avatar,
        @Schema(description = "个性签名")
        String signature,
        @Schema(description = "性别")
        String gender,
        @Schema(description = "关注数")
        Integer followCount,
        @Schema(description = "粉丝数")
        Integer fansCount,
        @Schema(description = "获赞总数")
        Integer likeTotalCount,
        @Schema(description = "当前用户是否已关注")
        Boolean isFollowed
) {}
