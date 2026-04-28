---
name: admin-user-management
feature: admin-user-management
doc_type: feature-design
status: approved
summary: 实现独立后台管理系统，包括 clx-admin 后端服务和 clx-admin-web 前端项目，支持用户管理、角色分配、操作日志记录
tags: [admin, user, management, rbac, audit, microservice]
created: 2026-04-28
---

# 用户管理后台功能设计（独立服务 + 独立前端）

## 0. 术语约定

| 术语 | 含义 | 代码指针 |
|------|------|----------|
| clx-admin | 后台管理后端服务 | `clx-admin/` 目录 |
| clx-admin-web | 后台管理前端项目 | `clx-admin-web/` 目录 |
| AdminUserController | 用户管理控制器 | `clx-admin/.../controller/UserController.java` |
| OperLogService | 操作日志服务 | `clx-admin/.../service/OperLogService.java` |
| 管理员 | 拥有 `admin` 角色的用户 | `sys_user_role` 表，`role_id=1` |
| 操作日志 | 管理员操作行为记录 | `sys_oper_log` 表（clx_auth 数据库） |

## 1. 需求摘要

实现**独立后台管理系统**：
- **独立后端服务**：`clx-admin`（端口 9700）
- **独立前端项目**：`clx-admin-web`（端口 5174）
- 用户列表分页、搜索、状态筛选
- 封禁/解封用户
- 修改用户资料
- 角色分配（单用户多角色）
- 操作日志记录

**明确不做**：
- 不实现批量操作
- 不实现密码重置（后续独立 feature）
- 不实现日志查询界面（只记录，后续独立 feature）
- 不实现数据统计仪表盘（后续独立 feature）

**决策与约束**：
- 后端独立为 `clx-admin` 微服务，复用 `clx-common` 公共模块
- 前端独立为 `clx-admin-web` 项目，采用 Vue 3 + Element Plus（后台管理标准技术栈）
- 跨服务数据访问：通过 Feign 调用 clx-user / clx-auth 服务
- 权限校验使用 sa-Token `@SaCheckRole("admin")`
- 与主站共享登录态：sa-Token 存 Redis，两套前端共享同一个 token

**被拒方案**：
- ~~嵌入现有 clx-user 服务~~：后台管理职责独立，便于后续扩展
- ~~嵌入现有 clx-web 前端~~：后台管理交互复杂，独立项目更灵活
- ~~React + Ant Design~~：Vue + Element Plus 是后台管理主流选择，生态成熟

## 2. 系统架构

### 2.1 整体架构

```
                    ┌─────────────────────────────────────────┐
                    │              Redis (Token)              │
                    └─────────────────────────────────────────┘
                                       ▲
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
              ┌─────┴─────┐      ┌─────┴─────┐      ┌─────┴─────┐
              │  clx-web  │      │clx-admin-web│    │   ...     │
              │  :5173    │      │   :5174    │      │           │
              └─────┬─────┘      └─────┬─────┘      └───────────┘
                    │                  │
                    ▼                  ▼
              ┌───────────┐      ┌───────────┐
              │ clx-auth  │◄────►│ clx-admin │
              │  :9100    │      │  :9700    │
              └─────┬─────┘      └─────┬─────┘
                    │                  │
                    ▼                  │ Feign
              ┌───────────┐            │
              │ clx-user  │◄───────────┘
              │  :9200    │
              └───────────┘
```

### 2.2 服务依赖

| 服务 | 端口 | 依赖 | 说明 |
|------|------|------|------|
| clx-admin | 9700 | clx-user, clx-auth | 后台管理服务 |
| clx-admin-web | 5174 | clx-admin | 后台管理前端 |

### 2.3 数据访问策略

| 数据 | 访问方式 | 原因 |
|------|----------|------|
| 用户数据 | Feign 调用 clx-user | 用户服务是数据Owner |
| 角色数据 | Feign 调用 clx-auth | 认证服务管理角色权限 |
| 操作日志 | 直连 clx_auth 数据库 | 写入频繁，不走 Feign |
| Token 验证 | sa-Token Redis 共享 | 共享登录态 |

## 3. 接口契约

### 3.1 用户列表查询

**请求**
```
GET /admin/user/page?page=1&size=10&username=test&userId=1&status=0
```

**响应**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "userId": 1,
        "username": "admin",
        "nickname": "超级管理员",
        "email": "admin@clx.com",
        "phone": "13800000000",
        "status": "0",
        "roles": ["admin"],
        "createTime": "2026-04-01 10:00:00",
        "lastLoginTime": "2026-04-28 09:30:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

### 3.2 封禁用户

**请求**
```
PUT /admin/user/{userId}/ban
```

**响应**
```json
{
  "code": 200,
  "msg": "封禁成功"
}
```

### 3.3 解封用户

**请求**
```
PUT /admin/user/{userId}/unban
```

### 3.4 修改用户资料

**请求**
```
PUT /admin/user/{userId}
Content-Type: application/json

{
  "nickname": "新昵称",
  "email": "new@email.com",
  "phone": "13900000000",
  "signature": "新签名"
}
```

### 3.5 获取用户详情

**请求**
```
GET /admin/user/{userId}
```

### 3.6 获取角色列表

**请求**
```
GET /admin/role/list
```

**响应**
```json
{
  "code": 200,
  "data": [
    { "roleId": 1, "roleName": "超级管理员", "roleCode": "admin" },
    { "roleId": 2, "roleName": "普通用户", "roleCode": "user" },
    { "roleId": 3, "roleName": "访客", "roleCode": "guest" }
  ]
}
```

### 3.7 更新用户角色

**请求**
```
PUT /admin/user/{userId}/roles
Content-Type: application/json

{
  "roleIds": [2, 3]
}
```

### 3.8 获取当前用户信息（含角色）

**请求**
```
GET /admin/me
```

**响应**
```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "admin",
    "nickname": "超级管理员",
    "roles": ["admin"]
  }
}
```

## 4. 实现提示

### 4.1 后端模块结构

```
clx-admin/
├── pom.xml
└── src/main/java/com/clx/admin/
    ├── ClxAdminApplication.java      # 启动类
    ├── config/
    │   └── SaTokenConfig.java        # sa-Token 配置（共享 Redis）
    ├── controller/
    │   ├── UserController.java       # 用户管理
    │   └── RoleController.java       # 角色管理
    ├── service/
    │   ├── UserService.java          # 用户服务（Feign 调用）
    │   ├── RoleService.java          # 角色服务
    │   └── OperLogService.java       # 操作日志服务
    ├── service/impl/
    │   └── OperLogServiceImpl.java   # 日志实现（直连 DB）
    ├── feign/
    │   ├── UserFeignClient.java      # 调用 clx-user
    │   └── AuthFeignClient.java      # 调用 clx-auth
    ├── mapper/
    │   └── OperLogMapper.java        # 操作日志 Mapper
    ├── dto/
    │   ├── UserQueryDTO.java
    │   └── UserUpdateDTO.java
    └── vo/
        ├── UserPageVO.java
        └── RoleVO.java
```

### 4.2 前端项目结构

```
clx-admin-web/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── index.html
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/
    │   ├── index.ts                  # API 封装
    │   └── types.ts                  # 类型定义
    ├── stores/
    │   └── user.ts                   # 用户状态（Pinia）
    ├── router/
    │   └── index.ts                  # 路由配置
    ├── layouts/
    │   └── AdminLayout.vue           # 后台布局（侧边栏+顶栏+内容区）
    ├── views/
    │   ├── login/
    │   │   └── LoginPage.vue         # 登录页
    │   ├── dashboard/
    │   │   └── DashboardPage.vue     # 首页仪表盘（预留）
    │   └── user/
    │       ├── UserListPage.vue      # 用户列表页
    │       └── UserEditDialog.vue    # 用户编辑弹窗
    └── styles/
        └── index.scss                # 全局样式
```

### 4.3 改动点

**后端新建**：

| 文件 | 说明 |
|------|------|
| `clx-admin/pom.xml` | Maven 模块配置 |
| `clx-admin/src/main/java/.../ClxAdminApplication.java` | 启动类 |
| `clx-admin/src/main/java/.../config/SaTokenConfig.java` | sa-Token 配置 |
| `clx-admin/src/main/java/.../controller/UserController.java` | 用户管理接口 |
| `clx-admin/src/main/java/.../controller/RoleController.java` | 角色管理接口 |
| `clx-admin/src/main/java/.../service/UserService.java` | 用户服务 |
| `clx-admin/src/main/java/.../service/RoleService.java` | 角色服务 |
| `clx-admin/src/main/java/.../service/OperLogService.java` | 操作日志服务 |
| `clx-admin/src/main/java/.../feign/UserFeignClient.java` | Feign 客户端 |
| `clx-admin/src/main/java/.../feign/AuthFeignClient.java` | Feign 客户端 |
| `clx-admin/src/main/java/.../mapper/OperLogMapper.java` | 日志 Mapper |
| `clx-admin/src/main/java/.../dto/*.java` | DTO 类 |
| `clx-admin/src/main/java/.../vo/*.java` | VO 类 |
| `clx-admin/src/main/resources/application.yml` | 配置文件 |

**后端修改**：

| 文件 | 改动 |
|------|------|
| `pom.xml`（根目录） | 添加 clx-admin 模块 |
| `clx-api/` | 新增 Feign 接口定义（供 clx-admin 调用） |
| `clx-user/.../controller/UserInternalController.java` | 新增内部接口供 Feign 调用 |
| `clx-auth/.../controller/AuthInternalController.java` | 新增内部接口供 Feign 调用 |

**前端新建**：

| 文件 | 说明 |
|------|------|
| `clx-admin-web/package.json` | 项目配置 |
| `clx-admin-web/vite.config.ts` | Vite 配置 |
| `clx-admin-web/src/main.ts` | 入口文件 |
| `clx-admin-web/src/App.vue` | 根组件 |
| `clx-admin-web/src/api/index.ts` | API 封装 |
| `clx-admin-web/src/api/types.ts` | 类型定义 |
| `clx-admin-web/src/stores/user.ts` | Pinia 状态 |
| `clx-admin-web/src/router/index.ts` | 路由配置 |
| `clx-admin-web/src/layouts/AdminLayout.vue` | 布局组件 |
| `clx-admin-web/src/views/login/LoginPage.vue` | 登录页 |
| `clx-admin-web/src/views/user/UserListPage.vue` | 用户列表 |
| `clx-admin-web/src/views/user/UserEditDialog.vue` | 编辑弹窗 |

### 4.4 推进顺序

1. **后端基础框架** → verify: 服务启动成功
   - 新建 `clx-admin/` 模块目录结构
   - 配置 `pom.xml` 依赖
   - 创建启动类和配置文件

2. **Feign 客户端准备** → verify: 编译通过
   - 新建 `clx-api` 中的 Feign 接口定义
   - clx-user / clx-auth 新增内部接口

3. **后端 Mapper 层** → verify: 单元测试通过
   - 新建 `OperLogMapper.java`

4. **后端 Service 层** → verify: 单元测试通过
   - 新建各 Service 接口和实现

5. **后端 Controller 层** → verify: 接口测试通过
   - 新建 `UserController.java`
   - 新建 `RoleController.java`

6. **前端项目初始化** → verify: 项目启动成功
   - 新建 `clx-admin-web/` 项目
   - 配置 Vite + Vue 3 + Element Plus + Pinia

7. **前端登录页** → verify: 登录流程完整
   - 新建 `LoginPage.vue`
   - 实现 Token 存储（共享 Redis）

8. **前端布局组件** → verify: 布局渲染正常
   - 新建 `AdminLayout.vue`

9. **前端用户管理页** → verify: CRUD 功能完整
   - 新建 `UserListPage.vue`
   - 新建 `UserEditDialog.vue`

10. **端到端验证** → verify: 完整流程可操作
    - 主站登录 admin → 后台管理系统自动登录
    - 用户管理功能完整

### 4.5 操作日志记录

| 操作类型 | 触发接口 | 记录内容 |
|----------|----------|----------|
| 用户查询 | GET /admin/user/page | 记录查询条件 |
| 封禁用户 | PUT /admin/user/{id}/ban | 记录目标用户ID |
| 解封用户 | PUT /admin/user/{id}/unban | 记录目标用户ID |
| 编辑资料 | PUT /admin/user/{id} | 记录修改字段 |
| 角色变更 | PUT /admin/user/{id}/roles | 记录新角色列表 |

### 4.6 测试设计

| 功能点 | 测试约束 | 验证方式 |
|--------|----------|----------|
| 用户列表分页 | 返回正确数据 | 接口测试 |
| 状态筛选 | 筛选正确 | 接口测试 |
| 封禁/解封 | 状态变更，日志记录 | 接口测试 |
| 角色分配 | 角色更新，日志记录 | 接口测试 |
| 权限校验 | 非管理员返回 403 | 接口测试 |
| Token 共享 | 主站登录后可访问后台 | 端到端测试 |
| 禁止封禁自己 | 返回 400 | 接口测试 |
| 禁止修改自己角色 | 返回 400 | 接口测试 |

### 4.7 风险与边界

| 风险 | 缓解措施 |
|------|----------|
| Feign 调用超时 | 配置合理超时时间，添加熔断 |
| Token 跨域问题 | 两个前端项目共享 Redis Token |
| 用户在后台修改自己导致权限丢失 | 后端校验禁止操作自己 |
| clx-user/clx-auth 服务不可用 | Feign 熔断降级 |

## 5. 前端 UI 设计

### 5.1 登录页

- 简洁登录表单
- 用户名 + 密码
- 登录成功后 Token 存 localStorage
- 自动跳转到用户管理页

### 5.2 主布局（Element Plus 风格）

```
┌────────────────────────────────────────────────────────────────┐
│  CLX 后台管理系统                              管理员 ▼ | 退出  │
├────────────┬───────────────────────────────────────────────────┤
│            │                                                   │
│  📊 首页   │   用户管理                                        │
│            │   ─────────────────────────────────────────────   │
│  👥 用户   │   搜索: [用户名] [状态 ▼] [查询] [重置]          │
│            │   ─────────────────────────────────────────────   │
│  📝 内容   │   用户名 │ 昵称 │ 邮箱 │ 状态 │ 角色 │ 操作      │
│            │   ─────────────────────────────────────────────   │
│  ⚙️ 系统   │   admin │ 超级管理员 │ ... │ 正常 │ admin │ [编辑]│
│            │   test  │ 测试用户   │ ... │ 正常 │ user  │ [编辑]│
│            │   ─────────────────────────────────────────────   │
│            │   < 1 2 3 ... >                                  │
└────────────┴───────────────────────────────────────────────────┘
```

### 5.3 编辑弹窗

- Element Plus Dialog
- 表单：昵称、邮箱、手机号、签名
- 角色多选：Checkbox Group
- 保存/取消按钮

## 6. 技术选型

### 6.1 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 与主站一致 |
| sa-Token | 1.39.0 | 共享 Redis Token |
| OpenFeign | - | 服务间调用 |
| MyBatis-Plus | 3.0.4 | 日志表操作 |

### 6.2 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4+ | 主流后台管理框架 |
| Element Plus | 2.6+ | 后台 UI 组件库 |
| Pinia | 2.1+ | 状态管理 |
| Vue Router | 4.x | 路由 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Axios | 1.x | HTTP 客户端 |
