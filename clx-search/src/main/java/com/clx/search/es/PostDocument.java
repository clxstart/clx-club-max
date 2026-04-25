package com.clx.search.es;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ES 帖子文档。
 */
@Data
public class PostDocument {

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

    /** 作者名称 */
    private String authorName;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    private String categoryName;

    /** 标签列表 */
    private List<TagInfo> tags;

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

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /**
     * 标签信息。
     */
    @Data
    public static class TagInfo {
        private Long id;
        private String name;
    }
}