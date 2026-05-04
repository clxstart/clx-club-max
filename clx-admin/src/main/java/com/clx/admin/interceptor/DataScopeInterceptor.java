package com.clx.admin.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.admin.annotation.DataScope;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Properties;

/**
 * 数据权限拦截器。
 *
 * <p>拦截 MyBatis 查询，自动拼接数据权限 SQL 条件。
 *
 * <p>实现原理：
 * 1. 拦截 Executor.query 方法
 * 2. 解析原始 SQL，找到 WHERE 子句
 * 3. 根据当前用户的组织 ID，拼接过滤条件
 * 4. 替换原始 SQL，执行修改后的查询
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
@Component
@Slf4j
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        // 获取 Mapper 方法上的 @DataScope 注解
        DataScope dataScope = getDataScopeAnnotation(ms);
        if (dataScope == null) {
            return invocation.proceed();
        }

        // 获取当前用户的组织 ID
        Long orgId = getCurrentUserOrgId();
        if (orgId == null) {
            // 超管或未登录用户不受数据权限限制
            return invocation.proceed();
        }

        // 拼接数据权限 SQL
        String newSql = appendDataScope(originalSql, dataScope, orgId);
        if (newSql.equals(originalSql)) {
            return invocation.proceed();
        }

        // 替换 SQL
        log.debug("数据权限 SQL 改写: {} -> {}", originalSql, newSql);
        // 通过反射修改 BoundSql 的 sql 字段
        try {
            java.lang.reflect.Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, newSql);
        } catch (Exception e) {
            log.warn("修改 SQL 失败: {}", e.getMessage());
        }

        return invocation.proceed();
    }

    @Override
    public void setProperties(Properties properties) {
    }

    /** 获取 Mapper 方法上的 @DataScope 注解 */
    private DataScope getDataScopeAnnotation(MappedStatement ms) {
        try {
            String mapperClassName = ms.getId().substring(0, ms.getId().lastIndexOf("."));
            String methodName = ms.getId().substring(ms.getId().lastIndexOf(".") + 1);
            Class<?> mapperClass = Class.forName(mapperClassName);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception e) {
            log.debug("获取 @DataScope 注解失败: {}", e.getMessage());
        }
        return null;
    }

    /** 获取当前用户的组织 ID */
    private Long getCurrentUserOrgId() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            // 从 JWT Payload 获取 orgId（需要登录时存入）
            Object orgId = StpUtil.getExtra("orgId");
            if (orgId != null) {
                return Long.valueOf(orgId.toString());
            }
            // 或从 Token Session 获取
            Object sessionOrgId = StpUtil.getSession().get("orgId");
            if (sessionOrgId != null) {
                return Long.valueOf(sessionOrgId.toString());
            }
        } catch (Exception e) {
            log.debug("获取用户组织ID失败: {}", e.getMessage());
        }
        return null;
    }

    /** 拼接数据权限 SQL */
    private String appendDataScope(String originalSql, DataScope dataScope, Long orgId) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();

            Expression where = plainSelect.getWhere();
            Expression dataScopeExpr = buildDataScopeExpression(dataScope, orgId);

            if (where == null) {
                plainSelect.setWhere(dataScopeExpr);
            } else {
                plainSelect.setWhere(new AndExpression(where, dataScopeExpr));
            }

            return select.toString();
        } catch (Exception e) {
            log.warn("SQL 解析失败，跳过数据权限: {}", e.getMessage());
            return originalSql;
        }
    }

    /** 构建数据权限表达式 */
    private Expression buildDataScopeExpression(DataScope dataScope, Long orgId) {
        // orgAlias.org_id = orgId
        EqualsTo orgEq = null;
        if (!dataScope.orgAlias().isEmpty()) {
            orgEq = new EqualsTo();
            orgEq.setLeftExpression(new Column(dataScope.orgAlias() + ".org_id"));
            orgEq.setRightExpression(new LongValue(orgId));
        }

        // userAlias.user_id = userId（可选）
        EqualsTo userEq = null;
        if (!dataScope.userAlias().isEmpty()) {
            userEq = new EqualsTo();
            userEq.setLeftExpression(new Column(dataScope.userAlias() + ".org_id"));
            userEq.setRightExpression(new LongValue(orgId));
        }

        // 组合：orgEq OR userEq
        if (orgEq != null && userEq != null) {
            return new net.sf.jsqlparser.expression.operators.conditional.OrExpression(orgEq, userEq);
        }
        return orgEq != null ? orgEq : userEq;
    }
}