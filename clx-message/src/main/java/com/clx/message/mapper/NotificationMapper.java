package com.clx.message.mapper;

import com.clx.message.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知 Mapper。
 */
@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    Notification selectById(@Param("id") Long id);

    /**
     * 根据聚合键查询（用于聚合检查）。
     */
    Notification selectByAggregateKey(@Param("aggregateKey") String aggregateKey);

    /**
     * 更新聚合数量。
     */
    int updateAggregateCount(@Param("id") Long id, @Param("count") int count);

    /**
     * 查询用户通知列表。
     */
    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("type") String type,
                                       @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户通知数。
     */
    int countByUserId(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 标记单条已读。
     */
    int markRead(@Param("id") Long id);

    /**
     * 按类型标记全部已读。
     */
    int markAllRead(@Param("userId") Long userId, @Param("type") String type);

    /**
     * 统计未读数（按类型分组）。
     */
    List<java.util.Map<String, Object>> countUnreadByType(@Param("userId") Long userId);

}