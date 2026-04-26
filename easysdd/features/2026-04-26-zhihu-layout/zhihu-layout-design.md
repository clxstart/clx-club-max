---
doc_type: feature-design
feature: 2026-04-26-zhihu-layout
status: approved
summary: 参考知乎的三栏布局重构前端，内容流与详情页分离，适配桌面和移动端
tags: [frontend, layout, responsive, ux]
---

## 0. 术语约定

| 术语 | 定义 | 防冲突结论 |
|---|---|---|
| **三栏布局** | 左侧固定导航 + 中间内容流 + 右侧辅助信息 | 新增概念，grep 无冲突 |
| **内容流** | 首页的帖子列表区域，单列展示，宽度充足 | 新增概念 |
| **详情页分离** | 点击帖子后跳转独立页面全屏展示，而非嵌在右侧半栏 | 新增概念 |
| **左导航** | 固定在左侧的垂直导航栏，替代当前顶部水平导航 | 新增概念，grep 无 `LeftNav`/`Sidebar` |

## 1. 决策与约束

### 需求摘要

- **做什么**：将当前两栏布局（左帖子列表 + 右详情）改为知乎风格三栏布局
- **为谁**：使用桌面和移动端的用户，提升阅读和浏览体验
- **成功标准**：
  1. 首页内容流宽度充足（≥600px），帖子卡片清晰可读
  2. 点击帖子后跳转独立详情页，全屏展示
  3. 左侧导航固定，切换页面时不重复渲染
  4. 移动端自动收起侧栏，原生单页体验
- **明确不做**：
  - 不改动配色和视觉风格（保持 Neumorphism）
  - 不改动后端 API
  - 不新增功能（私信、通知等），只改布局
  - 不引入新的状态管理库（保持 React state + props）

### 关键决策

| 决策 | 选择 | 理由 |
|---|---|---|
| 导航位置 | 左侧垂直导航栏 | 参考知乎；节省顶部空间；tab 多时不拥挤 |
| 详情展示 | 独立页面全屏展示 | 避免右侧半栏压缩内容；移动端体验一致 |
| 右侧内容 | 热门帖子 + 热词（首页）/ 相关推荐（详情页） | 参考知乎；移动端收起 |
| 移动端适配 | 左导航收起为底部导航，右侧栏隐藏 | 参考知乎移动端 |

### 被拒方案

| 方案 | 拒绝理由 |
|---|---|
| 保持两栏，只调比例 | 详情嵌在右侧本质问题未解决；移动端体验割裂 |
| 弹窗展示详情 | 弹窗遮挡内容流，用户无法同时比较多个帖子 |

### 主流程概述

**正常路径**：
1. 用户进入首页 → 看到三栏：左导航 + 中内容流 + 右热门
2. 点击帖子 → 跳转详情页（URL 变化），内容全屏展示
3. 点击返回或导航 → 回到内容流

**异常/边界**：
- 未登录用户：仍显示登录页，布局优化不影响认证逻辑
- 移动端：左导航收起为汉堡菜单或底部导航，右侧栏隐藏
- 直接访问详情页 URL：正常渲染，退出后回首页

## 2. 接口契约

### 2.1 组件拆分

```
App.tsx（重构）
├── AppLayout（新增）- 三栏布局容器
│   ├── LeftNav（新增）- 左侧垂直导航
│   ├── MainContent（新增）- 中间内容区
│   └── RightAside（新增）- 右侧辅助区
├── pages/（新增目录）
│   ├── HomePage（从 App 提取）- 首页内容流
│   ├── PostDetailPage（新增）- 帖子详情页
│   ├── QuizPage（从 App 提取）- 刷题页
│   ├── ComposePage（从 App 提取）- 发帖页
│   ├── SearchPage（从 App 提取）- 搜索页
│   └── AccountPage（从 App 提取）- 账号页
├── AuthPage（从 App 提取）- 登录注册页
└── shared/（新增目录）
    └── ListCard（提取为独立组件）
```

**拆分理由**：
- 当前 App.tsx 500+ 行，所有页面堆在一起 → 按页面拆分到独立文件
- 布局逻辑和业务逻辑耦合 → 抽取 AppLayout 处理三栏布局
- 详情和列表合并展示 → 分离为独立页面

### 2.2 AppLayout 组件契约

```tsx
// 来源：clx-web/src/components/layout/AppLayout.tsx（新增）

// Props
<AppLayout 
  showLeftNav={boolean}      // 登录后显示左侧导航
  showRightAside={boolean}   // 首页/详情页显示右侧栏
  rightContent?: ReactNode   // 右侧内容（热门帖子、热词等）
>
  {children}                 // 中间内容区
</AppLayout>

// 渲染示例
<AppLayout showLeftNav={true} showRightAside={true}>
  <HomePage />
</AppLayout>

// 响应式行为
// - 桌面（>960px）：三栏，左导航固定宽度 180px，中间 flex-1，右侧 300px
// - 平板（640-960px）：两栏，左侧收起为图标，右侧隐藏
// - 移动（<640px）：单栏，左侧收起为底部导航，右侧隐藏
```

### 2.3 LeftNav 组件契约

```tsx
// 来源：clx-web/src/components/layout/LeftNav.tsx（新增）

// Props
<LeftNav 
  currentTab={Tab}          // 当前激活的 tab
  onTabChange={(tab) => void}  // 切换 tab 回调
/>

// 渲染示例
<LeftNav 
  currentTab="home" 
  onTabChange={(tab) => setTab(tab)} 
/>

// 导航项
// - 首页（Home 图标）
// - 搜索（Search 图标）
// - 刷题（BookOpen 图标）
// - 发帖（Edit 图标）
// - 账号（User 图标）
// - 退出（LogOut 图标，底部）
```

### 2.4 路由契约

```tsx
// 来源：clx-web/src/App.tsx（重构后）

// 路由结构
// /auth         → AuthPage（登录注册）
// /             → HomePage（首页，内容流）
// /post/:id     → PostDetailPage（帖子详情）
// /quiz         → QuizPage（刷题）
// /compose      → ComposePage（发帖）
// /search       → SearchPage（搜索）
// /account      → AccountPage（账号）

// 状态归属
// - isLoggedIn: App 级（决定是否显示布局）
// - user: App 级（通过 props/context 传递）
// - posts: HomePage 级（列表数据）
// - selectedPost: URL 参数（PostDetailPage 从 URL 读取）
```

## 3. 实现提示

### 目标文件状况评估

当前 App.tsx：
- 行数：501 行
- 职责：路由、状态管理、所有页面渲染、API 调用
- 问题：单文件承载过多职责

**拆分方案**：
1. 抽取 `AppLayout` 组件处理三栏布局
2. 抽取 `LeftNav` 组件处理导航
3. 按页面拆分：HomePage / PostDetailPage / QuizPage / ComposePage / SearchPage / AccountPage
4. AuthPage 保持独立（未登录时全屏展示）

### 改动计划

| 文件 | 操作 | 说明 |
|---|---|---|
| `src/App.tsx` | 重构 | 只保留路由和全局状态，其他抽取 |
| `src/components/layout/AppLayout.tsx` | 新建 | 三栏布局容器 |
| `src/components/layout/LeftNav.tsx` | 新建 | 左侧垂直导航 |
| `src/components/layout/RightAside.tsx` | 新建 | 右侧辅助区（热门/热词） |
| `src/pages/HomePage.tsx` | 新建 | 首页内容流（帖子列表） |
| `src/pages/PostDetailPage.tsx` | 新建 | 帖子详情页（全屏） |
| `src/pages/QuizPage.tsx` | 新建 | 刷题页（调整为两栏） |
| `src/pages/ComposePage.tsx` | 新建 | 发帖页 |
| `src/pages/SearchPage.tsx` | 新建 | 搜索页 |
| `src/pages/AccountPage.tsx` | 新建 | 账号页 |
| `src/pages/AuthPage.tsx` | 新建 | 登录注册页 |
| `src/shared/ListCard.tsx` | 新建 | 提取为独立组件 |
| `src/styles/app.css` | 修改 | 添加三栏布局样式 |

### 实现风险与约束

1. **路由切换时状态保持**：当前用 tab state 切换，改用 URL 路由后需要确保状态不丢失
   - 解决：用 React Router，全局状态提升到 App 层

2. **移动端适配**：三栏布局在移动端需要动态收起
   - 解决：CSS 媒体查询 + 条件渲染

3. **详情页跳转后返回**：用户可能从详情页返回首页期望保持滚动位置
   - 解决：用 React Router 的 `keepalive` 或手动记录滚动位置

### 推进顺序

**Step 1：搭建布局骨架**
- 新建 `AppLayout` 组件，实现三栏容器
- 新建 `LeftNav` 组件，实现垂直导航
- 修改 App.tsx，用 AppLayout 包裹内容
- 退出信号：三栏结构可见，左导航可点击切换

**Step 2：抽取首页**
- 新建 `HomePage.tsx`，从 App.tsx 提取首页逻辑
- 新建 `RightAside.tsx`，提取热门帖子/热词
- 首页只展示内容流 + 右侧栏
- 退出信号：首页渲染正确，功能不变

**Step 3：新建详情页**
- 新建 `PostDetailPage.tsx`
- 点击帖子卡片跳转到 `/post/:id`
- 详情页全屏展示内容、评论、点赞
- 退出信号：详情页可访问，URL 变化，内容全屏

**Step 4：抽取其他页面**
- 依次抽取 QuizPage / ComposePage / SearchPage / AccountPage
- 每个页面独立渲染，不再通过 tab 切换
- 退出信号：各页面功能不变

**Step 5：抽取认证页**
- 新建 `AuthPage.tsx`
- 未登录时全屏展示，不显示布局
- 退出信号：登录流程不变

**Step 6：响应式适配**
- 修改 CSS 媒体查询
- 移动端左导航收起为底部导航或汉堡菜单
- 移动端右侧栏隐藏
- 退出信号：手机端布局正确

**Step 7：引入路由**
- 添加 `react-router-dom`
- 实现 `/` `/post/:id` `/quiz` 等路由
- 退出信号：浏览器前进/后退可用，刷新保持状态

**Step 8：收尾测试**
- 测试所有页面跳转
- 测试移动端适配
- 测试登录态切换
- 退出信号：功能全部通过

### 测试设计

| 功能点 | 测试约束 | 验证方式 |
|---|---|---|
| 三栏布局 | 桌面端三栏可见，宽度比例合理 | 手动测试（Chrome DevTools 响应式模式） |
| 详情页分离 | 点击帖子跳转新 URL，全屏展示 | 手动测试：点击帖子 → 检查 URL 和布局 |
| 移动端适配 | <640px 单栏，导航收起 | 手动测试：375px 宽度模拟 |
| 左导航切换 | 点击导航项切换页面 | 手动测试：依次点击各导航项 |
| 登录态切换 | 未登录显示 AuthPage | 手动测试：退出登录 → 检查页面 |
| 首页功能 | 帖子列表、筛选、分页 | 手动测试：切换分类/标签，刷新列表 |
| 详情页功能 | 评论、点赞、作者信息 | 手动测试：发表评论、点赞 |

## 4. 与项目级架构文档的关系

### 关联架构文档

- 无现有前端架构文档
- 本次改动不涉及后端，不影响其他架构文档

### 架构索引更新

无需更新 `CLAUDE.md`，前端布局属于实现细节。

### 后续建议

本次重构后，建议补充：
- `clx-web/README.md` 前端目录结构说明
- 前端组件库文档（组件 Props 和 Events 说明）
