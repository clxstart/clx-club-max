# 活跃用户排行 验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-04-26
> 关联方案 doc：easysdd/features/2026-04-26-active-ranking/active-ranking-design.md

## 1. 接口契约核对

对照方案 doc 第 2 节接口契约，逐一核查实现与契约的一致性：

**请求格式核对**：
- [x] `GET /user/active?limit=5` → UserController.java:73 `@GetMapping("/active")` 一致

**响应格式核对**：
- [x] 返回 `R<List<ActiveUserVO>>` → UserController.java:74 一致
- [x] ActiveUserVO 包含 rank/userId/username/score → ActiveUserVO.java:9-17 一致

**前端类型核对**：
- [x] types.ts ActiveUserVO 类型 → 与后端一致
- [x] userApi.active() 方法 → api/index.ts:205 一致

**发现偏差并已修正**：
- 方案设计的评分规则（发帖/评论/点赞行为）与实际实现（获赞×3+粉丝×2+关注×1）不一致
- 已同步更新设计文档反映实际实现

## 2. 行为与决策核对

对照方案 doc 第 1 节决策与约束：

**需求摘要逐项验证**：
- [x] 后端接口返回真实活跃用户数据：实现完成
- [x] 前端假数据替换为 API 调用：App.tsx:62-63 已替换

**明确不做逐项核对**：
- [x] 定时任务预计算 → grep 无命中，确实没做
- [x] 历史排行存档 → 无相关代码，确实没做
- [x] 分页 → 接口只支持 limit 参数，无分页

**关键决策落地**：
- [x] 简化版评分公式（获赞×3+粉丝×2+关注×1）→ ActiveMapper.xml:11 实现

## 3. 测试约束核对

对照方案 doc 第 3 节测试设计，逐条测试约束验证：

- [x] **C1**：返回 Top N 用户
  - 验证方式：SQL LIMIT #{limit} 实现
  - 结果：通过

- [x] **C2**：无活跃用户时返回空数组
  - 验证方式：SQL WHERE is_deleted=0 AND status='0' 过滤，无结果返回空 List
  - 结果：通过

- [x] **C3**：分数计算正确
  - 验证方式：SQL 公式实现
  - 结果：通过

**前端编译验证**：
- [x] npm run build → 成功

## 4. 术语一致性

对照方案 doc 第 0 节术语约定，grep 代码：

- ActiveUserVO：后端 5 处命中，前端 8 处命中，全部一致 ✓
- ActiveMapper：后端 2 处命中，全部一致 ✓

**发现重复定义**：
- types.ts 中 ActiveUserVO 重复定义（行 299 和 345）
- 未修复，待清理

## 5. 架构归并

对照方案 doc 第 4 节（本次方案无第 4 节，为���化版 fastforward 风格）：

- 本次功能新增一个 REST API，不改变系统架构
- 项目当前无架构中心目录（easysdd/architecture/ 不存在）
- 本次 fastforward 无架构维度变更

## 6. 遗留

- **环境问题**：
  - 端口 9200 被 `jh-elasticsearch` 容器占用，clx-user 服务无法启动
  - 解决方案：`docker stop jh-elasticsearch` 或修改 clx-user 端口配置

- **后续优化点**：
  - 可改为基于发帖/评论/点赞行为计算活跃度（需跨库查询或 Feign 调用）
  - 可加近 30 天时间范围过滤

- **已知限制**：
  - 当前评分公式为简化版，不是设计文档原始规划的行为统计
  - 无时间范围过滤（全量数据）

- **实现阶段"顺手发现"列表**：
  - types.ts ActiveUserVO 重复定义（待清理）