package com.clx.common.security.config;

import cn.dev33.satoken.stp.StpInterface;

import java.util.Collections;
import java.util.List;

/**
 * sa-Token 权限数据接口（占位实现）。
 *
 * <p>各服务需实现此类，查询数据库返回用户的角色/权限列表。
 */
public class DefaultPermissionProvider implements StpInterface {

    /**
     * 获取权限列表。
     *
     * @param loginId   登录用户ID
     * @param loginType  登录类型
     * @return 权限编码列表，默认返回空
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 获取角色列表。
     *
     * @param loginId   登录用户ID
     * @param loginType  登录类型
     * @return 角色编码列表，默认返回空
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
