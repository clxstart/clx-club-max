package com.clx.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

// 记住我功能配置类
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "clx.auth.remember-me")
public class RememberMeProperties {

    // 绝对有效期，，token最大存活时间
    private long timeout = TimeUnit.DAYS.toSeconds(30);

    // 活跃有效期，无操作多久后过期
    private long activeTimeout = TimeUnit.DAYS.toSeconds(7);
}