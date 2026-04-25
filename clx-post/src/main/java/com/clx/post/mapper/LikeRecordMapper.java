package com.clx.post.mapper;

import com.clx.post.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 点赞记录 Mapper。
 */
@Mapper
public interface LikeRecordMapper {

    /**
     * 插入点赞记录。
     */
    int insert(LikeRecord likeRecord);

    /**
     * 删除点赞记录。
     */
    int delete(@Param("userId") Long userId,
               @Param("targetType") String targetType,
               @Param("targetId") Long targetId);

    /**
     * 查询点赞记录。
     */
    LikeRecord select(@Param("userId") Long userId,
                      @Param("targetType") String targetType,
                      @Param("targetId") Long targetId);

    /**
     * 查询用户是否点赞。
     */
    boolean exists(@Param("userId") Long userId,
                   @Param("targetType") String targetType,
                   @Param("targetId") Long targetId);
}