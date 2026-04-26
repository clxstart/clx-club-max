package com.clx.search.datasource;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据源注册器 - 统一管理所有数据源（相当于"通讯录"）。
 *
 * 启动时自动收集所有 DataSource 实现，按名字存储，供 SearchFacade 查找使用。
 */
@Slf4j
@Component
public class DataSourceRegistry {

    // 数据源字典：名字 → 数据源实例
    private final Map<String, DataSource<?>> dataSourceMap = new HashMap<>();

    // Spring 自动注入所有 DataSource 实现
    private final List<DataSource<?>> dataSources;

    public DataSourceRegistry(List<DataSource<?>> dataSources) {
        this.dataSources = dataSources;
    }

    /** 启动时自动注册所有数据源 */
    @PostConstruct
    public void init() {
        for (DataSource<?> dataSource : dataSources) {
            String name = dataSource.getName();               // 获取数据源名字
            dataSourceMap.put(name, dataSource);              // 存入字典
            log.info("注册数据源: {}", name);
        }
        log.info("数据源注册完成，共 {} 个", dataSourceMap.size());
    }

    /** 根据名字查找数据源（如 "post"、"user"） */
    public DataSource<?> getDataSource(String type) {
        return dataSourceMap.get(type);
    }

    /** 获取所有已注册的数据源类型 */
    public Set<String> getRegisteredTypes() {
        return dataSourceMap.keySet();
    }

    /** 判断某类型数据源是否已注册 */
    public boolean hasDataSource(String type) {
        return dataSourceMap.containsKey(type);
    }
}