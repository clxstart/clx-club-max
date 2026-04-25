package com.clx.post.vo;

import lombok.Data;

/**
 * 标签VO。
 */
@Data
public class TagVO {

    /** 标签ID */
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签描述 */
    private String description;

    /** 标签颜色 */
    private String color;

    /** 帖子数量 */
    private Integer postCount;
}