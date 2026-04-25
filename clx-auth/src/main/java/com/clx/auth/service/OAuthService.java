package com.clx.auth.service;

import com.clx.auth.enums.OAuthPlatform;
import com.clx.auth.vo.LoginVO;

/**
 * OAuth 登录服务接口
 */
public interface OAuthService {

    /** 获取平台类型 */
    OAuthPlatform getPlatform();

    /** 生成授权 URL */
    String getAuthorizeUrl();

    /** 处理回调，完成登录 */
    LoginVO handleCallback(String code, String state);

    /** 绑定社交账号（已登录用户绑定第三方账号） */
    void bindAccount(String code, String state);
}
