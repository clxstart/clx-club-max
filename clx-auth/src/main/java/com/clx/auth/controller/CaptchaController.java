package com.clx.auth.controller;

import com.clx.auth.support.CaptchaGenerator;
import com.clx.auth.vo.CaptchaVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码接口
 */
@Slf4j
@Tag(name = "图形验证码")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaGenerator captchaGenerator;

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码")
    public R<CaptchaVO> getCaptcha() {
        return R.ok(captchaGenerator.generate());
    }
}