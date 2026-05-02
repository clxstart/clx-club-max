package com.clx.quiz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.quiz.dto.SubjectCreateRequest;
import com.clx.quiz.dto.SubjectQueryRequest;
import com.clx.quiz.service.SubjectService;
import com.clx.quiz.vo.SubjectDetailVO;
import com.clx.quiz.vo.SubjectVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目管理控制器。
 */
@Tag(name = "题目管理", description = "题目增删改查接口")
@RestController
@RequestMapping("/quiz/subject")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    /**
     * 新增题目。
     */
    @Operation(summary = "新增题目")
    @PostMapping("/add")
    public R<Boolean> add(@RequestBody SubjectCreateRequest request) {
        String createdBy = StpUtil.getLoginIdAsString();
        return R.ok(subjectService.add(request, createdBy));
    }

    /**
     * 删除题目。
     */
    @Operation(summary = "删除题目")
    @PostMapping("/delete/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(subjectService.delete(id));
    }

    /**
     * 分页查询题目。
     */
    @Operation(summary = "分页查询题目")
    @PostMapping("/page")
    public R<Map<String, Object>> page(@RequestBody SubjectQueryRequest request) {
        List<SubjectVO> list = subjectService.queryPage(request);
        int total = subjectService.count(request);
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", list);
        return R.ok(result);
    }

    /**
     * 获取题目详情。
     */
    @Operation(summary = "获取题目详情")
    @PostMapping("/detail")
    public R<SubjectDetailVO> detail(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        return R.ok(subjectService.getDetail(id));
    }
}