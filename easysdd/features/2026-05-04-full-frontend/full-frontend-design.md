---
name: full-frontend
feature: 2026-05-04-full-frontend
doc_type: feature-design
status: approved
summary: 完整前端开发 - 前后台分离架构，包含用户前台（clx-web React）和后台管理端（clx-admin-web Vue）的全页面实现
tags: [frontend, react, vue, neumorphism, admin, separation]
created: 2026-05-04
---

# 完整前端开发方案

## 0. 术语约定

| 术语 | 含义 | 来源 |
|------|------|------|
| 前台 | 用户端网站，clx-web，React + Neumorphism 风格 | 本方案定义 |
| 后台 | 管理端网站，clx-admin-web，Vue 3 + Element Plus | 本方案定义 |
| Gateway | API 网关，统一入口，端口 8080 | AGENTS.md |
| Neumorphism | 新拟态设计风格，柔和阴影凸起效果 | frontend-rewrite-design.md |

---

## 1. 需求摘要与约束

### 1.1 需求摘要

**用户目标**：完成前端样式的全部开发，实现前后台分离架构。

**核心行为**：
1. 用户前台（clx-web）：用户登录、浏览帖子、发帖评论、刷题、私信、个人主页
2. 后台管理（clx-admin-web）：用户管理、帖子审核、评论管理、数据统计、权限管理

**成功标准**：
- 前台 8 个页面全部可用（首页、详情、发帖、搜索、刷题、账号、用户主页、消息）
- 后台 6 个模块全部可用（用户、帖子、评论、统计、权限、日志）
- 所有页面通过 Gateway 调用后端 API
- Neumorphism 风格统一，响应式适配

### 1.2 明确不做

| ID | 不做 | 原因 |
|----|------|------|
| S1 | 不做 SSR/SEO 优化 | MVP 阶段，CSR 即可 |
| S2 | 不做 PWA 离线支持 | 非核心需求 |
| S3 | 不做国际化 i18n | 单一语言足够 |
| S4 | 不做深色模式 | 当前仅浅色主题 |
| S5 | 不做实时协作编辑 | 非核心需求 |

### 1.3 决策与约束

**D1**: 前台继续使用 React + Vite + Neumorphism 风格，复用现有 CSS 变量体系
- **原因**：已有 8 个页面骨架，风格已定
- **被拒方案**：重写为 Vue 或其他框架（浪费已有投入）

**D2**: 后台新建 clx-admin-web 项目，使用 Vue 3 + Element Plus
- **原因**：Element Plus 组件丰富，后台管理场景成熟
- **被拒方案**：React + Ant Design（前台技术栈不同，后台独立维护更清晰）

**D3**: 所有 API 请求通过 Gateway（端口 8080）
- **原因**：统一入口，便于认证、限流、监控
- **被拒方案**：直接访问各服务端口（绕过安全层）

**D4**: 前后台代码仓库分离
- **原因**：独立部署、独立版本、职责清晰
- **被拒方案**：Monorepo 合并（部署耦合）

**D5**: 用户认证使用 JWT Token，存储在 localStorage
- **原因**：当前架构已实现，保持一致
- **被拒方案**：Cookie Session（需要后端改造成本）

---

## 2. 接口契约

### 2.1 前台 API（clx-web）

**认证模块**（已实现，来自 authApi）：

```
POST /auth/login
输入：{ username, password, captchaId, captchaCode }
输出：{ code: 200, data: { tokenValue, tokenName, userId, username } }

POST /auth/register  
输入：{ username, password, confirmPassword, email, emailCode }
输出：{ code: 200, data: { token, userId } }

GET /auth/me
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { userId, username } }
```

**帖子模块**（已实现，来自 postApi）：

```
GET /post/list?page=1&size=20&sort=latest&categoryId=1
输出：{ code: 200, data: { posts: PostListItemVO[], total, page, size } }

GET /post/{id}
输出：{ code: 200, data: PostDetailVO }

POST /post/create
Header：Authorization: Bearer {token}
输入：{ title, content, categoryId?, tagIds? }
输出：{ code: 200, data: postId }

POST /post/{id}/like
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { likeCount } }

GET /post/{id}/comments
输出：{ code: 200, data: CommentVO[] }

POST /post/{id}/comment
Header：Authorization: Bearer {token}
输入：{ content, parentId?, replyToId? }
输出：{ code: 200, data: commentId }
```

**用户模块**（已实现，来自 userApi）：

```
GET /user/{userId}
输出：{ code: 200, data: UserProfileVO }

PUT /user/profile
Header：Authorization: Bearer {token}
输入：{ nickname?, avatar?, signature?, gender? }
输出：{ code: 200 }

POST /user/follow/{userId}
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { followCount } }

GET /user/{userId}/following?page=1&size=20
输出：{ code: 200, data: { total, list: UserSimpleVO[] } }

GET /user/{userId}/fans?page=1&size=20
输出：{ code: 200, data: { total, list: UserSimpleVO[] } }

GET /user/favorites?page=1&size=20
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { total, list: FavoriteItemVO[] } }

GET /user/active?limit=5
输出：{ code: 200, data: ActiveUserVO[] }
```

**搜索模块**（已实现，来自 searchApi）：

```
POST /search/aggregate
输入：{ keyword, types?, page?, size? }
输出：{ code: 200, data: { keyword, results: { post: SearchResult, user: SearchResult } } }

GET /search/hot?period=day&limit=5
输出：{ code: 200, data: HotKeywordVO[] }
```

**刷题模块**（已实现，来自 quizApi）：

```
GET /quiz/category/list
输出：{ code: 200, data: SubjectCategoryVO[] }

POST /quiz/practice/start
输入：{ labelIds?, count? }
输出：{ code: 200, data: { practiceId, totalCount, subjectIds } }

POST /quiz/practice/submit
输入：{ practiceId, subjectId, subjectType, answerContent }
输出：{ code: 200, data: SubmitResultVO }

POST /quiz/practice/finish
输入：{ practiceId }
输出：{ code: 200, data: PracticeResultVO }
```

### 2.2 后台 API（clx-admin-web）

**用户管理**：

```
POST /admin/user/page
Header：Authorization: Bearer {token}（需要 admin 角色）
输入：{ username?, status?, pageNo, pageSize }
输出：{ code: 200, data: { total, list: UserAdminVO[] } }

PUT /admin/user/{userId}/ban
Header：Authorization: Bearer {token}
输出：{ code: 200 }

PUT /admin/user/{userId}/unban
Header：Authorization: Bearer {token}
输出：{ code: 200 }

GET /admin/user/{userId}
Header：Authorization: Bearer {token}
输出：{ code: 200, data: UserDetailAdminVO }

PUT /admin/user/{userId}
Header：Authorization: Bearer {token}
输入：{ nickname?, status? }
输出：{ code: 200 }
```

**帖子管理**：

```
POST /admin/post/page
Header：Authorization: Bearer {token}
输入：{ title?, status?, authorId?, pageNo, pageSize }
输出：{ code: 200, data: { total, list: PostAdminVO[] } }

PUT /admin/post/{postId}/status
Header：Authorization: Bearer {token}
输入：{ status } // 0=正常, 1=隐藏, 2=删除
输出：{ code: 200 }

DELETE /admin/post/{postId}
Header：Authorization: Bearer {token}
输出：{ code: 200 }
```

**评论管理**：

```
POST /admin/comment/page
Header：Authorization: Bearer {token}
输入：{ content?, status?, pageNo, pageSize }
输出：{ code: 200, data: { total, list: CommentAdminVO[] } }

DELETE /admin/comment/{commentId}
Header：Authorization: Bearer {token}
输出：{ code: 200 }
```

**数据统计**：

```
GET /admin/stats/overview
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { dau, mau, newUsers, newPosts, newComments } }

GET /admin/stats/trend?days=7
Header：Authorization: Bearer {token}
输出：{ code: 200, data: { dates: string[], dau: number[], posts: number[] } }
```

**角色权限**：

```
GET /admin/role/list
Header：Authorization: Bearer {token}
输出：{ code: 200, data: RoleVO[] }

GET /admin/permission/list
Header：Authorization: Bearer {token}
输出：{ code: 200, data: PermissionVO[] }

PUT /admin/role/{roleId}/permissions
Header：Authorization: Bearer {token}
输入：{ permissionIds: number[] }
输出：{ code: 200 }
```

**操作日志**：

```
POST /admin/log/page
Header：Authorization: Bearer {token}
输入：{ operator?, action?, startTime?, endTime?, pageNo, pageSize }
输出：{ code: 200, data: { total, list: OperLogVO[] } }
```

---

## 3. 实现提示

### 3.1 项目结构

**前台 clx-web（已存在，补充完善）**：

```
clx-web/
├── src/
│   ├── pages/           # 页面组件（已存在 8 个，需完善）
│   │   ├── HomePage.tsx         # 首页（列表已完成，补充无限滚动）
│   │   ├── PostDetailPage.tsx   # 详情页（补充评论组件）
│   │   ├── ComposePage.tsx      # 发帖页（补充 Markdown 编辑器）
│   │   ├── SearchPage.tsx       # 搜索页（完善聚合结果展示）
│   │   ├── QuizPage.tsx         # 刷题页（完善答题流程）
│   │   ├── AccountPage.tsx      # 账号页（完善第三方绑定）
│   │   ├── AuthPage.tsx         # 登录注册页（完善注册流程）
│   │   ├── UserProfilePage.tsx  # 用户主页（完善关注列表）
│   │   └── MessagePage.tsx      # 新增：私信消息页
│   ├── components/
│   │   ├── layout/              # 布局组件（已存在）
│   │   ├── post/                # 帖子组件（已存在）
│   │   ├── aside/                # 侧栏组件（已存在）
│   │   ├── comment/             # 新增：评论组件
│   │   ├── message/             # 新增：消息组件
│   │   └── ui/                   # Neumorphism 基础组件
│   ├── api/                     # API 请求（已实现）
│   ├── styles/                  # 样式文件（已存在 app.css）
│   └── hooks/                   # 新增：自定义 Hooks
├── vite.config.ts               # 代理配置指向 Gateway
└── package.json
```

**后台 clx-admin-web（新建）**：

```
clx-admin-web/
├── src/
│   ├── views/           # 页面组件
│   │   ├── login/
│   │   │   └── LoginView.vue    # 管理员登录
│   │   ├── dashboard/
│   │   │   └── DashboardView.vue # 数据仪表盘
│   │   ├── user/
│   │   │   ├── UserListView.vue # 用户列表
│   │   │   └── UserEditView.vue # 用户编辑
│   │   ├── post/
│   │   │   └── PostListView.vue  # 帖子管理
│   │   ├── comment/
│   │   │   └── CommentListView.vue # 评论管理
│   │   ├── role/
│   │   │   └── RoleListView.vue  # 角色权限
│   │   └── log/
│   │       └── LogListView.vue   # 操作日志
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AdminLayout.vue  # 后台布局
│   │   │   ├── Sidebar.vue      # 左侧菜单
│   │   │   └── Header.vue       # 顶部栏
│   │   └── common/              # 公共组件
│   ├── api/
│   │   ├── index.ts             # axios 封装
│   │   ├── user.ts              # 用户 API
│   │   ├── post.ts              # 帖子 API
│   │   ├── stats.ts             # 统计 API
│   │   └── role.ts              # 角色 API
│   ├── stores/                  # Pinia 状态管理
│   ├── router/                  # Vue Router
│   ├── styles/                  # 全局样式
│   └── App.vue
├── vite.config.ts
└── package.json
```

### 3.2 关键实现点

**前台补充**：

| 模块 | 当前状态 | 需补充 |
|------|---------|--------|
| 评论 | 无组件 | CommentList、CommentInput、CommentItem 组件 |
| 消息 | 无页面 | MessagePage、ChatWindow、NotificationList 组件 |
| 无限滚动 | 无 | useInfiniteScroll hook + IntersectionObserver |
| Markdown 编辑器 | 无 | 简易工具栏 + 预览（可用 react-simplemde-editor） |
| 第三方绑定 | 部分 | OAuth 回调处理完善 |
| 实时消息 | 无 | WebSocket 连接 + 消息订阅 |

**后台新建**：

| 模块 | 组件 | 功能 |
|------|------|------|
| 登录 | LoginView | 管理员账号密码登录 |
| 仪表盘 | DashboardView | DAU/MAU/新用户卡片 + 趋势图 |
| 用户管理 | UserListView | 表格 + 搜索 + 封禁/解封操作 |
| 帖子管理 | PostListView | 表格 + 状态筛选 + 隐藏/删除 |
| 评论管理 | CommentListView | 表格 + 内容搜索 + 删除 |
| 角色权限 | RoleListView | 角色列表 + 权限分配树 |
| 操作日志 | LogListView | 表格 + 时间筛选 + 操作类型 |

### 3.3 推进顺序

**阶段 1：前台核心功能补全**
1. 评论组件开发 → 完成后可在帖子详情页发表评论
2. 消息页面开发 → 完成后可查看私信和通知
3. 发帖编辑器优化 → 完成后可使用 Markdown

**阶段 2：后台管理端搭建**
4. 创建 clx-admin-web 项目 → 完成后可启动空壳
5. 登录页面 + 布局框架 → 完成后可登录进入后台
6. 用户管理模块 → 完成后可查看/封禁用户
7. 帖子管理模块 → 完成后可审核/删除帖子
8. 评论管理模块 → 完成后可管理评论
9. 数据统计模块 → 完成后可查看仪表盘
10. 角色权限模块 → 完成后可分配权限

**阶段 3：联调与优化**
11. Gateway 路由配置 → 所有 API 通过网关
12. 响应式适配优化 → 移动端可用
13. 错误处理统一 → 全局 Toast 提示

### 3.4 测试设计

**前台测试验证**：

| 功能点 | 验证方式 | 关键用例 |
|--------|---------|---------|
| 登录 | 浏览器操作 | 正确登录跳转首页，错误显示提示 |
| 发帖 | 浏览器操作 | 登录后可发布，未登录跳转登录 |
| 评论 | 浏览器操作 | 详情页可发表评论，回复可嵌套显示 |
| 关注 | 浏览器操作 | 用户主页点击关注，数字增加 |
| 刷题 | 浏览器操作 | 开始练习 → 答题 → 提交 → 查看结果 |
| 消息 | 浏览器操作 | 发送私信，接收方实时收到 |

**后台测试验证**：

| 功能点 | 验证方式 | 关键用例 |
|--------|---------|---------|
| 登录 | 浏览器操作 | admin 登录成功，普通用户登录失败 |
| 用户管理 | 浏览器操作 | 搜索用户，封禁后前台无法登录 |
| 帖子管理 | 浏览器操作 | 隐藏帖子后前台不可见 |
| 角色分配 | 浏览器操作 | 分配角色后用户获得对应权限 |

### 3.5 风险点

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| WebSocket 连接不稳定 | 实时消息不可用 | 降级为轮询，显示连接状态 |
| Element Plus 样式冲突 | 后台样式异常 | 使用 CSS Module 隔离 |
| Gateway 路由配置复杂 | API 不可达 | 先直连测试再切换网关 |

---

## 4. 与项目级架构文档的关系

**本 feature 完成后需更新 CLAUDE.md**：

- [ ] 前端项目表：添加 clx-admin-web（端口 5174）
- [ ] 技术栈描述：补充前台 React + Neumorphism、后台 Vue 3 + Element Plus
- [ ] Gateway 路由表：添加 /admin/* 路由到 clx-admin 服务

**依赖的现有模块**：

- `clx-gateway`：API 网关，统一入口
- `clx-auth`：认证服务，JWT Token 签发
- `clx-admin`：后台管理服务，提供 admin API
- `clx-user`/`clx-post`/`clx-message`：业务服务

---

## 5. 参考资料

- 前台已有实现：`clx-web/src/`
- Neumorphism 样式规范：`clx-web/src/styles/app.css`
- API 类型定义：`clx-web/src/api/types.ts`
- 后台管理 API：`clx-admin/src/main/java/com/clx/admin/controller/`
