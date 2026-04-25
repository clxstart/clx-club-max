package com.clx.auth.service;

import com.clx.auth.entity.SocialBind;

import java.util.List;

/**
 * 社交账号绑定服务
 * <p>
 * 绑定逻辑由 OAuthService.bindAccount() 处理，本服务提供查询、检查、解绑功能
 */
public interface SocialBindService {

    /** 获取当前用户绑定的所有社交账号 */
    List<SocialBind> getMyBinds();

    /** 检查当前用户是否已绑定某平台 */
    boolean isBound(String platform);

    /** 获取当前用户某平台的绑定详情 */
    SocialBind getBindByPlatform(String platform);

    /** 解绑社交账号 */
    void unbind(Long bindId);
}