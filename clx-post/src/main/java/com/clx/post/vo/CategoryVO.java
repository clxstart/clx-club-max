package com.clx.post.vo;

import lombok.Data;

/**
 * 分类VO。
 */
@Data
public class CategoryVO {

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类编码 */
    private String code;

    /** 分类描述 */
    private String description;

    /** 分类图标 */
    private String icon;

    /** 帖子数量 */
    private Integer postCount;
}