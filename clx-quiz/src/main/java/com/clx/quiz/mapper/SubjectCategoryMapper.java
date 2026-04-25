package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题目分类 Mapper。
 */
@Mapper
public interface SubjectCategoryMapper {

    /**
     * 查询所有分类（树形结构）。
     */
    List<SubjectCategory> selectAll();

    /**
     * 根据id查询分类。
     */
    SubjectCategory selectById(Long id);

    /**
     * 新增分类。
     */
    int insert(SubjectCategory category);

    /**
     * 更新分类。
     */
    int update(SubjectCategory category);

    /**
     * 删除分类（软删除）。
     */
    int deleteById(Long id);
}