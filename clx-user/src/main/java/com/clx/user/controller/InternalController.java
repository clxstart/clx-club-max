package com.clx.user.controller;

import com.clx.common.core.domain.R;
import com.clx.user.dto.LikeEventDTO;
import com.clx.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部接口控制器（供其他服务调用）。
 */
@Tag(name = "内部接口", description = "服务间调用的内部接口")
@RestController
@RequestMapping("/internal/user")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    /**
     * 增加获赞数。
     */
    @Operation(summary = "增加获赞数")
    @PostMapping("/like/incr")
    public R<Void> incrLikeCount(@RequestBody LikeEventDTO dto) {
        userService.incrLikeTotalCount(dto.userId(), dto.delta());
        return R.ok();
    }

    /**
     * 减少获赞数。
     */
    @Operation(summary = "减少获赞数")
    @PostMapping("/like/decr")
    public R<Void> decrLikeCount(@RequestBody LikeEventDTO dto) {
        userService.incrLikeTotalCount(dto.userId(), -dto.delta());
        return R.ok();
    }
}