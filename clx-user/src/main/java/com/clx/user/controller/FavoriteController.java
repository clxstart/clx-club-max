package com.clx.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.user.service.FavoriteService;
import com.clx.user.vo.FavoriteItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器。
 */
@Tag(name = "收藏管理", description = "帖子收藏相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * 收藏帖子。
     */
    @Operation(summary = "收藏帖子")
    @PostMapping("/favorite/{postId}")
    public R<Void> addFavorite(@PathVariable Long postId) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.addFavorite(userId, postId);
        return R.ok();
    }

    /**
     * 取消收藏。
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping("/favorite/{postId}")
    public R<Void> removeFavorite(@PathVariable Long postId) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        favoriteService.removeFavorite(userId, postId);
        return R.ok();
    }

    /**
     * 获取收藏夹。
     */
    @Operation(summary = "获取收藏夹")
    @GetMapping("/favorites")
    public R<Map<String, Object>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        List<FavoriteItemVO> list = favoriteService.getFavorites(userId, page, size);
        int total = favoriteService.countFavorites(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return R.ok(result);
    }
}