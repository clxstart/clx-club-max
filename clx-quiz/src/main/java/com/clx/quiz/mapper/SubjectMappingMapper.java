package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectMapping;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目-分类-标签关联 Mapper。
 */
@Mapper
public interface SubjectMappingMapper {

    /**
     * 批量插入关联。
     */
    int batchInsert(@Param("list") List<SubjectMapping> list);

    /**
     * 查询题目的分类标签。
     */
    List<SubjectMapping> selectBySubjectId(Long subjectId);

    /**
     * 删除题目的关联。
     */
    int deleteBySubjectId(Long subjectId);
}