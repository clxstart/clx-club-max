---
doc_type: feature-design
feature: 2026-04-24-async-like
status: approved
summary: 点赞操作异步化，Redis 实时计数 + MQ 异步落库 + 定时校准
tags: [mq, redis, async, performance, like]
---

# 点赞异步化 Design

> Stage 1 | 2026-04-24 | 上一步：brainstorm

<a name="section-0"></a>

## 0. 术语约定

| 术语 | 定义 |
|------|------|
| LikeMessage | MQ 消息实体，包含 postId、userId、action(like/unlike) |
| LikeProducer | MQ 生产者，点赞时发送消息 |
| LikeConsumer | MQ 消费者，消费消息并更新 DB |
| LikeSyncService | 定时任务，校准 Redis 与 DB 计数 |
| 死信队列 | DLQ，消息重试 3 次失败后进入 |

**术语冲突检查**：grep 项目无 `LikeMessage`/`LikeProducer`/`LikeConsumer`/`LikeSyncService` 命中，无冲突。

---

<a name="section-1"></a>

## 1. 决策与约束

### 需求摘要

将帖子点赞操作异步化，作为 MQ 实践入口：
- 用户点赞时，Redis INCR 计数 + 发 MQ 消息，立刻返回成功
- MQ 消费者更新 DB `post.like_count` + 写 `like_record`
- 定时任务每 5 分钟校准 Redis/DB 计数偏差

**明确不做**：
- [scope-01] unlike（取消点赞）保持同步，不走 MQ
- [scope-02] 评论点赞保持同步，本次不改
- [scope-03] 不引入 Sentinel（后续独立 feature）
- [scope-04] 不做分布式事务（Seata）
- [scope-05] 不做前端乐观更新（后端负责一致性）

### 关键决策

| 决策 | 选择 | 原因 |
|------|------|------|
| D1 点赞走 MQ | 是 | 异步解耦，减少 DB 压力 |
| D2 unlike 保持同步 | 是 | 简化逻辑，unlike 失败直接反馈用户 |
| D3 Redis key 只存计数 | `post:like_count:{postId}` | 简单够用，用户是否点赞由 DB `like_record` 判断 |
| D4 返回 Redis 计数 | 是 | 用户立刻看到更新，体验好 |
| D5 重试策略 | 3 次后进 DLQ | 不丢失消息，人工可补救 |
| D6 校准频率 | 每 5 分钟 | 平衡实时性与负载 |
| D7 本 feature 放在 clx-post | 是 | 点赞本属帖子服务职责，不新建模块 |

### 被拒方案

| 方案 | 否决原因 |
|------|----------|
| 纯 MQ 异步，不返回计数 | 用户点赞后数字不变，体验差 |
| 前端乐观更新 | 想练后端一致性处理，不推给前端 |
| unlike 也异步化 | unlike 失败场景少，同步简单 |
| 评论点赞一起做 | 增加复杂度，先跑通帖子点赞 |

---

<a name="section-2"></a>

## 2. 接口契约

### 2.1 现有接口（保持不变）

**点赞帖子**：
```
POST /post/{id}/like
Authorization: Bearer {token}

Response 200:
{
  "code": 200,
  "data": {
    "likeCount": 123  // 现为 Redis 计数
  }
}

Response 400（已点赞）:
{
  "code": 400,
  "msg": "已点赞"
}
```
来源：`LikeController.java:28-32`

**取消点赞（保持同步）**：
```
DELETE /post/{id}/like
Authorization: Bearer {token}

Response 200:
{
  "code": 200,
  "data": {
    "likeCount": 122  // DB 计数（同步更新）
  }
}

Response 400（未点赞）:
{
  "code": 400,
  "msg": "未点赞"
}
```
来源：`LikeController.java:39-43`

### 2.2 MQ 消息格式（新增）

**LikeMessage**：
```json
{
  "postId": 1001,
  "userId": 12345,
  "action": "like",  // 固定为 "like"，unlike 不走 MQ
  "timestamp": 1714464000000,
  "uuid": "550e8400-e29b-41d4-a716-446655440000"  // 幂等唯一标识
}
```
用途：生产者发送 → 消费者接收 → 更新 DB

### 2.3 Redis Key 设计（新增）

| Key | 值类型 | 说明 |
|-----|--------|------|
| `post:like_count:{postId}` | String (Integer) | 帖子点赞计数 |

**假设**：首次点赞时 Redis key 不存在，INCR 从 0 开始；需在服务启动时从 DB 初始化热门帖子计数。

---

<a name="section-3"></a>

## 3. 实现提示

### 改动计划

**文件状态评估**：
- `LikeServiceImpl.java`：107 行，职责清晰（点赞逻辑），可直接改造
- `LikeController.java`：66 行，改动小（返回值来源切换）
- `pom.xml`：需添加 RabbitMQ 依赖

**改动文件列表**：

| 文件 | 动作 | 说明 |
|------|------|------|
| `clx-post/pom.xml` | 追加 | 添加 spring-boot-starter-amqp |
| `LikeMQConfig.java` | 新建 | Queue/Exchange/Binding/DLQ 配置 |
| `LikeMessage.java` | 新建 | MQ 消息实体 |
| `LikeProducer.java` | 新建 | 发送点赞消息到 MQ |
| `LikeConsumer.java` | 新建 | 消费消息，更新 DB |
| `LikeServiceImpl.java` | 改造 | likePost 用 Redis+MQ，unlike 保持同步 |
| `LikeSyncService.java` | 新建 | 定时校准 Redis/DB |
| `LikeController.java` | 改造 | unlike 时清除 Redis key |
| `LikeServiceTest.java` | 扩展 | 新增异步流程测试 |

### 推进顺序

**Step 1：添加 RabbitMQ 依赖**
- 改动：`pom.xml` 添加 `spring-boot-starter-amqp`
- 验证：`mvn compile` 成功

**Step 2：MQ 配置与消息实体**
- 新建：`LikeMQConfig.java`（Exchange/Queue/DLQ）
- 新建：`LikeMessage.java`
- 验证：启动服务，MQ 管理界面看到队列创建

**Step 3：生产者改造**
- 新建：`LikeProducer.java`
- 验证：单元测试发送消息成功

**Step 4：改造 LikeServiceImpl.likePost**
- 改动：Redis INCR + 发 MQ + 返回 Redis 计数
- 改动：likePost 去掉 `@Transactional`（不再同步写 DB）
- 验证：单元测试点赞后 Redis 有计数、MQ 有消息

**Step 5：消费者实现**
- 新建：`LikeConsumer.java`
- 验证：点赞后 DB `like_record` 和 `post.like_count` 更新

**Step 6：unlike 处理**
- 改动：`LikeServiceImpl.unlikePost` 保持同步 + 清除 Redis key
- 改动：`LikeController.unlikePost` 返回 DB 计数（不变）
- 验证：取消点赞后 Redis key 删除、DB 计数减少

**Step 7：定时校准**
- 新建：`LikeSyncService.java`
- 验证：手动修改 Redis 计数，5 分钟后 DB 被校准

**Step 8：补充测试**
- 扩展：`LikeServiceTest.java`
- 验证：`mvn test` 全部通过

### 测试设计

**功能点 F1：点赞异步流程**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C1 点赞后 Redis 计数 +1 | 单测 mock RedisService | `likePost_shouldIncrementRedisCount` |
| C2 点赞后 MQ 有消息 | 单测 mock LikeProducer | `likePost_shouldSendMessage` |
| C3 消费者写入 like_record | 集成测试（内存 MQ） | `consume_shouldInsertLikeRecord` |
| C4 消费者更新 post.like_count | 集成测试 | `consume_shouldUpdatePostCount` |
| C5 重复点赞被拦截 | 单测 | `likePost_again_shouldThrow` |

**功能点 F2：取消点赞同步流程**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C6 unlike 后 DB 计数 -1 | 单测 | `unlikePost_shouldDecrementDbCount` |
| C7 unlike 后 Redis key 删除 | 单测 mock RedisService | `unlikePost_shouldDeleteRedisKey` |
| C8 未点赞 unlike 报错 | 单测 | `unlikePost_notLiked_shouldThrow` |

**功能点 F3：幂等处理**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C9 重复消息不重复写 | 单测 mock LikeRecordMapper.exists | `consume_duplicate_shouldSkip` |
| C10 使用消息 uuid 判断 | 代码审查 | 消息 uuid = userId:postId:timestamp 组合 |

**功能点 F4：失败处理**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C11 消费失败重试 3 次 | 配置检查 | `spring.rabbitmq.listener.simple.retry` |
| C12 3 次后进 DLQ | 集成测试 | `consume_fail3_shouldGoToDLQ` |

**功能点 F5：定时校准**

| 约束 | 验证方式 | 关键用例 |
|------|----------|----------|
| C13 Redis > DB 时更新 DB | 单测 | `sync_shouldUpdateDbWhenRedisHigher` |
| C14 Redis < DB 时更新 Redis | 单测 | `sync_shouldUpdateRedisWhenDbHigher` |
| C15 Redis key 不存在时从 DB 初始化 | 单测 | `sync_shouldInitFromDb` |

### 高风险实现约束

| 风险 | 处理方式 |
|------|----------|
| R1 首次点赞 Redis key 不存在 | INCR 自动从 0 开始，无问题 |
| R2 服务重启 Redis 计数丢失 | 定时校准会修复；可考虑启动时初始化热门帖子 |
| R3 消费者处理顺序 | 单消费者 + 单队列，顺序保证；高并发时可考虑多队列 |
| R4 unlike 与 like 并发 | unlike 同步写 DB，MQ 消费者幂等检查可兜底 |

---

<a name="section-4"></a>

## 4. 与项目级架构文档的关系

### 需要更新

**CLAUDE.md**：
- 添加：点赞异步化说明（Redis + MQ）
- 添加：RabbitMQ 已用于点赞数据同步（clx-post）

**clx-post/pom.xml**：
- 添加：spring-boot-starter-amqp 依赖

### 不需要更新

- 服务端口表（不变）
- 模块结构（不新建模块）
- Gateway 路由（不变）

---

<a name="section-5"></a>

## 5. 遗留问题与后续扩展

| 问题 | 留给后续 |
|------|----------|
| 评论点赞异步化 | 下一个 feature |
| unlike 异步化 | 视需求决定 |
| 前端乐观更新 | 性能瓶颈时再考虑 |
| Redis 计数初始化策略 | 可在启动时加载热门帖子 |
| 多消费者并发 | 高并发场景才需要 |

---

## 审查清单

- [x] 术语 grep 无冲突
- [x] 需求摘要含"不做什么"
- [x] 推进步骤 8 步，每步可独立验证
- [x] 测试设计按功能点组织
- [x] 高风险约束已列出
- [x] MQ 消息格式有示例
- [x] Redis key 设计有说明