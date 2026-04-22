---
doc_type: feature-design
feature: 2026-04-20-auth-enhancement
status: approved
summary: 认证流程增强：找回密码、图形验证码、邮箱验证、模拟手机验证码
tags: [auth, user-system, phase1]
---

# 认证流程增强 Design

> Stage 1 | 2026-04-20 | 下一步：implement

---

## 需求摘要

本功能是用户体系建设的**第一轮**，聚焦**认证流程增强**，让注册更安全、密码可找回。

**核心用户行为**：
1. 用户注册时需要图形验证码 + 邮箱/手机验证
2. 用户忘记密码时能通过邮箱自助重置
3. 所有验证码5分钟内有效，防止重复发送

**明确不做**：
- 第三方登录（微信/QQ/GitHub）——依赖外部OAuth服务
- 真实手机验证码对接（短信服务商）——开发环境用模拟
- 复杂校验逻辑（用户名格式、密码强度）——保持简单
- 安全加固策略（异地登录提醒、登录锁定）

---

## 决策与约束

### 本 feature 放在 clx-auth 模块里
原因：认证功能属于 clx-auth 服务职责，已有登录注册逻辑，扩展在同一模块更自然。

### 技术选型

| 决策 | 选型 | 原因 |
|------|------|------|
| 图形验证码 | 自建方案（Canvas绘制字符） | 轻量级，不依赖第三方 |
| 验证码存储 | Redis 5分钟 | 快速过期，防止滥用 |
| 邮箱验证 | 注册时强制 | 确保邮箱真实有效 |
| 手机验证码 | 环境区分逻辑 | 开发环境模拟，生产预留接口 |

### 命名约定

| 新增术语 | 含义 |
|---------|------|
| `verification_code` | 统一的验证码处理（邮箱/手机共用） |
| `captcha` | 图形验证码 |
| `password_reset` | 密码重置流程 |

---

## 现状分析

数据库已有：
- `sys_user` 表：包含 `email`、`phone` 字段，但未使用
- `sys_login_log` 表：可记录登录行为

后端已有：
- `/auth/register`：基础注册接口（用户名+密码）
- `/auth/login`：基础登录接口
- JWT Token 体系

前端已有：
- `RegisterForm.tsx`：基础注册组件
- `LoginForm.tsx`：基础登录组件

**缺口**：无验证码、无找回密码、无邮箱服务

---

## 功能设计

### 1. 图形验证码

**后端接口**：
```
GET /auth/captcha
响应：
{
  "code": 200,
  "data": {
    "captchaId": "uuid",
    "captchaImage": "base64图片"
  }
}
```

**实现要点**：
- 验证码：4位随机数字/字母混合
- 存储Redis：key = `captcha:{id}`, value = 验证码, ttl = 5分钟
- 图片生成：Canvas绘制干扰线、噪点，输出base64

---

### 2. 邮箱验证

**注册流程改造**：
```
POST /auth/register
请求：
{
  "username": "test",
  "password": "xxx",
  "confirmPassword": "xxx",
  "email": "test@example.com",       // 新增
  "emailCode": "123456",          // 新增
  "captchaId": "uuid",              // 新增
  "captchaCode": "ABCD"            // 新增
}

第一步：发送验证码
POST /auth/email-code/send
请求：
{
  "email": "test@example.com",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}
响应：发送成功提示
```

**实现要点**：
- 验证码存储：key = `email:code:{email}`, ttl = 5分钟
- 防重复发送：检查key是否已存在
- 邮件发送：使用Spring JavaMail，配置在application.yml

---

### 3. 模拟手机验证码

**开发环境逻辑**：
```
POST /auth/sms-code/send
请求：
{
  "phone": "13800138000",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}

开发环境响应：固定返回 123456
生产环境：对接真实短信服务商预留
```

**实现要点**：
- 使用 `@Profile("dev")` 区分逻辑
- 前端测试时输入 123456 即可

---

### 4. 找回密码

**完整流程**：
```
第一步：发送重置邮件
POST /auth/password-reset/send
请求：
{
  "email": "test@example.com",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}
响应：发送成功提示

第二步：重置密码
POST /auth/password-reset/confirm
请求：
{
  "email": "test@example.com",
  "resetCode": "xyz789",     // 邮件中的验证码
  "newPassword": "newpwd123",
  "confirmPassword": "newpwd123"
}
响应：重置成功
```

**实现要点**：
- 重置码存储：key = `password:reset:{email}`, ttl = 30分钟
- 邮件模板：包含重置链接或验证码
- 密码更新：使用BCrypt重新加密

---

## 前端设计

### 注册组件改造

**新增表单字段**：
- 邮箱输入框
- 邮箱验证码输入框 + 发送按钮
- 图形验证码图片 + 刷新按钮
- 选择验证方式（邮箱/手机）

**交互流程**：
1. 用户填写基础信息
2. 点击"发送验证码" → 验证图形码 → 发送验证码
3. 用户输入收到的验证码
4. 提交注册

---

## 实现提示

### 推进步骤

1. **后端：新建图形验证码服务**
   - 新建 `CaptchaService.java`，生成验证码图片
   - 新建 `CaptchaController.java`，提供 `/auth/captcha` 接口
   - 退出条件：能请求接口返回base64图片

2. **后端：新建验证码管理服务**
   - 新建 `VerificationCodeService.java`，封装Redis操作
   - 统一邮箱/手机/密码重置验证码逻辑
   - 退出条件：单元测试通过

3. **后端：添加邮箱服务**
   - 配置 `application.yml`：SMTP服务器信息
   - 新建 `EmailService.java`，封装JavaMail发送
   - 退出条件：能发送测试邮件

4. **后端：改造注册接口**
   - `RegisterRequest` 增加 email、emailCode、captchaId、captchaCode 字段
   - 验证图形码 + 邮箱验证码
   - 保存邮箱到 `sys_user` 表
   - 退出条件：注册时验证码校验通过

4. **后端：添加手机验证码接口**
   - 新建 `/auth/sms-code/send` 接口
   - 开发环境返回固定码 123456
   - 退出条件：接口返回验证码

5. **后端：添加密码重置接口**
   - 新建 `/auth/password-reset/send` 和 `/auth/password-reset/confirm` 接口
   - 邮件发送重置码 + 验证码更新密码
   - 退出条件：能完成密码重置流程

6. **前端：改造注册组件**
   - `RegisterForm.tsx` 添加邮箱、验证码输入框
   - 添加图形验证码显示 + 刷新逻辑
   - 退出条件：注册表单包含所有新字段

7. **前端：创建找回密码组件**
   - 新建 `ForgotPasswordPage.tsx`、`ForgotPasswordForm.tsx`
   - 发送重置邮件 + 重置密码两步流程
   - 退出条件：能完成密码重置操作

8. **前端：路由配置**
   - 添加 `/forgot-password` 路由
   - 退出条件：访问 `/forgot-password` 能显示页面

9. **集成测试**
   - 端到端测试注册 + 找回密码
   - 退出条件：浏览器能完成完整流程

### 高风险实现约束

1. **邮箱服务配置必须在生产环境正确**
   - 如果SMTP配置错误，注册功能完全不可用
   - 缓解方案：开发环境用模拟发送，日志记录邮件内容

2. **验证码存储依赖Redis**
   - Redis挂掉会导致验证码功能不可用
   - 缓解方案：增加Redis健康检查，降级为不验证模式

---

## 测试设计

### 功能点与测试约束

| 功能点 | 测试约束 | 验证方式 | 关键用例 |
|--------|----------|----------|----------|
| 图形验证码生成 | 必须返回base64图片 | 自动化接口测试 | curl请求检查base64 |
| 图形验证码校验 | 输入错误3次重新生成 | 手动测试 | 输错3次后图片更新 |
| 邮箱验证码发送 | 5分钟内不能重复发送 | 手动测试 | 连发两次，第二次被拒绝 |
| 邮箱验证码校验 | 过期/错误码正确处理 | 手动测试 | 过期码提示重新发送 |
| 手机验证码模拟 | 开发环境返回固定码 | 自动化测试 | dev环境接口返回123456 |
| 密码重置流程 | 30分钟内有效 | 手动测试 | 发邮件→重置→用新密码登录 |
| 注册完整流程 | 强制邮箱验证 | 端到端测试 | 缺邮箱验证码时注册失败 |

---

## 范围守护检查项

- [ ] 注册流程强制校验邮箱验证码
- [ ] 图形验证码5分钟内有效
- [ ] 邮箱验证码5分钟内有效，防重复发送
- [ ] 手机验证码（开发环境）返回固定值123456
- [ ] 密码重置码30分钟内有效
- [ ] 密码重置成功后原密码失效
- [ ] 所有验证码接口都校验图形验证码
- [ ] Redis不可用时验证码功能降级提示用户

---

## 接口契约

### 1. 获取图形验证码
```bash
GET /auth/captcha

响应示例：
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "captchaId": "a1b2c3d4-e5f6-7890",
    "captchaImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA..."
  }
}
```

### 2. 发送邮箱验证码
```bash
POST /auth/email-code/send
Content-Type: application/json
{
  "email": "test@example.com",
  "captchaId": "a1b2c3d4-e5f6-7890",
  "captchaCode": "ABCD"
}

响应：
成功：{"code": 200, "msg": "验证码已发送到邮箱"}
失败：{"code": 400, "msg": "图形验证码错误"}
重复：{"code": 400, "msg": "验证码已发送，5分钟内勿重复"}
```

### 3. 注册（增强版）
```bash
POST /auth/register
Content-Type: application/json
{
  "username": "newuser",
  "password": "newpwd123",
  "confirmPassword": "newpwd123",
  "email": "test@example.com",
  "emailCode": "123456",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}

响应：
成功：{"code": 200, "data": {"token": "...", "userId": 123}}
失败：{"code": 400, "msg": "邮箱验证码错误"}
```

### 4. 发送手机验证码
```bash
POST /auth/sms-code/send
Content-Type: application/json
{
  "phone": "13800138000",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}

响应（开发环境）：
{"code": 200, "msg": "验证码已发送（开发环境：123456）"}

响应（生产环境）：
{"code": 200, "msg": "验证码已发送"}
```

### 5. 发送密码重置邮件
```bash
POST /auth/password-reset/send
Content-Type: application/json
{
  "email": "test@example.com",
  "captchaId": "uuid",
  "captchaCode": "ABCD"
}

响应：
{"code": 200, "msg": "重置链接已发送到邮箱"}
```

### 6. 确认密码重置
```bash
POST /auth/password-reset/confirm
Content-Type: application/json
{
  "email": "test@example.com",
  "resetCode": "xyz789",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}

响应：
成功：{"code": 200, "msg": "密码重置成功"}
失败：{"code": 400, "msg": "重置码已过期"}
```