---
doc_type: feature-acceptance
feature: 2026-04-26-paicoding-layout
status: completed
summary: 首页布局完全模仿技术派，顶部标签栏 + 紧凑帖子列表 + 右侧热门与排行榜
---

# paicoding-layout Acceptance

> Stage 3 | 2026-04-26 | 验收完成

---

## 改动文件清单

| 文件 | 操作 | 说明 |
|---|---|---|
| `src/components/layout/TopNavBar.tsx` | 新建 | 顶部水平标签导航栏 |
| `src/components/post/PostListItem.tsx` | 新建 | 紧凑帖子项组件 |
| `src/components/aside/HotPosts.tsx` | 新建 | 热门文章组件 |
| `src/components/aside/ActiveRank.tsx` | 新建 | 月度活跃排行榜组件 |
| `src/App.tsx` | 重构 | 用顶部导航替代左侧导航 |
| `src/pages/HomePage.tsx` | 重构 | 使用紧凑列表，接收分类筛选 |
| `src/api/types.ts` | 修改 | 新增 ActiveUserVO 类型 |
| `src/styles/app.css` | 修改 | 新增顶部导航、紧凑列表、右侧栏样式 |

---

## 设计对照验收

| 设计要求 | 实现状态 | 验证方式 |
|---|---|---|
| 顶部标签栏替代左侧导航 | ✅ 完成 | TopNavBar 组件 + App.tsx 重构 |
| 帖子卡片改为紧凑列表式 | ✅ 完成 | PostListItem 组件（标签+作者+时间+标题一行） |
| 右侧栏增加月度排行榜 | ✅ 完成 | ActiveRank 组件（mock 数据） |
| 移动端适配 | ✅ 完成 | CSS 媒体查询：标签栏可滑动，右侧栏隐藏 |

---

## 验证结果

**构建验证**：
```
✓ 1637 modules transformed
✓ built in 1.17s
```

**功能验证**（待用户手动测试）：
- [ ] 顶部标签栏点击分类后列表更新
- [ ] 帖子紧凑显示（一行标题）
- [ ] 右侧栏显示热门文章 + 排行榜
- [ ] 移动端（<960px）右侧栏隐藏

---

## 需后端配合

| 接口 | 状态 | 说明 |
|---|---|---|
| `GET /user/active-rank` | 待实现 | 当前使用 mock 数据 |

---

## 遗留项

1. **LeftNav.tsx 未删除**：保留文件，后续确认无问题后可删除
2. **排行榜 API**：需后端实现真实数据接口
3. **帖子点击跳转详情**：保持原有逻辑，需测试

---

## Commit Message

```
feat: refactor homepage layout to paicoding style

- Replace left sidebar navigation with top horizontal tag bar
- Compact post list: tag + author + time + title in one line
- Add HotPosts and ActiveRank components for right sidebar
- Mobile responsive: scrollable tag bar, hidden sidebar
- Use mock data for active rank (backend API pending)
```