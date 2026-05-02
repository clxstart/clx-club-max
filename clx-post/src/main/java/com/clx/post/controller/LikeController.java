package com.clx.post.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.post.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 点赞控制器 - 提供帖子、评论的点赞/取消点赞接口。
 *
 * 特点：Redis 计数 + MQ 异步写 DB，快速响应。
 */
@Tag(name = "点赞管理", description = "点赞相关接口")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    // ========== 帖子点赞 ==========

    /** 点赞帖子（Redis +1，发 MQ 异步写 DB） */
    @Operation(summary = "点赞帖子")
    @PostMapping("/post/{id}/like")
    public R<Map<String, Integer>> likePost(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int likeCount = likeService.likePost(id, userId);
        return R.ok(Map.of("likeCount", likeCount));
    }

    /** 取消点赞帖子（同步写 DB，确保数据一致性） */
    @Operation(summary = "取消点赞帖子")
    @DeleteMapping("/post/{id}/like")
    public R<Map<String, Integer>> unlikePost(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int likeCount = likeService.unlikePost(id, userId);
        return R.ok(Map.of("likeCount", likeCount));
    }

    // ========== 评论点赞 ==========

    /** 点赞评论 */
    @Operation(summary = "点赞评论")
    @PostMapping("/comment/{id}/like")
    public R<Map<String, Integer>> likeComment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int likeCount = likeService.likeComment(id, userId);
        return R.ok(Map.of("likeCount", likeCount));
    }

    /** 取消点赞评论 */
    @Operation(summary = "取消点赞评论")
    @DeleteMapping("/comment/{id}/like")
    public R<Map<String, Integer>> unlikeComment(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        int likeCount = likeService.unlikeComment(id, userId);
        return R.ok(Map.of("likeCount", likeCount));
    }
}