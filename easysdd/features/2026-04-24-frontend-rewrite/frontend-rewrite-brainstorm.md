---
doc_type: feature-brainstorm
feature: 2026-04-24-frontend-rewrite
status: confirmed
summary: 删除现有前端，基于 Neumorphism 设计系统从零重写，MVP 只做登录+首页+帖子详情
tags: [frontend, rewrite, neumorphism, react, vite]
---

# Frontend Rewrite Brainstorm

> Stage 0 | 2026-04-24 | 下一步：design

## 想做什么、为什么

用户对现有前端 UI 风格不满意，现有 shadcn/ui 风格与社区平台气质不搭。希望删除现有 clx-web 前端代码，基于 `stype/` 目录下的 Neumorphism 设计系统从零重写前端。

探索过程中确认了：
- 不是简单的"换皮肤"，而是组件级到架构级的完全重写
- 技术栈保持 React + Vite + Tailwind CSS 不变
- 第一版走 MVP 路线，只做核心流程

## 考虑过的方向

### 方向 A：只换皮肤
- 保留现有代码结构，只替换样式
- 代价：轻量，但 shadcn 和 Neumorphism 有风格冲突

### 方向 B：组件级重构
- 删除 UI 组件代码，保留路由/状态/API 层
- 代价：中等，风格一致性好

### 方向 C：完全重写（选定）
- 删除 clx-web 整个目录，从零搭建
- 代价：最大，但最干净，可顺便优化架构
- **结论：选定**，用户希望彻底重写

## 选定方向与遗留问题

**选定方向**：
- 删除 clx-web 前端代码（保留 API 调用逻辑参考）
- 基于 stype/Neumorphism 设计系统从零重写
- 技术栈：React + Vite + Tailwind CSS
- MVP 范围：登录页 + 首页（帖子列表）+ 帖子详情页

**明显不做**：
- 注册/忘记密码/OAuth/手机登录/社交绑定
- 用户中心/个人主页
- 复杂搜索功能

**遗留给 design 的问题**：
1. 目录结构怎么组织？
2. 状态管理方案？
3. 路由守卫实现方式？
4. API 层复用多少？
5. stype tokens 如何集成到 Tailwind？
