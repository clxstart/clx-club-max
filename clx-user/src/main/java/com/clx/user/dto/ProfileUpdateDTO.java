package com.clx.user.dto;

/**
 * 更新资料请求。
 */
public record ProfileUpdateDTO(
        String nickname,
        String avatar,
        String signature,
        String gender
) {}
