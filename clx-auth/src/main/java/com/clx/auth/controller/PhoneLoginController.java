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
 * 手机号登录控制器
 *
 * <p>功能：提供手机号登录相关接口
 *
 * <p>接口列表：
 * <ul>
 *   <li>POST /auth/phone/sms-code - 发送短信验证码</li>
 *   <li>POST /auth/phone/login - 手机号登录</li>
 * </ul>
 *
 * <p>使用流程：
 * <ol>
 *   <li>用户输入手机号和图形验证码</li>
 *   <li>调用 /sms-code 发送短信验证码</li>
 *   <li>用户收到短信，输入验证码</li>
 *   <li>调用 /login 完成登录</li>
 * </ol>
 *
 * @author CLX
 * @since 2026-04-22
 */
@Slf4j
@RestController
@RequestMapping("/auth/phone")
@RequiredArgsConstructor
@Tag(name = "手机号登录", description = "手机号验证码登录相关接口")
public class PhoneLoginController {

    /**
     * 手机号登录服务
     */
    private final PhoneLoginService phoneLoginService;

    /**
     * 发送短信验证码
     *
     * <p>调用时机：用户点击"获取验证码"按钮时
     *
     * <p>流程：
     * <ol>
     *   <li>验证图形验证码（防止接口被刷）</li>
     *   <li>生成 6 位数字验证码</li>
     *   <li>存入 Redis，5 分钟有效</li>
     *   <li>发送短信</li>
     * </ol>
     *
     * @param request 短信验证码请求（手机号 + 图形验证码）
     * @return 发送结果
     */
    @PostMapping("/sms-code")
    @Operation(summary = "发送短信验证码", description = "发送手机号登录验证码，需要图形验证码防刷")
    public R<String> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        log.info("发送短信验证码请求: phone={}", request.phone());

        phoneLoginService.sendSmsCode(
                request.phone(),
                request.captchaId(),
                request.captchaCode()
        );

        // 开发环境：返回验证码方便测试
        // 生产环境：应该只返回成功信息
        return R.ok("验证码已发送（开发环境请查看控制台日志）");
    }

    /**
     * 手机号登录
     *
     * <p>调用时机：用户输入验证码后点击登录
     *
     * <p>流程：
     * <ol>
     *   <li>验证短信验证码</li>
     *   <li>查找用户，没找到则自动创建</li>
     *   <li>生成 Token 返回</li>
     * </ol>
     *
     * @param request 手机号登录请求（手机号 + 验证码）
     * @return 登录结果，包含 Token
     */
    @PostMapping("/login")
    @Operation(summary = "手机号登录", description = "使用手机号和短信验证码登录，未注册自动创建账号")
    public R<LoginVO> login(@Valid @RequestBody PhoneLoginRequest request) {
        log.info("手机号登录请求: phone={}", request.phone());

        LoginVO loginVO = phoneLoginService.login(
                request.phone(),
                request.smsCode()
        );

        log.info("手机号登录成功: phone={}", request.phone());
        return R.ok(loginVO);
    }
}
