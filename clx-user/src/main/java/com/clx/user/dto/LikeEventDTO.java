package com.clx.user.dto;

/**
 * 点赞事件 DTO。
 */
public record LikeEventDTO(
        Long userId,
        int delta
) {}