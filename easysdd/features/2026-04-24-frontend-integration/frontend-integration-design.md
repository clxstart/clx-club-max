---
doc_type: feature-design
feature: 2026-04-24-frontend-integration
status: approved
summary: 对接已有后端 API，实现帖子交互（发帖/编辑/删除/点赞）、评论功能、搜索功能
tags: [frontend, api-integration, post, comment, search, neumorphism]
---

# Frontend Integration Design

> Stage 1 | 2026-04-24 | Status: draft

## 0. 术语约定

| 术语 | 定义 | 备注 |
|---|---|---|
| Post | 帖子，社区内容的基本单元 | 后端实体名，前端沿用 |
| Comment | 评论，对帖子的回复 | 支持多级嵌套 |
| Like | 点赞，对帖子或评论的赞同标记 | 支持帖子点赞和评论点赞 |
| Category | 分类，帖子的所属类别 | 预定义类别，用户不可创建 |
| Tag | 标签，帖子的关键词标记 | 预定义标签，用户不可创建 |
| Search | 聚合搜索，跨数据源搜索 | 搜索帖子/用户/评论 |
| NeuButton/NeuInput/NeuCard | Neumorphism 风格基础组件 | 已实现，复用 |
| HotKeyword | 热词，高频搜索词统计 | 搜索服务提供 |

**禁用词**：PostVO、CommentVO 等后端 VO 名不在前端代码出现，前端使用 Post、Comment 等简名。

---

## 1. 决策与约束

### 需求摘要

**做什么**：对接 clx-post 和 clx-search 后端 API，实现完整的社区交互功能。

**MVP 范围**：
- 帖子交互：发帖、编辑、删除、点赞/取消点赞
- 评论功能：查看评论列表、发评论（一级评论）、删除自己的评论、点赞评论
- 搜索功能：聚合搜索、热词展示

**明确不做**：
- 二级评论回复（parentId > 0 的嵌套评论）— MVP 只做一级评论展示和输入
- 用户中心/个人主页 — 后端 clx-user 未实现
- 消息/通知 — 后端未实现
- 管理后台 — 后端未实现
- OAuth/手机登录 — 已有后端但不在本次范围
- 深色模式 — Neumorphism MVP 只做浅色
- 富文本编辑器 — MVP 用纯文本，后续迭代

### 关键决策

| 冺策 | 选择 | 被拒方案 | 原因 |
|---|---|---|---|
| 发帖入口 | 导航栏按钮 → 弹出 Modal | 独立发帖页面 | 用户不用离开当前页，体验更流畅 |
| 评论展示 | 详情页底部列表 | 首页卡片内嵌 | 评论数据量大，列表更适合 |
| 搜索入口 | 导航栏搜索图标 → 独立搜索页 | 首页内嵌搜索框 | 搜索结果多类型，独立页面更清晰 |
| 状态管理 | zustand（延续现有方案） | React Query | 已有 authStore 模式可复用 |
| 删除确认 | Modal 确认框 | 直接删除 / Toast 撤销 | 防误删，撤销功能复杂度更高 |

### 假设与风险

**假设**：
- 后端 clx-post 9300 端口、clx-search 9400 端口已可用
- Vite dev server 已代理 `/api/*` 到后端（需确认 clx-search 代理）
- 用户只能编辑/删除自己的帖子/评论（后端权限校验）

**风险点**：
- 聚合搜索返回 Map<String, SearchResult>，前端类型处理需注意
- 评论嵌套结构（children）MVP 只展示一级，后续迭代需处理嵌套
- 点赞状态同步：点赞后列表和详情页数据需同步刷新

### 前置依赖

无前置依赖。本 feature 放在 `clx-web/src/features/` 目录下，属于前端层功能扩展。

---

## 2. 契约层

### API 接口

#### 帖子相关

| 功能 | 接口 | 方法 | 输入 | 输出 |
|---|---|---|---|---|
| 发帖 | `/post/create` | POST | `{title, content, categoryId?, tagIds?}` | `{code, data: postId}` |
| 编辑 | `/post/{id}` | PUT | `{title, content, categoryId?, tagIds?}` | `{code}` |
| 删除 | `/post/{id}` | DELETE | - | `{code}` |
| 点赞 | `/post/{id}/like` | POST | - | `{code, data: {likeCount}}` |
| 取消点赞 | `/post/{id}/like` | DELETE | - | `{code, data: {likeCount}}` |
| 分类列表 | `/category/list` | GET | - | `{code, data: [{id, name}]}` |
| 标签列表 | `/tag/list` | GET | - | `{code, data: [{id, name, color?}]}` |

**示例**（发帖）：
```typescript
// 输入
{ title: "新帖子标题", content: "帖子内容...", categoryId: 1, tagIds: [1, 2] }
// 输出
{ code: 200, data: 123 }  // 返回新帖子 ID
```

#### 评论相关

| 功能 | 接口 | 方法 | 输入 | 输出 |
|---|---|---|---|---|
| 评论列表 | `/post/{postId}/comments` | GET | - | `{code, data: [CommentVO...]}` |
| 发评论 | `/post/{postId}/comment` | POST | `{content, parentId=0}` | `{code, data: commentId}` |
| 删除评论 | `/post/{postId}/comment/{commentId}` | DELETE | - | `{code}` |
| 点赞评论 | `/comment/{id}/like` | POST | - | `{code, data: {likeCount}}` |

**CommentVO 结构**：
```typescript
interface Comment {
  id: number;
  content: string;
  author: { id: number; name: string; avatar?: string };
  likeCount: number;
  isLiked: boolean;
  createdAt: string;
  children?: Comment[];  // MVP 只展示一级，children 不渲染
}
```

#### 搜索相关

| 功能 | 接口 | 方法 | 输入 | 输出 |
|---|---|---|---|---|
| 聚合搜索 | `/search/aggregate` | POST | `{keyword, types?, page, size}` | `SearchVO` |
| 热词统计 | `/search/hot` | GET | `{period?, limit}` | `{code, data: [{keyword, count}]}` |

**SearchVO 结构**：
```typescript
interface SearchVO {
  keyword: string;
  totalTime: number;
  results: {
    posts?: { total: number; items: Post[] };
    users?: { total: number; items: User[] };
    comments?: { total: number; items: Comment[] };
  };
  suggest?: string[];
  partialSuccess?: boolean;
}
```

### 页面路由

新增路由：

| 路径 | 页面 | 认证要求 | 说明 |
|---|---|---|---|
| `/search` | SearchPage | 无 | 聚合搜索页 |
| `/posts/:id` | PostDetailPage | 需要 | 已有，扩展评论和点赞 |

### 组件 Props 契约

**CreatePostModal**（发帖弹窗）：
```typescript
interface CreatePostModalProps {
  open: boolean;
  onClose: () => void;
  onSuccess: (postId: number) => void;  // 发帖成功回调
}
// 渲染：标题输入、内容输入、分类选择、标签多选、提交按钮
```

**CommentList**（评论列表）：
```typescript
interface CommentListProps {
  postId: number;
}
// 渲染：评论列表、发表评论输入框、点赞按钮、删除按钮（自己的评论）
```

**SearchPage**（搜索页）：
```typescript
// 无 Props，通过 URL query 参数 ?q=keyword 获取搜索词
// 渲染：搜索输入框、热词列表、搜索结果（帖子/用户/评论 tabs）
```

---

## 3. 实现提示

### 改动计划

| 操作 | 文件/目录 | 说明 |
|---|---|---|
| 修改 | `clx-web/src/api/endpoints.ts` | 补充帖子交互、评论、搜索端点 |
| 新建 | `clx-web/src/features/post/api/postApi.ts` | 补充 create/update/delete/like 方法 |
| 新建 | `clx-web/src/features/post/api/commentApi.ts` | 评论 API |
| 新建 | `clx-web/src/api/searchApi.ts` | 搜索 API |
| 新建 | `clx-web/src/features/post/components/CreatePostModal.tsx` | 发帖弹窗 |
| 新建 | `clx-web/src/features/post/components/CommentList.tsx` | 评论列表组件 |
| 新建 | `clx-web/src/features/post/components/CommentItem.tsx` | 评论单项组件 |
| 新建 | `clx-web/src/features/search/pages/SearchPage.tsx` | 搜索页 |
| 新建 | `clx-web/src/features/search/components/SearchBar.tsx` | 搜索输入组件 |
| 新建 | `clx-web/src/features/search/components/HotKeywords.tsx` | 热词展示组件 |
| 新建 | `clx-web/src/features/search/components/SearchResultTabs.tsx` | 搜索结果 tabs |
| 修改 | `clx-web/src/layouts/MainLayout.tsx` | 导航栏添加发帖按钮、搜索图标 |
| 修改 | `clx-web/src/features/post/pages/PostDetailPage.tsx` | 集成评论列表、点赞交互 |
| 修改 | `clx-web/src/features/post/pages/HomePage.tsx` | 帖子卡片添加点赞按钮 |
| 修改 | `clx-web/src/features/post/components/PostCard.tsx` | 点赞按钮交互 |
| 修改 | `clx-web/src/routes/index.tsx` | 添加 /search 路由 |
| 修改 | `clx-web/vite.config.ts` | 确认 clx-search 9400 代理 |

### 推进顺序

**Step 1：API 层扩展**
- 补充 endpoints.ts（帖子交互、评论、搜索端点）
- 扩展 postApi.ts（create/update/delete/like）
- 新建 commentApi.ts
- 新建 searchApi.ts
- **退出信号**：TypeScript 编译无错误，API 方法可调用

**Step 2：发帖功能**
- 新建 CreatePostModal 组件（标题/内容/分类/标签）
- MainLayout 导航栏添加发帖按钮
- 发帖成功后刷新首页列表
- **退出信号**：点击发帖按钮弹出 Modal，填写内容提交后首页能看到新帖子

**Step 3：点赞功能**
- PostCard 添加点赞按钮，显示点赞数和 isLiked 状态
- PostDetailPage 添加点赞按钮
- 点赞后更新状态和数量
- **退出信号**：点赞按钮可点击，状态切换，数量实时更新

**Step 4：帖子编辑/删除**
- PostDetailPage 添加编辑/删除按钮（仅作者可见）
- 编辑复用 CreatePostModal（传入 postId 加载数据）
- 删除前 Modal 确认，删除后跳转首页
- **退出信号**：作者能看到编辑/删除按钮，编辑保存成功，删除后帖子消失

**Step 5：评论功能**
- 新建 CommentItem 组件
- 新建 CommentList 组件（列表 + 输入框）
- PostDetailPage 集成 CommentList
- 评论发表后刷新列表
- **退出信号**：详情页能看到评论列表，能发表评论，能看到自己的评论并删除

**Step 6：评论点赞**
- CommentItem 添加点赞按钮
- 点赞后更新状态和数量
- **退出信号**：评论点赞按钮可点击，状态切换

**Step 7：搜索功能**
- 新建 SearchBar、HotKeywords、SearchResultTabs 组件
- 新建 SearchPage
- MainLayout 导航栏添加搜索图标
- 路由添加 /search
- **退出信号**：点击搜索图标进入搜索页，输入关键词能看到结果，能看到热词

**Step 8：验证与细节**
- vite.config.ts 确认代理配置
- 所有交互流程浏览器验证
- 响应式适配
- **退出信号**：所有功能浏览器验证通过，移动端可用

### 测试设计

**F1：发帖功能**
- 约束：标题必填，内容必填，分类/标签可选
- 约束：发帖成功返回帖子 ID 并跳转详情页（或刷新首页）
- 约束：空标题/空内容提交被拒绝并显示提示
- 验证方式：手动测试发帖流程
- 用例骨架：
  - 点击发帖 → Modal 打开
  - 填写标题内容 → 提交成功 → 首页看到新帖子
  - 不填标题 → 提交失败 → 显示"标题不能为空"

**F2：点赞功能**
- 约束：点赞后按钮状态变化，数量 +1
- 约束：取消点赞后按钮恢复，数量 -1
- 约束：点赞状态在列表页和详情页同步
- 验证方式：手动测试点赞交互
- 用例骨架：
  - 点击点赞 → 状态变已点赞，数量增加
  - 再次点击 → 取消点赞，数量减少
  - 首页点赞 → 进入详情页 → 状态一致

**F3：编辑/删除帖子**
- 约束：只有作者能看到编辑/删除按钮
- 约束：编辑保存后内容更新
- 纄束：删除前确认，删除后帖子消失
- 验证方式：手动测试编辑删除
- 用例骨架：
  - 作者进入详情页 → 看到编辑/删除按钮
  - 点击编辑 → Modal 打开预填数据 → 保存成功
  - 点击删除 → 确认框 → 确认后跳转首页，帖子消失

**F4：评论功能**
- 约束：评论列表正确展示
- 约束：发表评论后列表刷新
- 约束：只有作者能看到删除按钮
- 验证方式：手动测试评论
- 用例骨架：
  - 进入详情页 → 看到评论列表
  - 输入评论 → 提交成功 → 列表出现新评论
  - 点击删除 → 确认 → 评论消失

**F5：搜索功能**
- 约束：输入关键词能看到搜索结果
- 约束：热词列表展示
- 约束：点击热词触发搜索
- 验证方式：手动测试搜索
- 用例骨架：
  - 点击搜索图标 → 进入搜索页
  - 输入关键词 → 看到帖子/用户/评论结果
  - 看到热词列表 → 点击热词 → 搜索结果展示

### 高风险实现约束

- **点赞状态同步**：点赞后需同步更新 PostCard 和 PostDetailPage 中的 isLiked 和 likeCount，考虑使用乐观更新
- **删除确认**：删除帖子/评论必须有 Modal 确认，不能直接删除
- **权限判断**：编辑/删除按钮只在 author.id === currentUserId 时显示
- **搜索类型安全**：SearchVO.results 是 Map，前端需安全处理可能为空的字段

---

## 4. 与项目级架构文档的关系

本 feature 是前端层功能扩展，不涉及后端架构变化。

完成后需在 `CLAUDE.md` 更新：
- 前端功能描述（补充帖子交互、评论、搜索）

前端模块结构扩展：
```
clx-web/src/features/
├── auth/           # 已有：登录
├── post/           # 扩展：发帖/编辑/删除/点赞/评论
├── search/         # 新增：聚合搜索
```

---

## 5. 参考资料

- 后端 DTO/VO：`clx-post/src/main/java/com/clx/post/dto/`、`clx-post/src/main/java/com/clx/post/vo/`
- 后端控制器：`clx-post/src/main/java/com/clx/post/controller/`
- 现有前端 API：`clx-web/src/api/endpoints.ts`、`clx-web/src/features/post/api/postApi.ts`
- Neumorphism 设计规范：`stype/提示词.txt`