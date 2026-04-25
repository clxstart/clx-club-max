---
doc_type: feature-design
feature: 2026-04-24-frontend-rewrite
status: approved
summary: 删除现有前端，基于 Neumorphism 设计系统从零重写，MVP 只做登录+首页+帖子详情
tags: [frontend, rewrite, neumorphism, react, vite, tailwind]
---

# Frontend Rewrite Design

> Stage 1 | 2026-04-24 | Status: draft

## 1. 决策与约束

### 需求摘要

**做什么**：删除现有 clx-web 前端代码，基于 `stype/` 目录下的 Neumorphism 设计系统从零重写前端。

**MVP 范围**：
- 登录页（用户名密码登录）
- 首页（帖子列表）
- 帖子详情页

**明确不做**（后续迭代）：
- 注册/忘记密码/OAuth/手机登录/社交绑定
- 用户中心/个人主页/设置
- 发帖功能
- 评论功能（详情页只展示，不支持新增）
- 复杂搜索（只保留关键词搜索入口）
- 点赞功能（UI 展示但不交互）

### 关键决策

| 决策 | 选择 | 被拒方案 | 原因 |
|---|---|---|---|
| 目录结构 | features-based（按功能域组织） | pages-based（按页面组织） | 功能域内聚，后续扩展方便 |
| 状态管理 | zustand（保持现有方案） | Redux / Jotai / React Query | 已有 authStore 模式可复用，迁移成本低 |
| 路由方案 | React Router v6 + AuthGuard | TanStack Router | 现有代码已有成熟模式，无需换新方案 |
| API 层 | 复用现有 client/request/endpoints | 完全重写 | 端点定义和请求封装已完整，只改调用方式 |
| 样式方案 | Tailwind + CSS 变量 | shadcn/ui 组件库 | Neumorphism 风格与 shadcn 冲突，直接用 Tailwind 自定义 |

### 假设与风险

**假设**：
- 后端 clx-auth 端口 9100，clx-post 端口 9300 已可用
- Vite dev server 会代理 `/api/*` 到后端
- 登录成功后 Token 存 localStorage，请求头带 `Authorization: Bearer {token}`

**风险点**：
- Neumorphism 风格在深色模式下需要特殊处理（MVP 只做浅色）
- 双重阴影在移动端可能影响性能（先用 CSS，后续观察）

### 前置依赖

无前置依赖。本 feature 放在 `clx-web/` 目录下，属于前端层独立重写。

---

## 2. 契约层

### API 接口（复用现有）

MVP 需要调用的接口（来自现有 `endpoints.ts`）：

| 功能 | 接口 | 方法 | 输入 | 输出 |
|---|---|---|---|---|
| 登录 | `/auth/login` | POST | `{username, password}` | `{code, data: {token, tokenName}}` |
| 当前用户 | `/auth/me` | GET | Header: `Authorization` | `{code, data: {id, username, ...}}` |
| 登出 | `/auth/logout` | POST | Header: `Authorization` | `{code}` |
| 帖子列表 | `/api/post/list` | GET | `{page, size, sort?, categoryId?}` | `{posts, total, page, size}` |
| 帖子详情 | `/api/post/{id}` | GET | - | `{id, title, content, author, ...}` |
| 分类列表 | `/api/category/list` | GET | - | `[{id, name, code}]` |
| 热门帖子 | `/api/post/hot` | GET | `{limit}` | `[{id, title, likeCount}]` |

**示例**（来自现有 `authApi.ts`）：

```typescript
// 登录请求
authApi.login({ username: "admin", password: "admin123" })
// 响应
{ code: 200, data: { token: "xxx", tokenName: "Authorization" } }

// 获取当前用户
authApi.getCurrentUser()
// 响应
{ code: 200, data: { id: 1, username: "admin", avatar: "...", email: "..." } }
```

### 页面路由

| 路径 | 页面 | 认证要求 |
|---|---|---|
| `/login` | LoginPage | 无 |
| `/` | HomePage（帖子列表） | 需要 |
| `/posts` | HomePage（别名） | 需要 |
| `/posts/:id` | PostDetailPage | 需要 |

### 组件 Props 契约

**PostCard**（帖子卡片）：
```typescript
interface PostCardProps {
  post: {
    id: number;
    title: string;
    summary: string;
    author: { id: number; name: string; avatar?: string };
    category?: { id: number; name: string };
    tags: { id: number; name: string; color?: string }[];
    likeCount: number;
    commentCount: number;
    createdAt: string;
  };
  onClick?: () => void;
}
// 渲染：Neumorphism 卡片样式，标题、摘要、作者头像、统计数据
```

**NeuButton**（Neumorphism 按钮）：
```typescript
interface NeuButtonProps {
  children: React.ReactNode;
  variant?: 'raised' | 'pressed'; // 凸起或凹陷
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
}
// 渲染：双重阴影，按下时切换 inset 阴影
```

**NeuInput**（Neumorphism 输入框）：
```typescript
interface NeuInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  type?: 'text' | 'password';
  disabled?: boolean;
  className?: string;
}
// 渲染：凹陷阴影效果，focus 时加深阴影
```

---

## 3. 实现提示

### 改动计划

本 feature 是**删除重建**，改动文件列表：

| 操作 | 文件/目录 | 说明 |
|---|---|---|
| 删除 | `clx-web/src/features/` | 清空现有功能代码 |
| 删除 | `clx-web/src/layouts/` | 清空现有布局 |
| 删除 | `clx-web/src/routes/` | 清空现有路由 |
| 删除 | `clx-web/src/shared/styles/` | 清空现有样式 |
| 保留 | `clx-web/src/api/` | API 层复用，只改拦截器 |
| 保留 | `clx-web/src/config/` | 配置复用 |
| 保留 | `clx-web/src/store/` | 状态管理复用，只改 authStore |
| 新建 | `clx-web/src/components/ui/` | Neumorphism 基础组件 |
| 新建 | `clx-web/src/features/auth/` | 登录功能 |
| 新建 | `clx-web/src/features/post/` | 帖子功能 |
| 新建 | `clx-web/src/layouts/` | MainLayout / AuthLayout |
| 新建 | `clx-web/src/routes/` | 路由配置 |
| 修改 | `clx-web/src/shared/styles/globals.css` | 引入 stype 样式 |
| 修改 | `clx-web/tailwind.config.js` | 合并 stype tailwind preset |

### 推进顺序

按"功能可见度"从高到低，每步可独立验证：

**Step 1：基础设施搭建**
- 合并 stype tailwind preset 到 `tailwind.config.js`
- 引入 `neumorphism-globals.css` 到 `globals.css`
- 创建基础 UI 组件：`NeuButton`、`NeuInput`、`NeuCard`
- **退出信号**：浏览器能看到 Neumorphism 风格的按钮和输入框示例页

**Step 2：登录页实现**
- 创建 `LoginPage` 组件（用户名密码表单）
- 修改 `authStore` 适配登录流程
- 配置 `/login` 路由
- **退出信号**：能登录成功，Token 存入 localStorage，跳转首页

**Step 3：路由守卫 + MainLayout**
- 实现 `AuthGuard`（检查 Token，无则跳转登录）
- 创建 `MainLayout`（导航栏 + 内容区）
- 配置首页路由 `/`
- **退出信号**：未登录访问首页会跳转登录；登录后能看到导航栏

**Step 4：首页（帖子列表）**
- 创建 `PostCard` 组件
- 创建 `HomePage` 组件（帖子列表 + 分页）
- 调用 `/api/post/list` 获取数据
- **退出信号**：首页能展示帖子列表，分页可用

**Step 5：帖子详情页**
- 创建 `PostDetailPage` 组件（标题、内容、作者、统计）
- 调用 `/api/post/{id}` 获取详情
- 配置 `/posts/:id` 路由
- **退出信号**：点击帖子卡片能进入详情页，内容正确展示

**Step 6：登出功能**
- 导航栏添加登出按钮
- 调用 `/auth/logout` 并清除 Token
- **退出信号**：登出后跳转登录页，再访问首页会被拦截

**Step 7：优化与细节**
- 加载状态骨架屏
- 错误提示 toast
- 响应式布局适配移动端
- **退出信号**：所有交互有反馈，移动端可用

### 测试设计

按功能点组织：

**F1：登录功能**
- 约束：用户名密码正确时返回 Token 并跳转首页
- 约束：错误时显示提示信息
- 约束：Token 存 localStorage
- 验证方式：手动测试登录流程
- 用例骨架：
  - 输入 admin/admin123 → 成功跳转
  - 输入错误密码 → 显示"用户名或密码错误"
  - 未登录访问首页 → 跳转登录页

**F2：帖子列表**
- 约束：能展示帖子标题、摘要、作者、统计
- 约束：分页功能可用
- 约束：加载时显示骨架屏
- 验证方式：手动测试首页
- 用例骨架：
  - 首页加载后能看到帖子列表
  - 点击下一页能看到新数据
  - 网络慢时能看到加载状态

**F3：帖子详情**
- 约束：能展示完整内容
- 约束：URL `/posts/:id` 正确解析
- 验证方式：手动测试详情页
- 用例骨架：
  - 点击帖子卡片 → 进入详情页
  - 详情页内容与列表卡片一致

**F4：登出功能**
- 约束：登出后 Token 清除
- 约束：登出后跳转登录页
- 验证方式：手动测试登出
- 用例骨架：
  - 点击登出 → 跳转登录页
  - 再访问首页 → 被拦截到登录页

### 高风险实现约束

- **样式一致性**：所有组件必须使用 `stype/` 定义的阴影和颜色，禁止混用 shadcn 默认样式
- **Token 管理**：登出必须清除 localStorage，否则 AuthGuard 漏判
- **路由解析**：`:id` 参数必须是数字，否则 API 调用失败

---

## 4. 与项目级架构文档的关系

本 feature 是前端层重写，不涉及后端架构变化。

完成后需在 `CLAUDE.md` 更新：
- 前端技术栈描述（React + Vite + Tailwind + Neumorphism）
- 服务端口表（确认代理配置）

引用来源：
- stype 设计系统：`stype/提示词.txt`
- tailwind preset：`stype/neumorphism-tailwind-preset.js`
- CSS 变量：`stype/neumorphism-globals.css`

---

## 5. 参考资料

- 现有 API 层：`clx-web/src/api/`
- 现有状态管理：`clx-web/src/store/`
- Neumorphism 设计规范：`stype/提示词.txt`
- 现有路由模式：`clx-web/src/routes/index.tsx`