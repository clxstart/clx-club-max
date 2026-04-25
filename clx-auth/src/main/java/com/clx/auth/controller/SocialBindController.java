package com.clx.auth.controller;

import com.clx.auth.entity.SocialBind;
import com.clx.auth.enums.OAuthPlatform;
import com.clx.auth.service.SocialBindService;
import com.clx.auth.service.OAuthService;
import com.clx.auth.vo.SocialBindVO;
import com.clx.common.core.domain.R;
import com.clx.common.core.exception.ServiceException;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 社交账号绑定管理
 * <p>
 * 已登录用户绑定/解绑第三方账号（GitHub/微信/钉钉等）
 *
 * @author CLX */
@Slf4j
@RestController
@RequestMapping("/auth/bindings")
@RequiredArgsConstructor
@Tag(name = "社交账号绑定")
public class SocialBindController {

    private final SocialBindService socialBindService;
    private final List<OAuthService> oAuthServices;
    private Map<OAuthPlatform, OAuthService> serviceMap;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 初始化服务映射 */
    private Map<OAuthPlatform, OAuthService> getServiceMap() {
        if (serviceMap == null) {
            serviceMap = oAuthServices.stream()
                    .collect(Collectors.toMap(OAuthService::getPlatform, Function.identity()));
        }
        return serviceMap;
    }

    private OAuthService getService(OAuthPlatform platform) {
        OAuthService service = getServiceMap().get(platform);
        if (service == null) {
            throw ServiceException.validationFailed("暂不支持绑定 " + platform.getName());
        }
        return service;
    }

    /** 获取当前用户绑定的所有社交账号 */
    @GetMapping
    @Operation(summary = "获取绑定列表")
    public R<List<SocialBindVO>> getMyBinds() {
        List<SocialBind> binds = socialBindService.getMyBinds();
        List<SocialBindVO> voList = binds.stream().map(this::toVO).toList();
        return R.ok(voList);
    }

    /** 解绑指定社交账号 */
    @DeleteMapping("/{id}")
    @Operation(summary = "解绑账号")
    public R<Void> unbind(@PathVariable Long id) {
        log.info("解绑社交账号: bindId={}", id);
        socialBindService.unbind(id);
        return R.ok();
    }

    /** 获取指定平台的绑定授权 URL */
    @GetMapping("/{platform}/authorize")
    @Operation(summary = "获取绑定授权URL")
    public R<String> getBindAuthorizeUrl(
            @Parameter(description = "平台：github/wechat/dingtalk", required = true)
            @PathVariable String platform) {

        OAuthPlatform oauthPlatform = OAuthPlatform.fromCode(platform);
        if (oauthPlatform == null) {
            throw ServiceException.validationFailed("不支持的绑定平台: " + platform);
        }

        return R.ok(getService(oauthPlatform).getAuthorizeUrl());
    }

    /** 绑定回调处理 */
    @PostMapping("/{platform}/callback")
    @Operation(summary = "绑定回调")
    public R<Void> bindCallback(
            @Parameter(description = "平台标识", required = true)
            @PathVariable String platform,
            @Parameter(description = "授权码") @RequestParam String code,
            @Parameter(description = "状态码") @RequestParam String state) {

        OAuthPlatform oauthPlatform = OAuthPlatform.fromCode(platform);
        if (oauthPlatform == null) {
            throw ServiceException.validationFailed("不支持的绑定平台: " + platform);
        }

        log.info("绑定 {} 账号: code={}, state={}", oauthPlatform.getName(), code, state);
        getService(oauthPlatform).bindAccount(code, state);
        return R.ok();
    }

    private SocialBindVO toVO(SocialBind bind) {
        String bindTime = bind.getBindTime() != null ? bind.getBindTime().format(DATE_FORMATTER) : null;
        return new SocialBindVO(bind.getId(), bind.getSocialType(), bind.getSocialName(), bind.getSocialAvatar(), bindTime);
    }
}
