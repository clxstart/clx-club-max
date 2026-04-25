package com.clx.post.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体。
 */
@Data
public class Tag {

    /** 标签ID */
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签描述 */
    private String description;

    /** 标签颜色 */
    private String color;

    /** 是否删除 */
    private Boolean isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}