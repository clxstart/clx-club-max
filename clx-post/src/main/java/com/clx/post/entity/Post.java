package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子实体。
 */
@Data
public class Post {

    /** 帖子ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 摘要 */
    private String summary;

    /** 作者ID */
    private Long authorId;

    /** 作者名称（冗余） */
    private String authorName;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（冗余） */
    private String categoryName;

    /** 浏览数 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 是否置顶 */
    private Boolean isTop;

    /** 是否精华 */
    private Boolean isEssence;

    /** 状态:0正常,1禁用,2草稿 */
    private String status;

    /** 是否删除 */
    private Boolean isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}