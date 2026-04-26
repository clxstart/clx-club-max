package com.clx.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.user.service.FollowService;
import com.clx.user.vo.UserSimpleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关注关系控制器。
 */
@Tag(name = "关注管理", description = "关注/粉丝相关接口")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 关注用户。
     */
    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    public R<Map<String, Integer>> follow(@PathVariable Long userId) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        int followCount = followService.follow(currentUserId, userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("followCount", followCount);
        return R.ok(result);
    }

    /**
     * 取消关注。
     */
    @Operation(summary = "取消关注")
    @DeleteMapping("/follow/{userId}")
    public R<Map<String, Integer>> unfollow(@PathVariable Long userId) {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long currentUserId = StpUtil.getLoginIdAsLong();
        int followCount = followService.unfollow(currentUserId, userId);
        Map<String, Integer> result = new HashMap<>();
        result.put("followCount", followCount);
        return R.ok(result);
    }

    /**
     * 获取关注列表。
     */
    @Operation(summary = "获取关注列表")
    @GetMapping("/{userId}/following")
    public R<Map<String, Object>> getFollowing(@PathVariable Long userId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        List<UserSimpleVO> list = followService.getFollowing(userId, page, size);
        int total = followService.countFollowing(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return R.ok(result);
    }

    /**
     * 获取粉丝列表。
     */
    @Operation(summary = "获取粉丝列表")
    @GetMapping("/{userId}/fans")
    public R<Map<String, Object>> getFans(@PathVariable Long userId,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        List<UserSimpleVO> list = followService.getFans(userId, page, size);
        int total = followService.countFans(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return R.ok(result);
    }
}