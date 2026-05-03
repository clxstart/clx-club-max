package com.clx.auth.service.impl;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.config.OAuthProperties;
import com.clx.auth.oauth.GithubUserInfo;
import com.clx.auth.entity.SocialBind;
import com.clx.auth.entity.User;
import com.clx.auth.enums.OAuthPlatform;
import com.clx.auth.mapper.SocialBindMapper;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.OAuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.constant.StatusConstants;
import com.clx.common.core.exception.AuthException;
import com.clx.common.core.exception.ServiceException;
import com.clx.common.security.constant.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * GitHub OAuth 登录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuthServiceImpl implements OAuthService {

    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";
    private static final String REDIS_STATE_KEY_PREFIX = "oauth:state:";
    private static final String REDIS_CODE_KEY_PREFIX = "oauth:code:used:";

    private final OAuthProperties oauthProperties;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;
    private final SocialBindMapper socialBindMapper;
    private final SaTokenConfig saTokenConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuthPlatform getPlatform() {
        return OAuthPlatform.GITHUB;
    }

    @Override
    public String getAuthorizeUrl() {
        String state = generateRandomState();
        String stateKey = REDIS_STATE_KEY_PREFIX + state;
        redisTemplate.opsForValue().set(stateKey, "1", 5, TimeUnit.MINUTES);

        String clientId = oauthProperties.getGithub().getClientId();
        String redirectUri = oauthProperties.getGithub().getRedirectUri();
        String authorizeUrl = String.format(
                "%s?client_id=%s&redirect_uri=%s&scope=user:email&state=%s",
                GITHUB_AUTHORIZE_URL, clientId, redirectUri, state
        );

        log.info("生成 GitHub 授权 URL: {}", authorizeUrl);
        return authorizeUrl;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO handleCallback(String code, String state) {
        log.info("处理 GitHub OAuth 回调: code={}, state={}", code, state);

        validateState(state);
        String accessToken = exchangeCodeForToken(code);
        GithubUserInfo githubUser = fetchUserInfo(accessToken);
        User user = findOrCreateUser(githubUser);
        LoginVO loginVO = generateToken(user);

        log.info("GitHub 登录成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return loginVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindAccount(String code, String state) {
        log.info("绑定 GitHub 账号: code={}", code);

        validateState(state);
        String accessToken = exchangeCodeForToken(code);
        GithubUserInfo githubUser = fetchUserInfo(accessToken);

        Long currentUserId = StpUtil.getLoginIdAsLong();
        String githubId = String.valueOf(githubUser.getId());

        SocialBind existingBind = socialBindMapper.selectBySocialTypeAndId("github", githubId);
        if (existingBind != null) {
            if (existingBind.getUserId().equals(currentUserId)) {
                throw ServiceException.validationFailed("该 GitHub 账号已绑定到当前用户");
            }
            throw ServiceException.validationFailed("该 GitHub 账号已被其他用户绑定");
        }

        SocialBind socialBind = new SocialBind();
        socialBind.setUserId(currentUserId);
        socialBind.setSocialType("github");
        socialBind.setSocialId(githubId);
        socialBind.setSocialName(githubUser.getLogin());
        socialBind.setSocialAvatar(githubUser.getAvatarUrl());
        socialBind.setBindTime(LocalDateTime.now());

        int inserted = socialBindMapper.insert(socialBind);
        if (inserted <= 0) {
            throw ServiceException.of(500, "账号绑定失败");
        }

        log.info("GitHub 账号绑定成功: userId={}, githubId={}", currentUserId, githubId);
    }

    private void validateState(String state) {
        String stateKey = REDIS_STATE_KEY_PREFIX + state;
        String exists = redisTemplate.opsForValue().get(stateKey);

        if (exists == null) {
            log.warn("GitHub OAuth state 验证失败: state={}", state);
            throw new AuthException(400, "授权已过期，请重新登录");
        }
        redisTemplate.delete(stateKey);
    }

    private String exchangeCodeForToken(String code) {
        String codeKey = REDIS_CODE_KEY_PREFIX + code;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(codeKey))) {
            throw new AuthException(400, "授权码已使用");
        }
        redisTemplate.opsForValue().set(codeKey, "1", 10, TimeUnit.MINUTES);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", oauthProperties.getGithub().getClientId());
        params.add("client_secret", oauthProperties.getGithub().getClientSecret());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<java.util.Map> response = restTemplate.postForEntity(GITHUB_ACCESS_TOKEN_URL, request, java.util.Map.class);

        java.util.Map<String, Object> body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            String error = body != null ? (String) body.get("error") : "unknown";
            String errorDesc = body != null ? (String) body.get("error_description") : "";
            log.error("GitHub access_token 获取失败: error={}, description={}", error, errorDesc);
            throw ServiceException.of(503, "GitHub 授权失败: " + errorDesc);
        }

        return (String) body.get("access_token");
    }

    private GithubUserInfo fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "CLX-Community");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<GithubUserInfo> response = restTemplate.exchange(GITHUB_USER_INFO_URL, HttpMethod.GET, entity, GithubUserInfo.class);

        GithubUserInfo userInfo = response.getBody();
        if (userInfo == null || userInfo.getId() == null) {
            throw ServiceException.of(503, "获取 GitHub 用户信息失败");
        }
        return userInfo;
    }

    private User findOrCreateUser(GithubUserInfo githubUser) {
        String githubId = String.valueOf(githubUser.getId());
        SocialBind existingBind = socialBindMapper.selectBySocialTypeAndId("github", githubId);

        if (existingBind != null) {
            existingBind.setSocialName(githubUser.getLogin());
            existingBind.setSocialAvatar(githubUser.getAvatarUrl());
            socialBindMapper.update(existingBind);

            User user = userMapper.selectById(existingBind.getUserId());
            if (user == null) {
                throw ServiceException.of(500, "用户数据异常");
            }
            return user;
        }

        Long userId = System.currentTimeMillis() * 1000 + (int) (Math.random() * 1000);
        String username = "github_" + githubUser.getLogin().toLowerCase(Locale.ROOT);

        int retry = 0;
        while (userMapper.existsByUsername(username) && retry < 10) {
            username = "github_" + githubUser.getLogin().toLowerCase(Locale.ROOT) + "_" + (int) (Math.random() * 1000);
            retry++;
        }

        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUsername(username);
        newUser.setPassword(generateRandomPassword());
        newUser.setNickname(githubUser.getName() != null ? githubUser.getName() : githubUser.getLogin());
        newUser.setEmail(githubUser.getEmail());
        newUser.setStatus(StatusConstants.NORMAL);
        newUser.setIsDeleted(0);

        if (userMapper.insert(newUser) <= 0) {
            throw ServiceException.of(500, "用户创建失败");
        }

        SocialBind socialBind = new SocialBind();
        socialBind.setUserId(userId);
        socialBind.setSocialType("github");
        socialBind.setSocialId(githubId);
        socialBind.setSocialName(githubUser.getLogin());
        socialBind.setSocialAvatar(githubUser.getAvatarUrl());
        socialBind.setBindTime(LocalDateTime.now());

        if (socialBindMapper.insert(socialBind) <= 0) {
            throw ServiceException.of(500, "账号绑定失败");
        }

        log.info("新用户创建成功: userId={}, username={}", userId, username);
        return newUser;
    }

    private LoginVO generateToken(User user) {
        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(saTokenConfig.getTimeout())
                .setActiveTimeout(saTokenConfig.getActiveTimeout()));

        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());
        StpUtil.getSession().set("rememberMe", false);

        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                saTokenConfig.getTimeout(),
                saTokenConfig.getActiveTimeout(),
                false
        );
    }

    private String generateRandomState() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(sb.toString());
    }
}
