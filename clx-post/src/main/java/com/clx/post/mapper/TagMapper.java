package com.clx.post.mapper;

import com.clx.post.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签 Mapper。
 */
@Mapper
public interface TagMapper {

    /**
     * 查询所有标签。
     */
    List<Tag> selectAll();

    /**
     * 根据ID查询标签。
     */
    Tag selectById(@Param("id") Long id);

    /**
     * 根据帖子ID查询标签列表。
     */
    List<Tag> selectByPostId(@Param("postId") Long postId);
}