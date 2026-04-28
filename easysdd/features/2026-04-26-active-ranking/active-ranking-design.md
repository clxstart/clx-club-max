---
name: active-ranking
feature: active-ranking
doc_type: feature-design
status: completed
summary: 实现月度活跃用户排行接口，替换前端假数据
tags: [user, ranking, api]
created: 2026-04-26
completed: 2026-04-26
---

# 活跃用户排行接口设计

## 0. 术语约定

| 术语 | 含义 | 代码指针 |
|------|------|----------|
| ActiveUserVO | 活跃用户排行返回结构 | `clx-user/.../vo/ActiveUserVO.java` |
| ActiveMapper | 活跃度聚合查询 Mapper | `clx-user/.../mapper/ActiveMapper.java` |
| 活跃度分数 | 获赞×3 + 粉丝×2 + 关注×1 | `ActiveMapper.xml` |

## 1. 需求摘要

前端 `ActiveRank` 组件显示月度活跃排行榜，目前使用假数据。需要实现后端接口返回真实活跃用户数据。

**明确不做**：
- 不实现定时任务预计算（后续优化）
- 不实现历史排行存档
- 不实现分页（Top N 即可）

## 2. 接口契约

### 请求
```
GET /user/active?limit=5
```

### 响应
```json
{
  "code": 200,
  "data": [
    { "rank": 1, "userId": 1, "username": "admin", "score": 1185 },
    { "rank": 2, "userId": 2, "username": "test", "score": 457 }
  ]
}
```

### 类型定义
```java
@Data
public class ActiveUserVO {
    private Integer rank;      // 排名
    private Long userId;       // 用户ID
    private String username;   // 用户名
    private Integer score;     // 活跃度分数
}
```

## 3. 实现提示

### 改动点
| 文件 | 改动 |
|------|------|
| `clx-user/.../vo/ActiveUserVO.java` | 新增返回 VO |
| `clx-user/.../mapper/ActiveMapper.java` | 新增聚合查询接口 |
| `clx-user/.../mapper/ActiveMapper.xml` | SQL 实现 |
| `clx-user/.../service/UserService.java` | 新增 `getActiveUsers()` 方法 |
| `clx-user/.../service/impl/UserServiceImpl.java` | 实现业务逻辑 |
| `clx-user/.../controller/UserController.java` | 新增 REST 接口 |
| `clx-web/src/App.tsx` | 替换假数据为 API 调用 |

### 活跃度评分规则
**当前实现（简化版）**：基于用户表静态字段计算
| 字段 | 权重 |
|------|------|
| 获赞数 | × 3 |
| 粉丝数 | × 2 |
| 关注数 | × 1 |

**后续优化**：可改为基于发帖/评论/点赞行为计算，需要跨库查询或 Feign 调用。

| 行为 | 分数 |
|------|------|
| 发帖 | +10 |
| 评论 | +5 |
| 点赞他人 | +2 |
| 被点赞 | +3 |

**时间范围**：当前无时间限制（全量），后续可加近 30 天过滤。

### 推进顺序
1. 新建 `ActiveUserVO.java`
2. 新建 `ActiveMapper.java` + XML
3. 修改 `UserService.java` 添加方法签名
4. 修改 `UserServiceImpl.java` 实现逻辑
5. 修改 `UserController.java` 暴露接口
6. 修改前端 `App.tsx` 调用真实接口

### 退出信号
- 接口返回正确的活跃用户列表
- 前端页面正常显示排行榜

### 测试设计
| 功能点 | 测试约束 | 验证方式 | 用例骨架 |
|--------|----------|----------|----------|
| 正常返回 | 返回 Top N 用户 | 接口调用验证 | `GET /user/active?limit=5` 返回 5 条数据 |
| 空数据处理 | 无活跃用户时返回空数组 | 边界测试 | 清空数据后调用返回 `[]` |
| 分数计算 | 分数 = 获赞×3 + 粉丝×2 + 关注×1 | 数据验证 | 手造数据验证分数正确 |
