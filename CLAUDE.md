# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

CLX 是一个超大型社区平台（类似知乎、贴吧、微博），采用微服务架构。

**当前阶段**：最简登录版本，让项目先能跑起来，后续逐步扩展。

**技术栈**：JDK 17、Spring Boot 3.2.5、Spring Cloud 2023.0.1、Spring Cloud Alibaba 2023.0.1.0

## 当前里程碑

**阶段 1（当前完成）**：最简登录（sa-Token 核心模式）
- 删除了 OAuth2 Authorization Server、JWT 整合等复杂配置
- 保留 Maven 多模块结构
- clx-auth 独立运行，直连数据库验证用户
- 支持用户名密码登录，返回 sa-Token

## 构建命令

```bash
# 构建所有模块
mvn clean compile -DskipTests

# 启动认证服务
mvn spring-boot:run -pl clx-auth -Dspring-boot.run.profiles=dev
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| clx-auth | 9100 | 认证中心，用户登录 |
| clx-user | 9201 | 用户服务，个人资料、关注/粉丝、收藏 |
| clx-post | 9300 | 帖子服务 |
| clx-search | 9400 | 搜索服务，聚合搜索、ES全文搜索、热词分析 |
| clx-message | 9500 | 消息服务，私信、通知、在线状态 |
| clx-quiz | 9600 | 刷题服务，题库管理、练习流程、错题本 |
| clx-admin | 9700 | 后台管理服务，用户管理、角色分配 |
| clx-gateway | 8080 | API 网关（暂未实现） |
| clx-admin-web | 5174 | 后台管理前端（Vue 3 + Element Plus） |

## 模块结构

```
clx/
├── clx-common/
│   ├── clx-common-core/           # 统一响应 R、异常、常量
│   ├── clx-common-security/       # Spring Security（放行） + sa-Token
│   └── clx-common-redis/          # Redis 配置和服务
├── clx-api/                       # Feign API（暂空）
├── clx-auth/                      # 认证服务（当前可用）
│   ├── entity/User.java           # 用户实体
│   ├── mapper/UserMapper.java     # MyBatis-Plus Mapper
│   ├── service/AuthService.java   # 登录验证
│   └── controller/AuthController.java
├── clx-user/                      # 用户服务（当前可用）
│   ├── entity/                    # User、UserFollow、PostFavorite
│   ├── mapper/                    # MyBatis Mapper
│   ├── service/                   # 用户资料、关注、收藏服务
│   └── controller/                # API 控制器
├── clx-post/                      # 帖子服务（当前可用）
│   ├── entity/                    # Post、Comment、Category、Tag、LikeRecord
│   ├── mapper/                    # MyBatis Mapper
│   ├── service/                   # 帖子、评论、点赞、分类、标签服务
│   └── controller/                # API 控制器
├── clx-search/                    # 搜索服务（当前可用）
│   ├── controller/                # API 控制器
│   ├── datasource/                # 数据源实现（Post/User/Picture/Web）
│   ├── entity/                    # SearchLog、HotKeyword
│   ├── es/                        # ES 文档定义
│   ├── manager/                   # SearchFacade 业务聚合
│   ├── mapper/                    # MyBatis Mapper
│   └── service/                   # 搜索、热词、数据同步服务
├── clx-message/                   # 消息服务（当前可用）
│   ├── entity/                    # ChatMessage、ChatSession、Notification
│   ├── mapper/                    # MyBatis Mapper
│   ├── service/                   # 私信、通知、在线状态服务
│   ├── ws/                        # WebSocket 处理器、会话管理
│   ├── mq/                        # RabbitMQ 生产者/消费者
│   └── controller/                # API 控制器
├── clx-quiz/                      # 刷题服务（当前可用）
│   ├── entity/                    # Subject、SubjectCategory、SubjectLabel、Practice、WrongBook
│   ├── mapper/                    # MyBatis Mapper
│   ├── service/                   # 题目、练习、错题本服务
│   ├── service/handler/           # 题型策略（Radio/Multiple/Judge/Brief）
│   └── controller/                # API 控制器
├── clx-admin/                     # 后台管理服务（当前可用）
│   ├── controller/                # UserController、RoleController
│   ├── service/                   # OperLogService（操作日志）
│   ├── mapper/                    # OperLogMapper
│   └── feign/                     # Feign 客户端（调用 clx-user/clx-auth）
├── clx-gateway/                   # API 网关（暂未实现）
├── clx-web/                       # 前端（React + Vite + Tailwind + Neumorphism）
│   ├── src/components/ui/         # Neumorphism 基础组件（NeuButton/NeuInput/NeuCard/Toast）
│   ├── src/components/layout/     # 布局组件（TopNavBar、LeftNav）
│   ├── src/pages/                  # 页面组件（Home、PostDetail、UserProfile、Compose、Search、Quiz、Account、Auth）
│   ├── src/api/                    # API 请求和类型定义
│   └── src/styles/                 # 全局样式
├── clx-admin-web/                 # 后台管理前端（当前可用）
│   ├── src/views/                  # 页面组件（Login、UserList、UserEdit）
│   ├── src/layouts/               # 布局组件（AdminLayout）
│   ├── src/api/                    # API 请求和类型定义
│   └── src/stores/                 # Pinia 状态管理
└── doc/sql/                       # 数据库脚本
```

## 当前安全架构（最简版）

- **Spring Security**：只做 CSRF 禁用和路径放行
- **sa-Token**：核心模式，用户登录后生成 UUID Token，存 Redis

**核心文件**：
- `SimpleSecurityConfig.java` - Spring Security 放行所有请求
- `StpInterfaceImpl.java` - sa-Token 权限接口（暂返回空）

## 登录接口

```bash
# 登录
curl -X POST http://localhost:9100/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 返回
{"code":200,"data":{"token":"xxx","tokenName":"Authorization"}}

# 获取当前用户
curl http://localhost:9100/auth/me \
  -H "Authorization: Bearer xxx"

# 登出
curl -X POST http://localhost:9100/auth/logout \
  -H "Authorization: Bearer xxx"
```

## 数据库

环境部署使用 Docker：`clx-mysql`、`clx-redis`、`clx-nacos`

数据库：
- `clx_user`（用户数据）
- `clx_post`（帖子、评论、分类、标签数据）
- `clx_search`（搜索日志、热词数据）
- `clx_message`（私信、会话、通知数据）
- `clx_quiz`（题目、分类、标签、练习记录、错题本）

表结构：`doc/sql/schema.sql`
初始化数据：`doc/sql/init_data.sql`

**默认账号**：
- admin / admin123
- test / test123

## 搜索基础设施

聚合搜索服务依赖以下 Docker 容器（见 `docker/search/docker-compose.yml`）：

- **Elasticsearch 8.11**：端口 9201，单节点部署
- **RabbitMQ 3.12**：端口 5672（AMQP）、15672（管理界面）
- **Kibana**：可选，用于 ES 可视化

ES 索引定义：`doc/es/` 目录下
搜索表结构：`doc/sql/search_schema.sql`

## 点赞异步化架构

帖子点赞采用 Redis + MQ 异步化架构：

- **点赞流程**：Redis INCR 计数 + 发送 MQ 消息 → 返回 Redis 计数
- **MQ 消费者**：写入 `like_record` + 更新 `post.like_count`
- **取消点赞**：同步写 DB + 清除 Redis key（不走 MQ）
- **定时校准**：每 5 分钟对比 Redis/DB 计数，修正偏差

**核心文件**：
- `LikeMQConfig.java` - MQ 配置（Exchange/Queue/DLQ）
- `LikeProducer.java` - 点赞消息生产者
- `LikeConsumer.java` - 点赞消息消费者
- `LikeSyncService.java` - 定时校准服务

**Redis Key**：`post:like_count:{postId}`

**MQ 配置**：重试 3 次后进入死信队列（DLQ）

## 实时消息与通知架构

消息服务采用 WebSocket + Redis Pub/Sub + RabbitMQ 分层架构：

**私信实时投递**：
- 发送私信 → 写入 DB + 更新 Redis 未读计数 → MessageRouter 路由
- 接收者在线 → Redis Pub/Sub 广播 → WebSocket 推送
- 接收者离线 → 写入 Redis List 离线队列，上线补发

**通知分层处理**：
| 通知类型 | 实时性 | 投递方式 |
|----------|--------|----------|
| 私信 | 秒级实时 | WebSocket 实时推送 |
| 评论回复 | 准实时 | MQ 异步，可聚合 |
| 点赞/关注 | 不追求 | 刷新时拉取 |
| 系统公告 | 不追求 | 启动时检查 |

**核心文件**：
- `WsSessionManager.java` - WebSocket 会话管理（单设备，踢旧连接）
- `MessageRouter.java` - 消息路由（在线推/离线存）
- `ChatServiceImpl.java` - 私信业务
- `NotificationServiceImpl.java` - 通知业务（含聚合）
- `OfflineMessageCleaner.java` - 离线消息定时清理

**Redis Key**：
- `user:online:{userId}` - 最后活跃时间，TTL 120 秒
- `unread:{userId}` - 未读计数 Hash
- `msg:offline:{userId}` - 离线消息队列

**WebSocket 路径**：`ws://localhost:9500/ws/message?token={sa-Token}`

## 后续扩展路线图

1. **阶段 1**：最简登录（sa-Token 核心模式） ✅
2. **阶段 2**：添加 JWT 支持
3. **阶段 3**：添加权限控制（@SaCheckPermission）
4. **阶段 4**：实现 clx-user 服务 ✅
5. **阶段 5**：启用 Gateway 路由
6. **阶段 6**：启用 OAuth2 SSO（企业微信、钉钉）
7. **阶段 7**：社区功能开发（帖子、评论、点赞、分类标签） ✅
8. **阶段 8**：聚合搜索服务（ES全文搜索、热词分析） ✅
9. **阶段 9**：前端重写（Neumorphism 风格，登录+首页+详情页） ✅
10. **阶段 10**：实时消息与通知系统（私信、通知、在线状态） ✅
11. **阶段 11**：刷题系统（题库管理、练习流程、错题本） ✅

## 工作准则

遵循 Karpathy 四原则，减少 LLM 编码常见错误：

### 1. 先想再做（Think Before Coding）

- 明确说出假设，不确定就问，不要沉默选择
- 存在多种解读时列出来，让用户选择
- 有更简单方案就说出来，必要时反驳用户
- 困惑时停下来，说清楚哪里不懂再继续

### 2. 简单至上（Simplicity First）

- 不做超出需求的 feature
- 不为单次使用创建抽象
- 不加没人要的"灵活性"/"可配置性"
- 不处理不可能发生的错误
- **自检**：高级工程师会说这代码过度复杂吗？如果是，简化

### 3. 手术式修改（Surgical Changes）

- 不"顺手优化"邻近代码/注释/格式
- 不重构没坏的东西
- 匹配现有风格，即使你不会这样写
- 发现无关死代码只提一句，不删
- **自检**：每行改动能否追溯到用户请求？

### 4. 目标驱动执行（Goal-Driven Execution）

把命令式任务变成可验证的目标：
- "加校验" → "给无效输入写测试，然后让测试通过"
- "修 bug" → "写一个复现 bug 的测试，然后让测试通过"
- "重构 X" → "确保重构前后测试都通过"

多步任务写成计划：
```
1. [步骤] → verify: [检查点]
2. [步骤] → verify: [检查点]
```

## 注意事项

1. 当前不使用 Nacos，本地开发已禁用
2. 写代码要有精简注释，通俗易懂
3. 当前不使用 Gateway，直接访问各服务端口
4. 当前不使用 JWT，sa-Token 默认 UUID Token 模式
5. Gateway 使用 WebFlux，添加依赖需排除 `spring-boot-starter-web`