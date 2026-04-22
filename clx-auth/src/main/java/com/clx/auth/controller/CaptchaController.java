package com.clx.auth.controller;

import com.clx.auth.service.CaptchaService;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码控制器。
 *
 * <p>提供图形验证码生成接口，返回 base64 格式的验证码图片。
 *
 * <p>使用场景：
 * <ul>
 *   <li>用户注册时需要图形验证码</li>
 *   <li>发送邮箱/手机验证码时需要图形验证码</li>
 *   <li>密码重置时需要图形验证码</li>
 * </ul>
 */
@Slf4j
@Tag(name = "图形验证码接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取图形验证码。
     *
     * <p>返回一个 4 位随机验证码的 base64 图片。
     *
     * <p>请求示例：
     * <pre>
     * GET /auth/captcha
     * </pre>
     *
     * <p>响应示例：
     * <pre>
     * {
     *   "code": 200,
     *   "msg": "操作成功",
     *   "data": {
     *     "captchaId": "a1b2c3d4-e5f6-7890",
     *     "captchaImage": "data:image/png;base64,..."
     *   }
     * }
     * </pre>
     *
     * @return 图形验证码结果
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码")
    public R<CaptchaService.CaptchaResult> getCaptcha() {
        log.info("获取图形验证码请求");
        return R.ok(captchaService.generateCaptcha());
    }
}
