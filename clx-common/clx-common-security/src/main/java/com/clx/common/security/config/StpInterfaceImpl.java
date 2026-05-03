package com.clx.common.security.config;

import cn.dev33.satoken.stp.StpInterface;

import java.util.Collections;
import java.util.List;

/**
 * 默认权限数据接口（占位实现）。
 *
 * <p><b>重要说明</b>：此为占位实现，始终返回空列表。
 * 各服务需根据自身业务提供具体的实现（如查询数据库获取用户角色/权限）。
 *
 * <p>使用方式：
 * <ol>
 *   <li>在具体服务中创建类实现 {@link StpInterface} 接口</li>
 *   <li>使用 @Service 注解，确保被 Spring 扫描</li>
 *   <li>实现 getPermissionList 和 getRoleList 方法</li>
 * </ol>
 *
 * <p>示例实现：
 * <pre>
 * &#64;Service
 * public class AuthStpInterfaceImpl implements StpInterface {
 *     &#64;Autowired
 *     private UserRoleService userRoleService;
 *
 *     &#64;Override
 *     public List&lt;String&gt; getPermissionList(Object loginId, String loginType) {
 *         return userRoleService.getPermissionsByUserId((Long) loginId);
 *     }
 *
 *     &#64;Override
 *     public List&lt;String&gt; getRoleList(Object loginId, String loginType) {
 *         return userRoleService.getRolesByUserId((Long) loginId);
 *     }
 * }
 * </pre>
 *
 * @see StpInterface sa-Token 权限数据接口
 */
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
