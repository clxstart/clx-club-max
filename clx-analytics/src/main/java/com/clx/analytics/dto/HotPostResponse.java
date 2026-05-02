package com.clx.analytics.dto;

import lombok.Data;

/**
 * 热门帖子响应
 */
@Data
public class HotPostResponse {

    /** 帖子ID */
    private Long postId;

    /** 标题 */
    private String title;

    /** 作者名 */
    private String authorName;

    /** 浏览数 */
    private Long viewCount;

    /** 点赞数 */
    private Long likeCount;

    /** 评论数 */
    private Long commentCount;
}