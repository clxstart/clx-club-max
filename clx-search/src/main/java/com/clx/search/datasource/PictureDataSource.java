package com.clx.search.datasource;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图片数据源（必应图片爬虫）。
 *
 * 数据来源：https://cn.bing.com/images/search
 */
@Slf4j
@Component
public class PictureDataSource implements DataSource<PictureDataSource.Picture> {

    @Override
    public List<Picture> doSearch(String keyword, int page, int size) {
        List<Picture> pictures = new ArrayList<>();
        try {
            int first = (page - 1) * size;
            String url = String.format("https://cn.bing.com/images/search?q=%s&first=%d", keyword, first);

            // 设置超时和 User-Agent 防止被封
            Document doc = Jsoup.connect(url)
                    .timeout(5000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            Elements elements = doc.select(".iuscp.isv");
            for (Element element : elements) {
                try {
                    // 取图片地址（murl）
                    String m = element.select(".iusc").attr("m");
                    if (m == null || m.isEmpty()) continue;

                    Map<String, Object> map = JSONUtil.toBean(m, Map.class);
                    String murl = (String) map.get("murl");

                    // 取标题
                    String title = element.select(".inflnk").attr("aria-label");

                    if (murl != null && title != null) {
                        Picture picture = new Picture();
                        picture.setTitle(title);
                        picture.setUrl(murl);
                        pictures.add(picture);

                        if (pictures.size() >= size) break;
                    }
                } catch (Exception e) {
                    log.warn("解析图片失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("爬取图片失败: {}", e.getMessage());
            // 失败时返回空列表，不抛异常
        }
        return pictures;
    }

    @Override
    public String getName() {
        return "picture";
    }

    /**
     * 图片结果。
     */
    @Data
    public static class Picture {
        private String title;
        private String url;
    }
}