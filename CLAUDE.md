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
| clx-user | 9200 | 用户服务（暂未实现） |
| clx-gateway | 8080 | API 网关（暂未实现） |

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
├── clx-user/                      # 用户服务（暂未实现）
├── clx-gateway/                   # API 网关（暂未实现）
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

数据库：`clx_user`（用户数据）

表结构：`doc/sql/schema.sql`
初始化数据：`doc/sql/init_data.sql`

**默认账号**：
- admin / admin123
- test / test123

## 后续扩展路线图

1. **阶段 1（当前）**：最简登录（sa-Token 核心模式） ✅
2. **阶段 2**：添加 JWT 支持
3. **阶段 3**：添加权限控制（@SaCheckPermission）
4. **阶段 4**：实现 clx-user 服务
5. **阶段 5**：启用 Gateway 路由
6. **阶段 6**：启用 OAuth2 SSO（企业微信、钉钉）
7. **阶段 7**：社区功能开发

## 注意事项

1. 当前不使用 Nacos，本地开发已禁用
2. 当前不使用 Gateway，直接访问 clx-auth 9100 端口
3. 当前不使用 JWT，sa-Token 默认 UUID Token 模式
4. Gateway 使用 WebFlux，添加依赖需排除 `spring-boot-starter-web`