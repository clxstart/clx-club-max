---
doc_type: feature-design
feature: 2026-04-26-user-profile
status: approved
summary: 用户模块完整搭建，包含个人资料、个人主页、关注/粉丝、收藏功能
tags: [user, profile, follow, favorite, social, frontend]
---

# 用户模块方案设计

> Stage 1 | 2026-04-26 | 待 review

## 0. 术语约定

| 术语 | 定义 | 防冲突结论 |
|------|------|------------|
| **用户资料** | 用户的头像、昵称、签名、性别等可编辑信息 | 新增概念，grep 无 `UserProfile` 冲突 |
| **个人主页** | 展示用户信息和发布内容的页面 | 新增概念 |
| **关注关系** | 用户与用户之间的单向关注关系 | Notification 已预留 `follow` 类型，无冲突 |
| **收藏夹** | 用户收藏的帖子列表，单级无分类 | 新增概念，grep 无 `Favorite` 冲突 |

## 1. 决策与约束

### 需求摘要

- **做什么**：补齐用户模块核心功能——个人资料查看/编辑、个人主页、关注/粉丝、收藏
- **为谁**：社区用户，想了解自己/他人的信息，想收藏好内容、关注好作者
- **成功标准**：
  1. 用户可查看/编辑自己的资料（头像、昵称、签名）
  2. 用户可访问自己/他人的个人主页，看到发帖历史
  3. 用户可关注/取关他人，关注/粉丝/获赞三数展示
  4. 用户可收藏/取消收藏帖子，查看收藏夹
- **明确不做**：
  - 私信功能（message 服务已有，前端对接另开 feature）
  - 黑名单/屏蔽用户
  - 用户等级/勋章系统
  - 推荐算法（关注推荐、内容推荐）

### 关键决策

| 决策 | 选择 | 理由 |
|------|------|------|
| 个人主页 URL | `/user/{userId}` | 语义清晰，传统社区风格 |
| 关注数更新策略 | 同步更新 | 简单可靠，高并发风险可接受（社区初期） |
| 获赞数来源 | 独立字段 `like_total_count` | 删帖不丢获赞数，数据更准确 |
| 收藏表归属 | clx-user | 用户视角内聚，个人主页展示收藏列表 |
| clx-user 职责 | 用户资料 + 关注关系 + 收藏 | 用户视角的功能聚合 |
| 前端布局 | 三栏延续（和首页统一） | 保持 Neumorphism 风格一致性 |

### 被拒方案

| 方案 | 拒绝理由 |
|------|----------|
| URL 用 `/u/{username}` | username 可能变更，userId 更稳定 |
| 关注数异步更新 | 初期实现复杂，验证困难 |
| 获赞数累加帖子 like_count | 删帖子会导致获赞数丢失 |
| 收藏归 clx-post | 增加跨服务依赖，个人主页查询不便 |

### 功能归属确认

| 功能 | 服务 | 理由 |
|------|------|------|
| 用户资料 CRUD | clx-user | 用户核心数据，独立服务管理 |
| 关注关系 CRUD | clx-user | 用户间关系，自然归属用户服务 |
| 收藏 CRUD | clx-user | 用户行为，个人主页展示 |
| 获赞数统计 | clx-user | 用户维度聚合，定时任务累加 |
| 发帖历史查询 | clx-post | 帖子数据在 post 服务，提供按作者查询 API |

### 前置依赖

- **无结构性问题**：clx-user 是空壳，直接搭建；sys_user 表字段完整
- **clx-auth 的 User 实体需扩展**：当前缺少 avatar、signature 等字段，但本 feature 在 clx-user 新建完整实体，不改 auth

## 2. 关键接口契约

### 2.1 用户资料 API（clx-user 服务，端口 9200）

#### 获取用户资料

```
GET /user/{userId}
→ 200: UserProfileVO
→ 404: 用户不存在

示例：
GET /user/123
→ {
    "userId": 123,
    "username": "admin",
    "nickname": "管理员",
    "avatar": "https://xxx.com/avatar.jpg",
    "signature": "代码改变世界",
    "gender": 1,
    "followCount": 10,
    "fansCount": 100,
    "likeTotalCount": 500,
    "isFollowed": false  // 当前用户是否已关注该用户
  }
```

#### 更新当前用户资料

```
PUT /user/profile
Body: { nickname?, avatar?, signature?, gender? }
→ 200: void
→ 400: 昵称已存在

示例：
PUT /user/profile
→ { "nickname": "新昵称", "signature": "新签名" }
→ 200
```

#### 获取当前用户资料（快捷入口）

```
GET /user/me
→ 200: UserProfileVO
→ 401: 未登录
```

### 2.2 关注关系 API（clx-user 服务）

#### 关注用户

```
POST /user/follow/{userId}
→ 200: { followCount: 新关注数 }
→ 400: 不能关注自己 / 已关注
```

#### 取消关注

```
DELETE /user/follow/{userId}
→ 200: { followCount: 新关注数 }
→ 400: 未关注
```

#### 获取关注列表

```
GET /user/{userId}/following?page=1&size=20
→ 200: { total, list: [UserSimpleVO] }

UserSimpleVO: { userId, nickname, avatar, signature }
```

#### 获取粉丝列表

```
GET /user/{userId}/fans?page=1&size=20
→ 200: { total, list: [UserSimpleVO] }
```

### 2.3 收藏 API（clx-user 服务）

#### 收藏帖子

```
POST /user/favorite/{postId}
→ 200: void
→ 400: 已收藏
```

#### 取消收藏

```
DELETE /user/favorite/{postId}
→ 200: void
→ 400: 未收藏
```

#### 获取收藏夹

```
GET /user/favorites?page=1&size=20
→ 200: { total, list: [FavoriteItemVO] }

FavoriteItemVO: {
  postId, title, summary, authorName,
  likeCount, createdAt, favoritedAt
}
```

### 2.4 发帖历史 API（clx-post 服务，端口 9300）

#### 获取用户发布的帖子

```
GET /post/user/{userId}?page=1&size=20
→ 200: PostListVO（复用现有结构）
```

> 来源：PostController 新增接口，复用 PostListVO

### 2.5 获赞数同步（clx-user 服务）

#### 点赞事件接收（内部调用，不走 HTTP）

```
// clx-post 点赞成功后，调用 clx-user 的内部接口
POST /internal/user/like/incr
Body: { userId: 被点赞者ID, delta: 1 }

POST /internal/user/like/decr
Body: { userId: 被点赞者ID, delta: -1 }
```

> **假设**：使用 Feign 调用，或消息队列解耦。本方案采用简单 HTTP 内部调用。

## 3. 实现提示

### 3.1 改动计划

#### 新建文件（clx-user 服务）

```
clx-user/src/main/java/com/clx/user/
├── controller/
│   ├── UserController.java        # 用户资料 API
│   ├── FollowController.java      # 关注关系 API
│   └── FavoriteController.java    # 收藏 API
├── service/
│   ├── UserService.java
│   ├── FollowService.java
│   ├── FavoriteService.java
│   └── impl/
│       ├── UserServiceImpl.java
│       ├── FollowServiceImpl.java
│       └── FavoriteServiceImpl.java
├── entity/
│   ├── User.java                  # 用户实体（完整字段）
│   ├── UserFollow.java            # 关注关系实体
│   └── PostFavorite.java          # 收藏实体
├── mapper/
│   ├── UserMapper.java
│   ├── UserFollowMapper.java
│   └── PostFavoriteMapper.java
├── vo/
│   ├── UserProfileVO.java         # 用户资料视图
│   └── UserSimpleVO.java          # 用户简要视图
└── dto/
    └── LikeEventDTO.java          # 点赞事件 DTO
```

#### 新建文件（前端 clx-web）

```
clx-web/src/
├── features/user/
│   ├── UserProfilePage.tsx        # 个人主页页面
│   ├── UserEditModal.tsx         # 编辑资料弹窗
│   ├── FollowListModal.tsx       # 关注/粉丝列表弹窗
│   ├── FavoriteList.tsx          # 收藏夹组件
│   └── userApi.ts                # 用户相关 API
├── routes/
│   └── index.tsx                 # 新增 /user/:userId 路由
└── api/
    └── types.ts                  # 新增 UserProfileVO、FollowVO 等类型
```

#### 追加到已有文件

| 文件 | 追加内容 | 理由 |
|------|----------|------|
| `clx-post/PostController.java` | 新增 `GET /post/user/{userId}` | 按作者查询帖子 |
| `clx-post/LikeConsumer.java` | 点赞成功后调用 clx-user 增加获赞数 | 获赞数同步 |
| `clx-web/src/api/index.ts` | 新增 userApi | 用户相关请求 |
| `clx-web/src/App.tsx` | 新增 User Tab 和路由 | 导航入口 |

### 3.2 推进顺序

| 步骤 | 动作 | 退出信号 |
|------|------|----------|
| 1 | 搭建 clx-user 服务骨架：依赖、配置、启动类 | 服务可启动，健康检查通过 |
| 2 | 实现 User 实体 + Mapper + 资料查询/更新 | `GET/PUT /user/{userId}` 可用 |
| 3 | 实现 UserFollow 实体 + Mapper + 关注/取关 | `POST/DELETE /user/follow/{id}` 可用 |
| 4 | 实现 PostFavorite 实体 + Mapper + 收藏 | `POST/DELETE /user/favorite/{id}` 可用 |
| 5 | clx-post 新增按作者查询接口 | `GET /post/user/{userId}` 可用 |
| 6 | clx-post 点赞成功后同步获赞数到 clx-user | 点赞后用户获赞数 +1 |
| 7 | 前端：个人主页页面 + 路由 | 访问 `/user/123` 显示主页 |
| 8 | 前端：关注/粉丝列表、收藏夹、编辑资料弹窗 | 所有交互可用 |

### 3.3 测试设计

#### 功能点：用户资料查看/编辑

| 测试约束 | 验证方式 | 关键用例 |
|----------|----------|----------|
| 可查看任意用户资料 | API 测试 | 访问存在/不存在的 userId |
| 仅可编辑自己的资料 | API 测试 | 用 A 的 token 编辑 B，返回 403 |
| 昵称可重复（或不可） | 单元测试 | 相同昵称注册，验证唯一性 |

#### 功能点：关注关系

| 测试约束 | 验证方式 | 关键用例 |
|----------|----------|----------|
| 不能关注自己 | API 测试 | 关注自己返回 400 |
| 重复关注幂等 | API 测试 | 关注两次，第二次返回 400 |
| 取关后关注数 -1 | API 测试 | 关注 → 取关，验证数字变化 |
| 关注列表分页正确 | API 测试 | 关注 25 人，分页查第 2 页 |

#### 功能点：收藏

| 测试约束 | 验证方式 | 关键用例 |
|----------|----------|----------|
| 收藏状态可在帖子详情显示 | 集成测试 | 收藏后帖子详情 isFavorited=true |
| 取消收藏幂等 | API 测试 | 取关两次，第二次返回 400 |
| 收藏夹按时间倒序 | API 测试 | 收藏多个帖子，验证顺序 |

#### 功能点：获赞数同步

| 测试约束 | 验证方式 | 关键用例 |
|----------|----------|----------|
| 点赞帖子后作者获赞数 +1 | 集成测试 | A 点赞 B 的帖子，B 的 likeTotalCount +1 |
| 取消点赞后作者获赞数 -1 | 集成测试 | 取消点赞，数字减少 |
| 删除帖子不影响获赞数 | 集成测试 | 删帖子，作者获赞数不变 |

### 3.4 高风险实现约束

| 风险 | 说明 | 应对 |
|------|------|------|
| 关注数同步更新的并发问题 | 高并发时可能超卖（关注数不准） | 初期接受，后续可改异步 |
| 获赞数同步的点对点调用 | clx-post 和 clx-user 紧耦合 | 用 RestTemplate 调用，后续可改 MQ |
| User 实体与 clx-auth 重复 | 两边都有 User 实体 | clx-user 用完整字段，auth 保持精简 |

## 4. 与项目级架构文档的关系

### 服务归属确认

- 本 feature 落地在 **clx-user 服务**
- clx-user 端口：**9200**
- 职责：用户资料、关注关系、收藏夹

### 跨服务依赖

```
clx-auth (9100)  →  用户登录验证，token 解析
clx-post (9300)  →  按作者查询帖子、点赞事件通知
clx-user (9200)  ←  本 feature 核心
```

### 架构文档更新提示

完成本 feature 后，需更新 `CLAUDE.md`：
- 将"阶段 4：实现 clx-user 服务"标记为 ✅
- 更新 clx-user 模块结构描述

## 5. 数据库变更

### 新增表

#### user_follow（关注关系表）

```sql
CREATE TABLE IF NOT EXISTS `user_follow` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '关注者ID',
    `target_id` bigint NOT NULL COMMENT '被关注者ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
    KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';
```

#### post_favorite（收藏表）

```sql
CREATE TABLE IF NOT EXISTS `post_favorite` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `post_id` bigint NOT NULL COMMENT '帖子ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
    KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子收藏表';
```

### 修改表

#### sys_user 新增字段

```sql
ALTER TABLE `sys_user` ADD COLUMN `like_total_count` int DEFAULT '0' COMMENT '获赞总数' AFTER `login_count`;
```

> `avatar`、`signature`、`gender`、`birthday` 字段已存在，无需修改。
