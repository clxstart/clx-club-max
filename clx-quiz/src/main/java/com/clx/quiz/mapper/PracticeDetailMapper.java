package com.clx.quiz.mapper;

import com.clx.quiz.entity.PracticeDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 练习详情 Mapper。
 */
@Mapper
public interface PracticeDetailMapper {

    /**
     * 批量插入练习详情。
     */
    int batchInsert(@Param("list") List<PracticeDetail> list);

    /**
     * 更新练习详情（提交答案）。
     */
    int update(PracticeDetail detail);

    /**
     * 查询练习的所有详情。
     */
    List<PracticeDetail> selectByPracticeId(Long practiceId);

    /**
     * 查询某题的练习详情。
     */
    PracticeDetail selectByPracticeIdAndSubjectId(@Param("practiceId") Long practiceId,
                                                   @Param("subjectId") Long subjectId);
}