package com.clx.admin.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解。
 *
 * <p>标记在 Controller 方法上，AOP 切面自动记录操作日志。
 *
 * <p>使用示例：
 * <pre>
 * &#64;OperLog(module = "用户管理", action = "查询用户列表")
 * &#64;PostMapping("/page")
 * public R&lt;?&gt; getUserPage(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /**
     * 模块名称。
     */
    String module();

    /**
     * 操作动作。
     */
    String action();

    /**
     * 是否记录请求参数，默认 false。
     */
    boolean recordParam() default false;

    /**
     * 是否记录响应结果，默认 false。
     */
    boolean recordResult() default false;
}
