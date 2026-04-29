package com.clx.api.user.feign;

import com.clx.api.user.dto.PageResultDTO;
import com.clx.api.user.dto.UserPageVO;
import com.clx.api.user.dto.UserQueryDTO;
import com.clx.api.user.dto.UserUpdateDTO;
import com.clx.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户服务 Feign 客户端。
 */
@FeignClient(name = "clx-user", contextId = "userFeignClient", url = "${feign.client.config.clx-user.url:}")
public interface UserFeignClient {

    /**
     * 分页查询用户列表。
     */
    @PostMapping("/internal/user/page")
    R<PageResultDTO<UserPageVO>> getUserPage(@RequestBody UserQueryDTO query);

    /**
     * 获取用户详情。
     */
    @GetMapping("/internal/user/{userId}")
    R<UserPageVO> getUserById(@PathVariable("userId") Long userId);

    /**
     * 更新用户资料。
     */
    @PutMapping("/internal/user/{userId}")
    R<Void> updateUser(@PathVariable("userId") Long userId, @RequestBody UserUpdateDTO dto);

    /**
     * 封禁用户。
     */
    @PutMapping("/internal/user/{userId}/ban")
    R<Void> banUser(@PathVariable("userId") Long userId);

    /**
     * 解封用户。
     */
    @PutMapping("/internal/user/{userId}/unban")
    R<Void> unbanUser(@PathVariable("userId") Long userId);

    /**
     * 获取用户角色列表。
     */
    @GetMapping("/internal/user/{userId}/roles")
    R<List<Long>> getUserRoles(@PathVariable("userId") Long userId);

    /**
     * 更新用户角色。
     */
    @PutMapping("/internal/user/{userId}/roles")
    R<Void> updateUserRoles(@PathVariable("userId") Long userId, @RequestBody List<Long> roleIds);
}