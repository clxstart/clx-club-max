package com.clx.post.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情VO。
 */
@Data
public class PostDetailVO {

    /** 帖子ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

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

    /** 浏览数 */
    private Integer viewCount;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

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