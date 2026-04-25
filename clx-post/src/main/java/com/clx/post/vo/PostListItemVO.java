package com.clx.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子列表项VO。
 */
@Data
public class PostListItemVO {

    /** 帖子ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 作者信息 */
    private AuthorVO author;

    /** 分类 */
    private CategoryVO category;

    /** 标签列表 */
    private List<TagVO> tags;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /**
     * 作者信息。
     */
    @Data
    public static class AuthorVO {
        private Long id;
        private String name;
        private String avatar;
    }

    /**
     * 分类信息。
     */
    @Data
    public static class CategoryVO {
        private Long id;
        private String name;
    }

    /**
     * 标签信息。
     */
    @Data
    public static class TagVO {
        private Long id;
        private String name;
        private String color;
    }
}