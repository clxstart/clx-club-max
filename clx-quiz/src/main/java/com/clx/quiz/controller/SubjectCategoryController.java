package com.clx.quiz.controller;

import com.clx.common.core.domain.R;
import com.clx.quiz.entity.SubjectCategory;
import com.clx.quiz.service.SubjectCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目分类控制器。
 */
@Tag(name = "题目分类", description = "题目分类管理接口")
@RestController
@RequestMapping("/quiz/category")
@RequiredArgsConstructor
public class SubjectCategoryController {

    private final SubjectCategoryService categoryService;

    /**
     * 获取分类列表。
     */
    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public R<List<SubjectCategory>> list() {
        return R.ok(categoryService.getAll());
    }

    /**
     * 获取分类详情。
     */
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public R<SubjectCategory> getById(@PathVariable Long id) {
        return R.ok(categoryService.getById(id));
    }

    /**
     * 新增分类。
     */
    @Operation(summary = "新增分类")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody SubjectCategory category) {
        return R.ok(categoryService.add(category));
    }

    /**
     * 更新分类。
     */
    @Operation(summary = "更新分类")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody SubjectCategory category) {
        return R.ok(categoryService.update(category));
    }

    /**
     * 删除分类。
     */
    @Operation(summary = "删除分类")
    @PostMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(categoryService.delete(id));
    }
}