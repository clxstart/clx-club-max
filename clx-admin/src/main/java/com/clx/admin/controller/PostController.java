package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.clx.admin.annotation.OperLog;
import com.clx.admin.feign.PostFeignClient;
import com.clx.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 帖子管理控制器。
 */
@RestController
@RequestMapping("/admin/post")
@RequiredArgsConstructor
public class PostController {

    private final PostFeignClient postFeignClient;

    /** 分页查询帖子列表 */
    @OperLog(module = "帖子管理", action = "查询帖子列表")
    @SaCheckRole("admin")
    @PostMapping("/page")
    public R<Map<String, Object>> getPostPage(@RequestBody Map<String, Object> query) {
        return postFeignClient.getPostPage(query);
    }

    /** 获取帖子详情 */
    @OperLog(module = "帖子管理", action = "查看帖子详情")
    @SaCheckRole("admin")
    @GetMapping("/{postId}")
    public R<Map<String, Object>> getPostById(@PathVariable Long postId) {
        return postFeignClient.getPostById(postId);
    }

    /** 更新帖子状态 */
    @OperLog(module = "帖子管理", action = "更新帖子状态", recordParam = true)
    @SaCheckRole("admin")
    @PutMapping("/{postId}/status")
    public R<Void> updatePostStatus(@PathVariable Long postId, @RequestParam Integer status) {
        return postFeignClient.updatePostStatus(postId, status);
    }

    /** 删除帖子 */
    @OperLog(module = "帖子管理", action = "删除帖子")
    @SaCheckRole("admin")
    @DeleteMapping("/{postId}")
    public R<Void> deletePost(@PathVariable Long postId) {
        return postFeignClient.deletePost(postId);
    }
}