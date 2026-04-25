package com.clx.post.mapper;

import com.clx.post.entity.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子标签关联 Mapper。
 */
@Mapper
public interface PostTagMapper {

    /**
     * 批量插入帖子标签关联。
     */
    int batchInsert(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);

    /**
     * 删除帖子的所有标签关联。
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 查询帖子的标签ID列表。
     */
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);
}