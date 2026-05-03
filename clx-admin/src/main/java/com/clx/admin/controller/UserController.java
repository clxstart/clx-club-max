package com.clx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.clx.admin.feign.AuthFeignClient;
import com.clx.admin.feign.UserFeignClient;
import com.clx.admin.dto.PageResultDTO;
import com.clx.admin.dto.UserQueryDTO;
import com.clx.admin.dto.UserUpdateDTO;
import com.clx.admin.service.OperLogService;
import com.clx.admin.vo.UserInfoVO;
import com.clx.admin.vo.UserPageVO;
import com.clx.common.core.domain.R;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器。
 */
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class UserController {

    private final UserFeignClient userFeignClient;
    private final AuthFeignClient authFeignClient;
    private final OperLogService operLogService;

    /** 分页查询用户列表 */
    @SaCheckRole("admin")
    @PostMapping("/page")
    public R<PageResultDTO<UserPageVO>> getUserPage(@RequestBody UserQueryDTO query,
                                                      HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<PageResultDTO<UserPageVO>> result = userFeignClient.getUserPage(query);
        operLogService.logAsync("用户管理", "查询用户列表", request.getRequestURI(), "POST",
                JSON.toJSONString(query), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 获取用户详情 */
    @SaCheckRole("admin")
    @GetMapping("/{userId}")
    public R<UserPageVO> getUserById(@PathVariable Long userId) {
        return userFeignClient.getUserById(userId);
    }

    /** 封禁用户 */
    @SaCheckRole("admin")
    @PutMapping("/{userId}/ban")
    public R<Void> banUser(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(userId)) {
            return R.fail(400, "不能封禁自己");
        }

        long start = System.currentTimeMillis();
        R<Void> result = userFeignClient.banUser(userId);
        operLogService.logAsync("用户管理", "封禁用户", request.getRequestURI(), "PUT",
                String.valueOf(userId), null, "0", null,
                System.currentTimeMillis() - start,
                currentUserId, StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 解封用户 */
    @SaCheckRole("admin")
    @PutMapping("/{userId}/unban")
    public R<Void> unbanUser(@PathVariable Long userId, HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = userFeignClient.unbanUser(userId);
        operLogService.logAsync("用户管理", "解封用户", request.getRequestURI(), "PUT",
                String.valueOf(userId), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 更新用户资料 */
    @SaCheckRole("admin")
    @PutMapping("/{userId}")
    public R<Void> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO dto,
                                HttpServletRequest request) {
        long start = System.currentTimeMillis();
        R<Void> result = userFeignClient.updateUser(userId, dto);
        operLogService.logAsync("用户管理", "编辑用户资料", request.getRequestURI(), "PUT",
                JSON.toJSONString(dto), null, "0", null,
                System.currentTimeMillis() - start,
                StpUtil.getLoginIdAsLong(), StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 获取用户角色列表 */
    @SaCheckRole("admin")
    @GetMapping("/{userId}/roles")
    public R<List<Long>> getUserRoles(@PathVariable Long userId) {
        return userFeignClient.getUserRoles(userId);
    }

    /** 更新用户角色 */
    @SaCheckRole("admin")
    @PutMapping("/{userId}/roles")
    public R<Void> updateUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds,
                                     HttpServletRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (currentUserId.equals(userId)) {
            return R.fail(400, "不能修改自己的角色");
        }

        long start = System.currentTimeMillis();
        R<Void> result = userFeignClient.updateUserRoles(userId, roleIds);
        operLogService.logAsync("用户管理", "修改用户角色", request.getRequestURI(), "PUT",
                JSON.toJSONString(roleIds), null, "0", null,
                System.currentTimeMillis() - start,
                currentUserId, StpUtil.getLoginIdAsString(), request.getRemoteAddr());
        return result;
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    public R<UserInfoVO> getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return R.fail(401, "未登录");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        R<UserPageVO> userResult = userFeignClient.getUserById(userId);
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            return R.fail(404, "用户不存在");
        }
        R<List<String>> rolesResult = authFeignClient.getUserRoleCodes(userId);

        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(userId);
        vo.setUsername(userResult.getData().getUsername());
        vo.setNickname(userResult.getData().getNickname());
        vo.setRoles(rolesResult.getCode() == 200 ? rolesResult.getData() : List.of());
        return R.ok(vo);
    }
}