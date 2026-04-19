package com.clx.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO。
 *
 * <p>使用Java Record定义不可变数据对象，自动生成getter、equals、hashCode、toString。
 *
 * <p>验证规则：
 * <ul>
 *   <li>用户名：不能为空，最大50字符</li>
 *   <li>密码：不能为空，最大128字符</li>
 * </ul>
 *
 * <p>注意：密码长度限制128字符是因为BCrypt有72字符的输入限制，
 * 超过72字符的部分会被忽略。128字符限制是为了兼容各种密码管理器
 * 生成的超长密码，同时在Service层做最终校验。
 *
 * @param username 用户名
 * @param password 密码（明文，仅传输时使用，不会存储）
 */
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 128, message = "密码长度不能超过128个字符")
        String password
) {
}