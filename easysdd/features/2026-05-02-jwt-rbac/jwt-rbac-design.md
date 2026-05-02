---
doc_type: feature-design
feature: 2026-05-02-jwt-rbac
status: approved
summary: JWT Token 模式（含权限信息）+ RBAC 权限控制完善，支持旧 Token 过渡期兼容
tags: [auth, jwt, rbac, security]
---

# JWT 与 RBAC 权限完善 Design

> Stage 1 | 2026-05-02 | 下一步：用户 review

---

## 需求摘要

本功能完善认证授权体系，包含两个核心目标：

1. **JWT Token 模式**：替换当前 UUID Token，JWT Payload 包含 userId + 角色信息
2. **RBAC 权限控制**：完善全平台接口的权限校验

**核心用户行为**：
1. 用户登录后获得 JWT Token，Payload 包含 userId 和角色列表
2. 请求接口时，后端从 JWT 解析用户身份和角色，进行权限校验
3. 活跃用户不被踢下线（滑动过期）
4. 过渡期内旧 UUID Token 仍可使用

**明确不做**：
- OAuth2 第三方登录扩展（已有 separate feature）
- 细粒度资源权限（数据行级权限）
- 权限缓存优化（Redis 缓存权限列表，后续优化）
- 前端权限菜单动态渲染（后续 feature）
- 网关层统一鉴权（Gateway 未实现）

---

## 决策与约束

### 本 feature 放在 clx-common-security + clx-auth 模块里

原因：
- JWT 配置在 `clx-common-security`，影响所有服务
- 权限数据查询在 `clx-auth`，已有 AuthStpInterfaceImpl
- 权限注解分散在各服务 Controller

### 技术选型

| 决策 | 选型 | 原因 |
|------|------|------|
| JWT 模式 | Simple 模式 | 已配置，Token 是 JWT 格式但会话仍存 Redis，支持踢人下线 |
| JWT Payload | userId + roles | 满足权限校验需求，避免每次查库 |
| 滑动过期 | sa-Token active-timeout | 已配置，每次请求自动续期 |
| 旧 Token 兼容 | 双模式验证 | 过渡期内同时支持 UUID 和 JWT Token |
| 权限注解 | @SaCheckRole / @SaCheckPermission | sa-Token 原生支持，已在 admin 模块验证 |

### 命名约定

| 术语 | 含义 |
|------|------|
| `JWT Simple 模式` | Token 格式为 JWT，会话数据存 Redis，保留踢人等功能 |
| `active-timeout` | 滑动过期时间，用户活跃时自动续期 |
| `旧 Token` | 当前 UUID 格式的 sa-Token |
| `权限编码` | 如 `system:user:add`，对应接口权限 |
| `角色编码` | 如 `admin`、`user`，对应用户角色 |

### 关键决策记录

| 决策 | 选择 | 被拒方案 | 原因 |
|------|------|----------|------|
| JWT Payload 内容 | userId + roles | userId + roles + permissions | 权限可能频繁变更，放 JWT 会导致权限变更后需重新登录 |
| 权限变更后刷新 | 下次请求自动生效 | 立即踢下线 | 用户体验更好，权限变更不频繁 |
| 过渡期策略 | 双模式验证 | 强制重新登录 | 用户无感知，平滑过渡 |
| 过渡期时长 | 30 天 | 7 天 / 永久 | 30 天足够覆盖大部分用户的 Token 周期 |

---

## 现状分析

### 已具备

1. **JWT 配置**：
   - `SaTokenJwtConfig.java` 已配置 `StpLogicJwtForSimple`
   - `jwt-secret-key` 已配置

2. **RBAC 表结构**：
   - `sys_role`：角色表
   - `sys_permission`：权限表
   - `sys_user_role`：用户角色关联
   - `sys_role_permission`：角色权限关联

3. **权限查询**：
   - `AuthStpInterfaceImpl` 已实现
   - `UserMapper.xml` 有 `selectRoleCodesByUserId` 和 `selectPermissionCodesByUserId`

4. **权限注解使用**：
   - `clx-admin` 模块已使用 `@SaCheckRole("admin")`

### 缺失

1. **JWT Payload 未包含角色信息**：当前 JWT 只有默认字段
2. **旧 Token 兼容**：无兼容逻辑
3. **其他服务无权限控制**：post、user、message 等服务没有权限注解
4. **权限管理接口缺失**：角色 CRUD、权限 CRUD 接口未实现

---

## 功能设计

### 1. JWT Payload 增强

**目标**：JWT Payload 包含 userId + roles，减少查库次数

**JWT Payload 结构**：
```json
{
  "loginId": 1,
  "roles": ["admin"],
  "loginType": "login",
  "rnStr": "xxx"
}
```

**实现方式**：
- 登录时，将角色列表存入 SaSession
- Sa-Token JWT Simple 模式会自动将 Session 数据编码到 JWT

**示例**：
```java
// 登录时
StpUtil.login(userId);
StpUtil.getSession().set("roles", roleList);

// JWT 自动包含 roles
// 后续请求可直接从 Token 解析，无需查 Redis
```

---

### 2. 旧 Token 兼容

**目标**：过渡期内（30天）旧 UUID Token 和新 JWT Token 都能使用

**实现方式**：
1. 检测 Token 格式：JWT 是三段式（用 `.` 分隔），UUID 是无分隔符的字符串
2. 根据格式选择验证逻辑：
   - JWT → sa-Token JWT 验证
   - UUID → sa-Token 原有 Redis 验证

**核心代码位置**：`SaTokenExceptionHandler` 或自定义拦截器

**过渡期结束后**：删除兼容逻辑，只保留 JWT 验证

---

### 3. 权限注解完善

**目标**：各服务接口添加权限注解

**权限分级**：

| 服务 | 权限策略 | 说明 |
|------|----------|------|
| clx-auth | 无需登录 / 登录即可 | 登录、注册、找回密码公开 |
| clx-user | 登录即可 | 用户资料、关注、收藏 |
| clx-post | 登录即可 + 内容校验 | 发帖需登录，删帖只能删自己的 |
| clx-search | 无需登录 | 搜索公开 |
| clx-message | 登录即可 | 私信、通知 |
| clx-admin | @SaCheckRole("admin") | 管理功能需要管理员角色 |

**内容权限**：
- 删帖：只能删自己的，或管理员可删任何帖子
- 编辑：只能编辑自己的内容

**实现方式**：
```java
// 管理员权限
@SaCheckRole("admin")
public R<Void> deleteUser(Long userId) { ... }

// 登录即可
@SaCheckLogin
public R<Void> createPost(PostCreateRequest request) { ... }

// 内容权限（业务层校验）
public void deletePost(Long postId, Long userId) {
    Post post = postMapper.selectById(postId);
    if (!post.getAuthorId().equals(userId) && !StpUtil.hasRole("admin")) {
        throw new ServiceException(403, "无权删除此帖子");
    }
    postMapper.deleteById(postId);
}
```

---

### 4. 权限管理接口

**目标**：提供角色和权限的管理接口

**接口列表**：

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| /admin/role/list | GET | admin | 角色列表 |
| /admin/role | POST | admin | 新增角色 |
| /admin/role | PUT | admin | 修改角色 |
| /admin/role/{id} | DELETE | admin | 删除角色 |
| /admin/permission/list | GET | admin | 权限列表 |
| /admin/permission | POST | admin | 新增权限 |
| /admin/permission | PUT | admin | 修改权限 |
| /admin/permission/{id} | DELETE | admin | 删除权限 |
| /admin/role/{roleId}/permissions | GET | admin | 角色的权限列表 |
| /admin/role/{roleId}/permissions | PUT | admin | 分配权限给角色 |

---

### 5. 前端适配

**目标**：前端无需改动，Token 使用方式不变

**当前前端行为**：
- 登录后存储 Token 到 localStorage
- 请求时带 `Authorization: Bearer {token}`

**JWT 兼容性**：
- JWT 格式变化对前端透明
- 前端只需继续携带 Token，后端负责解析

---

## 实现提示

### 推进步骤

1. **后端：增强 JWT Payload**
   - 修改 `AuthServiceImpl.login`，登录时将角色列表存入 Session
   - 验证 JWT Payload 包含 roles 字段
   - 退出条件：登录后 JWT 解码可见 roles 数组

2. **后端：实现旧 Token 兼容**
   - 新建 `TokenCompatibilityFilter`，检测 Token 格式并分发验证
   - 注册为 Spring Filter
   - 退出条件：旧 Token 和新 JWT 都能正常访问接口

3. **后端：添加权限管理接口**
   - 新建 `RoleController` 和 `PermissionController`（在 clx-admin）
   - 实现 CRUD 接口
   - 退出条件：可通过接口管理角色和权限

4. **后端：完善权限注解**
   - clx-post：发帖/删帖/评论加 @SaCheckLogin
   - clx-user：关注/收藏加 @SaCheckLogin
   - clx-message：私信/通知加 @SaCheckLogin
   - 退出条件：未登录访问返回 401

5. **后端：内容权限校验**
   - PostService 删帖时校验作者或管理员
   - CommentService 删评论时校验作者或管理员
   - 退出条件：普通用户无法删除他人内容

6. **测试：权限校验验证**
   - 测试 admin 角色可访问管理接口
   - 测试普通用户无权访问管理接口
   - 测试内容权限校验
   - 退出条件：所有权限场景测试通过

7. **测试：Token 兼容性验证**
   - 用旧 Token 访问接口 → 成功
   - 用新 JWT 访问接口 → 成功
   - 退出条件：两种 Token 都能正常使用

8. **清理：删除过渡代码**
   - 30 天过渡期结束后，删除 `TokenCompatibilityFilter`
   - 更新文档说明只支持 JWT
   - 退出条件：代码清理完成，只保留 JWT 验证

### 高风险实现约束

1. **JWT 密钥安全**
   - 生产环境必须使用环境变量 `JWT_SECRET_KEY` 设置强密钥
   - 密钥泄露 = 所有 Token 可被伪造

2. **权限变更延迟**
   - 角色变更后，用户下次登录才会在 JWT 中生效
   - 如需立即生效，需要踢用户下线

3. **过渡期兼容**
   - 必须确保过渡期内两种 Token 都能使用
   - 否则会导致用户被强制下线

---

## 测试设计

### 功能点与测试约束

| 功能点 | 测试约束 | 验证方式 | 关键用例 |
|--------|----------|----------|----------|
| JWT Payload 包含角色 | 登录后 JWT 解码可见 roles | 单元测试 | admin 登录 → JWT 包含 ["admin"] |
| 旧 Token 兼容 | 旧 Token 访问接口成功 | 集成测试 | 用旧 Token 访问 /auth/me |
| 新 JWT 访问 | 新 JWT 访问接口成功 | 集成测试 | 登录获取 JWT → 访问接口 |
| 权限注解生效 | 未登录访问返回 401 | 自动化测试 | 无 Token 访问 /post/create |
| 角色权限校验 | 非管理员访问管理接口返回 403 | 自动化测试 | 普通用户访问 /admin/user/page |
| 内容权限校验 | 只能删除自己的内容 | 手动测试 | 用户A删用户B的帖子 → 403 |
| 滑动过期 | 活跃用户不被踢 | 手动测试 | 持续请求，Token 不过期 |

---

## 范围守护检查项

- [ ] JWT Payload 包含 userId 和 roles
- [ ] 过渡期内旧 Token 和新 JWT 都能使用
- [ ] 管理接口需要 admin 角色
- [ ] 普通接口只需登录
- [ ] 内容权限由业务层校验
- [ ] 不做数据行级权限
- [ ] 不做前端权限菜单
- [ ] 不做网关层鉴权

---

## 接口契约

### 1. 登录（增强版）

```bash
POST /auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin123",
  "captchaId": "uuid",
  "captchaCode": "ABCD",
  "rememberMe": false
}

响应：
{
  "code": 200,
  "data": {
    "tokenValue": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "tokenName": "Authorization",
    "timeout": 14400,
    "activeTimeout": 7200,
    "rememberMe": false
  }
}

# JWT Payload 解码后：
{
  "loginId": 1,
  "roles": ["admin"],
  "loginType": "login",
  "rnStr": "xxx"
}
```

### 2. 角色管理

```bash
# 获取角色列表
GET /admin/role/list
Authorization: Bearer {jwt}

响应：
{
  "code": 200,
  "data": [
    {"roleId": 1, "roleName": "超级管理员", "roleCode": "admin"},
    {"roleId": 2, "roleName": "普通用户", "roleCode": "user"}
  ]
}

# 新增角色
POST /admin/role
Authorization: Bearer {jwt}
{
  "roleName": "版主",
  "roleCode": "moderator",
  "description": "版块管理员"
}

响应：
{
  "code": 200,
  "data": {"roleId": 4}
}
```

### 3. 权限校验失败

```bash
# 未登录
GET /post/create
响应：
{
  "code": 401,
  "msg": "未登录"
}

# 无权限
POST /admin/user/page
Authorization: Bearer {普通用户jwt}
响应：
{
  "code": 403,
  "msg": "无此角色权限"
}
```

---

## 与项目级架构文档的关系

本 feature 完成后，需更新 `CLAUDE.md`：

1. **当前安全架构**：更新为 JWT + RBAC 模式
2. **后续扩展路线图**：标记"阶段 2：添加 JWT 支持"和"阶段 3：添加权限控制"为已完成
3. **权限控制**：添加权限注解使用说明

---

## 术语表

| 术语 | 定义 | 来源 |
|------|------|------|
| JWT | JSON Web Token，无状态认证令牌 | 行业标准 |
| Simple 模式 | sa-Token JWT 模式之一，Token 是 JWT 格式但会话仍存 Redis | sa-Token 文档 |
| active-timeout | 滑动过期时间，用户活跃时自动续期 | sa-Token 配置 |
| RBAC | Role-Based Access Control，基于角色的访问控制 | 行业标准 |
| 权限编码 | 权限的唯一标识，如 `system:user:add` | 本项目约定 |
| 角色编码 | 角色的唯一标识，如 `admin`、`user` | 本项目约定 |
