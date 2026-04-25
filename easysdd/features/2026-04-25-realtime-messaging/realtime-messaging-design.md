---
doc_type: feature-design
feature: 2026-04-25-realtime-messaging
status: approved
summary: 实时消息与通知系统，私信 WebSocket 秒级推送 + 分层通知 + 在线状态
tags: [websocket, messaging, notification, realtime, im, redis]
---

# 实时消息与通知系统 Design

> Stage 1 | 2026-04-25 | 上一步：brainstorm

<a name="section-0"></a>

## 0. 术语约定

| 术语 | 定义 |
|------|------|
| ChatMessage | 私信消息实体，存储于 `clx_message` 数据库 |
| ChatSession | 会话实体，两个用户之间至多一个会话 |
| Notification | 通知实体，涵盖评论回复/点赞/关注/系统公告四种类型 |
| WsSessionManager | WebSocket 会话管理器，维护用户在线连接 |
| MessageRouter | 消息路由器，根据消息类型选择投递策略（实时/准实时/拉取） |
| 上线补发 | 用户上线后，服务端推送离线期间积攒的未读消息 |

**术语冲突检查**：grep 项目代码无 `ChatMessage`/`ChatSession`/`Notification`/`WsSessionManager`/`MessageRouter` 命中，无冲突。

---

<a name="section-1"></a>

## 1. 决策与约束

### 需求摘要

为 CLX 社区添加实时消息与通知系统，提升用户粘性和社交属性：

- **私信**：用户间一对一文字聊天，WebSocket 秒级实时推送，离线消息上线补发
- **评论回复通知**：有人回复你的帖子/评论时，准实时推送
- **点赞/关注通知**：刷新时拉取，支持聚合（"3人赞了你的帖子"）
- **系统公告**：启动时检查，全量推送
- **在线状态**：显示"最后活跃时间"（"3分钟前在线"）

**明确不做**：

- [scope-01] 群聊（单聊优先，后续扩展）
- [scope-02] 消息搜索（暂不引入 ES 到消息模块）
- [scope-03] 消息撤回/编辑（后续扩展）
- [scope-04] 多设备同步（单设备假设，同时只允许一个 WebSocket 连接）
- [scope-05] 富媒体消息（文字优先，图片后续）
- [scope-06] 消息已读回执（后续扩展）

### 关键决策

| 决策 | 选择 | 原因 |
|------|------|------|
| D1 新建 clx-message 服务 | 独立模块，端口 9500 | 消息系统职责独立、规模可扩展，不适合塞进 clx-user（职责不匹配）或 clx-post（已承载帖子业务） |
| D2 WebSocket 集群广播 | Redis Pub/Sub | 项目已有 Redis 基础设施，无需引入新组件；单 Pub/Sub channel 按接收者 userId 路由，避免广播风暴 |
| D3 私信离线存储上限 | 最近 500 条/会话，30 天过期 | 假设：社区私信频率中等，500 条覆盖绝大多数场景；超过 500 条删除最旧消息 |
| D4 通知聚合窗口 | 1 小时内同类通知聚合 | "3人赞了你的帖子"比连续 3 条独立通知体验更好 |
| D5 未读计数存储 | Redis Hash `unread:{userId}` | 高频读写，Redis 天然适合；DB 只做持久化备份 |
| D6 心跳间隔 | 30 秒客户端 ping，60 秒服务端超时 | 平衡实时性与连接开销 |
| D7 数据库 | 新建 `clx_message` 数据库 | 与 `clx_user`/`clx_post` 解耦，独立扩容 |

### 被拒方案

| 方案 | 否决原因 |
|------|----------|
| 消息放 clx-user | clx-user 定位是用户信息 CRUD，消息是独立领域，放进去会变成筐 |
| 消息放 clx-post | 帖子服务已承载帖子/评论/点赞业务，再加消息职责过多 |
| 用 RabbitMQ 做 WebSocket 集群广播 | 已有 Redis Pub/Sub 足够，MQ 适合异步消费不适合实时广播 |
| 私信不限量存储 | 存储成本失控，社区场景不需要 IM 级消息存留 |
| Netty 自建 WebSocket | Spring WebSocket 封装足够，自建增加维护成本 |

### 前置依赖

- 无结构性前置依赖。clx-message 是全新服务，不依赖其他模块的代码改造。
- 运行时依赖：MySQL（`clx_message` 库）、Redis（已有基础设施）、RabbitMQ（已有基础设施，用于通知异步投递）

### 主流程概述

**私信发送**：
```
发送者 → HTTP POST /message/send → ChatService
  → 写入 DB (chat_message + 更新 chat_session)
  → 更新 Redis 未读数
  → MessageRouter.route()
    → 接收者在线? → Redis Pub/Sub 广播 → WsSessionManager 推送 WebSocket
    → 接收者离线? → 仅存 DB，等待上线补发
```

**上线补发**：
```
用户 WebSocket 连接建立 → WsSessionManager.onConnect()
  → 查询 Redis 未读计数
  → 查询 DB 各会话最近未读消息
  → 批量推送
```

**通知触发（以评论回复为例）**：
```
clx-post 评论写入 → HTTP 调用 clx-message /notification/trigger
  → NotificationService.create()
  → 写入 DB (notification)
  → 聚合检查（1 小时内同类）
  → 更新 Redis 未读数
  → 准实时推送（5 秒延迟，合并同批通知）
```

---

<a name="section-2"></a>

## 2. 接口契约

### 2.1 WebSocket 连接

**建立连接**：
```
ws://localhost:9500/ws/message?token={sa-Token}

成功：连接保持，服务端可推送消息
失败（token 无效）：连接关闭，code=4401
```

**客户端→服务端消息格式**：
```json
{"type": "ping"}

{"type": "chat", "to": 10001, "content": "你好"}
```

**服务端→客户端消息格式**：
```json
// Pong（心跳响应）
{"type": "pong"}

// 私信推送
{"type": "chat", "from": 10001, "fromName": "张三", "content": "你好", "sessionId": 1, "timestamp": 1714464000000}

// 通知推送（评论回复）
{"type": "notification", "category": "comment_reply", "title": "李四 回复了你的帖子", "content": "写得太好了！", "timestamp": 1714464000000}

// 离线消息批量推送
{"type": "offline", "chats": [...], "notifications": [...], "unread": {"chat": 5, "notification": 3}}

// 未读数更新
{"type": "unread", "chat": 5, "notification": 3}
```

### 2.2 私信 REST 接口

**发送私信**：
```
POST /message/send
Authorization: Bearer {token}

请求：
{
  "toUserId": 10001,
  "content": "你好"
}

响应 200：
{
  "code": 200,
  "data": {
    "messageId": 100001,
    "sessionId": 1,
    "timestamp": 1714464000000
  }
}

响应 400（发送给自己）：
{"code": 400, "msg": "不能给自己发私信"}

响应 400（内容为空）：
{"code": 400, "msg": "消息内容不能为空"}
```

**获取会话列表**：
```
GET /message/sessions?page=1&size=20
Authorization: Bearer {token}

响应 200：
{
  "code": 200,
  "data": {
    "list": [
      {
        "sessionId": 1,
        "targetUserId": 10001,
        "targetNickname": "张三",
        "targetAvatar": "https://...",
        "lastMessage": "你好",
        "lastTime": 1714464000000,
        "unreadCount": 2
      }
    ],
    "total": 5
  }
}
```

**获取会话消息历史**：
```
GET /message/sessions/{sessionId}/messages?cursor={lastMessageId}&size=20
Authorization: Bearer {token}

响应 200：
{
  "code": 200,
  "data": {
    "list": [
      {
        "messageId": 100001,
        "fromUserId": 10001,
        "content": "你好",
        "timestamp": 1714464000000,
        "direction": "received"
      }
    ],
    "hasMore": true
  }
}
```

**标记会话已读**：
```
PUT /message/sessions/{sessionId}/read
Authorization: Bearer {token}

响应 200：
{"code": 200, "data": null}
```

### 2.3 通知 REST 接口

**获取通知列表**：
```
GET /notification/list?type={comment|like|follow|system}&page=1&size=20
Authorization: Bearer {token}

响应 200：
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 1,
        "type": "like",
        "title": "赞了你的帖子",
        "content": "张三、李四 赞了你的帖子「Spring Boot 入门」",
        "isRead": false,
        "createTime": 1714464000000
      }
    ],
    "total": 10
  }
}
```

**标记通知已读**：
```
PUT /notification/read/{id}          ← 单条已读
PUT /notification/read-all?type={type}  ← 按类型全部已读
Authorization: Bearer {token}

响应 200：
{"code": 200, "data": null}
```

**获取未读数**：
```
GET /notification/unread-count
Authorization: Bearer {token}

响应 200：
{
  "code": 200,
  "data": {
    "comment": 3,
    "like": 5,
    "follow": 1,
    "system": 0,
    "chat": 2
  }
}
```

### 2.4 通知触发接口（内部调用）

**触发通知**：
```
POST /notification/trigger
X-Internal-Token: {internal-token}

请求：
{
  "userId": 10001,
  "type": "comment_reply",
  "title": "李四 回复了你的帖子",
  "content": "写得太好了！",
  "sourceId": 100,
  "sourceType": "comment"
}

响应 200：
{"code": 200, "data": {"notificationId": 1}}
```

### 2.5 在线状态接口

**获取用户在线状态**：
```
GET /message/online-status?userIds=10001,10002,10003
Authorization: Bearer {token}

响应 200：
{
  "code": 200,
  "data": {
    "10001": {"online": true, "lastActiveTime": 1714464000000},
    "10002": {"online": false, "lastActiveTime": 1714463700000},
    "10003": {"online": false, "lastActiveTime": null}
  }
}
```

### 2.6 Redis Key 设计

| Key | 类型 | 说明 |
|-----|------|------|
| `ws:session:{userId}` | String | WebSocket 连接所在服务实例 ID |
| `unread:{userId}` | Hash | 未读计数，field: chat/comment/like/follow/system |
| `user:online:{userId}` | String | 最后活跃时间戳，TTL 120 秒 |
| `msg:offline:{userId}` | List | 离线消息队列（上线补发用），LRANGE 取最近 50 条 |
| `notify:aggregate:{userId}:{type}:{sourceId}` | String | 聚合锁，TTL 1 小时 |

---

<a name="section-3"></a>

## 3. 实现提示

### 改动计划

**新建模块**：`clx-message`（端口 9500），完整的独立微服务。

**新建文件列表**：

| 文件 | 说明 |
|------|------|
| `clx-message/pom.xml` | 模块 POM，依赖 web/websocket/redis/mybatis/amqp |
| `ClxMessageApplication.java` | 启动类 |
| `application.yml` + `application-dev.yml` | 配置文件 |
| `config/WebSocketConfig.java` | WebSocket 配置 |
| `config/RabbitMQConfig.java` | 通知异步投递 MQ 配置 |
| `config/CorsConfig.java` | CORS 配置 |
| `config/SecurityConfig.java` | Spring Security 放行 |
| `ws/WsSessionManager.java` | WebSocket 会话管理（连接/断开/推送） |
| `ws/MessageWebSocketHandler.java` | WebSocket 消息处理器 |
| `ws/WsHandshakeInterceptor.java` | 握手拦截器（token 验证） |
| `entity/ChatMessage.java` | 私信消息实体 |
| `entity/ChatSession.java` | 会话实体 |
| `entity/Notification.java` | 通知实体 |
| `mapper/ChatMessageMapper.java` | 私信 Mapper |
| `mapper/ChatSessionMapper.java` | 会话 Mapper |
| `mapper/NotificationMapper.java` | 通知 Mapper |
| `dto/SendMessageRequest.java` | 发送私信请求 |
| `dto/NotificationTriggerRequest.java` | 通知触发请求 |
| `dto/WsEnvelope.java` | WebSocket 消息信封 |
| `service/ChatService.java` | 私信业务 |
| `service/impl/ChatServiceImpl.java` | 私信业务实现 |
| `service/NotificationService.java` | 通知业务 |
| `service/impl/NotificationServiceImpl.java` | 通知业务实现 |
| `service/OnlineStatusService.java` | 在线状态业务 |
| `service/impl/OnlineStatusServiceImpl.java` | 在线状态业务实现 |
| `service/MessageRouter.java` | 消息路由（在线推/离线存） |
| `controller/MessageController.java` | 私信 REST 接口 |
| `controller/NotificationController.java` | 通知 REST 接口 |
| `controller/OnlineStatusController.java` | 在线状态接口 |
| `mq/NotificationProducer.java` | 通知 MQ 生产者 |
| `mq/NotificationConsumer.java` | 通知 MQ 消费者 |
| `task/OfflineMessageCleaner.java` | 离线消息过期清理定时任务 |

**新增 SQL**：`doc/sql/message_schema.sql`（`clx_message` 库的表结构）

**修改已有文件**：

| 文件 | 动作 | 理由 |
|------|------|------|
| `pom.xml`（根） | 追加 | 添加 `<module>clx-message</module>` |
| `CLAUDE.md` | 追加 | 添加 clx-message 服务说明 |

### 推进顺序

**Step 1：搭建 clx-message 模块骨架**
- 新建：pom.xml、启动类、application.yml、application-dev.yml、SecurityConfig、CorsConfig
- 新建：message_schema.sql
- 改动：根 pom.xml 添加 module
- 验证：`mvn compile` 成功，服务能启动

**Step 2：WebSocket 基础设施**
- 新建：WebSocketConfig、WsHandshakeInterceptor、MessageWebSocketHandler、WsSessionManager
- 实现：握手时验证 sa-Token，建立连接后维护 session 映射
- 实现：心跳 ping/pong（30 秒间隔）
- 验证：wscat 连接 `ws://localhost:9500/ws/message?token=xxx` 成功，心跳正常

**Step 3：在线状态与 Redis Pub/Sub**
- 新建：OnlineStatusService、OnlineStatusController
- 实现：连接时更新 `user:online:{userId}`，心跳时刷新 TTL
- 实现：Redis Pub/Sub 订阅 `ws:broadcast` channel，收到消息后推送给本地连接
- 验证：查 Redis `user:online:{userId}` 有值，GET `/message/online-status` 返回正确

**Step 4：私信核心流程**
- 新建：ChatMessage/ChatSession 实体、Mapper、ChatService、MessageRouter、MessageController
- 实现：发送私信 → 写 DB + 更新 Redis 未读 + 路由推送
- 实现：MessageRouter 判断在线/离线，在线走 Redis Pub/Sub + WebSocket 推送，离线写 Redis List
- 实现：会话列表、消息历史（游标分页）、标记已读
- 验证：两个浏览器窗口互发私信，秒级送达

**Step 5：上线补发**
- 改动：WsSessionManager.onConnect() 查询离线消息并批量推送
- 实现：从 Redis List `msg:offline:{userId}` 取最近 50 条，推送后清空
- 验证：用户 A 给离线用户 B 发消息，B 上线后收到补发

**Step 6：通知系统**
- 新建：Notification 实体、Mapper、NotificationService、NotificationController
- 新建：NotificationProducer/Consumer（RabbitMQ 异步）
- 新建：NotificationTriggerRequest
- 实现：触发通知 → MQ → 消费者写 DB + 聚合 + 更新未读
- 实现：通知列表、标记已读、未读计数
- 验证：调用 `/notification/trigger`，通知出现在列表中，聚合生效

**Step 7：离线消息清理与边界处理**
- 新建：OfflineMessageCleaner
- 实现：定时清理 30 天过期私信、超 500 条/会话删最旧
- 实现：发送给自己的校验、空内容校验
- 验证：手动插入超量消息，清理后条数 ≤ 500

**Step 8：补充测试**
- 扩展：ChatServiceTest、NotificationServiceTest、OnlineStatusServiceTest、WsSessionManagerTest
- 验证：`mvn test` 全部通过

### 测试设计

**功能点 F1：私信实时投递**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C1 发送私信后 DB 有记录 | 单测 mock mapper | `send_shouldInsertMessage` |
| C2 发送私信后会话更新 | 单测 | `send_shouldUpdateSession` |
| C3 接收者在线时 WebSocket 推送 | 单测 mock WsSessionManager | `send_online_shouldPush` |
| C4 接收者离线时写入离线队列 | 单测 mock RedisService | `send_offline_shouldQueue` |
| C5 发送给自己报错 | 单测 | `send_self_shouldThrow` |
| C6 空内容报错 | 单测 | `send_empty_shouldThrow` |

**功能点 F2：上线补发**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C7 上线时推送离线消息 | 单测 | `connect_shouldPushOffline` |
| C8 推送后清空离线队列 | 单测 | `connect_shouldClearOffline` |

**功能点 F3：通知系统**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C9 触发通知写入 DB | 单测 | `trigger_shouldInsertNotification` |
| C10 1 小时内同类通知聚合 | 单测 | `trigger_duplicate_shouldAggregate` |
| C11 标记已读后未读数 -1 | 单测 | `read_shouldDecrementUnread` |
| C12 通知 MQ 消费者写 DB | 单测 | `consume_shouldInsertNotification` |

**功能点 F4：在线状态**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C13 连接时 Redis 记录在线 | 单测 | `connect_shouldSetOnline` |
| C14 断开时 Redis 更新最后活跃时间 | 单测 | `disconnect_shouldUpdateLastActive` |
| C15 TTL 过期后查询返回离线 | 单测 | `expired_shouldReturnOffline` |

**功能点 F5：离线消息清理**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C16 30 天前的私信被清理 | 单测 | `clean_shouldDeleteOldMessages` |
| C17 超过 500 条/会话保留最近 500 条 | 单测 | `clean_shouldKeepLatest500` |

### 高风险实现约束

| 风险 | 处理方式 |
|------|----------|
| R1 WebSocket 连接在 Gateway/Nginx 代理下断连 | 配置 Nginx `proxy_set_header Upgrade`；当前阶段直连 clx-message，暂不经过 Gateway |
| R2 单设备假设被打破（用户开两个标签页） | 后建立的连接踢掉先建立的（写 `ws:session:{userId}` 时检查旧连接），旧连接推送 "kicked" 消息后关闭 |
| R3 Redis Pub/Sub 消息丢失（Redis 重启） | 接受短暂丢失，私信有 DB 持久化兜底，上线补发可弥补；不引入可靠消息队列做 WebSocket 广播 |
| R4 sa-Token 验证在 WebSocket 握手阶段 | 握手拦截器中调用 `StpUtil.getLoginIdByToken(token)` 验证，失败返回 4401 关闭连接 |
| R5 通知聚合并发竞争 | Redis SETNX 做聚合锁，TTL 1 小时；锁内聚合，锁外直接插入 |

---

<a name="section-4"></a>

## 4. 与项目级架构文档的关系

### 需要更新

**CLAUDE.md**：
- 添加：clx-message 服务（端口 9500）到服务端口表
- 添加：clx-message 到模块结构
- 添加：消息架构说明（WebSocket + Redis Pub/Sub + RabbitMQ 通知）
- 添加：新增数据库 `clx_message`

**根 pom.xml**：
- 添加：`<module>clx-message</module>`

### 不需要更新

- clx-auth（无需改动，sa-Token 验证逻辑不变）
- clx-post（暂不集成通知触发，后续 feature 再做）
- clx-search（无关）
- clx-gateway（暂未启用）
- 现有数据库（只新增 `clx_message`，不改旧库）

---

## 审查清单

- [x] 术语 grep 无冲突
- [x] 需求摘要含"不做什么"（6 条 scope-guard）
- [x] 推进步骤 8 步，每步可独立验证
- [x] 测试设计按功能点组织（5 个功能点，17 条约束）
- [x] 高风险约束已列出（5 条）
- [x] 接口契约有具体示例（含正常路径和错误路径）
- [x] Redis Key 设计有说明
- [x] 跨模块依赖已说明（clx-post 通知触发后续集成）
