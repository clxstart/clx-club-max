package com.clx.search.datasource;

import com.clx.search.enums.SearchTypeEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 数据源注册器（统一管理所有数据源）。
 *
 * 职责：
 * 1. 启动时注册所有数据源
 * 2. 根据搜索类型返回对应的数据源
 */
@Slf4j
@Component
public class DataSourceRegistry {

    private final Map<String, DataSource<?>> dataSourceMap = new HashMap<>();

    // 注入所有 DataSource 实现（Spring 自动注入）
    private final java.util.List<DataSource<?>> dataSources;

    public DataSourceRegistry(java.util.List<DataSource<?>> dataSources) {
        this.dataSources = dataSources;
    }

    @PostConstruct
    public void init() {
        // 自动注册所有 DataSource 实现
        for (DataSource<?> dataSource : dataSources) {
            String name = dataSource.getName();
            dataSourceMap.put(name, dataSource);
            log.info("注册数据源: {}", name);
        }
        log.info("数据源注册完成，共 {} 个", dataSourceMap.size());
    }

    /**
     * 根据类型获取数据源。
     *
     * @param type 搜索类型
     * @return 数据源，不存在则返回 null
     */
    public DataSource<?> getDataSource(String type) {
        return dataSourceMap.get(type);
    }

    /**
     * 获取所有已注册的数据源类型。
     */
    public Set<String> getRegisteredTypes() {
        return dataSourceMap.keySet();
    }

    /**
     * 判断数据源是否已注册。
     */
    public boolean hasDataSource(String type) {
        return dataSourceMap.containsKey(type);
    }
}