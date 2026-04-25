package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类实体。
 */
@Data
public class Category {

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

    /** 排序 */
    private Integer sort;

    /** 状态:0正常,1禁用 */
    private String status;

    /** 是否删除 */
    private Boolean isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}