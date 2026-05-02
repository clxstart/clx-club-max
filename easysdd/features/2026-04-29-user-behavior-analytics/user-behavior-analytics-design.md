---
name: user-behavior-analytics
feature: 2026-04-29-user-behavior-analytics
doc_type: feature-design
status: approved
summary: 基于 Hadoop 的用户行为分析平台，采集全量用户行为日志，离线批处理后展示运营报表
tags: [hadoop, hive, analytics, behavior-log, docker, echarts]
created: 2026-04-29
---

# 用户行为分析平台设计

## 0. 术语约定

| 术语 | 含义 | 代码指针 |
|------|------|----------|
| clx-analytics | 数据分析服务（新增模块） | `clx-analytics/` 目录 |
| BehaviorLog | 用户行为日志实体 | `clx-analytics/entity/BehaviorLog.java` |
| AnalyticsReport | 分析报表实体 | `clx-analytics/entity/AnalyticsReport.java` |
| Hadoop Docker | Docker 部署的 Hadoop 伪分布式集群 | `docker/hadoop/docker-compose.yml` |
| Hive ETL | Hive 离线分析脚本 | `doc/hive/` 目录 |
| 日活 DAU | Daily Active Users，日活跃用户数 | 报表指标 |
| 留存率 | 用户在 N 天后仍活跃的比例 | 报表指标 |

## 1. 需求摘要

**目标**：管理员每天早上登录后台，通过图表查看用户活跃度、热门内容、用户留存等运营指标。

**核心行为**：
1. 用户在主站产生行为（登录、浏览帖子、点赞、评论、收藏）
2. 行为日志写入 MySQL 行为日志表
3. 每天凌晨定时任务同步日志到 HDFS
4. Hive SQL 计算各项指标
5. 结果写回 MySQL 报表库
6. 管理员在后台报表页查看图表

**分析指标**：
| 指标 | 说明 |
|------|------|
| DAU/WAU/MAU | 日/周/月活跃用户数 |
| 新增用户数 | 每日新注册用户 |
| 热门帖子 Top10 | 按浏览/点赞/评论排序 |
| 热门话题 | 哪些话题讨论最多 |
| 用户留存率 | 次日留存、7日留存、30日留存 |
| 内容统计 | 每日新增帖子/评论数 |

**明确不做**：
- 不做实时分析（Flink 场景，后续独立 feature）
- 不做推荐系统（后续独立 feature）
- 不做用户画像标签（后续独立 feature）
- 不做大数据可视化大屏（后续独立 feature）

**决策与约束**：

| 决策 | 选择 | 理由 |
|------|------|------|
| 数据同步方案 | 自定义 Shell 脚本 + SQL 导出 | 简单可控，学习成本低；DataX/Sqoop 配置复杂 |
| 定时调度 | Crontab | 轻量够用；Airflow 过重 |
| 前端图表库 | ECharts | 生态成熟，国内文档丰富 |
| Hadoop 部署 | Docker 伪分布式 | 学习目的，单机模拟集群 |
| 行为日志存储 | MySQL 行为日志表 | 复用现有基础设施；后续量大可迁移 ClickHouse |

**被拒方案**：
- ~~DataX 数据同步~~：配置复杂，学习成本高
- ~~Airflow 调度~~：过重，当前阶段不需要
- ~~实时分析~~：需要 Flink，超出范围

## 2. 系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              用户行为采集层                                   │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐           │
│  │ 登录事件 │  │ 浏览事件 │  │ 点赞事件 │  │ 评论事件 │  │ 收藏事件 │           │
│  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘  └────┬────┘           │
└───────┼────────────┼────────────┼────────────┼────────────┼──────────────────┘
        │            │            │            │            │
        ▼            ▼            ▼            ▼            ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MySQL 行为日志表                                    │
│                     behavior_log（统一行为事件表）                            │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │ 每日凌晨 02:00
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          数据同步脚本                                        │
│              export_behavior.sh → HDFS /user/clx/behavior/                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Hadoop 生态（Docker）                               │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                          HDFS                                        │   │
│  │        /user/clx/behavior/dt=2026-04-29/behavior.log                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                          Hive                                        │   │
│  │        ods_behavior_log（外部表）→ dws_user_stats（汇总表）           │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        │ Hive INSERT OVERWRITE
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          MySQL 报表库                                        │
│                     analytics_report（分析结果表）                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          clx-analytics 服务                                  │
│                     报表查询 API → clx-admin-web 展示                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 模块依赖

| 模块 | 端口 | 说明 | 新增？ |
|------|------|------|--------|
| clx-analytics | 9800 | 数据分析服务 | ✅ 新增 |
| clx-admin-web | 5174 | 后台管理前端（扩展报表页） | 已有 |
| Hadoop Docker | - | HDFS + Hive | ✅ 新增 |

### 2.3 数据流

```
Day N:
用户行为 → behavior_log 表（写入）

Day N+1 02:00:
behavior_log → SQL 导出 → HDFS

Day N+1 03:00:
Hive ETL 作业 → analytics_report 表

Day N+1 早上:
管理员访问报表页 → 查询 analytics_report
```

## 3. 接口契约

### 3.1 行为日志写入接口

**请求**
```
POST /analytics/behavior
Content-Type: application/json

{
  "userId": 12345,
  "behaviorType": "view_post",
  "targetId": 100,
  "targetType": "post",
  "extra": "{\"title\":\"帖子标题\"}"
}
```

**响应**
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

**行为类型枚举**：

| behaviorType | 说明 | targetType |
|--------------|------|------------|
| login | 登录 | - |
| view_post | 浏览帖子 | post |
| like_post | 点赞帖子 | post |
| like_comment | 点赞评论 | comment |
| comment | 发表评论 | post |
| favorite | 收藏帖子 | post |
| follow | 关注用户 | user |

### 3.2 报表查询接口

**请求**
```
GET /analytics/report/daily?date=2026-04-29
```

**响应**
```json
{
  "code": 200,
  "data": {
    "date": "2026-04-29",
    "dau": 1250,
    "wau": 5600,
    "mau": 18500,
    "newUsers": 89,
    "newPosts": 156,
    "newComments": 423,
    "retention1d": 0.45,
    "retention7d": 0.22,
    "retention30d": 0.08
  }
}
```

### 3.3 热门内容接口

**请求**
```
GET /analytics/report/hot-posts?date=2026-04-29&type=view&limit=10
```

**响应**
```json
{
  "code": 200,
  "data": [
    {
      "postId": 1001,
      "title": "Java 并发编程实战",
      "authorName": "张三",
      "viewCount": 5620,
      "likeCount": 342,
      "commentCount": 89
    }
  ]
}
```

### 3.4 趋势数据接口

**请求**
```
GET /analytics/report/trend?startDate=2026-04-01&endDate=2026-04-29&metric=dau
```

**响应**
```json
{
  "code": 200,
  "data": {
    "dates": ["2026-04-01", "2026-04-02", "..."],
    "values": [1200, 1350, 1280, "..."]
  }
}
```

## 4. 实现提示

### 4.1 改动点

**新建文件**：

| 文件 | 说明 |
|------|------|
| `clx-analytics/pom.xml` | Maven 模块配置 |
| `clx-analytics/src/main/java/.../ClxAnalyticsApplication.java` | 启动类 |
| `clx-analytics/src/main/java/.../entity/BehaviorLog.java` | 行为日志实体 |
| `clx-analytics/src/main/java/.../entity/AnalyticsReport.java` | 报表实体 |
| `clx-analytics/src/main/java/.../mapper/BehaviorLogMapper.java` | 行为日志 Mapper |
| `clx-analytics/src/main/java/.../mapper/AnalyticsReportMapper.java` | 报表 Mapper |
| `clx-analytics/src/main/java/.../service/BehaviorLogService.java` | 行为日志服务 |
| `clx-analytics/src/main/java/.../service/AnalyticsService.java` | 报表查询服务 |
| `clx-analytics/src/main/java/.../controller/BehaviorLogController.java` | 行为日志接口 |
| `clx-analytics/src/main/java/.../controller/AnalyticsController.java` | 报表查询接口 |
| `clx-analytics/src/main/resources/application.yml` | 配置文件 |
| `doc/sql/analytics_schema.sql` | 数据库表结构 |
| `docker/hadoop/docker-compose.yml` | Hadoop Docker 配置 |
| `doc/hive/create_tables.hql` | Hive 建表语句 |
| `doc/hive/daily_etl.hql` | 每日 ETL 脚本 |
| `doc/scripts/export_behavior.sh` | 数据导出脚本 |
| `doc/scripts/run_etl.sh` | ETL 执行脚本 |
| `clx-admin-web/src/views/analytics/AnalyticsPage.vue` | 报表页面 |
| `clx-admin-web/src/api/analytics.ts` | 报表 API |

**修改文件**：

| 文件 | 改动 |
|------|------|
| `pom.xml`（根目录） | 添加 clx-analytics 模块 |
| `clx-admin-web/src/router/index.ts` | 添加报表页路由 |
| `clx-admin-web/src/layouts/AdminLayout.vue` | 添加报表菜单项 |
| `CLAUDE.md` | 更新服务端口和模块说明 |

### 4.2 数据库表设计

**behavior_log 行为日志表**：

```sql
CREATE TABLE `behavior_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `behavior_type` varchar(30) NOT NULL COMMENT '行为类型',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID',
  `target_type` varchar(20) DEFAULT NULL COMMENT '目标类型',
  `extra` json DEFAULT NULL COMMENT '扩展信息',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_behavior_type` (`behavior_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为日志表';
```

**analytics_report 分析报表表**：

```sql
CREATE TABLE `analytics_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `report_date` date NOT NULL COMMENT '报表日期',
  `report_type` varchar(20) NOT NULL COMMENT '报表类型:daily/hot_posts/trend',
  `metric_name` varchar(50) NOT NULL COMMENT '指标名称',
  `metric_value` decimal(20,4) DEFAULT NULL COMMENT '指标值',
  `dimension` varchar(100) DEFAULT NULL COMMENT '维度(JSON)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_type_metric` (`report_date`, `report_type`, `metric_name`),
  KEY `idx_report_date` (`report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析报表表';
```

### 4.3 Hive 表设计

**ods_behavior_log（ODS 层外部表）**：

```sql
CREATE EXTERNAL TABLE ods_behavior_log (
  user_id BIGINT,
  behavior_type STRING,
  target_id BIGINT,
  target_type STRING,
  extra STRING,
  ip STRING,
  create_time STRING
)
PARTITIONED BY (dt STRING)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
STORED AS TEXTFILE
LOCATION '/user/clx/behavior';
```

**dws_user_daily_stats（DWS 层汇总表）**：

```sql
CREATE TABLE dws_user_daily_stats (
  stat_date STRING,
  dau BIGINT,
  new_users BIGINT,
  retention_1d DOUBLE,
  retention_7d DOUBLE,
  retention_30d DOUBLE
)
STORED AS ORC;
```

### 4.4 推进顺序

1. **创建数据库表** → verify: 表创建成功，可插入测试数据
2. **搭建 Hadoop Docker 环境** → verify: HDFS 可访问，Hive 可执行 SQL
3. **新建 clx-analytics 模块** → verify: 服务启动成功，健康检查通过
4. **实现行为日志写入接口** → verify: 接口可调用，数据写入数据库
5. **实现数据同步脚本** → verify: 脚本可导出数据到 HDFS
6. **实现 Hive ETL 脚本** → verify: Hive 可计算出指标并写入报表表
7. **配置定时任务** → verify: 每日凌晨自动执行 ETL
8. **实现报表查询接口** → verify: API 返回正确数据
9. **新建前端报表页面** → verify: 页面正常展示图表
10. **端到端验证** → verify: 完整流程可跑通

### 4.5 测试设计

| 功能点 | 测试约束 | 验证方式 |
|--------|----------|----------|
| 行为日志写入 | 接口返回 200，数据入库 | API 测试 |
| 数据同步 | HDFS 文件存在且内容正确 | Shell 脚本测试 |
| Hive ETL | 报表表有正确数据 | Hive 查询验证 |
| 报表查询 | API 返回正确指标 | API 测试 |
| 前端展示 | 图表正确渲染 | 浏览器验证 |
| 定时任务 | 每日凌晨自动执行 | Crontab 日志 |

### 4.6 风险与边界

| 风险 | 缓解措施 |
|------|----------|
| Hadoop Docker 内存占用大 | 限制容器内存，伪分布式模式 |
| 数据同步失败 | 脚本添加重试逻辑，失败发送告警 |
| Hive 作业失败 | 添加错误处理，记录失败日志 |
| 行为日志表数据量大 | 按月分表，定期归档历史数据 |

## 5. 前端 UI 设计

### 5.1 报表页面布局

```
┌────────────────────────────────────────────────────────────────────┐
│  数据报表                              日期选择: [2026-04-29] 📅    │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │  日活    │  │  周活    │  │  月活    │  │ 新增用户 │          │
│  │  1,250   │  │  5,600   │  │ 18,500   │  │    89    │          │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘          │
│                                                                    │
│  ┌─────────────────────────────────────────────────────────────┐  │
│  │                     用户活跃趋势（折线图）                    │  │
│  │       📈                                                      │  │
│  │  2000 ─┬────────────────────────────────────────────         │  │
│  │       │            ╱╲                                        │  │
│  │  1500 ─┤          ╱  ╲    ╱╲                                 │  │
│  │       │        ╱╲╱    ╲╱  ╲                                  │  │
│  │  1000 ─┤      ╱              ╲                               │  │
│  │       │    ╱╲                  ╲                             │  │
│  │   500 ─┤──╱──────────────────────────────                    │  │
│  │       └────────────────────────────────────                  │  │
│  │         04-01 04-05 04-10 04-15 04-20 04-25 04-29           │  │
│  └─────────────────────────────────────────────────────────────┘  │
│                                                                    │
│  ┌────────────────────────┐  ┌────────────────────────────────┐  │
│  │    热门帖子 Top10      │  │      用户留存率                │  │
│  │ ─────────────────────  │  │ ──────────────────────────     │  │
│  │ 1. Java并发编程实战    │  │  次日留存: 45%  ████████░░    │  │
│  │ 2. Spring Boot教程    │  │  7日留存:  22%  ████░░░░░░    │  │
│  │ 3. Redis实战指南      │  │  30日留存: 8%   █░░░░░░░░░    │  │
│  │ ...                   │  │                                │  │
│  └────────────────────────┘  └────────────────────────────────┘  │
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

## 6. 与项目级架构文档的关系

### 需要更新的文档

| 文档 | 更新内容 |
|------|----------|
| `CLAUDE.md` | 新增 clx-analytics 服务端口（9800），新增模块说明 |
| `doc/sql/schema.sql` | 引用新增的 analytics_schema.sql |

### 新增的组件

- **clx-analytics**：数据分析服务，提供行为日志采集和报表查询能力
- **docker/hadoop/**：Hadoop Docker 部署配置
- **doc/hive/**：Hive ETL 脚本
