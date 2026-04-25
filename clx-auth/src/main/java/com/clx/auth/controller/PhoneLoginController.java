package com.clx.auth.controller;

import com.clx.auth.dto.PhoneLoginRequest;
import com.clx.auth.dto.SmsCodeRequest;
import com.clx.auth.service.PhoneLoginService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 手机号验证码登录：发送验证码、验证码登录。
 */
@Slf4j
@RestController
@RequestMapping("/auth/phone")
@RequiredArgsConstructor
@Tag(name = "手机号登录")
public class PhoneLoginController {

    private final PhoneLoginService phoneLoginService;

    /** 发送短信验证码（需先通过图形验证码防刷） */
    @PostMapping("/sms-code")
    @Operation(summary = "发送短信验证码")
    public R<String> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        log.info("发送短信验证码: phone={}", request.phone());
        phoneLoginService.sendSmsCode(request.phone(), request.captchaId(), request.captchaCode());
        return R.ok("验证码已发送（开发环境请查看控制台日志）");
    }

    /** 手机号验证码登录，未注册自动创建账号 */
    @PostMapping("/login")
    @Operation(summary = "手机号登录")
    public R<LoginVO> login(@Valid @RequestBody PhoneLoginRequest request) {
        log.info("手机号登录: phone={}", request.phone());
        LoginVO loginVO = phoneLoginService.login(request.phone(), request.smsCode());
        return R.ok(loginVO);
    }
}
