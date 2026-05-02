package com.clx.post.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.post.dto.PostCreateRequest;
import com.clx.post.dto.PostListRequest;
import com.clx.post.dto.PostUpdateRequest;
import com.clx.post.service.PostService;
import com.clx.post.vo.PostDetailVO;
import com.clx.post.vo.PostListItemVO;
import com.clx.post.vo.PostListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子控制器 - 提供帖子的 CRUD、搜索、热门榜等接口。
 */
@Tag(name = "帖子管理", description = "帖子CRUD相关接口")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ========== 写操作（需登录） ==========

    /** 创建帖子 */
    @SaCheckLogin
    @Operation(summary = "创建帖子")
    @PostMapping("/create")
    public R<Long> create(@Valid @RequestBody PostCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();  // 获取当前登录用户
        Long postId = postService.create(request, userId);
        return R.ok(postId);
    }

    /** 更新帖子（仅作者可操作） */
    @SaCheckLogin
    @Operation(summary = "更新帖子")
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        postService.update(id, request, userId);
        return R.ok();
    }

    /** 删除帖子（仅作者可操作） */
    @SaCheckLogin
    @Operation(summary = "删除帖子")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        postService.delete(id, userId);
        return R.ok();
    }

    // ========== 读操作 ==========

    /** 获取帖子详情（登录用户返回是否点赞/收藏状态） */
    @Operation(summary = "获取帖子详情")
    @GetMapping("/{id}")
    public R<PostDetailVO> getDetail(@PathVariable Long id) {
        Long userId = null;
        if (StpUtil.isLogin()) {
            userId = StpUtil.getLoginIdAsLong();  // 未登录也能查看
        }
        PostDetailVO detail = postService.getDetail(id, userId);
        return R.ok(detail);
    }

    /** 获取帖子列表（支持分页、分类筛选） */
    @Operation(summary = "获取帖子列表")
    @GetMapping("/list")
    public R<PostListVO> getList(PostListRequest request) {
        PostListVO list = postService.getList(request);
        return R.ok(list);
    }

    /** 搜索帖子（按关键词） */
    @Operation(summary = "搜索帖子")
    @GetMapping("/search")
    public R<PostListVO> search(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "20") Integer size) {
        PostListVO result = postService.search(keyword, page, size);
        return R.ok(result);
    }

    /** 获取热门帖子（按点赞/浏览排序） */
    @Operation(summary = "获取热门帖子")
    @GetMapping("/hot")
    public R<List<PostListItemVO>> getHot(@RequestParam(defaultValue = "10") Integer limit) {
        List<PostListItemVO> posts = postService.getHot(limit);
        return R.ok(posts);
    }

    /** 按作者查询帖子（用户主页） */
    @Operation(summary = "按作者查询帖子")
    @GetMapping("/user/{userId}")
    public R<PostListVO> getByAuthor(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "20") Integer size) {
        PostListVO result = postService.getByAuthor(userId, page, size);
        return R.ok(result);
    }
}