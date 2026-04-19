package com.clx.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求 DTO。
 *
 * <p>使用 Java Record 定义不可变数据对象，自动生成 getter、equals、hashCode、toString。
 *
 * <p>验证规则：
 * <ul>
 *   <li>用户名：不能为空，最大50字符</li>
 *   <li>密码：不能为空，最大128字符</li>
 *   <li>记住我：可选，默认为 false</li>
 * </ul>
 *
 * <p>请求示例：
 * <pre>
 * POST /auth/login
 * Content-Type: application/json
 *
 * // 普通登录
 * {"username":"admin","password":"admin123"}
 *
 * // 记住我登录
 * {"username":"admin","password":"admin123","rememberMe":true}
 * </pre>
 *
 * @param username 用户名（必填）
 * @param password 密码（明文，仅传输时使用，不会存储）
 * @param rememberMe 是否记住我（可选，默认 false）
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 128, message = "密码长度不能超过128个字符")
        String password,

        Boolean rememberMe
) {}