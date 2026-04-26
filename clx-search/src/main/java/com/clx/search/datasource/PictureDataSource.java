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
 * 图片数据源 - 爬取必应图片搜索结果。
 *
 * 用户搜"Spring"时，爬取必应图片搜索，返回相关图片链接。
 */
@Slf4j
@Component
public class PictureDataSource implements DataSource<PictureDataSource.Picture> {

    @Override
    public List<Picture> doSearch(String keyword, int page, int size) {
        List<Picture> pictures = new ArrayList<>();
        try {
            // 计算分页起始位置
            int first = (page - 1) * size;
            // 构建必应图片搜索 URL
            String url = String.format("https://cn.bing.com/images/search?q=%s&first=%d", keyword, first);

            // 发送 HTTP 请求，模拟浏览器访问（防反爬）
            Document doc = Jsoup.connect(url)
                    .timeout(5000)  // 5 秒超时
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")  // 模拟浏览器
                    .get();

            // 解析图片列表（必应图片结果容器）
            Elements elements = doc.select(".iuscp.isv");
            for (Element element : elements) {
                try {
                    // 提取图片元数据（JSON 格式存储在 m 属性中）
                    String m = element.select(".iusc").attr("m");
                    if (m == null || m.isEmpty()) continue;

                    // 解析 JSON，获取真实图片地址
                    Map<String, Object> map = JSONUtil.toBean(m, Map.class);
                    String murl = (String) map.get("murl");  // 图片原图地址

                    // 提取图片标题
                    String title = element.select(".inflnk").attr("aria-label");

                    if (murl != null && title != null) {
                        Picture picture = new Picture();
                        picture.setTitle(title);  // 图片标题
                        picture.setUrl(murl);     // 图片链接
                        pictures.add(picture);

                        if (pictures.size() >= size) break;  // 够数量就停
                    }
                } catch (Exception e) {
                    log.warn("解析图片失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("爬取图片失败: {}", e.getMessage());
        }
        return pictures;
    }

    @Override
    public String getName() {
        return "picture";  // 数据源名称
    }

    /** 图片搜索结果 */
    @Data
    public static class Picture {
        private String title;  // 图片标题
        private String url;    // 图片链接
    }
}