package com.clx.common.security.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * sa-Token 权限数据接口实现
 *
 * 当前阶段：返回空权限，后续实现从数据库读取
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // TODO: 后续从数据库读取用户权限
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO: 后续从数据库读取用户角色
        return Collections.emptyList();
    }

}