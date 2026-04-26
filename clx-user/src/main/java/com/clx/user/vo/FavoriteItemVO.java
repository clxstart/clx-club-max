package com.clx.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 收藏项视图。
 */
@Schema(description = "收藏项")
public record FavoriteItemVO(
        @Schema(description = "帖子ID")
        Long postId,
        @Schema(description = "标题")
        String title,
        @Schema(description = "摘要")
        String summary,
        @Schema(description = "作者名称")
        String authorName,
        @Schema(description = "点赞数")
        Integer likeCount,
        @Schema(description = "帖子创建时间")
        LocalDateTime createdAt,
        @Schema(description = "收藏时间")
        LocalDateTime favoritedAt
) {}