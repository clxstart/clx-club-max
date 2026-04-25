---
doc_type: feature-design
feature: 2026-04-22-community-homepage
status: completed
summary: 开发完整社区首页，新建 clx-post 服务，使用 ES 搜索，个性化推荐
tags: [首页, 帖子, 微服务, Elasticsearch, 推荐]
---

# Community Homepage Design

> Stage 1 | 2026-04-22 | 待用户 review

---

## 0. 术语约定

| 术语 | 定义 | 代码命名 |
|------|------|----------|
| 帖子 | 用户发布的内容单元，包含标题、正文、分类、标签 | `Post` |
| 评论 | 对帖子的回复 | `Comment` |
| 分类 | 帖子的归属类别（如技术、生活） | `Category` |
| 标签 | 帖子的关键词标记 | `Tag` |
| 点赞 | 用户对帖子/评论的正面互动 | `Like` |
| 内容流 | 首页展示的帖子列表 | `PostFeed` |
| 推荐 | 基于用户行为的帖子推荐 | `Recommendation` |

---

## 1. 决策与约束

### 需求摘要

**做什么**：开发完整社区首页，用户可以浏览帖子、发布内容、互动（点赞/评论）、通过分类标签筛选、关键词搜索、看到个性化推荐。

**为谁做**：已登录用户，作为学习/演示项目，代码质量要达到商业级。

**怎么算成功**：
- 用户能完整走通"浏览 → 发布 → 互动 → 搜索/筛选"闭环
- 新建独立的 `clx-post` 微服务
- 有单元测试覆盖核心逻辑
- 前后端接口有清晰契约

### 明确不做

| 不做 | 验证方式 |
|------|----------|
| 用户个人主页 | grep 无 `UserProfile` 相关代码 |
| 关注/粉丝 | 无 `Follow` 表和相关接口 |
| 私信 | 无 `Message` 服务调用 |
| 通知中心 | 无 `Notification` 接口 |
| 管理后台 | 无 `/admin/post` 接口 |
| 帖子审核 | 无 `Audit` 状态字段 |
| 富文本编辑器 | 只支持纯文本/Markdown |

### 关键决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 服务归属 | 新建 `clx-post` | 清晰微服务边界，便于后续扩展 |
| 搜索方案 | Elasticsearch | 商业级搜索能力，支持个性化推荐基础 |
| 推荐策略 | 个性化推荐 | 用户要求高质量，但初期可用热度兜底 |
| 缓存 | Redis 热门帖子 | 减轻 DB/ES 压力 |
| 测试 | 单测核心逻辑 + 集成测试 API | 商业级代码质量要求 |

### 前置依赖

- Elasticsearch 部署（Docker）
- `clx-post` 服务基础框架搭建

---

## 2. 接口契约

### 帖子列表 API

**接口**: `GET /post/list`

**请求示例**:
```json
{
  "page": 1,
  "size": 20,
  "sort": "latest",  // latest | hot | recommend
  "categoryId": null,
  "tagId": null
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "posts": [
      {
        "id": 1001,
        "title": "Spring Boot 最佳实践",
        "summary": "总结10个常用技巧...",
        "author": {"id": 1, "name": "admin"},
        "category": {"id": 1, "name": "技术"},
        "tags": [{"id": 1, "name": "Java"}],
        "likeCount": 128,
        "commentCount": 45,
        "createdAt": "2026-04-22T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

### 发布帖子 API

**接口**: `POST /post/create`

**请求示例**:
```json
{
  "title": "我的第一篇帖子",
  "content": "今天学习了...",
  "categoryId": 1,
  "tagIds": [1, 2]
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1002,
    "title": "我的第一篇帖子"
  }
}
```

### 帖子详情 API

**接口**: `GET /post/{id}`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1001,
    "title": "Spring Boot 最佳实践",
    "content": "完整正文内容...",
    "author": {"id": 1, "name": "admin", "avatar": "url"},
    "category": {"id": 1, "name": "技术"},
    "tags": [{"id": 1, "name": "Java"}],
    "likeCount": 128,
    "commentCount": 45,
    "isLiked": true,
    "createdAt": "2026-04-22T10:00:00"
  }
}
```

### 点赞 API

**接口**: `POST /post/{id}/like`

**响应示例**:
```json
{
  "code": 200,
  "data": {"likeCount": 129}
}
```

### 评论列表 API

**接口**: `GET /post/{postId}/comments`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "comments": [
      {
        "id": 1,
        "content": "写得很好！",
        "author": {"id": 2, "name": "test"},
        "createdAt": "2026-04-22T11:00:00"
      }
    ]
  }
}
```

### 发布评论 API

**接口**: `POST /post/{postId}/comment`

**请求示例**:
```json
{
  "content": "感谢分享！"
}
```

### 搜索 API

**接口**: `GET /post/search`

**请求示例**:
```
keyword=Spring&page=1&size=20
```

### 分类列表 API

**接口**: `GET /category/list`

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {"id": 1, "name": "技术", "postCount": 50},
    {"id": 2, "name": "生活", "postCount": 30}
  ]
}
```

### 标签列表 API

**接口**: `GET /tag/list`

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {"id": 1, "name": "Java", "postCount": 25},
    {"id": 2, "name": "Python", "postCount": 20}
  ]
}
```

---

## 3. 实现提示

### 改动计划

**新建文件**:
- `clx-post/` 服务目录（完整微服务）
- `Post.java`, `Comment.java`, `Category.java`, `Tag.java`, `Like.java`
- `PostMapper.java`, `PostMapper.xml`
- `PostService.java`, `PostServiceImpl.java`
- `PostController.java`
- 前端: `PostListPage.tsx`, `PostDetailPage.tsx`, `PostCreatePage.tsx`

**修改文件**:
- `pom.xml` 添加 clx-post 模块
- `clx-gateway` 路由配置（如有）
- 前端路由添加帖子相关页面

### 推进顺序

| 步骤 | 内容 | 退出信号 |
|------|------|----------|
| 1 | 数据库：创建帖子相关表 | SQL 执行成功，表结构正确 |
| 2 | 新建 clx-post 服务框架 | 服务能启动，健康检查通过 |
| 3 | 实现帖子 CRUD（不含搜索） | 能发布、查看、删除帖子 |
| 4 | 实现评论功能 | 能评论帖子 |
| 5 | 实现点赞功能 | 能点赞/取消点赞 |
| 6 | 实现分类/标签 | 能筛选帖子 |
| 7 | 集成 ES，实现搜索 | 能关键词搜索 |
| 8 | 实现推荐（热度兜底） | 首页有推荐区域 |
| 9 | 前端首页页面 | 浏览器能看到完整首页 |
| 10 | 单元测试 | 核心逻辑有测试覆盖 |

### 测试设计

| 功能点 | 测试约束 | 验证方式 | 用例骨架 |
|--------|----------|----------|----------|
| 发布帖子 | 必填字段校验、分类存在 | 单测参数校验 | 空标题 → 400 |
| 帖子列表 | 分页正确、排序生效 | 集成测试 | 第2页数据正确 |
| 点赞 | 点赞数+1、重复点赞不叠加 | 单测 | 点赞2次 → count+1 |
| 评论 | 评论关联帖子正确 | 集成测试 | 评论出现在帖子详情 |
| 搜索 | ES 返回匹配结果 | 集成测试 | "Java" 返回 Java 标签帖子 |

---

## 4. 与项目级架构文档的关系

### 彶响响到的架构文档

- `CLAUDE.md`: 更新模块结构，添加 clx-post 服务说明
- `doc/sql/schema.sql`: 新增帖子相关表结构

### 服务新增

`clx-post` 服务职责：
- 帖子 CRUD
- 评论管理
- 点赞管理
- 分类/标签管理
- 搜索服务
- 推荐服务

端口: 9300