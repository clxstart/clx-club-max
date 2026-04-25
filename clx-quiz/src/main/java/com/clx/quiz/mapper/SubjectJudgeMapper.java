package com.clx.quiz.mapper;

import com.clx.quiz.entity.SubjectJudge;
import org.apache.ibatis.annotations.Mapper;

/**
 * 判断题 Mapper。
 */
@Mapper
public interface SubjectJudgeMapper {

    /**
     * 插入判断题答案。
     */
    int insert(SubjectJudge judge);

    /**
     * 查询题目的答案。
     */
    SubjectJudge selectBySubjectId(Long subjectId);

    /**
     * 删除题目的答案。
     */
    int deleteBySubjectId(Long subjectId);
}