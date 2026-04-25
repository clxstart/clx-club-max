package com.clx.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OAuth 平台枚举
 * <p>
 * 定义支持的第三方登录平台
 *
 * @author CLX
 */
@Getter
@RequiredArgsConstructor
public enum OAuthPlatform {

    /** GitHub */
    GITHUB("github", "GitHub"),

    /** 企业微信（待实现） */
    WECHAT("wechat", "企业微信"),

    /** 钉钉（待实现） */
    DINGTALK("dingtalk", "钉钉"),

    /** 飞书（待实现） */
    FEISHU("feishu", "飞书");

    /** 平台标识（用于 URL 和数据库存储） */
    private final String code;

    /** 平台显示名称 */
    private final String name;

    /**
     * 根据 code 获取平台枚举
     *
     * @param code 平台标识
     * @return 平台枚举，不存在返回 null
     */
    public static OAuthPlatform fromCode(String code) {
        for (OAuthPlatform platform : values()) {
            if (platform.getCode().equalsIgnoreCase(code)) {
                return platform;
            }
        }
        return null;
    }
}