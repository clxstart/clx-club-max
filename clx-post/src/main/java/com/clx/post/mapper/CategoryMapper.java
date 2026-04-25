package com.clx.post.mapper;

import com.clx.post.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类 Mapper。
 */
@Mapper
public interface CategoryMapper {

    /**
     * 查询所有分类。
     */
    List<Category> selectAll();

    /**
     * 根据ID查询分类。
     */
    Category selectById(@Param("id") Long id);

    /**
     * 根据编码查询分类。
     */
    Category selectByCode(@Param("code") String code);
}