package com.clx.admin.config;

import com.clx.admin.interceptor.DataScopeInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * MyBatis 配置。
 *
 * <p>注册数据权限拦截器到 MyBatis 插件链。
 */
@Configuration
public class MybatisConfig {

    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @PostConstruct
    public void addInterceptor() {
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
        }
    }
}
