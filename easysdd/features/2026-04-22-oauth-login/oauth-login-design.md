---
doc_type: feature-design
feature: 2026-04-22-oauth-login
status: approved
summary: 添加第三方登录（GitHub、手机号、QQ、微信）和详细代码注释
---

# 第三方登录与手机号登录 Design

> Stage 2 | 2026-04-22 | 下一步：执行步骤1（数据库变更），然后执行步骤6（配置），最后测试步骤7

## 决策摘要

**平台优先级**：GitHub → 手机号 → QQ → 微信

**首次登录策略**：自动创建新用户

**多账号绑定**：支持！一个用户可绑定多个平台

**代码注释**：每行关键代码都有详细注释

---

## 推进步骤

### 步骤1：数据库变更（15分钟）
- 创建 sys_social_bind 表
- 修改 sys_user 表添加 phone 字段
- 退出条件：SQL执行成功，表结构正确

### 步骤2：GitHub OAuth 实现（4小时）
- 创建 GithubOAuthService（详细注释）
- 创建 GithubOAuthController（详细注释）
- 实现授权 URL 生成
- 实现回调处理（自动创建用户）
- 退出条件：浏览器能完成 GitHub 登录

### 步骤3：手机号登录实现（3小时）
- 对接短信服务商（阿里云/腾讯云）
- 实现发送验证码接口
- 实现手机号登录接口（自动创建用户）
- 退出条件：手机号能登录

### 步骤4：QQ 登录实现（2小时）
- 类似 GitHub 流程
- 申请 QQ 互联应用
- 退出条件：QQ 能登录

### 步骤5：微信登录实现（2小时）
- 类似 GitHub 流程
- 申请微信开放平台应用
- 退出条件：微信能登录

### 步骤6：多账号绑定功能（2小时）
- 实现绑定接口
- 实现查询已绑定账号接口
- 退出条件：能绑定多个账号

### 步骤7：前端页面（4小时）
- 登录页面添加第三方登录按钮
- 手机号登录页面
- 绑定管理页面
- 退出条件：UI 完整，流程通顺

### 步骤8：代码审查和注释补充（2小时）
- 检查所有代码是否有详细注释
- 补充缺失的注释
- 退出条件：每行关键代码都有注释

---

## 代码注释规范示例

```java
/**
 * 处理 GitHub OAuth 登录回调
 * 
 * 整体流程：
 * 1. 从 URL 参数获取 code 和 state
 * 2. 验证 state 防止 CSRF 攻击
 * 3. 用 code 向 GitHub 换取 access_token
 * 4. 用 access_token 获取用户 GitHub 信息
 * 5. 根据 GitHub ID 查找本地用户
 *    - 找到了 → 直接登录
 *    - 没找到 → 创建新用户
 * 6. 生成 JWT Token 返回给前端
 * 
 * 安全注意：
 * - code 只能用一次，用了就失效
 * - state 必须验证，防止 CSRF
 * - access_token 要加密存储
 * 
 * @param code GitHub 返回的授权码，示例："abc123def456"
 * @param state 随机状态码，示例："xyz789"
 * @return 登录结果，包含 JWT Token
 * @throws AuthException 授权码无效或过期时抛出
 */
public LoginVO githubCallback(String code, String state) {
    // 步骤1：验证 state
    // 为什么要验证？防止 CSRF 攻击
    // 怎么验证？从 session 或 Redis 取出之前存的 state，对比是否一致
    validateState(state);
    
    // 步骤2：用 code 换 access_token
    // 调用 GitHub API：POST https://github.com/login/oauth/access_token
    String accessToken = exchangeCodeForToken(code);
    
    // 步骤3：获取用户信息
    // 调用 GitHub API：GET https://api.github.com/user
    GithubUserInfo userInfo = fetchUserInfo(accessToken);
    
    // 步骤4：查找或创建用户
    // ...
}
```
