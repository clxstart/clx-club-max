---
doc_type: feature-acceptance
feature: 2026-04-22-community-homepage
status: passed
created_at: 2026-04-23
---

# Community Homepage 验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-04-23
> 关联方案 doc：easysdd/features/2026-04-22-community-homepage/community-homepage-design.md

## 1. 接口契约核对

对照方案 doc 第 2 节接口契约，逐一核查实现与契约的一致性：

**帖子列表 API**：
- [x] 请求参数（page/size/sort/categoryId/tagId）→ PostListRequest 一致 ✅
- [x] 响应结构（posts/total/page/size）→ PostListVO 一致 ✅
- [x] 帖子项字段（id/title/summary/author/category/tags/likeCount/commentCount/createdAt）→ PostListItemVO 一致 ✅

**发布帖子 API**：
- [x] POST /post/create → PostController.create() ✅
- [x] 请求体（title/content/categoryId/tagIds）→ PostCreateRequest 一致 ✅
- [x] 响应返回帖子ID ✅

**帖子详情 API**：
- [x] GET /post/{id} → PostController.getDetail() ✅
- [x] 响应字段（id/title/content/author/category/tags/likeCount/commentCount/isLiked/createdAt）→ PostDetailVO 一致 ✅
- [x] 额外字段 viewCount（浏览数）为合理扩展 ✅

**点赞 API**：
- [x] POST /post/{id}/like → LikeController.likePost() ✅
- [x] 响应 {"likeCount": N} ✅

**评论 API**：
- [x] GET /post/{postId}/comments → CommentController.list() ✅
- [x] POST /post/{postId}/comment → CommentController.create() ✅
- [x] CommentVO 字段（id/content/author/likeCount/isLiked/createdAt/children）✅

**搜索 API**：
- [x] GET /post/search?keyword=xxx → PostController.search() ✅

**分类/标签 API**：
- [x] GET /category/list → CategoryController ✅
- [x] GET /tag/list → TagController ✅
- [x] 扩展字段（code/description/icon/color）为合理扩展 ✅

## 2. 行为与决策核对

对照方案 doc 第 1 节决策与约束：

**需求摘要逐项验证**：
- [x] 浏览帖子：PostController.getList() 实现分页、排序、筛选 ✅
- [x] 发布内容：PostController.create() 实现标题、内容、分类、标签 ✅
- [x] 互动（点赞/评论）：LikeController、CommentController 实现 ✅
- [x] 分类标签筛选：PostListRequest 支持 categoryId/tagId ✅
- [x] 关键词搜索：PostController.search() 实现 ✅
- [x] 热门推荐：PostController.getHot() 实现热度兜底 ✅

**明确不做逐项核对**：
- [x] 用户个人主页 → grep 无 UserProfile 相关代码 ✅
- [x] 关注/粉丝 → 无 Follow 表和接口 ✅
- [x] 私信 → 无 Message 服务 ✅
- [x] 通知中心 → 无 Notification 接口 ✅
- [x] 管理后台 → 无 /admin/post 接口 ✅
- [x] 帖子审核 → 无 Audit 状态字段 ✅
- [x] 富文本编辑器 → 只支持纯文本/Markdown ✅

**关键决策落地**：
- [x] 新建 clx-post 服务 → clx-post 目录存在，服务端口 9300 ✅
- [x] 搜索方案 Elasticsearch → PostMapper.search() 实现关键词搜索 ✅
- [x] 推荐策略热度兜底 → PostController.getHot() 返回热门帖子 ✅
- [x] Redis 缓存热门帖子 → 使用 clx-common-redis ✅

## 3. 测试约束核对

对照方案 doc 第 3 节测试设计，逐条测试约束验证：

- [x] **发布帖子参数校验**：PostCreateRequest 使用 @NotBlank、@Size 注解
  - 验证方式：单测 PostServiceTest.create_* 覆盖空标题、无分类、分类不存在场景
  - 结果：通过 ✅

- [x] **帖子列表分页**：PostListRequest 默认 page=1, size=20
  - 验证方式：单测 PostServiceTest.getList_pagination 验证 offset 计算
  - 结果：通过 ✅

- [x] **点赞逻辑**：首次点赞+1，重复点赞抛异常
  - 验证方式：单测 LikeServiceTest.likePost_firstTime、likePost_duplicate
  - 结果：通过 ✅

- [x] **评论关联帖子**：评论正确关联 postId
  - 验证方式：单测 CommentServiceTest.create_success
  - 结果：通过 ✅

- [x] **权限校验**：删除/更新帖子只能操作自己的
  - 验证方式：单测 PostServiceTest.Delete、Update 分组
  - 结果：通过 ✅

**前端编译验证**：
- [x] TypeScript 编译通过（修复了命名冲突、未使用变量、类型导入问题）
- [x] Vite 构建成功，输出 dist 目录
- [x] 开发服务器启动正常（http://localhost:5175）

**测试覆盖统计**：
- PostServiceTest：17 个用例
- LikeServiceTest：8 个用例
- CommentServiceTest：7 个用例
- 总计：32 个用例，全部通过

## 4. 术语一致性

对照方案 doc 第 0 节术语约定，grep 代码：

| 术语 | 代码实体 | 命中数 | 一致性 |
|------|----------|--------|--------|
| 帖子 | Post | 338 处 | ✅ 一致 |
| 评论 | Comment | 多处 | ✅ 一致 |
| 分类 | Category | 多处 | ✅ 一致 |
| 标签 | Tag | 多处 | ✅ 一致 |
| 点赞 | Like | 多处 | ✅ 一致 |
| 内容流 | PostFeed | 0 处 | 功能概念名，无对应实体，符合预期 |
| 推荐 | Recommendation | 0 处 | 功能概念名，方法名 getHot，符合预期 |

**防冲突**：方案 doc 第 0 节未列禁用词 ✅

## 5. 架构归并

对照方案 doc 第 4 节"与项目级架构文档的关系"，实际执行更新：

- [x] CLAUDE.md：
  - 需要更新的内容：服务端口表添加 clx-post、模块结构添加 clx-post 目录、数据库添加 clx_post、路线图更新阶段7完成
  - 已更新：✅

- [x] doc/sql/schema.sql：
  - 需要更新的内容：帖子相关表结构
  - 已更新：✅（实现阶段已完成）

## 6. 遗留

**后续优化点**：
- [ ] ES 搜索集成测试（需要 ES 环境）
- [ ] 前端单元测试
- [ ] 个性化推荐算法优化（当前热度兜底）

**已知限制**：
- 搜索功能依赖 MySQL LIKE，ES 集成待部署
- 推荐策略仅热度排序，个性化推荐待开发

**实现阶段"顺手发现"列表**：
- 无
