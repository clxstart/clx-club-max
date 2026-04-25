---
doc_type: feature-acceptance
feature: 2026-04-24-async-like
status: completed
summary: 点赞异步化验收完成，实现与方案一致，测试全部通过
---

# 点赞异步化 验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-04-25
> 关联方案 doc：easysdd/features/2026-04-24-async-like/async-like-design.md

## 1. 接口契约核对

对照方案 doc 第 2 节接口契约，逐一核查实现与契约的一致性：

**契约示例逐项核对**：

- [x] POST /post/{id}/like（LikeController.java:27-32）
  - 契约要求：返回 Redis 计数
  - 代码实际行为：`likeService.likePost()` 返回 `redisService.increment()` 的结果 ✓

- [x] DELETE /post/{id}/like（LikeController.java:38-42）
  - 契约要求：返回 DB 计数（同步更新）
  - 代码实际行为：`likeService.unlikePost()` 返回 `postMapper.selectById().getLikeCount()` ✓

- [x] LikeMessage 消息格式（LikeMessage.java）
  - 契约要求：postId、userId、action、timestamp、uuid
  - 代码实际行为：全部字段已定义，uuid 格式为 `userId:postId:timestamp` ✓

- [x] Redis Key（LikeServiceImpl.java:34）
  - 契约要求：`post:like_count:{postId}`
  - 代码实际行为：`LIKE_COUNT_KEY = "post:like_count:"` ✓

**流程图核对**：

- [x] 点赞流程：Redis INCR → MQ send → 返回计数（LikeServiceImpl.java:37-52）
- [x] 消费流程：幂等检查 → 写 like_record → 更新 post.like_count（LikeConsumer.java:34-59）
- [x] unlike 流程：检查 → 删除 like_record → 减 DB 计数 → 清 Redis key（LikeServiceImpl.java:56-76）

## 2. 行为与决策核对

对照方案 doc 第 1 节决策与约束：

**需求摘要逐项验证**：

- [x] 用户点赞时，Redis INCR 计数 + 发 MQ 消息，立刻返回成功
  - 实测：LikeServiceImpl.likePost 第 44-48 行 ✓

- [x] MQ 消费者更新 DB `post.like_count` + 写 `like_record`
  - 实测：LikeConsumer.consume 第 48-56 行 ✓

- [x] 定时任务每 5 分钟校准 Redis/DB 计数偏差
  - 实测：LikeSyncService.sync 第 36 行 `@Scheduled(fixedRate = 5 * 60 * 1000)` ✓

**明确不做逐项核对**：

- [x] [scope-01] unlike（取消点赞）保持同步，不走 MQ
  - grep LikeProducer 不在 unlikePost 中被调用 ✓

- [x] [scope-02] 评论点赞保持同步，本次不改
  - grep likeComment/unlikeComment 方法无 MQ 相关代码 ✓

- [x] [scope-03] 不引入 Sentinel
  - grep 无 sentinel 依赖 ✓

- [x] [scope-04] 不做分布式事务（Seata）
  - grep 无 seata 依赖 ✓

- [x] [scope-05] 不做前端乐观更新
  - 前端代码无变更（clx-web 目录已删除，不在本次 feature 范围） ✓

**关键决策落地**：

- [x] D1 点赞走 MQ → LikeProducer.send() 在 likePost 中调用 ✓
- [x] D2 unlike 保持同步 → unlikePost 无 MQ 调用，直接写 DB ✓
- [x] D3 Redis key 只存计数 → key 格式 `post:like_count:{postId}` ✓
- [x] D4 返回 Redis 计数 → likePost 返回 increment 结果 ✓
- [x] D5 重试策略 3 次后进 DLQ → application-dev.yml:33-38 配置 `max-attempts: 3` ✓
- [x] D6 校准频率每 5 分钟 → LikeSyncService.sync `fixedRate = 5 * 60 * 1000` ✓
- [x] D7 本 feature 放在 clx-post → 所有文件在 clx-post 模块 ✓

## 3. 测试约束核对

对照方案 doc 第 3 节测试设计，逐条测试约束验证：

**功能点 F1：点赞异步流程**

- [x] **C1**：点赞后 Redis 计数 +1
  - 验证方式：单测（LikeServiceTest.java:57-68 `likePost_shouldIncrementRedisCount`）
  - 结果：通过 ✓

- [x] **C2**：点赞后 MQ 有消息
  - 验证方式：单测（LikeServiceTest.java:72-84 `likePost_shouldSendMessage`）
  - 结果：通过 ✓

- [x] **C3**：消费者写入 like_record
  - 验证方式：单测（LikeConsumerTest.java:38-47 `consume_shouldInsertLikeRecord`）
  - 结果：通过 ✓

- [x] **C4**：消费者更新 post.like_count
  - 验证方式：单测（LikeConsumerTest.java:50-59 `consume_shouldUpdatePostCount`）
  - 结果：通过 ✓

- [x] **C5**：重复点赞被拦截
  - 验证方式：单测（LikeServiceTest.java:87-97 `likePost_again_shouldThrow`）
  - 结果：通过 ✓

**功能点 F2：取消点赞同步流程**

- [x] **C6**：unlike 后 DB 计数 -1
  - 验证方式：单测（LikeServiceTest.java:106-120 `unlikePost_shouldDecrementDbCount`）
  - 结果：通过 ✓

- [x] **C7**：unlike 后 Redis key 删除
  - 验证方式：单测（LikeServiceTest.java:124-137 `unlikePost_shouldDeleteRedisKey`）
  - 结果：通过 ✓

- [x] **C8**：未点赞 unlike 报错
  - 验证方式：单测（LikeServiceTest.java:141-150 `unlikePost_notLiked_shouldThrow`）
  - 结果：通过 ✓

**功能点 F3：幂等处理**

- [x] **C9**：重复消息不重复写
  - 验证方式：单测（LikeConsumerTest.java:64-72 `consume_duplicate_shouldSkip`）
  - 结果：通过 ✓

- [x] **C10**：使用消息 uuid 判断
  - 验证方式：代码审查（LikeConsumer.java:42 使用 likeRecordMapper.exists）
  - 结果：通过 ✓

**功能点 F4：失败处理**

- [x] **C11**：消费失败重试 3 次
  - 验证方式：配置检查（application-dev.yml:33-38）
  - 结果：`max-attempts: 3` ✓

- [x] **C12**：3 次后进 DLQ
  - 验证方式：配置检查（LikeMQConfig.java:75-80 死信队列配置）
  - 结果：`deadLetterExchange(LIKE_DLQ)` ✓

**功能点 F5：定时校准**

- [x] **C13**：Redis > DB 时更新 DB
  - 验证方式：单测（LikeSyncServiceTest.java:48-56）
  - 结果：通过 ✓

- [x] **C14**：Redis < DB 时更新 Redis
  - 验证方式：单测（LikeSyncServiceTest.java:59-68）
  - 结果：通过 ✓

- [x] **C15**：Redis key 不存在时从 DB 初始化
  - 验证方式：单测（LikeSyncServiceTest.java:71-81）
  - 结果：通过 ✓

**测试运行结果**：

```
Tests run: 41, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## 4. 术语一致性

对照方案 doc 第 0 节术语约定，grep 代码：

- **LikeMessage**：代码命中 6 处，全部一致 ✓
  - LikeMessage.java（定义）
  - LikeConsumer.java（引用）
  - LikeProducer.java（引用）
  - LikeServiceImpl.java（引用）

- **LikeProducer**：代码命中 3 处，全部一致 ✓
  - LikeProducer.java（定义）
  - LikeServiceImpl.java（引用）

- **LikeConsumer**：代码命中 2 处，全部一致 ✓
  - LikeConsumer.java（定义）

- **LikeSyncService**：代码命中 1 处，一致 ✓
  - LikeSyncService.java（定义）

- **死信队列（DLQ）**：代码命中 `like.dlq`（LikeMQConfig.java:25） ✓

**防冲突**：方案 doc 第 0 节列的术语 grep 无冲突 ✓

## 5. 架构归并

对照方案 doc 第 4 节"与项目级架构文档的关系"，逐项实际执行更新：

- [x] CLAUDE.md
  - 需要更新的内容：添加点赞异步化说明（Redis + MQ）
  - 已更新：新增"点赞异步化架构"章节 ✓

- [x] clx-post/pom.xml
  - 需要更新的内容：添加 spring-boot-starter-amqp 依赖
  - 已更新：第 96-100 行已添加 ✓

- [x] 服务端口表
  - 不需要更新：端口未变更 ✓

- [x] 模块结构
  - 不需要更新：未新建模块 ✓

- [x] Gateway 路由
  - 不需要更新：路由未变更 ✓

## 6. 遗留

**后续优化点**：
- 评论点赞异步化（下一个 feature）
- unlike 异步化（视需求决定）
- 前端乐观更新（性能瓶颈时再考虑）
- Redis 计数初始化策略（可在启动时加载热门帖子）
- 多消费者并发（高并发场景才需要）

**已知限制**：
- 服务重启后 Redis 计数可能丢失，需等待定时校准修复
- unlike 与 like 并发时可能出现短暂计数偏差，幂等检查可兜底

**实现阶段"顺手发现"列表**：
- 无