package com.clx.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 关注关系实体。
 */
@Data
public class UserFollow {

    /** 主键ID */
    private Long id;

    /** 关注者ID */
    private Long userId;

    /** 被关注者ID */
    private Long targetId;

    /** 创建时间 */
    private LocalDateTime createTime;
}