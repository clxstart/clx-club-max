package com.clx.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置 - 创建 ES 客户端连接。
 * 进行全文检索
 * 从配置文件读取地址/端口/认证信息，注入 ElasticsearchClient 供搜索服务使用。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ESConfig {

    private String host = "localhost";  // ES 地址
    private int port = 9201;            // ES 端口（本项目用 9201 避免冲突）
    private String username = "";       // 用户名（可选）
    private String password = "";       // 密码（可选）

    /** ES 低级客户端，负责 HTTP 连接 */
    @Bean
    public RestClient restClient() {
        return RestClient.builder(new HttpHost(host, port, "http"))
                .build();
    }

    /** ES 高级客户端，提供搜索/索引等 API */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        RestClientTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));
        return new ElasticsearchClient(transport);
    }
}