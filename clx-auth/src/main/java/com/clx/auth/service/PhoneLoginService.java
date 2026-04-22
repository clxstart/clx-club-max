package com.clx.auth.service;

import com.clx.auth.vo.LoginVO;

/**
 * 手机号登录服务接口
 *
 * @author CLX
 * @since 2026-04-22
 */
public interface PhoneLoginService {

    /**
     * 发送短信验证码
     *
     * @param phone       手机号
     * @param captchaId   图形验证码ID
     * @param captchaCode 图形验证码
     */
    void sendSmsCode(String phone, String captchaId, String captchaCode);

    /**
     * 手机号登录
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @return 登录结果
     */
    LoginVO login(String phone, String smsCode);
}
