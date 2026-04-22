package com.clx.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.config.OAuthProperties;
import com.clx.auth.dto.GithubUserInfo;
import com.clx.auth.entity.SocialBind;
import com.clx.auth.mapper.SocialBindMapper;
import com.clx.auth.service.SocialBindService;
import com.clx.common.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 社交账号绑定服务实现
 *
 * <p>功能：
 * <ul>
 *   <li>查询已绑定账号</li>
 *   <li>解绑账号</li>
 *   <li>绑定新的社交账号</li>
 * </ul>
 *
 * @author CLX
 * @since 2026-04-22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SocialBindServiceImpl implements SocialBindService {

    // ========== 常量 ==========

    private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";

    // ========== 依赖注入 ==========

    private final SocialBindMapper socialBindMapper;
    private final OAuthProperties oauthProperties;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    // ========== 查询绑定列表 ==========

    /**
     * 获取当前用户绑定的所有社交账号
     *
     * @return 绑定列表
     */
    @Override
    public List<SocialBind> getMyBinds() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("查询用户绑定的社交账号: userId={}", userId);
        return socialBindMapper.selectByUserId(userId);
    }

    // ========== 解绑 ==========

    /**
     * 解绑社交账号
     *
     * <p>流程：
     * <ol>
     *   <li>验证绑定关系属于当前用户</li>
     *   <li>删除绑定</li>
     * </ol>
     *
     * @param bindId 绑定ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long bindId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("解绑社交账号: userId={}, bindId={}", userId, bindId);

        // 查询绑定关系
        List<SocialBind> binds = socialBindMapper.selectByUserId(userId);
        SocialBind bind = binds.stream()
                .filter(b -> b.getId().equals(bindId))
                .findFirst()
                .orElse(null);

        if (bind == null) {
            log.warn("绑定关系不存在或不属于当前用户: userId={}, bindId={}", userId, bindId);
            throw ServiceException.notFound("绑定关系");
        }

        // 删除绑定
        int deleted = socialBindMapper.deleteById(bindId);
        if (deleted <= 0) {
            log.error("解绑失败: bindId={}", bindId);
            throw ServiceException.of(500, "解绑失败");
        }

        log.info("解绑成功: userId={}, bindId={}, socialType={}", userId, bindId, bind.getSocialType());
    }

    // ========== 绑定GitHub ==========

    /**
     * 绑定GitHub账号
     *
     * <p>流程：
     * <ol>
     *   <li>验证state</li>
     *   <li>用code换取access_token</li>
     *   <li>获取GitHub用户信息</li>
     *   <li>检查是否已被其他账号绑定</li>
     *   <li>创建绑定关系</li>
     * </ol>
     *
     * @param code  GitHub授权码
     * @param state 状态码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindGithub(String code, String state) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("绑定GitHub账号: userId={}, code={}", userId, code);

        // 验证state
        validateState(state);

        // 检查是否已绑定GitHub
        SocialBind existingBind = socialBindMapper.selectByUserIdAndType(userId, "github");
        if (existingBind != null) {
            log.warn("用户已绑定GitHub账号: userId={}", userId);
            throw ServiceException.alreadyExists("GitHub账号");
        }

        // 获取access_token
        String accessToken = exchangeCodeForToken(code);

        // 获取GitHub用户信息
        GithubUserInfo githubUser = fetchUserInfo(accessToken);

        // 检查GitHub账号是否已被其他用户绑定
        String githubId = String.valueOf(githubUser.getId());
        SocialBind bindByGithub = socialBindMapper.selectBySocialTypeAndId("github", githubId);
        if (bindByGithub != null && !bindByGithub.getUserId().equals(userId)) {
            log.warn("GitHub账号已被其他用户绑定: githubId={}", githubId);
            throw ServiceException.alreadyExists("该GitHub账号已被其他用户绑定");
        }

        // 创建绑定关系
        SocialBind socialBind = new SocialBind();
        socialBind.setUserId(userId);
        socialBind.setSocialType("github");
        socialBind.setSocialId(githubId);
        socialBind.setSocialName(githubUser.getLogin());
        socialBind.setSocialAvatar(githubUser.getAvatarUrl());

        int inserted = socialBindMapper.insert(socialBind);
        if (inserted <= 0) {
            log.error("创建绑定关系失败: userId={}, githubId={}", userId, githubId);
            throw ServiceException.of(500, "绑定失败");
        }

        log.info("GitHub账号绑定成功: userId={}, githubId={}, login={}", userId, githubId, githubUser.getLogin());
    }

    // ========== 私有方法 ==========

    private void validateState(String state) {
        String stateKey = "oauth:state:" + state;
        String exists = redisTemplate.opsForValue().get(stateKey);
        if (exists == null) {
            log.warn("state验证失败: state={}", state);
            throw ServiceException.validationFailed("授权已过期，请重新获取");
        }
        redisTemplate.delete(stateKey);
    }

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", oauthProperties.getGithub().getClientId());
        params.add("client_secret", oauthProperties.getGithub().getClientSecret());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GITHUB_ACCESS_TOKEN_URL, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            String error = body != null ? (String) body.get("error") : "unknown";
            log.error("GitHub access_token获取失败: error={}", error);
            throw ServiceException.of(503, "GitHub授权失败");
        }

        return (String) body.get("access_token");
    }

    private GithubUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "CLX-Community");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<GithubUserInfo> response = restTemplate.exchange(
                GITHUB_USER_INFO_URL, HttpMethod.GET, entity, GithubUserInfo.class);

        GithubUserInfo userInfo = response.getBody();
        if (userInfo == null || userInfo.getId() == null) {
            log.error("GitHub用户信息获取失败");
            throw ServiceException.of(503, "获取GitHub用户信息失败");
        }
        return userInfo;
    }
}
