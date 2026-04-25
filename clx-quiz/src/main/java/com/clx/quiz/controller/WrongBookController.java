package com.clx.quiz.controller;

import com.clx.common.core.domain.R;
import com.clx.quiz.service.WrongBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 错题本控制器。
 */
@Tag(name = "错题本", description = "错题本管理接口")
@RestController
@RequestMapping("/quiz/wrong-book")
@RequiredArgsConstructor
public class WrongBookController {

    private final WrongBookService wrongBookService;

    /**
     * 查询错题本。
     */
    @Operation(summary = "查询错题本")
    @PostMapping("/list")
    public R<Map<String, Object>> list(@RequestBody Map<String, Integer> params) {
        Long userId = 1L; // TODO: 从登录上下文获取
        int pageNo = params.getOrDefault("pageNo", 1);
        int pageSize = params.getOrDefault("pageSize", 10);
        return R.ok(wrongBookService.list(userId, pageNo, pageSize));
    }

    /**
     * 移除错题。
     */
    @Operation(summary = "移除错题")
    @PostMapping("/remove")
    public R<Boolean> remove(@RequestBody Map<String, Long> params) {
        Long userId = 1L; // TODO: 从登录上下文获取
        Long subjectId = params.get("subjectId");
        return R.ok(wrongBookService.remove(userId, subjectId));
    }
}