---
doc_type: feature-design
feature: 2026-04-26-paicoding-layout
status: approved
summary: 完全模仿技术派首页布局：顶部标签栏 + 紧凑帖子列表 + 右侧热门与排行榜
tags: [frontend, layout, ux, refactor]
---

## 0. 术语约定

| 术语 | 定义 | 防冲突结论 |
|---|---|---|
| **顶部标签栏** | 固定在顶部的水平分类导航，替代左侧垂直导航 | 新增概念，grep `TopNav` 无冲突 |
| **紧凑列表** | 帖子以紧凑行式展示，标签+作者+时间+标题一行 | 新增概念，grep 无冲突 |
| **热门文章** | 右侧栏展示的高浏览量帖子列表 | 新增概念 |
| **月度排行** | 右侧栏展示的用户活跃度排名 | 新增概念 |

---

## 1. 决策与约束

### 需求摘要

- **做什么**：首页布局完全模仿技术派
- **为谁做**：桌面和移动端用户，提升浏览效率和社区感
- **成功标准**：
  1. 顶部标签栏替代左侧导航，分类可点击筛选
  2. 帖子卡片改为紧凑列表式（标签 + 作者 + 时间 + 标题一行）
  3. 右侧栏增加月度活跃排行榜
  4. 移动端适配：标签栏可滑动，右侧栏隐藏
- **明确不做**：
  - 不改动后端 API
  - 不改变配色和 Neumorphism 风格
  - 不新增发帖、评论等功能逻辑

### 关键决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 导航位置 | 顶部水平标签栏 | 模仿技术派；节省垂直空间；标签多时可滑动 |
| 帖子展示 | 紧凑列表式 | 模仿技术派；一屏可展示更多帖子；信息密度高 |
| 右侧内容 | 热门文章 + 月度排行榜 | 模仿技术派；增加社区活跃度展示 |
| 左侧导航 | 隐藏 | 技术派无左侧导航，更简洁 |

### 被拒方案

| 方案 | 拒绝理由 |
|---|---|
| 保留左侧导航 + 顶部标签 | 布局冗余，不符合技术派风格 |
| 帖子保持大卡片式 | 与技术派差异明显，信息密度低 |

### 主流程概述

**正常路径**：
1. 用户进入首页 → 看到顶部标签栏 + 紧凑帖子列表 + 右侧热门/排行
2. 点击标签 → 筛选对应分类帖子
3. 点击帖子 → 跳转详情页（保持现有逻辑）

**异常/边界**：
- 未登录用户：显示登录页（保持现有逻辑）
- 移动端：标签栏可滑动，右侧栏隐藏
- 无帖子时：显示空状态提示

---

## 2. 接口契约

### 2.1 组件拆分

```
App.tsx（修改）
├── TopNavBar（新增）- 顶部标签导航栏
├── MainLayout（修改）- 内容区布局
│   ├── PostList（重构）- 紧凑帖子列表
│   └── RightAside（修改）- 右侧栏
│       ├── HotPosts（新增）- 热门文章
│       └── ActiveRank（新增）- 月度活跃排行榜
└── pages/（保持）
```

### 2.2 TopNavBar 组件契约

```tsx
// clx-web/src/components/layout/TopNavBar.tsx（新增）

interface TopNavBarProps {
  categories: CategoryVO[];      // 分类列表
  activeCategory: string;        // 当前选中分类（空字符串=全部）
  onCategoryChange: (id: string) => void;
}

// 渲染示例
<TopNavBar
  categories={[{id: 1, name: '技术'}, {id: 2, name: '求职'}]}
  activeCategory=""
  onCategoryChange={(id) => setCategory(id)}
/>

// 样式：固定顶部，水平滚动，标签带选中态
```

### 2.3 PostList 紧凑项契约

```tsx
// clx-web/src/components/post/PostListItem.tsx（新增）

interface PostListItemProps {
  post: PostListItemVO;
  onClick: () => void;
}

// 渲染结构（一行）
// [标签] [作者] · [时间]
// 标题
// [赞 x] [评 x]

// 样式：紧凑，hover 高亮，点击跳转详情
```

### 2.4 ActiveRank 组件契约

```tsx
// clx-web/src/components/aside/ActiveRank.tsx（新增）

interface ActiveRankProps {
  users: ActiveUserVO[];  // 活跃用户列表
}

interface ActiveUserVO {
  rank: number;
  userId: number;
  username: string;
  score: number;
}

// 渲染示例
<ActiveRank users={[
  {rank: 1, userId: 1, username: 'admin', score: 1185},
  {rank: 2, userId: 2, username: 'test', score: 457},
]} />

// 样式：排名 + 头像 + 用户名 + 积分
```

### 2.5 API 需求

| 接口 | 状态 | 说明 |
|---|---|---|
| `GET /post/list?categoryId={id}` | 已有 | 按分类筛选帖子 |
| `GET /post/hot?limit=5` | 已有 | 热门帖子（需确认） |
| `GET /user/active-rank?period=month` | **需新增** | 月度活跃排行榜 |

**新增接口契约**：

```json
// GET /user/active-rank?period=month&limit=10
{
  "code": 200,
  "data": [
    {"rank": 1, "userId": 1, "username": "admin", "score": 1185},
    {"rank": 2, "userId": 2, "username": "test", "score": 457}
  ]
}
```

---

## 3. 实现提示

### 3.1 改动计划

| 文件 | 操作 | 说明 |
|---|---|---|
| `src/components/layout/TopNavBar.tsx` | 新建 | 顶部标签栏 |
| `src/components/layout/LeftNav.tsx` | 删除 | 不再需要左侧导航 |
| `src/components/post/PostListItem.tsx` | 新建 | 紧凑帖子项 |
| `src/components/aside/ActiveRank.tsx` | 新建 | 月度排行榜 |
| `src/components/aside/HotPosts.tsx` | 新建 | 热门文章（从 ListCard 演化） |
| `src/pages/HomePage.tsx` | 重构 | 使用新组件 |
| `src/App.tsx` | 修改 | 移除 LeftNav，添加 TopNavBar |
| `src/styles/app.css` | 修改 | 新增紧凑列表样式、顶部导航样式 |
| `clx-user/src/main/java/.../controller/UserController.java` | 新增 | 活跃排行榜接口 |

### 3.2 推进顺序

| 步骤 | 内容 | 退出信号 |
|---|---|---|
| 1 | 新建 TopNavBar 组件，固定在顶部 | 标签栏可见，可点击切换 |
| 2 | 修改 App.tsx，用 TopNavBar 替代 LeftNav | 左侧导航消失，顶部导航生效 |
| 3 | 新建 PostListItem 紧凑项组件 | 帖子列表变紧凑 |
| 4 | 重构 HomePage，使用 PostListItem | 首页帖子列表显示正确 |
| 5 | 新建 HotPosts 和 ActiveRank 组件 | 右侧栏组件完成 |
| 6 | 后端新增活跃排行榜 API（mock 数据先） | API 可调用 |
| 7 | 修改 RightAside，集成 HotPosts + ActiveRank | 右侧栏显示热门+排行 |
| 8 | CSS 媒体查询，移动端适配 | 移动端标签栏可滑动，右侧栏隐藏 |
| 9 | 全流程测试 | 所有功能正常 |

### 3.3 测试设计

| 功能点 | 测试约束 | 验证方式 |
|---|---|---|
| 顶部标签栏 | 所有分类可见，选中态高亮 | 手动测试 |
| 标签筛选 | 点击分类后列表更新 | 手动测试 |
| 紧凑帖子列表 | 标签+作者+时间+标题一行显示 | 手动测试 |
| 点击跳转详情 | 跳转到 `/post/:id` | 手动测试 |
| 热门文章 | 显示前5篇 | 手动测试 |
| 月度排行榜 | 显示前10名用户 | 手动测试 |
| 移动端适配 | <640px 标签栏可滑动，右侧栏隐藏 | Chrome DevTools |

### 3.4 高风险实现约束

| 约束 | 说明 | 缓解措施 |
|---|---|---|
| 左侧导航完全移除 | 影响所有页面布局 | 先注释，确认无问题后再删除 |
| 后端 API 依赖 | 排行榜需后端配合 | 先用 mock 数据，后端并行开发 |

---

## 4. 与项目级架构文档的关系

### 关联架构文档

- `CLAUDE.md` - 更新前端布局说明
- `easysdd/features/2026-04-26-zhihu-layout/zhihu-layout-design.md` - 参考上次布局重构

### 架构索引更新

更新 `CLAUDE.md`：
- 模块结构图：移除 LeftNav，新增 TopNavBar
- 前端组件列表更新

---

## 5. 需要后端配合

| 接口 | 优先级 | 负责人 |
|---|---|---|
| `GET /user/active-rank` | P0 | 待定 |

**接口说明**：
- 统计维度：月度发帖数 + 评论数 + 点赞数
- 返回 Top 10
- 无需登录
