package com.clx.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子收藏实体。
 */
@Data
public class PostFavorite {

    /** 主键ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 帖子ID */
    private Long postId;

    /** 创建时间 */
    private LocalDateTime createTime;
}