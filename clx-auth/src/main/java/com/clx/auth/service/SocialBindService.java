package com.clx.auth.service;

import com.clx.auth.entity.SocialBind;

import java.util.List;

/**
 * 社交账号绑定服务接口
 *
 * @author CLX
 * @since 2026-04-22
 */
public interface SocialBindService {

    /**
     * 获取当前用户绑定的所有社交账号
     *
     * @return 绑定列表
     */
    List<SocialBind> getMyBinds();

    /**
     * 解绑社交账号
     *
     * @param bindId 绑定ID
     */
    void unbind(Long bindId);

    /**
     * 绑定GitHub账号
     *
     * @param code  GitHub授权码
     * @param state 状态码
     */
    void bindGithub(String code, String state);
}
