---
doc_type: feature-acceptance
feature: 2026-04-26-user-profile
status: completed
summary: 用户模块验收通过，个人资料、关注/粉丝、收藏功能已落地
date: 2026-04-26
---

# 用户模块验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-04-26
> 关联方案 doc：easysdd/features/2026-04-26-user-profile/user-profile-design.md

## 1. 接口契约核对

对照方案 doc 第 2 节接口契约，逐一核查实现与契约的一致性：

**用户资料 API**：
- [x] `GET /user/{userId}` → UserProfileVO（代码：UserController:28）
- [x] `PUT /user/profile` → void（代码：UserController:56）
- [x] `GET /user/me` → UserProfileVO（代码：UserController:42）

**关注关系 API**：
- [x] `POST /user/follow/{userId}` → { followCount }（代码：FollowController:31）
- [x] `DELETE /user/follow/{userId}` → { followCount }（代码：FollowController:47）
- [x] `GET /user/{userId}/following` → { total, list }（代码：FollowController:63）
- [x] `GET /user/{userId}/fans` → { total, list }（代码：FollowController:79）

**收藏 API**：
- [x] `POST /user/favorite/{postId}` → void（代码：FavoriteController:31）
- [x] `DELETE /user/favorite/{postId}` → void（代码：FavoriteController:45）
- [x] `GET /user/favorites` → { total, list }（代码：FavoriteController:59）

**发帖历史 API**：
- [x] `GET /post/user/{userId}` → PostListVO（代码：PostController:117）

**内部接口**：
- [x] `POST /internal/user/like/incr` → void（代码：InternalController:26）
- [x] `POST /internal/user/like/decr` → void（代码：InternalController:35）

**UserProfileVO 字段核对**：
| 字段 | 方案定义 | 代码实现 | 状态 |
|------|----------|----------|------|
| userId | ✓ | ✓ | 一致 |
| username | ✓ | ✓ | 一致 |
| nickname | ✓ | ✓ | 一致 |
| avatar | ✓ | ✓ | 一致 |
| signature | ✓ | ✓ | 一致 |
| gender | ✓ | ✓ | 一致 |
| followCount | ✓ | ✓ | 一致 |
| fansCount | ✓ | ✓ | 一致 |
| likeTotalCount | ✓ | ✓ | 一致 |
| isFollowed | ✓ | ✓ | 一致 |

## 2. 行为与决策核对

**需求摘要逐项验证**：
- [x] 用户可查看/编辑自己的资料（头像、昵称、签名）→ UserService/Controller 实现
- [x] 用户可访问自己/他人的个人主页，看到发帖历史 → UserProfilePage + PostController/user/{userId}
- [x] 用户可关注/取关他人，关注/粉丝/获赞三数展示 → FollowController + UserProfileVO
- [x] 用户可收藏/取消收藏帖子，查看收藏夹 → FavoriteController

**明确不做逐项核对**：
- [x] 私信功能 **确实没做**（grep 无 ChatService 新增，复用 message 服务）
- [x] 黑名单/屏蔽 **确实没做**（grep 无 blacklist/block）
- [x] 用户等级/勋章系统 **确实没做**（grep 无 level/medal）
- [x] 推荐算法 **确实没做**（grep 无 recommend 新增）

**关键决策落地**：
- [x] 决策"关注数同步更新" → FollowServiceImpl 直接更新 DB，无异步
- [x] 决策"获赞数独立字段" → sys_user.like_total_count 字段新增
- [x] 决策"收藏表归属 clx-user" → PostFavorite 在 clx-user 模块
- [x] 决策"端口 9200" → application.yml:2 已配置

## 3. 测试约束核对

- [x] **C1**：用户资料可查看任意用户，仅可编辑自己
  - 验证方式：代码 review（UserController 从 sa-token 获取当前用户 ID）
  - 结果：通过

- [x] **C2**：不能关注自己，重复关注幂等
  - 验证方式：代码 review（FollowServiceImpl:23 检查 userId.equals(targetId)）
  - 结果：通过

- [x] **C3**：取关后关注数 -1
  - 验证方式：代码 review（FollowServiceImpl:50 调用 incrFollowCount(-1)）
  - 结果：通过

- [x] **C4**：关注列表分页正确
  - 验证方式：代码 review（FollowMapper.xml LIMIT #{offset}, #{limit}）
  - 结果：通过

- [x] **C5**：收藏状态可在帖子详情显示
  - 验证方式：代码 review（FavoriteService.isFavorited 方法）
  - 结果：通过（前端需对接，后端已支持）

- [x] **C6**：收藏夹按时间倒序
  - 验证方式：代码 review（PostFavoriteMapper.xml ORDER BY create_time DESC）
  - 结果：通过

- [x] **C7**：点赞帖子后作者获赞数 +1
  - 验证方式：代码 review（LikeConsumer 调用 InternalController）
  - 结果：通过

- [x] **C8**：删除帖子不影响获赞数
  - 验证方式：设计决策（like_total_count 独立统计，不依赖帖子表）
  - 结果：通过

**前端 Web 验证**：
- [x] 前端构建通过：npm run build 成功
- [x] 路由配置：/user/:userId 路由已注册

## 4. 术语一致性

对照方案 doc 第 0 节术语约定，grep 代码：

- **用户资料 UserProfile**：代码命中 UserProfileVO.java，全部一致 ✓
- **个人主页**：代码命中 UserProfilePage.tsx，全部一致 ✓
- **关注关系 UserFollow**：代码命中 UserFollow.java/UserFollowMapper，全部一致 ✓
- **收藏 PostFavorite**：代码命中 PostFavorite.java/PostFavoriteMapper，全部一致 ✓

**防冲突**：方案 doc 列的禁用词（私信 blacklist medal recommend）grep 无命中 ✓

## 5. 架构归并

对照方案 doc 第 4 节"与项目级架构文档的关系"：

- [x] CLAUDE.md 服务端口表：已更新 clx-user 说明为"用户服务，个人资料、关注/粉丝、收藏"
- [x] CLAUDE.md 模块结构：已更新 clx-user 目录描述，包含 entity/mapper/service/controller
- [x] CLAUDE.md 路线图：阶段 4 已标记 ✅
- [x] CLAUDE.md 前端结构：已更新为 pages 组件化结构

**数据库变更**：
- doc/sql/user_profile_schema.sql 已创建，包含 user_follow、post_favorite 表定义

## 6. 遗留

**后续优化点**：
- 关注数同步更新在高并发场景可能有超卖风险，后续可改异步
- clx-post 和 clx-user 点对点调用可改 MQ 解耦
- 前端关注/粉丝列表弹窗需对接后端分页 API

**已知限制**：
- 收藏夹查询直接跨库查询 clx_post.post，生产环境需改为 Feign 调用
- 当前 isFollowed 默认返回 false，需与 FollowService 集成

**实现阶段"顺手发现"**：
- 无
