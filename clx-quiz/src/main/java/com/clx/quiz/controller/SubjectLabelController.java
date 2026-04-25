package com.clx.quiz.controller;

import com.clx.common.core.domain.R;
import com.clx.quiz.entity.SubjectLabel;
import com.clx.quiz.service.SubjectLabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目标签控制器。
 */
@Tag(name = "题目标签", description = "题目标签管理接口")
@RestController
@RequestMapping("/quiz/label")
@RequiredArgsConstructor
public class SubjectLabelController {

    private final SubjectLabelService labelService;

    /**
     * 获取标签列表。
     */
    @Operation(summary = "获取标签列表")
    @GetMapping("/list")
    public R<List<SubjectLabel>> list(@RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return R.ok(labelService.getByCategoryId(categoryId));
        }
        return R.ok(labelService.getAll());
    }

    /**
     * 获取标签详情。
     */
    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public R<SubjectLabel> getById(@PathVariable Long id) {
        return R.ok(labelService.getById(id));
    }

    /**
     * 新增标签。
     */
    @Operation(summary = "新增标签")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody SubjectLabel label) {
        return R.ok(labelService.add(label));
    }

    /**
     * 更新标签。
     */
    @Operation(summary = "更新标签")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody SubjectLabel label) {
        return R.ok(labelService.update(label));
    }

    /**
     * 删除标签。
     */
    @Operation(summary = "删除标签")
    @PostMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(labelService.delete(id));
    }
}