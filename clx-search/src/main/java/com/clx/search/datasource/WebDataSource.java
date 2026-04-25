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
 * 网页数据源（必应网页搜索）。
 *
 * 数据来源：https://cn.bing.com/search
 */
@Slf4j
@Component
public class WebDataSource implements DataSource<WebDataSource.WebResult> {

    @Override
    public List<WebResult> doSearch(String keyword, int page, int size) {
        List<WebResult> results = new ArrayList<>();
        try {
            // 必应搜索 URL
            String url = String.format("https://cn.bing.com/search?q=%s&count=%d", keyword, size);

            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            // 解析搜索结果
            Elements items = doc.select("#b_results .b_algo");
            for (Element item : items) {
                try {
                    Element titleEl = item.selectFirst("h2 a");
                    Element descEl = item.selectFirst(".b_caption p");

                    if (titleEl != null) {
                        WebResult result = new WebResult();
                        result.setTitle(titleEl.text());
                        result.setUrl(titleEl.attr("href"));
                        result.setDescription(descEl != null ? descEl.text() : "");
                        result.setSource("bing");
                        results.add(result);

                        if (results.size() >= size) break;
                    }
                } catch (Exception e) {
                    log.warn("解析网页结果失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("爬取网页失败: {}", e.getMessage());
            // 失败时返回空列表
        }
        return results;
    }

    @Override
    public String getName() {
        return "web";
    }

    /**
     * 网页搜索结果。
     */
    @Data
    public static class WebResult {
        private String title;
        private String url;
        private String description;
        private String source;
    }
}