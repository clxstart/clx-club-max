package com.clx.message.mapper;

import com.clx.message.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会话 Mapper。
 */
@Mapper
public interface ChatSessionMapper {

    /**
     * 插入会话。
     */
    int insert(ChatSession session);

    /**
     * 根据用户对查询会话。
     */
    ChatSession selectByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    /**
     * 根据ID查询。
     */
    ChatSession selectById(@Param("id") Long id);

    /**
     * 更新最后消息。
     */
    int updateLastMessage(@Param("id") Long id, @Param("lastMessage") String lastMessage);

    /**
     * 增加未读数。
     */
    int incrementUnread(@Param("id") Long id, @Param("isUser1") boolean isUser1);

    /**
     * 清零未读数。
     */
    int clearUnread(@Param("id") Long id, @Param("isUser1") boolean isUser1);

    /**
     * 查询用户的会话列表。
     */
    java.util.List<ChatSession> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户会话数。
     */
    int countByUserId(@Param("userId") Long userId);

}