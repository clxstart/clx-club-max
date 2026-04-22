package com.clx.auth.controller;

import com.clx.auth.entity.SocialBind;
import com.clx.auth.service.SocialBindService;
import com.clx.auth.service.oauth.GithubOAuthService;
import com.clx.auth.vo.SocialBindVO;
import com.clx.common.core.domain.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 社交账号绑定控制器
 *
 * <p>功能：管理用户的第三方账号绑定关系
 *
 * <p>接口列表：
 * <ul>
 *   <li>GET /auth/bindings - 获取当前用户绑定的所有社交账号</li>
 *   <li>DELETE /auth/bindings/{id} - 解绑社交账号</li>
 *   <li>POST /auth/bindings/github - 绑定GitHub账号</li>
 * </ul>
 *
 * @author CLX
 * @since 2026-04-22
 */
@Slf4j
@RestController
@RequestMapping("/auth/bindings")
@RequiredArgsConstructor
@Tag(name = "社交账号绑定", description = "管理用户的第三方账号绑定关系")
public class SocialBindController {

    private final SocialBindService socialBindService;
    private final GithubOAuthService githubOAuthService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前用户绑定的所有社交账号
     *
     * @return 绑定列表
     */
    @GetMapping
    @Operation(summary = "获取绑定列表", description = "获取当前用户绑定的所有第三方账号")
    public R<List<SocialBindVO>> getMyBinds() {
        List<SocialBind> binds = socialBindService.getMyBinds();

        List<SocialBindVO> voList = binds.stream()
                .map(this::toVO)
                .toList();

        return R.ok(voList);
    }

    /**
     * 解绑社交账号
     *
     * @param id 绑定ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "解绑账号", description = "解绑指定的社交账号")
    public R<Void> unbind(@PathVariable Long id) {
        log.info("解绑社交账号请求: bindId={}", id);
        socialBindService.unbind(id);
        return R.ok();
    }

    /**
     * 获取GitHub绑定授权URL
     *
     * <p>用于已登录用户绑定GitHub账号
     *
     * @return 授权URL
     */
    @GetMapping("/github/authorize")
    @Operation(summary = "获取GitHub绑定授权URL", description = "获取用于绑定GitHub账号的授权URL")
    public R<String> getGithubBindAuthorizeUrl() {
        String authorizeUrl = githubOAuthService.getAuthorizeUrl();
        return R.ok(authorizeUrl);
    }

    /**
     * 绑定GitHub账号回调
     *
     * @param code  GitHub授权码
     * @param state 状态码
     * @return 绑定结果
     */
    @PostMapping("/github/callback")
    @Operation(summary = "GitHub绑定回调", description = "处理GitHub绑定的回调")
    public R<Void> bindGithub(
            @Parameter(description = "GitHub授权码")
            @RequestParam(value = "code") String code,
            @Parameter(description = "状态码")
            @RequestParam(value = "state") String state) {

        log.info("绑定GitHub账号回调: code={}, state={}", code, state);
        socialBindService.bindGithub(code, state);
        return R.ok();
    }

    // ========== 私有方法 ==========

    private SocialBindVO toVO(SocialBind bind) {
        String bindTime = bind.getBindTime() != null
                ? bind.getBindTime().format(DATE_FORMATTER)
                : null;

        return new SocialBindVO(
                bind.getId(),
                bind.getSocialType(),
                bind.getSocialName(),
                bind.getSocialAvatar(),
                bindTime
        );
    }
}
