package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectBrief;
import org.apache.ibatis.annotations.Mapper;

/**
 * 简答题 Mapper。
 */
@Mapper
public interface SubjectBriefMapper {

    /**
     * 插入简答题答案。
     */
    int insert(SubjectBrief brief);

    /**
     * 查询题目的答案。
     */
    SubjectBrief selectBySubjectId(Long subjectId);

    /**
     * 删除题目的答案。
     */
    int deleteBySubjectId(Long subjectId);
}