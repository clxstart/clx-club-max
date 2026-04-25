---
doc_type: feature-brainstorm
feature: 2026-04-24-frontend-integration
status: confirmed
summary: 对接已有后端 API，实现帖子交互（发帖/编辑/删除/点赞）、评论功能、搜索功能
tags: [frontend, api-integration, post, comment, search, neumorphism]
---

# Frontend Integration Brainstorm

> Stage 0 | 2026-04-24 | 下一步：design

## 想做什么、为什么

用户希望前端"彻底对接后端"，经过探索发现：

- **出发点**：当前前端只实现了登录页和帖子列表/详情页的展示功能，大量后端 API 未对接
- **关键发现**：后端 clx-post 和 clx-search 已完整实现，但前端交互缺失
- **转折**：从"全部对接包括未实现的后端"调整为"只对接已有后端"，范围收缩到可控

## 考虑过的方向

### 方向 A：对接已有后端（选定）
- 只对接 clx-post（帖子交互）+ clx-search（搜索）
- 不补未实现的后端（用户服务、消息、通知、管理后台）
- 价值：工作量可控，核心社区体验完整
- 代价：部分 API 端点定义保留但无实际页面（可接受）
- 结论：**选定**

### 方向 B：只做帖子交互
- 只做发帖/编辑/删除/点赞，搜索和评论留后续
- 价值：最小可行增量
- 代价：用户仍无法搜索和评论，体验不完整
- 结论：否决（用户要求全部做完）

### 方向 C：全部对接包括未实现后端
- 补全 clx-user、消息、通知、管理后台等所有模块
- 价值：功能最完整
- 代价：大工程，后端未实现，风险高
- 结论：否决（范围过大）

## 选定方向与遗留问题

**选定方向**：对接已有后端（clx-post + clx-search），实现帖子交互、评论功能、搜索功能。

**粗粒度轮廓**：
- 核心行为：发帖、编辑/删除自己的帖子、点赞/取消点赞、在帖子下评论、搜索帖子
- 明确不做：用户中心/个人主页、消息通知、管理后台、OAuth/手机登录、深色模式
- 最大未知：后端 DTO/VO 字段结构需确认

**遗留给 design 的问题**：
1. 后端 PostCreateRequest/PostUpdateRequest/CommentCreateRequest 字段结构
2. CommentVO 字段结构（评论列表渲染需要）
3. SearchVO 字段结构（聚合搜索结果渲染需要）
4. 前端路由设计（发帖页面、搜索页面）
5. 状态管理（帖子操作后如何刷新列表）

## 后端 API 清单

### 帖子（待对接）
- POST /post/create — 发帖
- PUT /post/{id} — 编辑
- DELETE /post/{id} — 删除
- POST /post/{id}/like — 点赞
- DELETE /post/{id}/like — 取消点赞
- GET /post/search — 搜索帖子（PostController 内置）

### 评论（待对接）
- GET /post/{postId}/comments — 评论列表
- POST /post/{postId}/comment — 发评论
- DELETE /post/{postId}/comment/{commentId} — 删除评论
- POST /comment/{id}/like — 点赞评论

### 搜索（待对接）
- POST /search/aggregate — 聚合搜索
- GET /search/hot — 热词统计