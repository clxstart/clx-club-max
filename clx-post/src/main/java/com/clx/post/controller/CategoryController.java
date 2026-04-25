package com.clx.post.controller;

import com.clx.common.core.domain.R;
import com.clx.post.service.CategoryService;
import com.clx.post.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器。
 */
@Tag(name = "分类管理", description = "分类相关接口")
@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表。
     */
    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public R<List<CategoryVO>> list() {
        return R.ok(categoryService.getAll());
    }
}