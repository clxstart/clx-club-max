package com.clx.message.mapper;

import com.clx.message.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息 Mapper。
 */
@Mapper
public interface ChatMessageMapper {

    /**
     * 插入消息。
     */
    int insert(ChatMessage message);

    /**
     * 根据ID查询。
     */
    ChatMessage selectById(@Param("id") Long id);

    /**
     * 查询会话消息列表（游标分页）。
     */
    List<ChatMessage> selectBySessionId(@Param("sessionId") Long sessionId, @Param("cursor") Long cursor, @Param("limit") int limit);

    /**
     * 统计会话消息数。
     */
    int countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 删除会话中超过N条的最旧消息。
     */
    int deleteOldest(@Param("sessionId") Long sessionId, @Param("keepCount") int keepCount);

    /**
     * 删除30天前的消息。
     */
    int deleteOldMessages(@Param("days") int days);

}