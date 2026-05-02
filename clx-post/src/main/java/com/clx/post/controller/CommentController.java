package com.clx.post.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.post.dto.CommentCreateRequest;
import com.clx.post.service.CommentService;
import com.clx.post.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器 - 提供评论的创建、删除、列表查询接口。
 */
@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/post/{postId}/comment")  // 嵌套资源：评论属于帖子
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 发表评论（支持回复，通过 parentId 指定父评论） */
    @SaCheckLogin
    @Operation(summary = "创建评论")
    @PostMapping
    public R<Long> create(@PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long commentId = commentService.create(postId, request, userId);
        return R.ok(commentId);
    }

    /** 删除评论（仅作者可操作） */
    @SaCheckLogin
    @Operation(summary = "删除评论")
    @DeleteMapping("/{commentId}")
    public R<Void> delete(@PathVariable Long postId, @PathVariable Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        commentService.delete(commentId, userId);
        return R.ok();
    }

    /** 获取评论列表（树形结构，登录用户返回归属状态） */
    @Operation(summary = "获取评论列表")
    @GetMapping("s")  // 完整路径: /post/{postId}/comments
    public R<List<CommentVO>> list(@PathVariable Long postId) {
        Long userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();
        }
        List<CommentVO> comments = commentService.getList(postId, userId);
        return R.ok(comments);
    }
}