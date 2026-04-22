package com.clx.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 社交账号绑定信息VO
 *
 * @param id           绑定ID
 * @param socialType   平台类型
 * @param socialName   平台昵称
 * @param socialAvatar 平台头像
 * @param bindTime     绑定时间
 */
@Schema(description = "社交账号绑定信息")
public record SocialBindVO(
        @Schema(description = "绑定ID")
        Long id,
        @Schema(description = "平台类型：github/qq/wechat")
        String socialType,
        @Schema(description = "平台昵称")
        String socialName,
        @Schema(description = "平台头像URL")
        String socialAvatar,
        @Schema(description = "绑定时间")
        String bindTime
) {}