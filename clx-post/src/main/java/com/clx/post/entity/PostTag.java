package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子标签关联实体。
 */
@Data
public class PostTag {

    /** ID */
    private Long id;

    /** 帖子ID */
    private Long postId;

    /** 标签ID */
    private Long tagId;

    /** 创建时间 */
    private LocalDateTime createTime;
}