package com.clx.post.mapper;

import com.clx.post.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子 Mapper。
 */
@Mapper
public interface PostMapper {

    /**
     * 插入帖子。
     */
    int insert(Post post);

    /**
     * 根据ID查询帖子。
     */
    Post selectById(@Param("id") Long id);

    /**
     * 更新帖子。
     */
    int update(Post post);

    /**
     * 删除帖子（软删除）。
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询帖子列表。
     */
    List<Post> selectList(
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId,
            @Param("sort") String sort,
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    /**
     * 查询帖子总数。
     */
    int selectCount(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId);

    /**
     * 增加浏览数。
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞数。
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞数。
     */
    int decrementLikeCount(@Param("id") Long id);

    /**
     * 设置点赞数（校准用）。
     */
    int updateLikeCount(@Param("id") Long id, @Param("likeCount") int likeCount);

    /**
     * 增加评论数。
     */
    int incrementCommentCount(@Param("id") Long id);

    /**
     * 减少评论数。
     */
    int decrementCommentCount(@Param("id") Long id);

    /**
     * 关键词搜索帖子。
     */
    List<Post> search(@Param("keyword") String keyword,
                      @Param("offset") Integer offset,
                      @Param("size") Integer size);

    /**
     * 搜索帖子总数。
     */
    int searchCount(@Param("keyword") String keyword);

    /**
     * 查询热门帖子。
     */
    List<Post> selectHot(@Param("limit") Integer limit);

    /**
     * 按作者查询帖子。
     */
    List<Post> selectByAuthor(@Param("authorId") Long authorId,
                              @Param("offset") Integer offset,
                              @Param("size") Integer size);

    /**
     * 按作者统计帖子数。
     */
    int countByAuthor(@Param("authorId") Long authorId);
}