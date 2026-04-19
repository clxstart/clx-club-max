# CLX 超大型社区项目 - 登录功能实现规划

> 本文档记录项目的整体设计思路、技术选型、架构规划，作为学习参考资料。

---

## 一、项目背景

### 1.1 项目性质

这是一个**超大型社区项目**（类似知乎、贴吧、微博），登录功能是项目第一阶段的里程碑。

**核心特点**：
- 微服务架构（Spring Cloud全家桶）
- 企业级SSO（LDAP、企业微信、钉钉等）
- 大规模用户（>100万）
- 分布式部署
- **AI Agent集成**（智能助手、内容审核、推荐系统）

### 1.2 第一阶段目标

**聚焦登录功能**：
- 账号密码登录
- JWT Token机制
- 企业级SSO集成
- API网关认证

**后续功能规划**（不在本阶段
- 私信/通知
- 用户关注/好友
- 内容审核（AI Agent）
- 搜索推荐（AI增强）
- **智能助手**（社区AI问答）

---

## 二、技术选型

### 2.1 核心版本

| 组件 | 版本 | 选择原因 |
|------|------|---------|
| **JDK** | **21** | 虚拟线程正式版、分代ZGC、性能大幅提升 |
| **Spring Boot** | 3.2.5 | 支持JDK 21虚拟线程，最新稳定版 |
| **Spring Cloud** | 2023.0.1 | 与Boot 3.2兼容 |
| **Spring Cloud Alibaba** | 2023.0.1.0 | Nacos集成 |
| **Spring Security** | 6.2.x | 原生OAuth2支持，Boot 3内置 |
| **MyBatis-Plus** | 3.5.5 | 简化CRUD，支持Boot 3 |
| **ShardingSphere** | 5.4.1 | 分库分表（后续） |
| **JWT (jjwt)** | 0.12.5 | Token生成解析（跨服务传递） |
| **sa-Token** | 1.39.0 | 权限认证、角色控制、踢人下线 |
| **Spring Security** | 6.2.x | OAuth2授权服务器、SSO |

### 2.5 安全架构：Spring Security OAuth2 + sa-Token 混合

**设计理念**：Spring Security 负责 OAuth2 SSO，sa-Token 负责权限控制。

```
┌─────────────────────────────────────────────────────────────┐
│                    安全架构总览                              │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Spring Security OAuth2 负责：                             │
│   ├── OAuth2 授权服务器（/oauth2/authorize）                │
│   ├── Token 发放（/oauth2/token）                           │
│   ├── OIDC 支持                                             │
│   ├── 第三方应用接入（企业微信、钉钉等）                      │
│   └── 企业级 SSO                                            │
│                                                             │
│   sa-Token 负责：                                           │
│   ├── 权限认证（@SaCheckPermission）                        │
│   ├── 角色认证（@SaCheckRole）                              │
│   ├── 踢人下线                                              │
│   ├── 账号封禁                                              │
│   └── Session管理                                           │
│                                                             │
│   JWT 负责：                                                │
│   ├── 跨服务 Token 传递                                     │
│   └── 用户信息携带                                          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**核心配置文件**：

| 文件 | 职责 |
|------|------|
| `SecurityConfig.java` | Spring Security 基础配置，多过滤链 |
| `AuthorizationServerConfig.java` | OAuth2授权服务器，JWT密钥管理 |
| `SaTokenConfig.java` | sa-Token整合JWT配置 |
| `SaTokenWebConfig.java` | sa-Token路径拦截配置 |
| `StpInterfaceImpl.java` | 权限/角色接口实现 |

**OAuth2 端点**：

| 端点 | 用途 |
|------|------|
| `/oauth2/authorize` | 授权页面 |
| `/oauth2/token` | Token发放 |
| `/oauth2/introspect` | Token查询 |
| `/oauth2/revoke` | Token撤销 |
| `/.well-known/openid-configuration` | OIDC发现 |

**sa-Token 权限注解**：

```java
// 检查登录
@SaCheckLogin
public void api() { }

// 检查角色
@SaCheckRole("admin")
public void adminApi() { }

// 检查权限
@SaCheckPermission("user:add")
public void addUser() { }
```

### 2.6 登录验证方式（完整支持）

```
┌─────────────────────────────────────────────────────────────┐
│                    多登录方式架构                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  基础登录                                                    │
│  ┌───────────────┐  ┌───────────────┐                      │
│  │ 账号密码      │  │ 短信验证码    │                      │
│  │ + 图形验证码  │  │ 手机号登录    │                      │
│  │ CaptchaService│  │ SmsCaptcha    │                      │
│  └───────────────┘  └───────────────┘                      │
│                                                             │
│  第三方登录                                                  │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  │
│  │ 微信扫码      │  │ 企业微信      │  │ 钉钉          │  │
│  │ WeChatLogin   │  │ OAuth2        │  │ OAuth2        │  │
│  └───────────────┘  └───────────────┘  └───────────────┘  │
│                                                             │
│  二次认证                                                    │
│  ┌───────────────┐                                         │
│  │ TOTP动态口令  │                                         │
│  │ Google Authenticator / 钉钉OTP                         │
│  │ TotpService   │                                         │
│  └───────────────┘                                         │
│                                                             │
│  行为验证                                                    │
│  ┌───────────────┐                                         │
│  │ 滑块验证码    │                                         │
│  │ SliderCaptcha │                                         │
│  └───────────────┘                                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**验证方式对应的类**：

| 类名 | 功能 | 使用场景 |
|------|------|---------|
| `CaptchaService` | 图形验证码（算术/字符/中文） | 登录防暴力破解 |
| `SliderCaptchaService` | 滑块验证码 | 行为验证、防机器人 |
| `SmsCaptchaService` | 短信验证码 | 手机号登录、二次认证 |
| `TotpService` | TOTP动态口令 | 高安全场景、敏感操作 |
| `WeChatLoginService` | 微信扫码登录 | C端用户、便捷登录 |

**使用示例**：

```java
// 1. 图形验证码
Map<String, Object> captcha = captchaService.generateArithmeticCaptcha();
// 返回：{captchaKey, captchaImage, expiration}

// 2. 短信验证码
smsCaptchaService.sendCaptcha("13800138000");

// 3. TOTP绑定
TotpSetupResult result = totpService.setupTotp(userId, "admin", "CLX社区");
// 返回：{secretKey, qrCodeUrl, backupCodes}

// 4. 微信扫码登录
WeChatQrCodeResult qr = weChatService.generateQrCode();
// 返回：{state, qrCodeUrl}
```

### 2.2 JDK 21 新特性（本项目将使用）

| 特性 | 说明 | 应用场景 |
|------|------|---------|
| **虚拟线程 (Virtual Threads)** | 轻量级线程，百万级并发 | 高并发登录、API请求处理 |
| **分代ZGC (Generational ZGC)** | 低延迟GC，大内存友好 | 高并发场景内存管理 |
| **Record模式匹配** | 简化数据类 | DTO、响应对象 |
| **字符串模板（预览）** | 安全字符串拼接 | SQL构建、日志格式化 |
| **结构化并发（预览）** | 简化并发任务管理 | 多服务调用编排 |

**虚拟线程示例**：
```java
// 传统线程：创建1000个线程会消耗大量内存
for (int i = 0; i < 1000; i++) {
    new Thread(() -> handleRequest()).start();  // 每个线程约1MB
}

// 虚拟线程：可以轻松创建百万级
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1_000_000; i++) {
        executor.submit(() -> handleRequest());  // 几乎不占内存
    }
}
```

### 2.3 Agent/AI 技术选型（后续接入）

| 技术 | 版本 | 职责 | 使用场景 |
|------|------|------|---------|
| **Spring AI** | 1.0.0-M4 | Spring官方AI框架 | OpenAI/Claude集成、Prompt管理 |
| **LangChain4j** | 0.36.2 | Java版LangChain | AI应用开发、Chain编排、Memory管理 |
| **Milvus** | 2.4.1 | 向量数据库 | 文档检索、语义搜索、RAG |
| **ONNX Runtime** | 1.17.1 | 本地推理引擎 | 本地Embedding计算、降低API调用成本 |

**Agent架构设计**（后续实现）：

```
┌─────────────────────────────────────────────────────────────┐
│                    clx-agent (AI智能服务)                    │
│                        端口: 9400                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ ChatAgent   │  │ AuditAgent  │  │ Recommend   │        │
│  │ (对话助手)   │  │ (内容审核)   │  │ Agent       │        │
│  └─────────────┘  └─────────────┘  │ (推荐系统)   │        │
│         │                │         └─────────────┘        │
│         │                │                │               │
│         ▼                ▼                ▼               │
│  ┌─────────────────────────────────────────────┐          │
│  │           LangChain4j Chain                  │          │
│  │  - PromptTemplate                            │          │
│  │  - Memory (对话记忆)                          │          │
│  │  - Tools (工具调用)                           │          │
│  │  - OutputParser                              │          │
│  └─────────────────────────────────────────────┘          │
│                       │                                    │
│                       ▼                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  OpenAI     │  │  Claude     │  │  本地模型    │        │
│  │  (GPT-4)    │  │  (Anthropic)│  │  (Ollama)   │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
│                                                             │
│  ┌─────────────────────────────────────────────┐          │
│  │              Milvus Vector DB                │          │
│  │  - 文档Embedding存储                         │          │
│  │  - 语义相似度检索                            │          │
│  │  - RAG知识库                                 │          │
│  └─────────────────────────────────────────────┘          │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.4 Agent应用场景规划

| 场景 | Agent类型 | 实现方式 |
|------|----------|---------|
| **智能客服** | ChatAgent | LangChain4j + Memory + Tools |
| **内容审核** | AuditAgent | 多模态模型 + 规则引擎 |
| **智能推荐** | RecommendAgent | Embedding + Milvus + 用户画像 |
| **帖子摘要** | SummaryAgent | LangChain4j Chain |
| **搜索增强** | SearchAgent | RAG + 语义搜索 |
| **写作助手** | WritingAgent | Prompt Template + 多轮对话 |

### 2.2 Token方案选择

**最终选择：JWT + Redis双存储**

| 方案 | 特点 | 缺点 | 是否选择 |
|------|------|------|---------|
| 纯JWT | 无状态，性能高 | 无法主动失效 | ❌ |
| 纯Session | 可主动失效 | 分布式需Session共享 | ❌ |
| **JWT + Redis** | 无状态 + 可失效 | 稍复杂 | ✅ |

**设计原理**：
1. JWT作为Access Token，携带用户信息，减少数据库查询
2. Redis存储JWT副本，支持主动失效（踢下线、密码修改后强制失效）
3. Refresh Token使用UUID，存储在Redis，用于刷新Access Token

---

## 三、系统架构

### 3.1 整体架构图

```
                        用户请求
                            ↓
                    ┌───────────────┐
                    │   Nginx/LB    │  ← SSL终止、负载均衡
                    │   (外部入口)   │
                    └───────┬───────┘
                            ↓
                    ┌───────────────┐
                    │ clx-gateway   │  ← API网关
                    │   端口: 8080   │
                    │               │
                    │ - Token验证    │
                    │ - 路由转发     │
                    │ - 限流防护     │
                    └───────┬───────┘
                            ↓
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
┌───────────────┐  ┌───────────────┐  ┌───────────────┐
│   clx-auth    │  │   clx-user    │  │  clx-post     │
│   认证中心     │  │   用户服务    │  │  帖子服务     │
│   端口: 9100   │  │   端口: 9200  │  │  端口: 9300   │
│               │  │               │  │  (后续)       │
│ - 登录认证    │  │ - 用户管理    │  │               │
│ - Token管理   │  │ - 角色权限    │  │               │
│ - SSO集成     │  │ - 组织机构    │  │               │
└───────┬───────┘  └───────┬───────┘  └───────────────┘
        ↓                  ↓
┌───────────────┐  ┌───────────────┐
│ Redis Cluster │  │ MySQL Cluster │
│  Token存储     │  │  数据持久化    │
│  缓存          │  │  分库分表      │
└───────────────┘  └───────────────┘

        ┌───────────────┐
        │     Nacos     │  ← 配置中心 + 服务注册
        │   3节点集群    │
        └───────────────┘
```

### 3.2 认证流程图

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│  用户    │     │  Gateway │     │ AuthCenter│     │  Redis   │     │  MySQL   │
└────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
     │                │                 │                │                │
     │ 1. POST /auth/login              │                │                │
     │  (username+password)             │                │                │
     │────────────────►│                │                │                │
     │                │ 2. 路由转发      │                │                │
     │                │────────────────►│                │                │
     │                │                 │ 3. 查询用户     │                │
     │                │                 │─────────────────────────────────►│
     │                │                 │                │   4.返回用户    │
     │                │                 │◄─────────────────────────────────│
     │                │                 │ 5. BCrypt验证密码│                │
     │                │                 │   (密码匹配)    │                │
     │                │                 │                │                │
     │                │                 │ 6. 生成JWT+UUID │                │
     │                │                 │   (Token对)     │                │
     │                │                 │───────────────►│                │
     │                │                 │ 7. 存储Token    │                │
     │                │                 │  - JWT → Redis │                │
     │                │                 │  - UUID → Redis│                │
     │                │                 │◄───────────────│                │
     │                │ 8. 返回Token     │                │                │
     │                │◄────────────────│                │                │
     │ 9. 返回Token   │                 │                │                │
     │  {accessToken, │                 │                │                │
     │   refreshToken}│                 │                │                │
     │◄───────────────│                 │                │                │
     │                │                 │                │                │
     │ 10. GET /user/list              │                │                │
     │  Header: Authorization           │                │                │
     │  = Bearer {accessToken}          │                │                │
     │────────────────►│                │                │                │
     │                │ 11. 提取Token    │                │                │
     │                │ 12. JWT签名验证  │                │                │
     │                │ 13. Redis有效性检查              │                │
     │                │────────────────────────────────►│                │
     │                │ 14. Token有效    │                │                │
     │                │◄───────────────────────────────│                │
     │                │ 15. 传递用户信息Header           │                │
     │                │  X-User-Id: 123                  │                │
     │                │  X-Username: admin               │                │
     │                │ 16. 转发到clx-user               │                │
     │                │────────────────────────────────────────────────►│
     │ 17. 返回用户列表                 │                │                │
     │◄───────────────│                 │                │                │
```

---

## 四、项目目录结构

### 4.1 已创建的结构

```
clx/
├── pom.xml                          # 父POM（版本管理）
│
├── clx-common/                      # 公共模块父工程
│   ├── pom.xml
│   │
│   ├── clx-common-core/             # 【核心公共】
│   │   ├── pom.xml
│   │   └── src/main/java/com/clx/common/core/
│   │       ├── domain/
│   │       │   ├── R.java           # 统一响应封装 ⭐
│   │       │   ├── LoginUser.java   # 登录用户上下文 ⭐
│   │       │   └── PageResult.java  # 分页结果
│   │       ├── exception/
│   │       │   ├── AuthException.java      # 认证异常 ⭐
│   │       │   ├── ServiceException.java   # 业务异常
│   │       │   └── GlobalExceptionHandler.java  # 全局异常处理 ⭐
│   │       └── constant/
│   │           ├── SecurityConstants.java  # 安全常量
│   │           ├── TokenConstants.java     # Token常量 ⭐
│   │           ├── CacheConstants.java     # 缓存常量
│   │           └── StatusConstants.java    # 状态常量
│   │
│   ├── clx-common-security/         # 【安全公共】
│   │   ├── pom.xml
│   │   └── src/main/java/com/clx/common/security/
│   │       ├── utils/
│   │       │   ├── JwtUtils.java    # JWT工具类 ⭐⭐⭐
│   │       │   └ SecurityUtils.java # 安全工具类
│   │       └── service/
│   │           └── TokenService.java # Token管理服务 ⭐⭐⭐
│   │
│   └── clx-common-redis/            # 【Redis公共】
│   │   ├── pom.xml
│   │   └── src/main/java/com/clx/common/redis/
│   │       ├── config/
│   │       │   └── RedisConfig.java # Redis配置
│   │       └── service/
│   │           └── RedisService.java # Redis服务类 ⭐
│   │
├── clx-api/                         # API定义父工程
│   ├── pom.xml
│   ├── clx-api-auth/
│   │   └── pom.xml                  # 认证API（Feign）
│   └── clx-api-user/
│   │   └── pom.xml                  # 用户API（Feign）
│   │
├── clx-auth/                        # 【认证中心】⭐⭐⭐
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/clx/auth/
│       │   ├── ClxAuthApplication.java    # 启动类
│       │   └── controller/
│       │       └── AuthController.java    # 认证接口 ⭐
│       └── resources/
│           ├── application.yml      # 主配置
│           └── application-dev.yml  # 开发配置
│
├── clx-user/                        # 【用户服务】⭐⭐
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/clx/user/
│       │   └── ClxUserApplication.java    # 启动类
│       └── resources/
│           ├── application.yml
│           └── application-dev.yml
│
├── clx-gateway/                     # 【API网关】⭐⭐
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/clx/gateway/
│       │   └── ClxGatewayApplication.java # 启动类
│       └── resources/
│           ├── application.yml      # 路由配置 ⭐
│           └── application-dev.yml
│
└── doc/                             # 文档目录
    ├── PROJECT_PLAN.md              # 本规划文档 ⭐
    └── sql/
        ├── schema.sql               # 表结构 ⭐
        └── init_data.sql            # 初始数据
```

---

## 五、核心类设计详解

### 5.1 R.java - 统一响应封装

**文件路径**：`clx-common-core/domain/R.java`

**设计目的**：所有API返回格式统一，方便前端处理。

```java
// 成功响应
R.ok(data)  → { "code": 200, "msg": "操作成功", "data": {...}, "timestamp": 123456 }

// 失败响应
R.fail("用户名错误")  → { "code": 500, "msg": "用户名错误", "data": null }
```

**为什么这样设计**：
- 前端只需判断 `code === 200` 就知道成功失败
- `timestamp` 用于排查请求时间
- 泛型 `<T>` 支持任意数据类型

---

### 5.2 LoginUser.java - 登录用户上下文

**文件路径**：`clx-common-core/domain/LoginUser.java`

**设计目的**：存储在Token中的用户信息，微服务间传递。

```java
LoginUser {
    userId,        // 用户唯一标识
    username,      // 用户名
    roles,         // 角色列表 ["admin", "user"]
    permissions,   // 权限列表 ["system:user:list"]
    status,        // 用户状态（禁用/锁定检查）
    loginTime,     // 登录时间（Token有效期计算）
    loginIp        // 登录IP（审计）
}
```

**为什么这样设计**：
- JWT携带这些信息，下游服务直接从Token解析，无需查数据库
- Redis存储副本，支持主动失效（修改密码后踢下线）

---

### 5.3 JwtUtils.java - JWT工具类

**文件路径**：`clx-common-security/utils/JwtUtils.java`

**核心方法**：

```java
// 生成Token
JwtUtils.generateToken(secret, subject, claims, expiration)

// 解析Token
JwtUtils.parseToken(secret, token)  → Claims

// 验证Token
JwtUtils.validateToken(secret, token)  → true/false

// 检查过期
JwtUtils.isExpired(secret, token)  → true/false
```

**为什么这样设计**：
- 使用 `Keys.hmacShaKeyFor()` 生成安全密钥，避免弱密钥问题
- HS512算法，安全性足够，性能比RS256好
- 抽象成工具类，所有认证逻辑复用

---

### 5.4 TokenService.java - Token管理服务

**文件路径**：`clx-common-security/service/TokenService.java`

**核心方法**：

```java
// 创建Token对
createTokenPair(LoginUser) → {accessToken, refreshToken, expiresIn}

// 验证Access Token
validateAccessToken(accessToken) → LoginUser 或 null

// 刷新Token
refreshToken(refreshToken) → 新Token对

// 登出（单设备）
logout(accessToken)

// 踢下线（所有设备）
kickOutAllDevices(userId)
```

**Redis存储结构**：

```
clx:auth:access:{accessToken}    → LoginUser对象（2小时过期）
clx:auth:refresh:{refreshToken}  → userId（7天过期）
clx:auth:user_tokens:{userId}    → Set<accessToken>（用户所有Token）
```

**为什么这样设计**：
- `accessToken` 存完整用户信息，Gateway验证时直接获取
- `user_tokens` 记录用户所有Token，支持踢下线所有设备

---

### 5.5 AuthException.java - 认证异常

**文件路径**：`clx-common-core/exception/AuthException.java`

**设计目的**：区分认证错误和业务错误。

```java
// 认证异常 → HTTP 401
AuthException.invalidToken()    → 401 "Token无效或已过期"
AuthException.loginFailed()     → 401 "用户名或密码错误"
AuthException.accountLocked()   → 401 "账号已被锁定"

// 业务异常 → HTTP 500/400
ServiceException.notFound("用户") → 404 "用户不存在"
```

**为什么这样设计**：
- 前端根据HTTP状态码判断：401跳登录页，500提示错误
- 静态工厂方法，统一异常消息，避免代码中散落字符串

---

## 六、数据库设计

### 6.1 核心表结构

| 表名 | 职责 | 分表策略 |
|------|------|---------|
| `sys_user` | 用户主表 | 后续分64张表（4库×16表） |
| `sys_role` | 角色表 | 不分表 |
| `sys_permission` | 权限表 | 不分表 |
| `sys_user_role` | 用户角色关联 | 按user_id分片（绑定表） |
| `sys_role_permission` | 角色权限关联 | 不分表 |
| `sys_organization` | 组织机构 | 不分表 |
| `sys_login_log` | 登录日志 | 按时间分表（归档） |
| `sys_social_bind` | 社交账号绑定 | 不分表 |

### 6.2 用户表字段设计

```sql
sys_user (
    user_id       -- 雪花算法生成，作为分片键
    username      -- 用户名，唯一索引
    password      -- BCrypt加密，强度12
    phone         -- 手机号，唯一索引
    email         -- 集合，普通索引
    nickname      -- 显示名称
    status        -- 状态：0正常 1禁用 2锁定
    last_login_ip -- 最后登录IP
    last_login_time -- 最后登录时间
    ...
)
```

**为什么用雪花算法生成ID**：
- 分布式唯一，不依赖数据库自增
- 时间有序，利于索引性能
- 直接作为分片键

---

## 七、实施计划

### 7.1 第一阶段（登录功能）- 6周

| 周 | 任务 | 产出 |
|---|------|------|
| **Week 1** | 项目骨架搭建 | Maven结构、公共模块、启动类 |
| **Week 2** | 基础设施对接 | Nacos、Redis、MySQL配置 |
| **Week 3** | 认证中心核心 | 账号密码登录、JWT生成验证 |
| **Week 4** | 网关认证过滤 | AuthGlobalFilter、白名单 |
| **Week 5** | 企业级SSO扩展 | LDAP、企业微信、钉钉 |
| **Week 6** | 完善与测试 | 登录锁定、限流、功能测试 |

### 7.2 第二阶段（Agent智能服务）- 4周

| 周 | 任务 | 产出 |
|---|------|------|
| **Week 7** | Agent服务骨架 | clx-agent模块、LangChain4j集成 |
| **Week 8** | ChatAgent实现 | 对话助手、Memory、Tools |
| **Week 9** | 向量检索集成 | Milvus安装、Embedding存储 |
| **Week 10** | 内容审核Agent | 多模态模型、规则引擎 |

### 7.3 当前进度

**已完成**（Week 1 - 项目骨架）：
- ✅ 父POM和版本管理（JDK 21）
- ✅ 公共模块（core、security、redis）
- ✅ 核心类（R、LoginUser、JwtUtils、TokenService）
- ✅ 服务骨架（auth、user、gateway启动类）
- ✅ 配置文件（application.yml）
- ✅ 数据库脚本（schema.sql、init_data.sql）
- ✅ Agent/AI依赖管理（LangChain4j、Milvus等）

**待完成**：
- ❌ 认证中心完整登录逻辑
- ❌ 用户服务实体类和Mapper
- ❌ 网关认证过滤器
- ❌ Spring Security配置
- ❌ 企业级SSO集成
- ❌ Agent服务模块

---

## 八、下一步工作

### 8.1 优先级排序

| 优先级 | 任务 | 说明 |
|--------|------|------|
| **P0** | 补充认证中心登录逻辑 | 验证用户名密码、生成Token、返回响应 |
| **P1** | 用户服务实体类 | User、Role、Permission实体 + MyBatis Mapper |
| **P2** | 网关认证过滤器 | Gateway GlobalFilter + Token验证 |
| **P3** | Spring Security配置 | 安全框架集成 |
| **P4** | 企业级SSO | LDAP、企业微信、钉钉 |

### 8.2 建议学习顺序

作为项目学习教练，建议你按以下顺序理解代码：

1. **先理解核心类**：
   - `R.java` - 统一响应
   - `LoginUser.java` - 用户上下文
   - `JwtUtils.java` - JWT原理

2. **再理解Token流程**：
   - `TokenService.java` - Token如何生成、存储、验证

3. **然后看配置**：
   - `application.yml` - 各服务如何配置
   - `pom.xml` - 依赖如何管理

4. **最后看架构**：
   - Gateway如何路由
   - Auth如何验证
   - User如何管理数据

---

## 九、常见问题解答

### Q1: 为什么选择JWT而不是Session？

**答**：
- 微服务架构，Session共享复杂
- JWT无状态，减轻服务器压力
- JWT + Redis双存储，既无状态又能主动失效

### Q2: BCrypt是什么？为什么用它加密密码？

**答**：
- BCrypt是专门为密码设计的哈希算法
- 自动加盐（salt），防止彩虹表攻击
- 强度可调，随硬件性能提升增加强度

### Q3: 为什么需要Refresh Token？

**答**：
- Access Token有效期短（2小时），减少被盗用风险
- Refresh Token有效期长（7天），无需频繁登录
- 刷新时验证Refresh Token，安全性和便利性平衡

### Q4: Gateway如何验证Token？

**答**：
1. 提取请求头中的Token
2. JWT签名验证（防篡改）
3. Redis有效性检查（支持主动失效）
4. 传递用户信息到下游服务（X-User-Id等Header）

### Q5: JDK 21虚拟线程有什么优势？

**答**：
- 传统线程每个约1MB内存，100万线程需要1TB内存
- 虚拟线程几乎不占内存，可轻松创建百万级
- 适合IO密集型场景（如登录请求处理、数据库查询）
- Spring Boot 3.2支持，配置 `spring.threads.virtual.enabled=true`

### Q6: LangChain4j是什么？为什么选它？

**答**：
- Java版本的LangChain，简化AI应用开发
- 提供：Prompt模板、Memory管理、Tool调用、Chain编排
- 与Spring生态无缝集成，比Python版更适合Java项目
- 支持OpenAI、Azure OpenAI、本地模型(Ollama)

### Q7: Milvus向量数据库做什么用？

**答**：
- 存储文本的Embedding向量（数值表示）
- 实现语义搜索：相似问题检索、文档匹配
- 用于RAG（检索增强生成）：先检索相关文档，再让AI回答
- 比 Elasticsearch 更适合语义理解场景

### Q8: Agent服务如何与社区功能结合？

**答**：
- 智能客服：回答社区使用问题
- 内容审核：自动识别违规内容
- 推荐系统：基于用户兴趣推荐帖子
- 搜索增强：理解用户意图，返回相关内容
- 写作助手：帮助用户生成帖子草稿

---

## 十、参考资料

### 10.1 技术文档

**Spring生态**：
- [Spring Boot 3.2 官方文档](https://docs.spring.io/spring-boot/docs/3.2.5/reference/html/)
- [Spring Cloud 2023 官方文档](https://docs.spring.io/spring-cloud/docs/2023.0.1/reference/html/)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/)

**JDK 21新特性**：
- [JDK 21 Virtual Threads](https://openjdk.org/jeps/444)
- [JDK 21 Generational ZGC](https://openjdk.org/jeps/439)

**Agent/AI技术**：
- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [LangChain4j GitHub](https://github.com/langchain4j/langchain4j)
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [Milvus 官方文档](https://milvus.io/docs)
- [ONNX Runtime](https://onnxruntime.ai/docs/)

**安全相关**：
- [JWT 最佳实践](https://datatracker.ietf.org/doc/html/rfc8725)

### 10.2 学习建议

**第一阶段（登录功能）**：
- **JWT原理**：搜索 "JWT原理详解" 理解签名验证流程
- **BCrypt**：搜索 "BCrypt密码加密原理"
- **微服务认证**：搜索 "微服务统一认证方案对比"

**第二阶段（Agent技术）**：
- **LangChain概念**：搜索 "LangChain原理详解"
- **RAG原理**：搜索 "RAG检索增强生成原理"
- **向量数据库**：搜索 "Milvus向量数据库入门"
- **虚拟线程**：搜索 "JDK21虚拟线程实战"

---

*文档生成时间：2026-04-17*
*作者：CLX项目学习教练*
*更新：JDK 21 + Agent/AI技术规划*