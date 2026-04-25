package com.clx.quiz.mapper;

import com.clx.quiz.entity.Practice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 练习记录 Mapper。
 */
@Mapper
public interface PracticeMapper {

    /**
     * 插入练习记录。
     */
    int insert(Practice practice);

    /**
     * 更新练习记录。
     */
    int update(Practice practice);

    /**
     * 根据id查询。
     */
    Practice selectById(Long id);
}