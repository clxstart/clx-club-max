# OAuth 登录功能验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-04-22
> 关联方案 doc：`easysdd/features/2026-04-22-oauth-login/oauth-login-design.md`

---

## 1. 接口契约核对

对照方案 doc 推进步骤，核查实现与设计的一致性：

### GitHub OAuth 接口

- [x] `GET /auth/oauth/github/authorize` → 返回授权 URL → 代码实现：`GithubOAuthController.java:78` ✓
- [x] `GET /auth/oauth/github/callback` → 处理回调，重定向到前端 → 代码实现：`GithubOAuthController.java:128` ✓

### 手机号登录接口

- [x] `POST /auth/phone/sms-code` → 发送验证码 → 代码实现：`PhoneLoginController.java:68` ✓
- [x] `POST /auth/phone/login` → 手机号登录 → 代码实现：`PhoneLoginController.java:99` ✓

### 绑定管理接口

- [x] `GET /auth/bindings` → 获取绑定列表 → 代码实现：`SocialBindController.java:56` ✓
- [x] `DELETE /auth/bindings/{id}` → 解绑 → 代码实现：`SocialBindController.java:74` ✓
- [x] `GET /auth/bindings/github/authorize` → 获取绑定授权URL → 代码实现：`SocialBindController.java:89` ✓
- [x] `POST /auth/bindings/github/callback` → 绑定回调 → 代码实现：`SocialBindController.java:103` ✓

### 前端路由

- [x] `/login` → 登录页面 ✓
- [x] `/phone-login` → 手机号登录页面 ✓
- [x] `/oauth-callback` → OAuth 回调处理页面 ✓
- [x] `/settings/bindings` → 绑定管理页面 ✓

---

## 2. 行为与决策核对

对照方案 doc 决策摘要：

### 需求摘要验证

- [x] **GitHub OAuth 登录**：用户点击 GitHub 按钮 → 跳转授权 → 回调处理 → Token 返回 → 实测通过 ✓
- [x] **手机号登录**：验证码发送 + 登录接口已实现，开发环境验证码打印日志 ✓
- [x] **自动创建新用户**：GitHub 首次登录自动创建用户（用户名格式 `github_xxx`）✓
- [x] **手机号首次登录自动创建**：用户名格式 `phone_xxxx` ✓

### 明确不做核对

- [x] **QQ 登录**：标记为 `deferred`，需企业资质，代码未实现 ✓
- [x] **微信登录**：标记为 `deferred`，需企业资质，代码未实现 ✓

### 关键决策落地

- [x] **首次登录策略：自动创建新用户** → `GithubOAuthService.findOrCreateUser()` / `PhoneLoginServiceImpl.createNewUser()` ✓
- [x] **多账号绑定支持** → `SocialBindService` 实现查询/解绑/绑定 ✓
- [x] **state 验证防 CSRF** → Redis 存储 5 分钟有效，回调时验证删除 ✓
- [x] **Token 格式一致** → 均使用 `StpUtil.login()` 生成 JWT Token ✓

---

## 3. 测试约束核对

对照 `{slug}-checklist.yaml` checks：

- [x] **C1：state 验证防 CSRF**
  - 验证方式：代码 review
  - 结果：`GithubOAuthService.validateState()` Redis 存储 5 分钟，回调时验证并删除 ✓

- [x] **C2：code 一次性使用**
  - 验证方式：GitHub API 机制保障
  - 结果：GitHub API 保证 code 只能用一次 ✓

- [x] **C3：新用户自动创建**
  - 验证方式：实际 OAuth 测试
  - 结果：GitHub 登录成功，创建了用户 `github_clxstart` ✓

- [x] **C4：已绑定用户直接登录**
  - 验证方式：代码 review + 实际测试
  - 结果：`selectById()` 从数据库查询真实用户，不重复创建 ✓

- [x] **C5：Token 格式一致**
  - 验证方式：实际测试返回 JWT Token
  - 结果：Token 格式与密码登录一致 ✓

- [x] **C6：绑定关系唯一**
  - 验证方式：数据库唯一索引
  - 结果：`uk_social_bind (social_type, social_id)` ✓

### 前端改动浏览器验证

- [x] **GitHub 登录按钮**：点击跳转到 GitHub 授权页 ✓
- [x] **手机号登录入口**：登录页手机图标点击跳转到 `/phone-login` ✓
- [x] **OAuth 回调处理**：重定向携带 Token 参数 ✓

---

## 4. 术语一致性

代码命名检查：

| 术语 | 命名位置 | 命名一致性 |
|------|----------|-----------|
| social_bind | 表名 `sys_social_bind`、实体类 `SocialBind` | ✓ |
| social_type | 字段 `socialType`、参数 `socialType` | ✓ |
| social_id | 字段 `socialId`、参数 `socialId` | ✓ |
| phone | 字段 `phone`、参数 `phone` | ✓ |
| sms_code | 参数 `smsCode`、Redis key `sms:code` | ✓ |
| oauth | URL `/auth/oauth/`、目录 `oauth/` | ✓ |

---

## 5. 架构归并

对照方案 doc，检查架构文档更新：

- [x] **数据库 schema**：`doc/sql/oauth_login_schema.sql` 已创建 ✓
- [x] **API 端点**：前端 `endpoints.ts` 已添加 OAuth 相关端点 ✓
- [x] **配置文档**：`doc/PHONE_LOGIN_CONFIG.md` 已创建，说明短信服务配置 ✓
- [x] **CLAUDE.md**：无需更新，OAuth 功能在后续扩展路线图中已有描述 ✓

本次 feature 新增模块/接口：
- 认证模块新增：OAuth 登录（GitHub）、手机号登录、绑定管理
- 前端新增：手机号登录页、OAuth 回调页、绑定管理页

---

## 6. 遗留

### 后续优化点

1. **QQ/微信登录**：需要企业资质申请应用，标记为 `deferred`
2. **短信服务商对接**：开发环境验证码打印日志，生产环境需配置阿里云/腾讯云
3. **绑定回调前端处理**：当前绑定 GitHub 后需手动刷新页面，可优化为自动跳转

### 已知限制

1. 后端重定向 URL 硬编码为 `localhost:5174`，生产环境需改为配置项
2. Token 在 URL 参数传递，可能被浏览器历史记录保存

### 实现阶段顺手发现

无

---

## 验收结论

本 feature 实现与方案设计一致，所有 checks 通过。QQ/微信登录因需要企业资质标记为延期，不影响核心功能验收。