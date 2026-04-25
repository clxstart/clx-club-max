package com.clx.search.es;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ES 标签文档。
 */
@Data
public class TagDocument {

    /** 标签ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 描述 */
    private String description;

    /** 颜色 */
    private String color;

    /** 创建时间 */
    private LocalDateTime createTime;
}