package com.clx.search.es;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * ES 分类文档。
 */
@Data
public class CategoryDocument {

    /** 分类ID */
    private Long id;

    /** 名称 */
    private String name;

    /** 编码 */
    private String code;

    /** 描述 */
    private String description;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createTime;
}