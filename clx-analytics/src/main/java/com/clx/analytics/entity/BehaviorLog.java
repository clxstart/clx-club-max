package com.clx.analytics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 行为日志实体
 */
@Data
@TableName("behavior_log")
public class BehaviorLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 行为类型: login/view_post/like_post/like_comment/comment/favorite/follow */
    private String behaviorType;

    /** 目标ID */
    private Long targetId;

    /** 目标类型: post/comment/user */
    private String targetType;

    /** 扩展信息(JSON) */
    private String extra;

    /** IP地址 */
    private String ip;

    /** 用户代理 */
    private String userAgent;

    /** 创建时间 */
    private LocalDateTime createTime;
}
