package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectRadio;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 单选题 Mapper。
 */
@Mapper
public interface SubjectRadioMapper {

    /**
     * 批量插入选项。
     */
    int batchInsert(@Param("list") List<SubjectRadio> list);

    /**
     * 查询题目的选项。
     */
    List<SubjectRadio> selectBySubjectId(Long subjectId);

    /**
     * 删除题目的选项。
     */
    int deleteBySubjectId(Long subjectId);
}