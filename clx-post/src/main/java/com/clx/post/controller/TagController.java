package com.clx.post.controller;

import com.clx.common.core.domain.R;
import com.clx.post.service.TagService;
import com.clx.post.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签控制器。
 */
@Tag(name = "标签管理", description = "标签相关接口")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 获取标签列表。
     */
    @Operation(summary = "获取标签列表")
    @GetMapping("/list")
    public R<List<TagVO>> list() {
        return R.ok(tagService.getAll());
    }
}