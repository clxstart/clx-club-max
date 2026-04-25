package com.clx.quiz.mapper;

import com.clx.quiz.entity.WrongBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 错题本 Mapper。
 */
@Mapper
public interface WrongBookMapper {

    /**
     * 插入错题记录。
     */
    int insert(WrongBook wrongBook);

    /**
     * 更新错题记录（累加次数）。
     */
    int update(WrongBook wrongBook);

    /**
     * 查询用户某题的错题记录。
     */
    WrongBook selectByUserAndSubject(@Param("userId") Long userId, @Param("subjectId") Long subjectId);

    /**
     * 分页查询用户错题本。
     */
    List<WrongBook> selectPage(@Param("userId") Long userId,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /**
     * 统计用户错题数量。
     */
    int count(@Param("userId") Long userId);

    /**
     * 删除错题记录。
     */
    int deleteById(Long id);
}