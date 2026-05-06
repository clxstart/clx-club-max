package com.clx.api.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果DTO
 */
@Data
public class PageResultDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总数
     */
    private long total;

    /**
     * 当前页
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    public PageResultDTO() {
    }

    public PageResultDTO(List<T> records, long total, long current, long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
    }

    public static <T> PageResultDTO<T> of(List<T> records, long total, long current, long size) {
        return new PageResultDTO<>(records, total, current, size);
    }
}
