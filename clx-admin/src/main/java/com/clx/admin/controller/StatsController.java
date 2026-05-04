package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.clx.admin.feign.AnalyticsFeignClient;
import com.clx.common.core.domain.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计控制器。
 */
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class StatsController {

    private final AnalyticsFeignClient analyticsFeignClient;

    /** 获取概览统计 */
    @SaCheckRole("admin")
    @GetMapping("/overview")
    public R<Map<String, Object>> getOverview() {
        return analyticsFeignClient.getOverview();
    }

    /** 获取趋势数据 */
    @SaCheckRole("admin")
    @GetMapping("/trend")
    public R<Map<String, Object>> getTrend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        return analyticsFeignClient.getTrend(days);
    }
}