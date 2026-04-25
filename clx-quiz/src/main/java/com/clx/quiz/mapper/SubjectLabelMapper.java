package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目标签 Mapper。
 */
@Mapper
public interface SubjectLabelMapper {

    /**
     * 查询所有标签。
     */
    List<SubjectLabel> selectAll();

    /**
     * 查询某分类下的标签。
     */
    List<SubjectLabel> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据id查询标签。
     */
    SubjectLabel selectById(Long id);

    /**
     * 新增标签。
     */
    int insert(SubjectLabel label);

    /**
     * 更新标签。
     */
    int update(SubjectLabel label);

    /**
     * 删除标签（软删除）。
     */
    int deleteById(Long id);
}