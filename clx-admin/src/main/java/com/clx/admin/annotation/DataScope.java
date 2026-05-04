package com.clx.admin.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解。
 *
 * <p>标记在 Controller 方法上，MyBatis 拦截器自动拼接数据权限 SQL。
 *
 * <p>使用示例：
 * <pre>
 * &#64;DataScope(orgAlias = "o", userAlias = "u")
 * &#64;PostMapping("/page")
 * public R&lt;?&gt; getUserPage(...) {
 *     // SQL: SELECT ... WHERE o.org_id = {当前用户组织ID}
 * }
 * </pre>
 *
 * <p>注意：对应的 SQL 需要使用指定的表别名。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 组织表的别名，用于拼接 org_id 过滤。
     */
    String orgAlias() default "";

    /**
     * 用户表的别名，用于拼接 user_id 过滤（可选）。
     */
    String userAlias() default "";
}
