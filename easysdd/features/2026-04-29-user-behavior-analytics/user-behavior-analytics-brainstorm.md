---
doc_type: feature-brainstorm
feature: 2026-04-29-user-behavior-analytics
status: confirmed
summary: 基于 Hadoop 的用户行为分析平台，采集全量用户行为日志，离线批处理后展示运营报表
tags: [hadoop, hive, analytics, behavior-log, docker]
---

# 用户行为分析平台 Brainstorm

> Stage 0 | 2026-04-29 | 下一步：design

## 想做什么、为什么

用户想学习 Hadoop 大数据技术栈，同时给 CLX 社区平台增加运营数据分析能力。最终落地方案是：管理员每天早上登录后台，通过图表查看用户活跃度、热门内容、用户留存等运营指标。

探索过程中确认了几个关键点：
- 目标用户是管理员，场景是后台报表页
- 指标全覆盖：日活/周活/月活、热门帖子 Top10、热门话题、用户留存率、内容统计
- 数据来源需要全量采集用户行为（登录、浏览、点赞、评论、收藏等）
- 本地环境不确定能否跑 Hadoop，采用 Docker 部署伪分布式模式

## 考虑过的方向

### 方向 A：Hadoop + Hive（选定）
- 描述：用 HDFS 存储原始日志，Hive 做 SQL 分析，定时调度每天凌晨跑批
- 价值：学习完整 Hadoop 生态，离线批处理是大数据经典场景
- 代价：需要 Docker 部署 Hadoop 集群，组件较多（HDFS、YARN、Hive）
- 结论：**选定**，技术学习优先

### 方向 B：ClickHouse
- 描述：用 ClickHouse 做 OLAP 分析，单机可跑，查询秒级返回
- 价值：更轻量，适合中小规模数据，实时性更好
- 代价：学不到 Hadoop 生态
- 结论：否决，用户明确想学 Hadoop

### 方向 C：MySQL + 定时任务
- 描述：直接在 MySQL 里做聚合统计，定时任务跑批
- 价值：最简单，无额外组件
- 代价：数据量大后性能差，学不到新技术
- 结论：否决，不符合学习目标

## 选定方向与遗留问题

**选定方向**：搭建完整的 Hadoop 离线数据处理管道，从日志采集到报表展示。用户行为日志写入 MySQL，通过 DataX 或脚本同步到 HDFS，Hive 做离线分析，结果写回 MySQL 供后台查询展示。

**核心行为**：
1. 用户在主站产生行为（登录、浏览帖子、点赞、评论、收藏）
2. 行为日志写入 MySQL 各业务表
3. 每天凌晨定时任务同步日志到 HDFS
4. Hive SQL 计算各项指标
5. 结果写回 MySQL 报表库
6. 管理员在后台查看图表

**明显不做**：
- 实时分析（Flink 场景）
- 推荐系统（后续独立 feature）
- 用户画像标签（后续独立 feature）

**最大未知**：
- Docker 部署 Hadoop 伪分布式模式的配置复杂度
- Hive 与 MySQL 的数据同步方案选择

**遗留给 design 的问题**：
1. 新增哪些行为日志表？字段如何设计？
2. 日志同步方案：DataX / Sqoop / 自定义脚本？
3. Hive 表结构如何设计？分区策略？
4. 定时调度用什么？Crontab / Airflow？
5. 后台报表页的前端图表库选型？
