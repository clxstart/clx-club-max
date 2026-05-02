package com.clx.quiz.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.quiz.dto.PracticeStartRequest;
import com.clx.quiz.dto.PracticeSubmitRequest;
import com.clx.quiz.service.PracticeService;
import com.clx.quiz.vo.PracticeResultVO;
import com.clx.quiz.vo.PracticeSubjectVO;
import com.clx.quiz.vo.SubmitResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 练习控制器。
 */
@Tag(name = "练习流程", description = "练习流程接口")
@RestController
@RequestMapping("/quiz/practice")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;

    /**
     * 开始练习。
     */
    @Operation(summary = "开始练习")
    @PostMapping("/start")
    public R<Map<String, Object>> start(@RequestBody PracticeStartRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(practiceService.start(request, userId));
    }

    /**
     * 获取练习题目。
     */
    @Operation(summary = "获取练习题目")
    @PostMapping("/subject")
    public R<PracticeSubjectVO> getSubject(@RequestBody Map<String, Object> params) {
        Long practiceId = Long.valueOf(params.get("practiceId").toString());
        Long subjectId = Long.valueOf(params.get("subjectId").toString());
        Integer subjectType = Integer.valueOf(params.get("subjectType").toString());
        return R.ok(practiceService.getSubject(practiceId, subjectId, subjectType));
    }

    /**
     * 提交答案。
     */
    @Operation(summary = "提交答案")
    @PostMapping("/submit")
    public R<SubmitResultVO> submit(@RequestBody PracticeSubmitRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(practiceService.submit(request, userId));
    }

    /**
     * 简答题自评。
     */
    @Operation(summary = "简答题自评")
    @PostMapping("/self-judge")
    public R<Boolean> selfJudge(@RequestBody Map<String, Object> params) {
        Long practiceId = Long.valueOf(params.get("practiceId").toString());
        Long subjectId = Long.valueOf(params.get("subjectId").toString());
        Integer isCorrect = Integer.valueOf(params.get("isCorrect").toString());
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(practiceService.selfJudge(practiceId, subjectId, isCorrect, userId));
    }

    /**
     * 结束练习。
     */
    @Operation(summary = "结束练习")
    @PostMapping("/finish")
    public R<PracticeResultVO> finish(@RequestBody Map<String, Long> params) {
        Long practiceId = params.get("practiceId");
        Long userId = StpUtil.getLoginIdAsLong();
        return R.ok(practiceService.finish(practiceId, userId));
    }
}