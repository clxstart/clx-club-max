package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.clx.admin.annotation.OperLog;
import com.clx.admin.feign.CommentFeignClient;
import com.clx.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论管理控制器。
 */
@RestController
@RequestMapping("/admin/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentFeignClient commentFeignClient;

    /** 分页查询评论列表 */
    @OperLog(module = "评论管理", action = "查询评论列表")
    @SaCheckRole("admin")
    @PostMapping("/page")
    public R<Map<String, Object>> getCommentPage(@RequestBody Map<String, Object> query) {
        return commentFeignClient.getCommentPage(query);
    }

    /** 删除评论 */
    @OperLog(module = "评论管理", action = "删除评论")
    @SaCheckRole("admin")
    @DeleteMapping("/{commentId}")
    public R<Void> deleteComment(@PathVariable Long commentId) {
        return commentFeignClient.deleteComment(commentId);
    }
}