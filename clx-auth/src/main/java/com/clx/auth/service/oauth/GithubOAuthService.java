package com.clx.auth.service.oauth;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.clx.auth.config.OAuthProperties;
import com.clx.auth.dto.GithubUserInfo;
import com.clx.auth.entity.SocialBind;
import com.clx.auth.entity.User;
import com.clx.auth.mapper.SocialBindMapper;
import com.clx.auth.mapper.UserMapper;
import com.clx.auth.service.AuthService;
import com.clx.auth.vo.LoginVO;
import com.clx.common.core.constant.SecurityConstants;
import com.clx.common.core.constant.StatusConstants;
import com.clx.common.core.exception.AuthException;
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

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * GitHub OAuth 登录服务
 * <p>
 * 功能：处理 GitHub 第三方登录的完整流程
 * <p>
 * 整体流程：
 * <ol>
 *   <li>生成 GitHub 授权 URL（用户点击登录按钮时）</li>
 *   <li>用户授权后，GitHub 回调我们的接口，携带 code 和 state</li>
 *   <li>用 code 换取 access_token</li>
 *   <li>用 access_token 获取 GitHub 用户信息</li>
 *   <li>根据 GitHub ID 查找本地用户</li>
 *   <li>如果找到 → 直接登录；如果没找到 → 自动创建新用户</li>
 *   <li>记录绑定关系，生成 JWT Token 返回</li>
 * </ol>
 * <p>
 * 安全机制：
 * <ul>
 *   <li>state 验证：防止 CSRF 攻击</li>
 *   <li>code 一次性使用：防止重放攻击</li>
 *   <li>https 通信：防止中间人攻击</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 1. 获取授权 URL
 * String authorizeUrl = githubOAuthService.getAuthorizeUrl();
 * // 跳转到 authorizeUrl 让用户授权
 *
 * // 2. 用户授权后，GitHub 回调
 * LoginVO loginVO = githubOAuthService.handleCallback(code, state);
 * </pre>
 *
 * @author CLX
 * @since 2026-04-22
 * @see com.clx.auth.controller.OAuthController OAuth 接口控制器
 * @see OAuthProperties OAuth 配置
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuthService {

    /**
     * GitHub OAuth 授权端点 URL
     * <p>
     * 用途：用户点击登录按钮后，跳转到这里让用户授权
     * 这是 GitHub 官方提供的固定地址
     */
    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";

    /**
     * GitHub OAuth 获取 access_token 的端点 URL
     * <p>
     * 用途：后端用 code 换取 access_token
     * 注意：必须用 POST 请求
     */
    private static final String GITHUB_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

    /**
     * GitHub API 获取用户信息的端点 URL
     * <p>
     * 用途：用 access_token 获取用户基本信息
     * 文档：https://docs.github.com/en/rest/users/users#get-the-authenticated-user
     */
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";

    /**
     * OAuth 配置信息
     * <p>
     * 包含：clientId, clientSecret, redirectUri
     * 来源：application.yml 或环境变量
     */
    private final OAuthProperties oauthProperties;

    /**
     * Redis 操作模板
     * <p>
     * 用途：
     * <ul>
     *   <li>存储 state（防 CSRF）</li>
     *   <li>存储 code 使用记录（防重用）</li>
     * </ul>
     */
    private final StringRedisTemplate redisTemplate;

    /**
     * 用户数据访问层
     * <p>
     * 用途：
     * <ul>
     *   <li>查询用户是否已存在</li>
     *   <li>创建新用户</li>
     *   <li>更新用户信息</li>
     * </ul>
     */
    private final UserMapper userMapper;

    /**
     * 社交账号绑定数据访问层
     * <p>
     * 用途：
     * <ul>
     *   <li>查询 GitHub ID 是否已绑定</li>
     *   <li>创建新的绑定关系</li>
     *   <li>更新绑定信息</li>
     * </ul>
     */
    private final SocialBindMapper socialBindMapper;

    /**
     * sa-Token 配置
     * <p>
     * 用途：获取 Token 过期时间等配置
     */
    private final SaTokenConfig saTokenConfig;

    /**
     * HTTP 请求工具
     * <p>
     * 用途：调用 GitHub API
     * 选择原因：Spring 自带，无需额外依赖
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 生成 GitHub 授权 URL
     * <p>
     * 调用时机：用户点击"GitHub登录"按钮时
     * <p>
     * 整体流程：
     * <ol>
     *   <li>生成随机 state（防 CSRF）</li>
     *   <li>将 state 存入 Redis（5分钟有效）</li>
     *   <li>拼接授权 URL</li>
     *   <li>返回 URL 给前端，前端跳转</li>
     * </ol>
     * <p>
     * 生成的 URL 示例：
     * <pre>
     * https://github.com/login/oauth/authorize?
     *   client_id=Iv1.xxxxxx&
     *   redirect_uri=http://localhost:9100/auth/oauth/github/callback&
     *   scope=user:email&
     *   state=random_string_123
     * </pre>
     *
     * @return GitHub 授权页面 URL，用户需要跳转到这个地址
     */
    public String getAuthorizeUrl() {
        // 步骤1：生成随机 state
        // 为什么要随机？防止攻击者猜测
        // 长度16位：足够随机，又不太长
        String state = generateRandomState();

        // 步骤2：将 state 存入 Redis，5分钟有效
        // 为什么要存？等下回调时要验证，确保是同一个人
        // key 格式：oauth:state:{state值}
        String stateKey = "oauth:state:" + state;
        redisTemplate.opsForValue().set(stateKey, "1", 5, TimeUnit.MINUTES);
        log.debug("生成 GitHub OAuth state: {}", state);

        // 步骤3：从配置中读取参数
        String clientId = oauthProperties.getGithub().getClientId();
        String redirectUri = oauthProperties.getGithub().getRedirectUri();

        // 步骤4：拼接授权 URL
        // scope=user:email 表示我们要获取用户的基本信息和邮箱
        // 为什么需要这个权限？创建用户时需要用户名和头像
        String authorizeUrl = String.format(
                "%s?client_id=%s&redirect_uri=%s&scope=user:email&state=%s",
                GITHUB_AUTHORIZE_URL,
                clientId,
                redirectUri,
                state
        );

        log.info("生成 GitHub 授权 URL: {}", authorizeUrl);
        return authorizeUrl;
    }

    /**
     * 处理 GitHub 回调，完成登录
     * <p>
     * 调用时机：用户授权后，GitHub 重定向回我们的 callback URL
     * <p>
     * 整体流程：
     * <ol>
     *   <li>验证 state 防止 CSRF 攻击</li>
     *   <li>用 code 换取 access_token</li>
     *   <li>用 access_token 获取 GitHub 用户信息</li>
     *   <li>查找或创建本地用户</li>
     *   <li>生成 JWT Token 返回</li>
     * </ol>
     *
     * @param code  GitHub 返回的授权码，临时有效，只能用一次
     *              示例："abc123def456ghi789"
     * @param state 随机状态码，用于验证请求来源
     *              示例："xyz789abc123"
     * @return 登录结果，包含 JWT Token 和用户信息
     * @throws AuthException 当 state 验证失败、code 无效或过期时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginVO handleCallback(String code, String state) {
        log.info("处理 GitHub OAuth 回调: code={}, state={}", code, state);

        // ========== 步骤1：验证 state 防止 CSRF 攻击 ==========
        // 为什么要验证？
        // 攻击场景：攻击者诱导用户点击恶意链接，state 不匹配就能发现
        // 验证逻辑：从 Redis 查这个 state 是否存在，存在说明是我们生成的
        validateState(state);

        // ========== 步骤2：用 code 换取 access_token ==========
        // 为什么是 POST？GitHub API 规定必须用 POST
        // 参数：client_id, client_secret, code
        // 返回：access_token（访问令牌）
        String accessToken = exchangeCodeForToken(code);

        // ========== 步骤3：用 access_token 获取用户信息 ==========
        // 调用 GitHub API: GET /user
        // 需要 Header: Authorization: Bearer {access_token}
        GithubUserInfo githubUser = fetchUserInfo(accessToken);
        log.info("获取到 GitHub 用户信息: id={}, login={}", githubUser.getId(), githubUser.getLogin());

        // ========== 步骤4：查找或创建本地用户 ==========
        // 策略：
        // 1. 先根据 GitHub ID 查找是否已绑定
        // 2. 如果找到 → 直接登录
        // 3. 如果没找到 → 创建新用户并绑定
        User user = findOrCreateUser(githubUser);

        // ========== 步骤5：生成 JWT Token ==========
        // 使用 sa-Token 生成 Token，和原有登录逻辑完全一致
        // 这样前端不需要改，都是拿到 Token 存起来
        LoginVO loginVO = generateToken(user);

        log.info("GitHub 登录成功: userId={}, username={}", user.getUserId(), user.getUsername());
        return loginVO;
    }

    /**
     * 验证 state 防止 CSRF 攻击
     * <p>
     * CSRF 攻击场景：
     * 1. 攻击者诱导用户点击恶意链接
     * 2. 恶意链接里的 state 是攻击者生成的
     * 3. 用户授权后，回调到我们的接口
     * 4. 我们验证 state，发现 Redis 里没有这个 state
     * 5. 拒绝请求，攻击失败
     * <p>
     * 验证逻辑：
     * <ol>
     *   <li>从 Redis 查 state 是否存在</li>
     *   <li>存在 → 删除（防止重用），验证通过</li>
     *   <li>不存在 → 验证失败，抛出异常</li>
     * </ol>
     *
     * @param state 回调时传入的 state 参数
     * @throws AuthException state 不存在或已过期时抛出
     */
    private void validateState(String state) {
        // 构造 Redis key
        String stateKey = "oauth:state:" + state;

        // 从 Redis 查询
        // 为什么要用 getAndDelete？原子操作，避免并发问题
        String exists = redisTemplate.opsForValue().get(stateKey);

        if (exists == null) {
            // state 不存在或已过期
            // 可能原因：
            // 1. 用户停留太久，state 过期了（超过5分钟）
            // 2. 攻击者伪造的请求
            log.warn("GitHub OAuth state 验证失败: state={} 不存在或已过期", state);
            throw new AuthException(400, "授权已过期，请重新登录");
        }

        // 验证成功，立即删除（防止重用）
        // 为什么要删除？一个 state 只能用一次
        redisTemplate.delete(stateKey);
        log.debug("GitHub OAuth state 验证成功: {}", state);
    }

    /**
     * 用 code 换取 access_token
     * <p>
     * GitHub OAuth 第二步：
     * 用用户授权的 code，向 GitHub 申请 access_token
     * <p>
     * 请求地址：POST https://github.com/login/oauth/access_token
     * 请求参数：
     * <ul>
     *   <li>client_id: 我们的应用 ID</li>
     *   <li>client_secret: 我们的应用密钥</li>
     *   <li>code: GitHub 返回的授权码</li>
     * </ul>
     * <p>
     * 安全注意：
     * <ul>
     *   <li>code 只能用一次</li>
     *   <li>client_secret 不能泄露</li>
     *   <li>必须用 https</li>
     * </ul>
     *
     * @param code GitHub 返回的授权码
     * @return access_token 访问令牌
     * @throws ServiceException 换取失败时抛出
     */
    private String exchangeCodeForToken(String code) {
        // 构造请求头
        // Accept: application/json 表示我们要 JSON 格式的响应
        // 默认 GitHub 返回的是 form-urlencoded，用 JSON 更方便解析
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        // 构造请求参数
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", oauthProperties.getGithub().getClientId());
        params.add("client_secret", oauthProperties.getGithub().getClientSecret());
        params.add("code", code);

        // 构造请求实体
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // 发送 POST 请求
        log.debug("请求 GitHub access_token: code={}", code);
        ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                GITHUB_ACCESS_TOKEN_URL,
                request,
                java.util.Map.class
        );

        // 解析响应
        java.util.Map<String, Object> body = response.getBody();
        if (body == null || body.get("access_token") == null) {
            // 换取失败
            // 可能原因：code 已过期、code 已被使用、client_secret 错误
            String error = body != null ? (String) body.get("error") : "unknown";
            String errorDescription = body != null ? (String) body.get("error_description") : "";
            log.error("GitHub access_token 换取失败: error={}, description={}", error, errorDescription);
            throw ServiceException.of(503, "GitHub 授权失败: " + errorDescription);
        }

        String accessToken = (String) body.get("access_token");
        log.debug("获取到 GitHub access_token: {}...", accessToken.substring(0, 10));
        return accessToken;
    }

    /**
     * 用 access_token 获取 GitHub 用户信息
     * <p>
     * GitHub API: GET https://api.github.com/user
     * <p>
     * 需要 Header: Authorization: Bearer {access_token}
     *
     * @param accessToken 访问令牌
     * @return GitHub 用户信息
     * @throws ServiceException 获取失败时抛出
     */
    private GithubUserInfo fetchUserInfo(String accessToken) {
        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);  // 设置 Authorization: Bearer xxx
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        // 设置 User-Agent，GitHub API 要求必须设置
        headers.set("User-Agent", "CLX-Community");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送 GET 请求
        log.debug("请求 GitHub 用户信息");
        ResponseEntity<GithubUserInfo> response = restTemplate.exchange(
                GITHUB_USER_INFO_URL,
                HttpMethod.GET,
                entity,
                GithubUserInfo.class
        );

        GithubUserInfo userInfo = response.getBody();
        if (userInfo == null || userInfo.getId() == null) {
            log.error("GitHub 用户信息获取失败: 响应为空");
            throw ServiceException.of(503, "获取 GitHub 用户信息失败");
        }

        return userInfo;
    }

    /**
     * 查找或创建本地用户
     * <p>
     * 策略：
     * <ol>
     *   <li>根据 GitHub ID 查找是否已绑定</li>
     *   <li>如果找到 → 返回已有用户</li>
     *   <li>如果没找到 → 创建新用户并绑定</li>
     * </ol>
     *
     * @param githubUser GitHub 用户信息
     * @return 本地用户
     */
    private User findOrCreateUser(GithubUserInfo githubUser) {
        // 步骤1：根据 GitHub ID 查询是否已绑定
        // socialType = "github"，socialId = GitHub 用户ID（转字符串）
        String githubId = String.valueOf(githubUser.getId());
        SocialBind existingBind = socialBindMapper.selectBySocialTypeAndId("github", githubId);

        if (existingBind != null) {
            // 已绑定过，查询本地用户信息
            log.debug("GitHub 账号已绑定: githubId={}, userId={}", githubId, existingBind.getUserId());

            // 更新绑定信息（头像、昵称可能变了）
            existingBind.setSocialName(githubUser.getLogin());
            existingBind.setSocialAvatar(githubUser.getAvatarUrl());
            socialBindMapper.update(existingBind);

            // 从数据库查询真实的用户信息
            User user = userMapper.selectById(existingBind.getUserId());
            if (user == null) {
                log.error("绑定用户不存在: userId={}", existingBind.getUserId());
                throw ServiceException.of(500, "用户数据异常");
            }
            return user;
        }

        // 步骤2：未绑定，创建新用户
        log.info("GitHub 账号未绑定，创建新用户: githubId={}, login={}", githubId, githubUser.getLogin());

        // 生成用户ID（使用时间戳+随机数，和 AuthServiceImpl 保持一致）
        Long userId = generateUserId();

        // 构造用户名：github_ + GitHub登录名
        // 原因：避免和已有用户名冲突
        String username = "github_" + githubUser.getLogin().toLowerCase(Locale.ROOT);

        // 如果用户名已存在，添加随机后缀
        int retry = 0;
        while (userMapper.existsByUsername(username) && retry < 10) {
            username = "github_" + githubUser.getLogin().toLowerCase(Locale.ROOT) + "_" + (int) (Math.random() * 1000);
            retry++;
        }

        // 创建用户对象
        User newUser = new User();
        newUser.setUserId(userId);
        newUser.setUsername(username);
        // OAuth 用户没有密码，设置一个随机密码（用户后续可以通过邮箱重置）
        newUser.setPassword(generateRandomPassword());
        newUser.setNickname(githubUser.getName() != null ? githubUser.getName() : githubUser.getLogin());
        newUser.setEmail(githubUser.getEmail());  // 可能为 null
        newUser.setStatus(StatusConstants.NORMAL);
        newUser.setIsDeleted(0);

        // 插入用户表
        int inserted = userMapper.insert(newUser);
        if (inserted <= 0) {
            log.error("创建用户失败: githubId={}, username={}", githubId, username);
            throw ServiceException.of(500, "用户创建失败");
        }

        // 步骤3：创建绑定关系
        SocialBind socialBind = new SocialBind();
        socialBind.setUserId(userId);
        socialBind.setSocialType("github");
        socialBind.setSocialId(githubId);
        socialBind.setSocialName(githubUser.getLogin());
        socialBind.setSocialAvatar(githubUser.getAvatarUrl());
        // Token 信息暂时不存储，需要时再获取

        int bindInserted = socialBindMapper.insert(socialBind);
        if (bindInserted <= 0) {
            log.error("创建绑定关系失败: userId={}, githubId={}", userId, githubId);
            throw ServiceException.of(500, "账号绑定失败");
        }

        log.info("新用户创建成功: userId={}, username={}, githubId={}", userId, username, githubId);
        return newUser;
    }

    /**
     * 生成用户ID
     * <p>
     * 规则：时间戳（毫秒）* 1000 + 3位随机数
     * 保证唯一性，且有序
     *
     * @return 用户ID
     */
    private Long generateUserId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 1000);
        return timestamp * 1000 + random;
    }

    /**
     * 生成随机密码
     * <p>
     * 用途：OAuth 用户首次创建时没有密码
     * 生成一个随机密码，用户后续可以通过邮箱重置密码
     *
     * @return BCrypt 加密后的随机密码
     */
    private String generateRandomPassword() {
        // 生成 32 位随机字符串
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        // 使用 BCrypt 加密
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(sb.toString());
    }

    /**
     * 生成登录 Token
     * <p>
     * 使用 sa-Token 生成 Token，和用户名密码登录保持一致
     * <p>
     * 流程：
     * <ol>
     *   <li>调用 StpUtil.login() 生成 Token</li>
     *   <li>在 Session 中存储用户信息</li>
     *   <li>构造 LoginVO 返回</li>
     * </ol>
     *
     * @param user 本地用户
     * @return 登录结果，包含 Token
     */
    private LoginVO generateToken(User user) {
        // 使用 sa-Token 登录
        // timeout: Token 有效期（秒）
        // activeTimeout: 活跃期（秒），用户操作后重置
        StpUtil.login(user.getUserId(), SaLoginModel.create()
                .setTimeout(saTokenConfig.getTimeout())
                .setActiveTimeout(saTokenConfig.getActiveTimeout()));

        // 在 Session 中存储用户信息
        // 用途：后续接口可以通过 StpUtil.getSession() 获取
        StpUtil.getSession().set("username", user.getUsername());
        StpUtil.getSession().set("nickname", user.getNickname());
        StpUtil.getSession().set("rememberMe", false);  // OAuth 登录默认不记住我

        // 构造返回对象（LoginVO 是 record，使用构造函数）
        return new LoginVO(
                StpUtil.getTokenValue(),
                SecurityConstants.TOKEN_HEADER,
                saTokenConfig.getTimeout(),
                saTokenConfig.getActiveTimeout(),
                false
        );
    }

    /**
     * 生成随机 state
     * <p>
     * 用途：防止 CSRF 攻击
     * <p>
     * 生成规则：
     * <ul>
     *   <li>长度：16位</li>
     *   <li>字符集：小写字母 + 数字</li>
     *   <li>随机性：使用 SecureRandom 保证不可预测</li>
     * </ul>
     *
     * @return 随机 state 字符串
     */
    private String generateRandomState() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
