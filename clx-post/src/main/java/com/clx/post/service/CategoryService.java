package com.clx.post.service;

import com.clx.post.vo.CategoryVO;

import java.util.List;

/**
 * 分类服务接口。
 */
public interface CategoryService {

    /**
     * 获取所有分类。
     */
    List<CategoryVO> getAll();
}