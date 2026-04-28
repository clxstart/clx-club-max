package com.clx.api.user.dto;

import lombok.Data;
import java.util.List;

/**
 * 分页结果 DTO。
 */
@Data
public class PageResultDTO<T> {

    /** 数据列表 */
    private List<T> records;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer current;

    /** 每页条数 */
    private Integer size;
}