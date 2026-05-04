package com.clx.post.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.clx.common.core.domain.R;
import com.clx.post.entity.Comment;
import com.clx.post.entity.Post;
import com.clx.post.service.CommentService;
import com.clx.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 内部 API 控制器 - 供后台管理调用。
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final PostService postService;
    private final CommentService commentService;

    // ========== 帖子管理 ==========

    /** 分页查询帖子列表 */
    @PostMapping("/post/page")
    public R<Map<String, Object>> getPostPage(@RequestBody Map<String, Object> query) {
        int pageNo = query.get("pageNo") != null ? (int) query.get("pageNo") : 1;
        int pageSize = query.get("pageSize") != null ? (int) query.get("pageSize") : 20;
        String title = query.get("title") != null ? (String) query.get("title") : null;
        Integer status = query.get("status") != null ? (Integer) query.get("status") : null;
        Long authorId = query.get("authorId") != null ? Long.valueOf(query.get("authorId").toString()) : null;

        LambdaQueryWrapper<Post> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Post::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(Post::getStatus, status);
        }
        if (authorId != null) {
            wrapper.eq(Post::getAuthorId, authorId);
        }
        wrapper.orderByDesc(Post::getCreatedAt);

        IPage<Post> page = postService.page(new Page<>(pageNo, pageSize), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("list", page.getRecords());
        return R.ok(result);
    }

    /** 获取帖子详情 */
    @GetMapping("/post/{postId}")
    public R<Map<String, Object>> getPostById(@PathVariable Long postId) {
        Post post = postService.getById(postId);
        if (post == null) {
            return R.fail(404, "帖子不存在");
        }
        return R.ok(convertPostToMap(post));
    }

    /** 更新帖子状态 */
    @PutMapping("/post/{postId}/status")
    public R<Void> updatePostStatus(@PathVariable Long postId, @RequestParam Integer status) {
        Post post = postService.getById(postId);
        if (post == null) {
            return R.fail(404, "帖子不存在");
        }
        post.setStatus(status);
        postService.updateById(post);
        return R.ok();
    }

    /** 删除帖子 */
    @DeleteMapping("/post/{postId}")
    public R<Void> deletePost(@PathVariable Long postId) {
        postService.removeById(postId);
        return R.ok();
    }

    // ========== 评论管理 ==========

    /** 分页查询评论列表 */
    @PostMapping("/comment/page")
    public R<Map<String, Object>> getCommentPage(@RequestBody Map<String, Object> query) {
        int pageNo = query.get("pageNo") != null ? (int) query.get("pageNo") : 1;
        int pageSize = query.get("pageSize") != null ? (int) query.get("pageSize") : 20;
        String content = query.get("content") != null ? (String) query.get("content") : null;
        Long postId = query.get("postId") != null ? Long.valueOf(query.get("postId").toString()) : null;

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (content != null && !content.isEmpty()) {
            wrapper.like(Comment::getContent, content);
        }
        if (postId != null) {
            wrapper.eq(Comment::getPostId, postId);
        }
        wrapper.orderByDesc(Comment::getCreatedAt);

        IPage<Comment> page = commentService.page(new Page<>(pageNo, pageSize), wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("total", page.getTotal());
        result.put("list", page.getRecords());
        return R.ok(result);
    }

    /** 删除评论 */
    @DeleteMapping("/comment/{commentId}")
    public R<Void> deleteComment(@PathVariable Long commentId) {
        commentService.removeById(commentId);
        return R.ok();
    }

    private Map<String, Object> convertPostToMap(Post post) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", post.getId());
        map.put("title", post.getTitle());
        map.put("content", post.getContent());
        map.put("authorId", post.getAuthorId());
        map.put("categoryId", post.getCategoryId());
        map.put("status", post.getStatus());
        map.put("viewCount", post.getViewCount());
        map.put("likeCount", post.getLikeCount());
        map.put("commentCount", post.getCommentCount());
        map.put("createdAt", post.getCreatedAt());
        return map;
    }
}