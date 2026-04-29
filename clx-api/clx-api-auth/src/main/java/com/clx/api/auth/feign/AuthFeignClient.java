package com.clx.api.auth.feign;

import com.clx.api.auth.dto.RoleVO;
import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 认证服务 Feign 客户端。
 */
@FeignClient(name = "clx-auth", contextId = "authFeignClient", url = "${feign.client.config.clx-auth.url:}")
public interface AuthFeignClient {

    /**
     * 获取所有角色列表。
     */
    @GetMapping("/internal/auth/roles")
    R<List<RoleVO>> getRoleList();

    /**
     * 获取用户角色编码列表。
     */
    @GetMapping("/internal/auth/user/{userId}/roles")
    R<List<String>> getUserRoleCodes(@PathVariable("userId") Long userId);
}