package com.clx.search.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.common.core.domain.R;
import com.clx.search.dto.SearchRequest;
import com.clx.search.manager.SearchFacade;
import com.clx.search.service.HotKeywordService;
import com.clx.search.vo.SearchVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索控制器。
 */
@Tag(name = "聚合搜索", description = "聚合搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchFacade searchFacade;
    private final HotKeywordService hotKeywordService;

    /**
     * 聚合搜索。
     */
    @Operation(summary = "聚合搜索")
    @PostMapping("/aggregate")
    public R<SearchVO> searchAggregate(@RequestBody SearchRequest request, HttpServletRequest httpRequest) {
        if (request.getKeyword() == null || request.getKeyword().trim().isEmpty()) {
            return R.fail("关键词不能为空");
        }
        String ip = getClientIp(httpRequest);
        SearchVO result = searchFacade.searchAll(request, ip);
        return R.ok(result);
    }

    /**
     * 单类型搜索。
     */
    @Operation(summary = "单类型搜索")
    @GetMapping("/single")
    public R<SearchVO.SearchResult> searchSingle(
            @RequestParam String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return R.fail("关键词不能为空");
        }
        SearchVO.SearchResult result = searchFacade.searchSingle(type, keyword, page, size);
        return R.ok(result);
    }

    /**
     * 搜索建议（暂时返回空，后续可接入 ES Suggest）。
     */
    @Operation(summary = "搜索建议")
    @GetMapping("/suggest")
    public R<List<String>> suggest(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") Integer size) {
        // TODO: 实现 ES Suggest
        return R.ok(new ArrayList<>());
    }

    /**
     * 热词统计。
     */
    @Operation(summary = "热词统计")
    @GetMapping("/hot")
    public R<List<HotKeywordService.HotKeywordVO>> hotKeywords(
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<HotKeywordService.HotKeywordVO> hotKeywords = hotKeywordService.getTodayHotKeywords(limit);
        return R.ok(hotKeywords);
    }

    /**
     * 获取客户端 IP。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}