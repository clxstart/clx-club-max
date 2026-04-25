---
doc_type: feature-design
feature: 2026-04-23-aggregate-search
status: approved
summary: 企业级聚合搜索平台，ES全文搜索 + Canal/MQ同步 + 5种数据源并发查询 + 搜索热词分析
tags: [搜索, Elasticsearch, Canal, 数据同步, 爬虫, 微服务, 并发]
---

# Aggregate Search Design

> Stage 1 | 2026-04-23 | 待用户 review

---

## 0. 术语约定

**术语 grep 检查结果**（2026-04-23 执行）：
- `DataSource`: 参考项目 `yuso` 使用，CLX 无冲突 → 可复用命名
- `SearchFacade`: 参考项目 `yuso` 使用，CLX 无冲突 → 可复用命名
- `Canal`: CLX 无使用 → 新引入术语
- `Elasticsearch`: `community-homepage` 设计中已提及搜索方案 → 本次落地实现
- `SearchLog`: CLX 无使用 → 新引入术语

| 术语 | 定义 | 代码命名 |
|------|------|----------|
| 聚合搜索 | 在同一入口并发查询多个数据源，合并返回结果 | `AggregateSearch` |
| 数据源 | 搜索的数据来源抽象，统一接口规范 | `DataSource<T>` |
| 搜索门面 | 聚合搜索入口，分发请求到各数据源 | `SearchFacade` |
| 数据源注册器 | 管理所有数据源的注册和获取 | `DataSourceRegistry` |
| ES 索引 | Elasticsearch 中存储的文档集合 | `Index` |
| 数据同步 | MySQL 数据实时同步到 ES | `SyncService` |
| Canal | 监听 MySQL binlog 的数据同步工具 | `Canal` |
| 搜索热词 | 用户高频搜索关键词统计 | `HotKeyword` |
| 搜索日志 | 记录每次搜索行为用于分析 | `SearchLog` |

---

## 1. 决策与约束

### 需求摘要

**做什么**：开发企业级聚合搜索平台，用户输入关键词可同时搜索帖子、用户、分类标签、图片、视频、网页等数据源，支持搜索建议、高亮、智能分析等增强功能。

**为谁做**：已登录用户，比参考项目（鱼搜索）更强大：数据源更多、架构更解耦、搜索体验更好、有智能分析。

**怎么算成功**：
- 用户能在一个页面并发搜索 5 种数据源
- ES 全文搜索能力落地（帖子/用户/分类/标签）
- Canal + RabbitMQ 数据同步管道跑通
- 搜索热词统计和看板展示
- 响应时间 < 500ms（并发查询）
- 有单元测试覆盖核心逻辑

### 明确不做

| 不做 | 验证方式 |
|------|----------|
| AI 智能推荐 | 无 `AIRecommend` 服务调用 |
| 语义搜索（向量检索） | 无 Milvus/Qdrant 配置 |
| 付费搜索 API（Google/Bing） | 无外部付费 API 密钥配置 |
| 视频实时爬取（B站/抖音） | 视频数据源返回 mock 数据，后续扩展 |
| ES 集群部署 | 单节点 Docker 部署，无分片策略 |
| 搜索结果排序个性化 | 按相关度+热度统一排序，无用户画像 |
| 搜索付费推广 | 无广告位逻辑 |

### 关键决策

| 决策 | 选择 | 原因 | 被拒方案 |
|------|------|------|----------|
| 服务归属 | 新建 `clx-search` 独立微服务 | 聚合搜索逻辑复杂，不适合放在 clx-post | 放在 clx-post 扩展 |
| 数据同步 | Canal + RabbitMQ | 解耦、实时性高、支持多表同步 | 双写（耦合业务代码） |
| ES 客户端 | Elasticsearch Java API Client | Spring Boot 3 官方推荐 | Spring Data ES（版本兼容问题） |
| 爬虫工具 | JSoup + HttpClient | 参考项目验证可行，轻量 | Selenium（太重） |
| 并发框架 | CompletableFuture | JDK 内置，足够用 | RxJava（引入额外依赖） |
| 热词存储 | Redis 计数 + 定时入库 | 高性能计数，持久化保障 | 直接写 MySQL（性能差） |
| 网页搜索 | 必应搜索 API（免费层） | 免费、稳定 | Google API（付费） |

### 前置依赖

1. **基础设施**：
   - Elasticsearch 8.x Docker 部署
   - RabbitMQ Docker 部署
   - Canal Docker 部署

2. **代码层面**：
   - `clx-search` 服务基础框架搭建
   - ES 索引设计完成

---

## 2. 接口契约

### 2.1 聚合搜索 API

**接口**: `POST /search/aggregate`

**请求示例**:
```json
{
  "keyword": "Spring Boot",
  "types": ["post", "user", "category", "picture", "web"],
  "page": 1,
  "size": 10,
  "enableHighlight": true,
  "enableSuggest": true
}
```

**响应示例**（正常）:
```json
{
  "code": 200,
  "data": {
    "keyword": "Spring Boot",
    "totalTime": 320,
    "results": {
      "post": {
        "total": 156,
        "items": [
          {
            "id": 1001,
            "title": "<em>Spring</em> <em>Boot</em> 最佳实践",
            "summary": "总结10个常用技巧...",
            "author": {"id": 1, "name": "admin"},
            "likeCount": 128,
            "score": 0.92
          }
        ]
      },
      "user": {
        "total": 23,
        "items": [
          {
            "id": 100,
            "username": "spring_boot_fan",
            "nickname": "Spring爱好者",
            "avatar": "url"
          }
        ]
      },
      "category": {
        "total": 2,
        "items": [
          {"id": 1, "name": "技术", "postCount": 50}
        ]
      },
      "picture": {
        "total": 50,
        "items": [
          {"title": "Spring Boot架构图", "url": "https://..."}
        ]
      },
      "web": {
        "total": 10,
        "items": [
          {"title": "Spring官方文档", "url": "https://spring.io", "source": "bing"}
        ]
      }
    },
    "suggest": ["Spring Boot教程", "Spring Boot配置", "Spring Boot启动"]
  }
}
```

**响应示例**（部分数据源失败）:
```json
{
  "code": 200,
  "data": {
    "keyword": "Spring Boot",
    "totalTime": 180,
    "results": {
      "post": {...},
      "user": {...},
      "picture": {"error": "爬取失败，请稍后重试"},
      "web": {"error": "API超时"}
    },
    "partialSuccess": true
  }
}
```

> 示例来源：设计阶段定义，参考 yuso 项目 SearchController 结构

### 2.2 单类型搜索 API

**接口**: `POST /search/single`

**请求示例**:
```json
{
  "keyword": "Java",
  "type": "post",
  "page": 1,
  "size": 20,
  "filters": {
    "categoryId": 1,
    "authorId": null,
    "dateRange": null
  },
  "sort": "relevance"
}
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "total": 256,
    "items": [...],
    "aggregations": {
      "categories": [{"key": "技术", "count": 120}],
      "authors": [{"key": "admin", "count": 50}]
    }
  }
}
```

### 2.3 搜索建议 API

**接口**: `GET /search/suggest`

**请求**: `keyword=Spr&page=1&size=5`

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "suggests": [
      {"text": "Spring", "count": 1200},
      {"text": "Spring Boot", "count": 800},
      {"text": "Spring Cloud", "count": 500}
    ]
  }
}
```

### 2.4 热词统计 API

**接口**: `GET /search/hot`

**请求**: `period=day&limit=10`

**响应示例**:
```json
{
  "code": 200,
  "data": [
    {"keyword": "Java", "count": 1500, "growth": "+12%"},
    {"keyword": "Spring Boot", "count": 800, "growth": "+5%"},
    {"keyword": "面试", "count": 600, "growth": "-3%"}
  ]
}
```

### 2.5 搜索日志 API（内部）

**接口**: `POST /search/log`（内部调用，不暴露前端）

**请求示例**:
```json
{
  "keyword": "Spring Boot",
  "userId": 100,
  "types": ["post", "user"],
  "resultCount": 156,
  "costTime": 320,
  "clickResults": [1001, 1002],
  "ip": "192.168.1.1",
  "timestamp": "2026-04-23T10:00:00"
}
```

### 2.6 DataSource 统一接口

```java
// clx-search/src/main/java/com/clx/search/datasource/DataSource.java
public interface DataSource<T> {
    /**
     * 执行搜索
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 分页结果
     */
    Page<T> doSearch(String keyword, int page, int size);

    /**
     * 数据源名称
     */
    String getName();
}
```

> 示例来源：参考 yuso 项目 DataSource.java，适配 CLX 需求

---

## 3. 实现提示

### 3.1 改动计划

**新建文件（核心）**:

| 文件 | 位置 | 职责 |
|------|------|------|
| `ClxSearchApplication.java` | `clx-search/` | 搜索服务启动类 |
| `SearchController.java` | `clx-search/controller/` | API 入口 |
| `SearchFacade.java` | `clx-search/manager/` | 门面，聚合搜索逻辑 |
| `DataSourceRegistry.java` | `clx-search/datasource/` | 数据源注册器 |
| `DataSource.java` | `clx-search/datasource/` | 数据源接口 |
| `PostDataSource.java` | `clx-search/datasource/` | ES 帖子搜索 |
| `UserDataSource.java` | `clx-search/datasource/` | ES 用户搜索 |
| `CategoryDataSource.java` | `clx-search/datasource/` | ES 分类搜索 |
| `TagDataSource.java` | `clx-search/datasource/` | ES 标签搜索 |
| `PictureDataSource.java` | `clx-search/datasource/` | 必应图片爬虫 |
| `WebDataSource.java` | `clx-search/datasource/` | 必应网页搜索 |
| `SearchService.java` | `clx-search/service/` | ES 搜索服务 |
| `SyncService.java` | `clx-search/service/` | Canal MQ 消费者 |
| `HotKeywordService.java` | `clx-search/service/` | 热词统计 |
| `SearchLogService.java` | `clx-search/service/` | 搜索日志 |
| `SearchRequest.java` | `clx-search/dto/` | 搜索请求 DTO |
| `SearchVO.java` | `clx-search/vo/` | 搜索结果 VO |
| `SearchLog.java` | `clx-search/entity/` | 搜索日志实体 |
| `HotKeyword.java` | `clx-search/entity/` | 热词实体 |
| `SearchLogMapper.java` | `clx-search/mapper/` | 日志 Mapper |
| `HotKeywordMapper.java` | `clx-search/mapper/` | 热词 Mapper |
| `PostDocument.java` | `clx-search/es/` | ES 帖子文档 |
| `UserDocument.java` | `clx-search/es/` | ES 用户文档 |
| `CategoryDocument.java` | `clx-search/es/` | ES 分类文档 |
| `TagDocument.java` | `clx-search/es/` | ES 标签文档 |
| `SearchTypeEnum.java` | `clx-search/enums/` | 搜索类型枚举 |
| `ESConfig.java` | `clx-search/config/` | ES 配置 |
| `RabbitMQConfig.java` | `clx-search/config/` | MQ 配置 |
| `CanalConfig.java` | `clx-search/config/` | Canal 配置 |
| `SearchPage.tsx` | `clx-web/src/features/search/` | 搜索页面 |
| `SearchBar.tsx` | `clx-web/src/features/search/` | 搜索栏组件 |
| `SearchResultList.tsx` | `clx-web/src/features/search/` | 结果列表组件 |
| `HotKeywordPanel.tsx` | `clx-web/src/features/search/` | 热词面板 |

**新建文件（配置）**:

| 文件 | 职责 |
|------|------|
| `clx-search/pom.xml` | 服务依赖 |
| `docker/search/docker-compose.yml` | ES + Canal + RabbitMQ |
| `doc/sql/search_schema.sql` | 搜索日志、热词表 |
| `doc/es/post_index.json` | 帖子索引定义 |
| `doc/es/user_index.json` | 用户索引定义 |

**修改文件**:

| 文件 | 改动 | 理由 |
|------|------|------|
| `pom.xml` | 添加 clx-search 模块 | 新服务需要加入父工程 |
| `CLAUDE.md` | 更新服务端口和模块结构 | 文档同步 |
| `clx-web/src/routes/index.tsx` | 添加搜索路由 | 前端入口 |
| `clx-web/vite.config.ts` | 添加 /search 代理 | API 代理 |

### 3.2 推进顺序

| 步骤 | 内容 | 退出信号 |
|------|------|----------|
| 1 | **基础设施部署**：Docker 启动 ES + RabbitMQ + Canal | `curl localhost:9200` 返回 ES 信息，MQ 管理界面可访问 |
| 2 | **ES 索引设计**：创建 post/user/category/tag 索引 | Kibana 能查询到索引，mapping 正确 |
| 3 | **新建 clx-search 服务框架**：启动类 + pom + 配置 | 服务能启动，健康检查通过 |
| 4 | **实现 DataSource 接口 + Registry** | 单测验证 Registry 能获取各 DataSource |
| 5 | **实现 PostDataSource（ES 搜索）** | 能搜索 ES 帖子并返回结果 |
| 6 | **实现 UserDataSource + CategoryDataSource + TagDataSource** | 能搜索用户/分类/标签 |
| 7 | **实现 PictureDataSource + WebDataSource（爬虫）** | 能爬取必应图片和网页 |
| 8 | **实现 SearchFacade + SearchController** | API `/search/aggregate` 能并发返回结果 |
| 9 | **实现 Canal + MQ 数据同步** | MySQL 帖子变更 → ES 索引更新 |
| 10 | **实现搜索热词统计** | `/search/hot` 能返回热词列表 |
| 11 | **实现搜索日志记录** | 搜索行为入库，能查询日志 |
| 12 | **前端搜索页面** | 浏览器能看到完整搜索页面，能并发搜索 |
| 13 | **单元测试 + 验证** | 核心逻辑有测试，响应时间 < 500ms |

### 3.3 测试设计

| 功能点 | 测试约束 | 验证方式 | 用例骨架 |
|--------|----------|----------|----------|
| 聚合搜索 | 5种数据源并发查询，部分失败不影响其他 | 集成测试 | 模拟 picture 爬虫失败 → post 结果正常返回 |
| ES 帖子搜索 | 关键词匹配、高亮、分页 | 单测 + ES 集成测试 | "Java" → 返回 Java 标签帖子，高亮生效 |
| 数据同步 | Canal MQ 消费 → ES 更新 | 集成测试 | 新建帖子 → 5s 内 ES 可搜索到 |
| 爬虫稳定性 | 超时/异常不阻塞主流程 | 单测 mock | JSoup 超时 → 返回空结果，不抛异常 |
| 热词统计 | Redis 计数准确，定时入库正确 | 单测 | 连续搜索10次 → Redis count = 10 |
| 搜索日志 | 关键信息完整记录 | 集成测试 | 搜索后 → 日志表有记录 |
| 响应时间 | 并发查询 < 500ms | 性能测试 | JMeter 100 并发 → 99% < 500ms |

### 3.4 高风险实现约束

| 约束 | 说明 | 缓解措施 |
|------|------|----------|
| ES 版本兼容 | Spring Boot 3 需 ES 8.x client | 使用官方 elasticsearch-java 8.x |
| Canal binlog 格式 | MySQL 8 默认 ROW 模式，Canal 需配置 | Docker Canal 配置正确 |
| 爬虫被封 IP | 必应可能封高频爬虫 | 限制频率，失败降级返回空 |
| MQ 消息丢失 | RabbitMQ 需持久化配置 | 开启消息持久化和 ACK |

---

## 4. 与项目级架构文档的关系

### 影响到的架构文档

- `CLAUDE.md`:
  - 添加 `clx-search` 服务说明（端口 9400）
  - 更新模块结构图
  - 更新服务端口表
- `pom.xml`:
  - 添加 `<module>clx-search</module>`
- `doc/sql/`:
  - 新增 `search_schema.sql`

### 服务新增

`clx-search` 服务职责：
- 聚合搜索入口（门面模式）
- ES 全文搜索（帖子/用户/分类/标签）
- 数据源管理（注册器模式）
- 外部数据爬取（图片/网页）
- Canal + MQ 数据同步消费
- 搜索热词统计
- 搜索日志记录

端口: **9400**

### ES 索引设计

| 索引名 | 文档类型 | 关键字段 |
|--------|----------|----------|
| `clx_post` | PostDocument | title, content, authorId, categoryId, tags |
| `clx_user` | UserDocument | username, nickname, signature |
| `clx_category` | CategoryDocument | name, code, description |
| `clx_tag` | TagDocument | name, description |

### Canal 同步表

| 表 | 同步目标 | MQ Topic |
|----|----------|----------|
| `clx_post.post` | clx_post 索引 | `search.post.sync` |
| `clx_user.sys_user` | clx_user 索引 | `search.user.sync` |
| `clx_post.category` | clx_category 索引 | `search.category.sync` |
| `clx_post.tag` | clx_tag 索引 | `search.tag.sync` |

---

## 附录：参考项目对比

| 功能 | yuso（参考） | CLX（本次） | 增强 |
|------|-------------|-------------|------|
| 数据源数量 | 3（post/user/picture） | 5+（post/user/category/tag/picture/web） | 数据更丰富 |
| 数据同步 | 双写 + 定时 | Canal + MQ | 解耦、实时 |
| ES 客户端 | Spring Data ES | ES Java API Client 8.x | 版本适配 |
| 热词分析 | 无 | Redis 计数 + 看板 | 智能分析 |
| 爬虫 | 图片（必应） | 图片 + 网页（必应） | 多源爬取 |
| 并发查询 | CompletableFuture | CompletableFuture + 超时降级 | 更稳定 |
| 搜索建议 | 无 | ES Suggest | 搜索体验 |
| 高亮 | 无 | ES Highlight | 搜索体验 |