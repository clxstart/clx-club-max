package com.clx.search.datasource;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 网页数据源 - 爬取必应搜索结果。
 *
 * 用户搜"Spring"时，爬取必应网页搜索，返回相关网页链接。
 */
@Slf4j
@Component
public class WebDataSource implements DataSource<WebDataSource.WebResult> {

    @Override
    public List<WebResult> doSearch(String keyword, int page, int size) {
        List<WebResult> results = new ArrayList<>();
        try {
            // 构建必应搜索 URL
            String url = String.format("https://cn.bing.com/search?q=%s&count=%d", keyword, size);

            // 发送 HTTP 请求，模拟浏览器访问
            Document doc = Jsoup.connect(url)
                    .timeout(5000)  // 5 秒超时
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")  // 模拟浏览器
                    .get();

            // 解析搜索结果列表
            Elements items = doc.select("#b_results .b_algo");  // 必应搜索结果容器
            for (Element item : items) {
                try {
                    // 提取标题和链接
                    Element titleEl = item.selectFirst("h2 a");
                    // 提取摘要描述
                    Element descEl = item.selectFirst(".b_caption p");

                    if (titleEl != null) {
                        WebResult result = new WebResult();
                        result.setTitle(titleEl.text());                           // 标题
                        result.setUrl(titleEl.attr("href"));                       // 链接
                        result.setDescription(descEl != null ? descEl.text() : ""); // 摘要
                        result.setSource("bing");                                   // 来源
                        results.add(result);

                        if (results.size() >= size) break;  // 够数量就停
                    }
                } catch (Exception e) {
                    log.warn("解析网页结果失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("爬取网页失败: {}", e.getMessage());
        }
        return results;
    }

    @Override
    public String getName() {
        return "web";  // 数据源名称
    }

    /** 网页搜索结果 */
    @Data
    public static class WebResult {
        private String title;       // 网页标题
        private String url;         // 网页链接
        private String description; // 网页摘要
        private String source;      // 来源（bing）
    }
}