# 登录功能架构文档

本文档详细讲解 CLX 项目登录功能涉及的所有后端类及其职责。

---

## 一、整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                           前端请求                                   │
└────────────────────────────────┬────────────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│  AuthController (controller/AuthController.java:38)                 │
│  - 接收 HTTP 请求                                                   │
│  - 参数校验 (@Valid)                                                │
│  - 调用 AuthService                                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│  AuthServiceImpl (service/impl/AuthServiceImpl.java:37)             │
│  - 核心业务逻辑                                                      │
│  - 验证码校验 → CaptchaService                                       │
│  - 用户查询 → UserMapper                                            │
│  - 密码校验 → BCryptPasswordEncoder                                  │
│  - 登录状态 → sa-Token (StpUtil)                                    │
│  - 失败计数 → Redis                                                  │
└────────────────────────────────┬────────────────────────────────────┘
                                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        底层依赖                                      │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │
│  │   MySQL     │  │   Redis     │  │  sa-Token   │                  │
│  │  (用户数据)  │  │ (Token存储) │  │ (JWT生成)   │                  │
│  └─────────────┘  └─────────────┘  └─────────────┘                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 二、涉及类清单

| 层级 | 类名 | 路径 | 职责 |
|------|------|------|------|
| **Controller** | `AuthController` | `clx-auth/.../controller/AuthController.java` | HTTP 接口入口 |
| **DTO** | `LoginRequest` | `clx-auth/.../dto/LoginRequest.java` | 登录请求参数 |
| **VO** | `LoginVO` | `clx-auth/.../vo/LoginVO.java` | 登录返回结果 |
| **VO** | `UserInfoVO` | `clx-auth/.../vo/UserInfoVO.java` | 用户信息返回 |
| **Service** | `AuthService` | `clx-auth/.../service/AuthService.java` | 认证接口定义 |
| **Service** | `AuthServiceImpl` | `clx-auth/.../service/impl/AuthServiceImpl.java` | 认证核心实现 |
| **Service** | `CaptchaService` | `clx-auth/.../service/CaptchaService.java` | 图形验证码服务 |
| **Entity** | `User` | `clx-auth/.../entity/User.java` | 用户实体 |
| **Mapper** | `UserMapper` | `clx-auth/.../mapper/UserMapper.java` | 数据访问接口 |
| **XML** | `UserMapper.xml` | `clx-auth/.../resources/mapper/UserMapper.xml` | SQL 映射 |
| **Common** | `R` | `clx-common-core/.../domain/R.java` | 统一响应封装 |
| **Common** | `AuthException` | `clx-common-core/.../exception/AuthException.java` | 认证异常 |
| **Common** | `TokenConstants` | `clx-common-core/.../constant/TokenConstants.java` | Token 常量 |
| **Common** | `SecurityConstants` | `clx-common-core/.../constant/SecurityConstants.java` | 安全常量 |
| **Security** | `SaTokenConfig` | `clx-common-security/.../config/SaTokenConfig.java` | BCrypt、CORS 配置 |
| **Security** | `SaTokenJwtConfig` | `clx-common-security/.../config/SaTokenJwtConfig.java` | JWT 模式配置 |
| **Security** | `StpInterfaceImpl` | `clx-common-security/.../config/StpInterfaceImpl.java` | 权限接口（暂空） |
| **Security** | `SaTokenExceptionHandler` | `clx-common-security/.../exception/SaTokenExceptionHandler.java` | 异常处理 |

---

## 三、类详细讲解

### 3.1 Controller 层

#### AuthController

**路径**: `clx-auth/src/main/java/com/clx/auth/controller/AuthController.java`

**职责**: HTTP 接口入口，接收请求、参数校验、调用 Service

```java
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;
```

**登录接口**:

```java
@PostMapping("/login")
public R<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    // 用户名标准化
    String username = request.username() == null ? "" : request.username().trim();
    
    // 处理 rememberMe
    boolean rememberMe = Boolean.TRUE.equals(request.rememberMe());
    
    // 解析客户端 IP
    String clientIp = resolveClientIp(servletRequest);

    // 调用 Service
    return R.ok(authService.login(username, request.password(), 
            request.captchaId(), request.captchaCode(), rememberMe, clientIp));
}
```

**IP 解析方法**:

```java
private String resolveClientIp(HttpServletRequest request) {
    // 优先读取代理传递的 IP
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
        return forwardedFor.split(",")[0].trim();
    }

    // 读取 X-Real-IP 头
    String realIp = request.getHeader("X-Real-IP");
    if (realIp != null && !realIp.isBlank()) {
        return realIp.trim();
    }

    // 取直接连接的 IP
    return request.getRemoteAddr();
}
```

---

### 3.2 DTO 层

#### LoginRequest

**路径**: `clx-auth/src/main/java/com/clx/auth/dto/LoginRequest.java`

**职责**: 定义登录请求参数，使用 Java 17 record 类型

```java
public record LoginRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名长度不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(max = 128, message = "密码长度不能超过128个字符")
        String password,

        @NotBlank(message = "图形验证码ID不能为空")
        String captchaId,

        @NotBlank(message = "图形验证码不能为空")
        @Size(min = 4, max = 4, message = "图形验证码必须是4位")
        String captchaCode,

        Boolean rememberMe
) {}
```

---

### 3.3 VO 层

#### LoginVO

**路径**: `clx-auth/src/main/java/com/clx/auth/vo/LoginVO.java`

**职责**: 登录成功后返回给前端的数据结构

```java
public record LoginVO(
        String token,           // JWT Token
        String tokenName,       // Token 名称
        long tokenTimeout,      // 绝对有效期（秒）
        long activeTimeout,     // 活跃有效期（秒）
        boolean rememberMe      // 是否记住我
) {}
```

---

#### UserInfoVO

**路径**: `clx-auth/src/main/java/com/clx/auth/vo/UserInfoVO.java`

**职责**: 获取当前用户信息接口的返回结构

```java
public record UserInfoVO(
        Long userId,        // 用户 ID
        String username,    // 用户名
        Object tokenInfo    // Token 详细信息
) {}
```

---

### 3.4 Service 层

#### AuthService (接口)

**路径**: `clx-auth/src/main/java/com/clx/auth/service/AuthService.java`

**职责**: 定义认证服务的接口契约

```java
public interface AuthService {
    LoginVO login(String username, String password, String captchaId, 
                   String captchaCode, boolean rememberMe, String clientIp);
    
    RegisterVO register(...);
    void logout();
    UserInfoVO getCurrentUser();
    LoginVO refreshToken();
    boolean existsByEmail(String email);
    void resetPassword(String email, String newPassword);
}
```

---

#### AuthServiceImpl (核心实现)

**路径**: `clx-auth/src/main/java/com/clx/auth/service/impl/AuthServiceImpl.java`

**职责**: 登录核心业务逻辑实现

**依赖注入**:

```java
private final UserMapper userMapper;                   // 用户数据访问
private final BCryptPasswordEncoder passwordEncoder;   // 密码加密器
private final StringRedisTemplate redisTemplate;       // Redis 操作
private final SaTokenConfig saTokenConfig;             // sa-Token 配置
private final RememberMeProperties rememberMeProperties; // 记住我配置
private final CaptchaService captchaService;           // 验证码服务
```

**login 方法核心逻辑**:

```java
public LoginVO login(...) {
    // 用户名标准化
    String normalizedUsername = normalizeUsername(username);
    String attemptKey = getAttemptKey(normalizedUsername);

    // 检查登录锁定
    checkLoginLock(attemptKey);

    // 验证图形验证码
    if (!captchaService.verifyCaptchaCode(captchaId, captchaCode)) {
        throw AuthException.captchaError();
    }

    // 查询用户
    User user = userMapper.selectByUsername(normalizedUsername);

    // 密码校验
    if (!isPasswordMatched(user, password)) {
        recordFailure(attemptKey);
        throw AuthException.loginFailed();
    }

    // 账户状态检查
    if (user.isDeleted()) { recordFailure(attemptKey); throw AuthException.loginFailed(); }
    if (user.isDisabled()) { throw AuthException.accountDisabled(); }
    if (user.isLocked()) { throw AuthException.accountLocked(); }

    // 计算有效期
    long loginTimeout = resolveLoginTimeout(rememberMe);
    long activeTimeout = resolveActiveTimeout(rememberMe, loginTimeout);

    // sa-Token 登录
    StpUtil.login(user.getUserId(), SaLoginModel.create()
            .setTimeout(loginTimeout)
            .setActiveTimeout(activeTimeout)
            .setIsLastingCookie(rememberMe));

    // 存储会话信息
    StpUtil.getSession().set("username", user.getUsername());
    StpUtil.getSession().set("nickname", user.getNickname());
    StpUtil.getSession().set("rememberMe", rememberMe);

    // 清除失败计数
    clearFailures(attemptKey);
    
    // 更新登录信息
    userMapper.updateLoginSuccess(user.getUserId(), clientIp);

    // 返回结果
    return new LoginVO(StpUtil.getTokenValue(), SecurityConstants.TOKEN_HEADER, ...);
}
```

**防时序攻击的密码校验**:

```java
private static final String DUMMY_PASSWORD_HASH =
        "$2a$10$cX1Bgw3VdxwApyokYRF3B.iYYKD5IOu/8siinuC.M6NkQSIW7A4we";

private boolean isPasswordMatched(User user, String rawPassword) {
    // 用户不存在时也执行密码比对，防止通过响应时间判断用户是否存在
    String encodedPassword = user == null ? DUMMY_PASSWORD_HASH : user.getPassword();
    return passwordEncoder.matches(rawPassword, encodedPassword) && user != null;
}
```

**登录失败锁定逻辑**:

```java
private void checkLoginLock(String attemptKey) {
    Long count = readFailureCount(attemptKey);
    // 超过5次则锁定
    if (count >= TokenConstants.MAX_LOGIN_ATTEMPT) {
        throw AuthException.tooManyAttempts();
    }
}

private void recordFailure(String attemptKey) {
    Long count = redisTemplate.opsForValue().increment(attemptKey);
    if (count != null) {
        // 设置30分钟过期
        redisTemplate.expire(attemptKey, TokenConstants.LOGIN_LOCK_TIME, TimeUnit.SECONDS);
    }
}

private String getAttemptKey(String normalizedUsername) {
    // 拼接 Redis key
    return TokenConstants.LOGIN_ATTEMPT_KEY + normalizedUsername;
}
```

**rememberMe 超时计算**:

```java
private long resolveLoginTimeout(boolean rememberMe) {
    return rememberMe 
        ? rememberMeProperties.getTimeout()    // 30天
        : saTokenConfig.getTimeout();          // 4小时
}

private long resolveActiveTimeout(boolean rememberMe, long loginTimeout) {
    long activeTimeout = rememberMe
        ? rememberMeProperties.getActiveTimeout()  // 7天
        : saTokenConfig.getActiveTimeout();        // 2小时
    
    // 活跃超时不能超过绝对超时
    if (loginTimeout > 0 && activeTimeout > loginTimeout) {
        return loginTimeout;
    }
    return activeTimeout;
}
```

---

### 3.5 Entity 层

#### User

**路径**: `clx-auth/src/main/java/com/clx/auth/entity/User.java`

**职责**: 用户实体，对应数据库表 `sys_user`

```java
public class User {
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private String password;    // 密码
    private String nickname;    // 昵称
    private String email;       // 邮箱
    private String phone;       // 手机号
    private String status;      // 状态
    private Integer isDeleted;  // 删除标记
    
    // 状态判断方法
    public boolean isDisabled() { return StatusConstants.DISABLED.equals(status); }
    public boolean isLocked() { return StatusConstants.LOCKED.equals(status); }
    public boolean isDeleted() { return Integer.valueOf(StatusConstants.DELETED).equals(isDeleted); }
}
```

---

### 3.6 Mapper 层

#### UserMapper

**路径**: `clx-auth/src/main/java/com/clx/auth/mapper/UserMapper.java`

**职责**: MyBatis Mapper 接口，定义用户数据访问方法

```java
@Mapper
public interface UserMapper {
    // 根据用户名查询用户
    User selectByUsername(@Param("username") String username);
    
    // 查询用户角色
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    
    // 查询用户权限
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
    
    // 登录成功后更新
    int updateLoginSuccess(@Param("userId") Long userId, @Param("loginIp") String loginIp);
    
    // 插入新用户
    int insert(User user);
    
    // 检查用户名是否存在
    boolean existsByUsername(@Param("username") String username);
    
    // 检查邮箱是否存在
    boolean existsByEmail(@Param("email") String email);
    
    // 更新密码
    int updatePasswordByEmail(@Param("email") String email, @Param("newPassword") String newPassword);
}
```

---

#### UserMapper.xml

**路径**: `clx-auth/src/main/resources/mapper/UserMapper.xml`

**职责**: MyBatis SQL 映射文件

**登录查询**:

```xml
<select id="selectByUsername" resultMap="UserResultMap">
    SELECT user_id, username, password, nickname, status, is_deleted
    FROM sys_user
    WHERE username = #{username}
      AND is_deleted = 0
    LIMIT 1
</select>
```

**登录成功更新**:

```xml
<update id="updateLoginSuccess">
    UPDATE sys_user
    SET last_login_ip = #{loginIp},
        last_login_time = NOW(),
        login_count = IFNULL(login_count, 0) + 1
    WHERE user_id = #{userId}
      AND is_deleted = 0
</update>
```

**用户名唯一性检查**:

```xml
<select id="existsByUsername" resultType="boolean">
    SELECT COUNT(*) > 0
    FROM sys_user
    WHERE username = #{username}
      AND is_deleted = 0
</select>
```

---

### 3.7 Common 模块

#### R (统一响应)

**路径**: `clx-common-core/src/main/java/com/clx/common/core/domain/R.java`

**职责**: 所有 API 接口的统一响应封装

```java
@Data
public class R<T> implements Serializable {
    public static final int SUCCESS = 200;
    public static final int FAIL = 500;

    private int code;           // 状态码
    private String msg;         // 消息
    private T data;             // 数据
    private long timestamp;     // 时间戳

    // 成功静态方法
    public static <T> R<T> ok() { return new R<>(SUCCESS, "操作成功", null); }
    public static <T> R<T> ok(T data) { return new R<>(SUCCESS, "操作成功", data); }

    // 失败静态方法
    public static <T> R<T> fail(String msg) { return new R<>(FAIL, msg, null); }
    public static <T> R<T> fail(int code, String msg) { return new R<>(code, msg, null); }

    // 判断方法
    public boolean isSuccess() { return SUCCESS == this.code; }
}
```

---

#### TokenConstants

**路径**: `clx-common-core/src/main/java/com/clx/common/core/constant/TokenConstants.java`

**职责**: Token 相关常量定义

```java
public final class TokenConstants {
    // Redis Key 前缀
    public static final String ACCESS_TOKEN_KEY = "clx:auth:access:";
    public static final String LOGIN_ATTEMPT_KEY = "clx:auth:attempt:";

    // 有效期（秒）
    public static final long ACCESS_TOKEN_EXPIRATION = 14400L;   // 4小时
    public static final long ACTIVE_TOKEN_EXPIRATION = 7200L;    // 2小时
    public static final long REFRESH_TOKEN_EXPIRATION = 604800L; // 7天

    // 登录失败锁定
    public static final int MAX_LOGIN_ATTEMPT = 5;      // 最大尝试次数
    public static final long LOGIN_LOCK_TIME = 1800L;   // 锁定时间
}
```

---

#### SecurityConstants

**路径**: `clx-common-core/src/main/java/com/clx/common/core/constant/SecurityConstants.java`

**职责**: 安全相关常量

```java
public final class SecurityConstants {
    public static final String TOKEN_HEADER = "Authorization";  // Token 头名
    public static final String TOKEN_PREFIX = "Bearer ";        // Token 前缀
    public static final String USER_ID_HEADER = "X-User-Id";    // 用户 ID 头
    public static final String USERNAME_HEADER = "X-Username";  // 用户名头
    public static final String ADMIN_ROLE = "admin";            // 管理员角色
}
```

---

#### AuthException

**路径**: `clx-common-core/src/main/java/com/clx/common/core/exception/AuthException.java`

**职责**: 认证相关异常，使用工厂方法创建

```java
@Getter
public class AuthException extends RuntimeException {
    private final int code;

    public AuthException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    // 静态工厂方法
    public static AuthException loginFailed() { 
        return new AuthException(ResponseCode.LOGIN_FAILED); 
    }
    public static AuthException captchaError() { 
        return new AuthException(ResponseCode.CAPTCHA_ERROR); 
    }
    public static AuthException accountLocked() { 
        return new AuthException(ResponseCode.ACCOUNT_LOCKED); 
    }
    public static AuthException tooManyAttempts() { 
        return new AuthException(ResponseCode.TOO_MANY_LOGIN_ATTEMPTS); 
    }
}
```

---

### 3.8 Security 模块

#### SaTokenConfig

**路径**: `clx-common-security/src/main/java/com/clx/common/security/config/SaTokenConfig.java`

**职责**: 安全公共配置

**BCrypt 密码加密器**:

```java
@Bean
public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**使用方式**:
```java
// 加密
String encoded = passwordEncoder.encode("admin123");
// 结果：$2a$10$xxx...（每次不同）

// 校验
boolean match = passwordEncoder.matches("admin123", encoded);
```

**CORS 跨域配置**:

```java
@Bean
public CorsFilter corsFilter(...) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(...);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(allowCredentials);
    config.setExposedHeaders(List.of("Authorization"));
    config.setMaxAge(3600L);
    return new CorsFilter(source);
}
```

---

#### SaTokenJwtConfig

**路径**: `clx-common-security/src/main/java/com/clx/common/security/config/SaTokenJwtConfig.java`

**职责**: 切换 sa-Token 为 JWT 模式

```java
@Configuration
public class SaTokenJwtConfig {
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
```

---

#### StpInterfaceImpl

**路径**: `clx-common-security/src/main/java/com/clx/common/security/config/StpInterfaceImpl.java`

**职责**: sa-Token 权限接口实现（当前返回空列表）

```java
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
```

---

#### SaTokenExceptionHandler

**路径**: `clx-common-security/src/main/java/com/clx/common/security/exception/SaTokenExceptionHandler.java`

**职责**: 统一处理 sa-Token 框架异常

```java
@RestControllerAdvice
@Order(-1)
public class SaTokenExceptionHandler {

    // 未登录异常
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<Void>> handleNotLoginException(NotLoginException e) {
        return ResponseEntity.status(401).body(R.fail(401, "请先登录"));
    }

    // 无权限异常
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<Void>> handleNotPermissionException(NotPermissionException e) {
        return ResponseEntity.status(403).body(R.fail(403, "没有权限访问"));
    }

    // 无角色异常
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<R<Void>> handleNotRoleException(NotRoleException e) {
        return ResponseEntity.status(403).body(R.fail(403, "没有权限访问"));
    }
}
```

---

### 3.9 验证码服务

#### CaptchaService

**路径**: `clx-auth/src/main/java/com/clx/auth/service/CaptchaService.java`

**职责**: 图形验证码生成与校验

```java
@Service
public class CaptchaService {
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private static final long CAPTCHA_EXPIRE_SECONDS = 600L;

    // 生成验证码
    public CaptchaResult generateCaptcha() {
        String captchaId = UUID.randomUUID().toString();
        String code = generateCode();
        String captchaImage = generateCaptchaImage(code);
        
        // 存 Redis
        redisTemplate.opsForValue().set("captcha:" + captchaId, code, 600, TimeUnit.SECONDS);
        
        return new CaptchaResult(captchaId, captchaImage);
    }

    // 校验验证码
    public boolean verifyCaptchaCode(String captchaId, String code) {
        String storedCode = redisTemplate.opsForValue().get("captcha:" + captchaId);
        if (storedCode == null) return false;
        
        boolean valid = storedCode.equalsIgnoreCase(code);
        if (valid) redisTemplate.delete("captcha:" + captchaId);
        return valid;
    }

    // 生成验证码图片
    private String generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 边框
        g.setColor(new Color(200, 200, 200));
        g.drawRect(0, 0, IMAGE_WIDTH - 1, IMAGE_HEIGHT - 1);

        // 干扰线
        for (int i = 0; i < INTERFERENCE_LINE_COUNT; i++) {
            g.setColor(randomColor(160, 200));
            g.drawLine(
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT),
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT)
            );
        }

        // 噪点
        for (int i = 0; i < NOISE_DOT_COUNT; i++) {
            g.setColor(randomColor(100, 200));
            g.fillOval(
                    random.nextInt(IMAGE_WIDTH),
                    random.nextInt(IMAGE_HEIGHT),
                    2, 2
            );
        }

        // 绘制验证码字符
        int charWidth = (IMAGE_WIDTH - 20) / CODE_LENGTH;
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(30, 130));
            g.setFont(new Font("Arial", Font.BOLD, 28 + random.nextInt(6)));
            int x = 10 + i * charWidth + random.nextInt(5);
            int y = 30 + random.nextInt(6) - 3;
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        g.dispose();

        // 转 base64
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("验证码图片生成失败", e);
        }
    }

    // 生成随机颜色
    private Color randomColor(int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }
}
```

---

## 四、登录流程图

```
┌──────────┐    POST /auth/login     ┌──────────────┐
│  前端    │ ───────────────────────▶│AuthController│
└──────────┘                         └──────┬───────┘
                                            │
                                            ▼
                                     ┌──────────────┐
                                     │ AuthService  │
                                     │   Impl       │
                                     └──────┬───────┘
                         ┌──────────────────┼──────────────────┐
                         ▼                  ▼                  ▼
                  ┌────────────┐     ┌────────────┐     ┌────────────┐
                  │CaptchaService│   │ UserMapper │     │   Redis    │
                  │(验证码校验) │     │(用户查询)  │     │(失败计数)  │
                  └────────────┘     └────────────┘     └────────────┘
                         │                  │
                         ▼                  ▼
                  ┌────────────┐     ┌────────────┐
                  │   Redis    │     │   MySQL    │
                  │(验证码存储)│     │ sys_user   │
                  └────────────┘     └────────────┘
                                            │
                         ┌──────────────────┴──────────────────┐
                         ▼                                     ▼
                  ┌────────────┐                        ┌────────────┐
                  │  sa-Token  │                        │   Redis    │
                  │StpUtil.login│                       │(JWT Token) │
                  └────────────┘                        └────────────┘
```

---

## 五、API 接口说明

### 登录接口

**请求**:
```http
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "captchaId": "uuid-xxx",
  "captchaCode": "AB3K",
  "rememberMe": true
}
```

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "tokenName": "Authorization",
    "tokenTimeout": 2592000,
    "activeTimeout": 604800,
    "rememberMe": true
  },
  "timestamp": 1713801234567
}
```

### 获取当前用户

**请求**:
```http
GET /auth/me
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "tokenInfo": {
      "tokenName": "Authorization",
      "tokenValue": "...",
      "tokenSessionTimeout": 2591000
    }
  }
}
```

### 登出

**请求**:
```http
POST /auth/logout
Authorization: Bearer xxx
```

**响应**:
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

## 六、异常处理

| 异常类型 | HTTP 状态码 | 触发场景 |
|----------|-------------|----------|
| `NotLoginException` | 401 | Token 无效/过期/被踢出 |
| `NotPermissionException` | 403 | 缺少权限 |
| `NotRoleException` | 403 | 缺少角色 |
| `SaTokenException` | 401 | 其他认证异常 |

---

## 七、安全特性

### 7.1 防暴力破解

- 登录失败 5 次后锁定账户 30 分钟
- 使用 Redis 记录失败次数
- Key 格式：`clx:auth:attempt:{username}`

### 7.2 防时序攻击

用户不存在时也执行密码比对，防止通过响应时间判断用户是否存在：

```java
String encodedPassword = user == null ? DUMMY_PASSWORD_HASH : user.getPassword();
return passwordEncoder.matches(rawPassword, encodedPassword) && user != null;
```

### 7.3 密码加密

- 使用 BCrypt 算法
- 每次加密生成不同哈希值（内置随机盐）
- 固定 60 字符输出
- 默认 10 轮计算

### 7.4 验证码保护

- 图形验证码 10 分钟过期
- 验证后立即删除（一次性使用）
- 去除易混淆字符（0/O、1/I/l）

---

## 八、有效期说明

| 场景 | 绝对有效期 | 活跃有效期 |
|------|------------|------------|
| 普通登录 | 4 小时 | 2 小时 |
| 记住我 | 30 天 | 7 天 |

- **绝对有效期**: Token 的最大存活时间，无论是否活跃都会过期
- **活跃有效期**: 无操作后自动过期时间，每次请求会刷新
