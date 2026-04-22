---
doc_type: issue-fix
issue: 2026-04-21-login-missing-register-link
severity: minor
status: fixed
fixed_by: Claude
fixed_at: 2026-04-21T14:30:00+08:00
tags: [frontend, auth, ux]
files_changed:
  - clx-web/src/features/auth/components/LoginForm.tsx
---

# 修复记录：登录页缺少注册按钮

## 问题描述

用户反馈：登录页面没有显示注册按钮，无法从登录页跳转到注册页。

## 根因分析

**定位文件**: `clx-web/src/features/auth/components/LoginForm.tsx:250-262`

**根因**: 登录表单底部缺少跳转到注册页的链接。

对比 `RegisterForm.tsx` 有"已经有账号了？去登录"链接，但 `LoginForm.tsx` 缺少反向的"还没有账号？去注册"链接。

## 修复方案

在登录表单底部（status 提示区域之前）添加注册链接：

```tsx
<div className="flex items-center justify-between gap-3 border-t border-[#39ff14]/12 pt-4 text-sm">
  <span className="text-[#39ff14]/55">还没有账号？</span>
  <Link to="/register" className="text-[#39ff14] transition hover:text-[#e6ff00] hover:underline">
    去注册
  </Link>
</div>
```

## 修复内容

| 文件 | 改动 |
|------|------|
| `LoginForm.tsx` | 在表单底部添加"去注册"链接 |

## 验证方式

1. 启动前端服务 `npm run dev`
2. 访问 `http://localhost:5173/login`
3. 确认表单底部显示"还没有账号？去注册"链接
4. 点击链接跳转到 `/register` 页面

## 风险评估

- **影响范围**: 仅登录表单 UI，无逻辑改动
- **回归风险**: 无
- **跨模块影响**: 无

## 后续建议

无。UI 补全，符合常规登录/注册页面双向跳转的交互设计。