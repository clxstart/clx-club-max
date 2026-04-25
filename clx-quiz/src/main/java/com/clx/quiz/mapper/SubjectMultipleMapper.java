package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectMultiple;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 多选题 Mapper。
 */
@Mapper
public interface SubjectMultipleMapper {

    /**
     * 批量插入选项。
     */
    int batchInsert(@Param("list") List<SubjectMultiple> list);

    /**
     * 查询题目的选项。
     */
    List<SubjectMultiple> selectBySubjectId(Long subjectId);

    /**
     * 删除题目的选项。
     */
    int deleteBySubjectId(Long subjectId);
}