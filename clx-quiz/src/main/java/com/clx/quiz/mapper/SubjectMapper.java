package com.clx.quiz.mapper;

import com.clx.quiz.entity.Subject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目 Mapper。
 */
@Mapper
public interface SubjectMapper {

    /**
     * 插入题目。
     */
    int insert(Subject subject);

    /**
     * 更新题目。
     */
    int update(Subject subject);

    /**
     * 删除题目（软删除）。
     */
    int deleteById(Long id);

    /**
     * 根据id查询题目。
     */
    Subject selectById(Long id);

    /**
     * 分页查询题目。
     */
    List<Subject> selectPage(@Param("categoryId") Long categoryId,
                             @Param("labelId") Long labelId,
                             @Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    /**
     * 统计题目数量。
     */
    int count(@Param("categoryId") Long categoryId,
              @Param("labelId") Long labelId,
              @Param("keyword") String keyword);

    /**
     * 根据标签随机查询题目id。
     */
    List<Long> selectRandomIdsByLabels(@Param("labelIds") List<Long> labelIds,
                                       @Param("limit") int limit);
}