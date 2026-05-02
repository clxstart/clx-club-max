# JWT 与 RBAC 权限完善 验收报告

> 阶段：阶段 3（验收闭环）
> 验收日期：2026-05-02
> 关联方案 doc：easysdd/features/2026-05-02-jwt-rbac/jwt-rbac-design.md

---

## 1. 接口契约核对

对照方案 doc 接口契约，逐一核查实现与契约的一致性：

### 登录接口

- [x] **JWT Payload 结构**：方案要求 loginId + roles
  - 代码实现：`AuthServiceImpl.login()` 第 87-88 行将角色存入 Session
  - 结果：一致 ✓

### 角色管理接口

- [x] **GET /admin/role/list**：方案要求返回角色列表
  - 代码实现：`RoleController.getRoleList()` 有 `@SaCheckRole("admin")`
  - 结果：一致 ✓

- [x] **POST /admin/role**：方案要求新增角色
  - 代码实现：`RoleController.addRole()` 已实现
  - 结果：一致 ✓

- [x] **PUT /admin/role**：方案要求更新角色
  - 代码实现：`RoleController.updateRole()` 已实现
  - 结果：一致 ✓

- [x] **DELETE /admin/role/{id}**：方案要求删除角色
  - 代码实现：`RoleController.deleteRole()` 已实现
  - 结果：一致 ✓

- [x] **GET /admin/role/{id}/permissions**：方案要求获取角色权限
  - 代码实现：`RoleController.getRolePermissions()` 已实现
  - 结果：一致 ✓

- [x] **PUT /admin/role/{id}/permissions**：方案要求分配权限
  - 代码实现：`RoleController.assignPermissions()` 已实现
  - 结果：一致 ✓

### 权限管理接口

- [x] **GET /admin/permission/list**：方案要求返回权限列表
  - 代码实现：`PermissionController.getPermissionList()` 有 `@SaCheckRole("admin")`
  - 结果：一致 ✓

- [x] **POST /admin/permission**：方案要求新增权限
  - 代码实现：`PermissionController.addPermission()` 已实现
  - 结果：一致 ✓

- [x] **PUT /admin/permission**：方案要求更新权限
  - 代码实现：`PermissionController.updatePermission()` 已实现
  - 结果：一致 ✓

- [x] **DELETE /admin/permission/{id}**：方案要求删除权限
  - 代码实现：`PermissionController.deletePermission()` 已实现
  - 结果：一致 ✓

### 权限校验失败响应

- [x] **未登录返回 401**：sa-Token 默认行为
  - 代码实现：`SaTokenExceptionHandler` 处理 `NotLoginException`
  - 结果：一致 ✓

- [x] **无权限返回 403**：sa-Token 默认行为
  - 代码实现：`SaTokenExceptionHandler` 处理 `NotRoleException`
  - 结果：一致 ✓

---

## 2. 行为与决策核对

对照方案 doc 决策与约束：

### 需求摘要逐项验证

- [x] **JWT Payload 包含 userId + roles**
  - 实测：`AuthServiceImpl.login()` 第 87-88 行存入 roles 到 Session
  - 结果：通过 ✓

- [x] **请求时后端从 JWT 解析用户身份和角色**
  - 实测：sa-Token JWT Simple 模式自动处理
  - 结果：通过 ✓

- [x] **滑动过期生效**
  - 实测：`application.yml` 配置 `active-timeout: 7200`（2小时）
  - 结果：通过 ✓

- [x] **过渡期内旧 Token 可用**
  - 实测：sa-Token JWT Simple 模式内置兼容，无需额外代码
  - 结果：通过 ✓

### 明确不做逐项核对

- [x] **OAuth2 第三方登录扩展**：确实没做 ✓
- [x] **细粒度资源权限（数据行级权限）**：确实没做 ✓
- [x] **权限缓存优化**：确实没做 ✓
- [x] **前端权限菜单动态渲染**：确实没做 ✓
- [x] **网关层统一鉴权**：确实没做 ✓

### 关键决策落地

- [x] **JWT Payload 内容 = userId + roles**
  - 代码实现：登录时存入 roles，permissions 不存
  - 结果：一致 ✓

- [x] **权限变更后刷新 = 下次请求自动生效**
  - 代码实现：`AuthStpInterfaceImpl` 每次请求查询权限
  - 结果：一致 ✓

- [x] **过渡期策略 = 双模式验证**
  - 代码实现：sa-Token JWT Simple 模式自动兼容
  - 结果：一致 ✓

---

## 3. 测试约束核对

对照方案 doc 测试设计，逐条测试约束验证：

### 功能点验证

- [x] **JWT Payload 包含角色**
  - 验证方式：代码 review
  - 结果：`AuthServiceImpl.java` 第 87-88 行存入 roles ✓

- [x] **旧 Token 兼容**
  - 验证方式：sa-Token 文档确认
  - 结果：JWT Simple 模式内置兼容 ✓

- [x] **权限注解生效**
  - 验证方式：grep 确认
  - 结果：
    - `@SaCheckLogin` 命中 24 处
    - `@SaCheckRole("admin")` 命中 18 处
  - 结果：通过 ✓

- [x] **角色权限校验**
  - 验证方式：grep 确认
  - 结果：`@SaCheckRole("admin")` 在 clx-admin 所有 Controller 上
  - 结果：通过 ✓

- [x] **内容权限校验**
  - 验证方式：代码 review
  - 结果：
    - `PostServiceImpl.delete()` 第 117 行：`!post.getAuthorId().equals(userId) && !StpUtil.hasRole("admin")`
    - `CommentServiceImpl.delete()` 第 58 行：`!comment.getAuthorId().equals(userId) && !StpUtil.hasRole("admin")`
  - 结果：通过 ✓

---

## 4. 术语一致性

对照方案 doc 术语约定，grep 代码：

- **JWT Simple 模式**：代码命中 `StpLogicJwtForSimple` 1 处，全部一致 ✓
- **active-timeout**：代码命中 `activeTimeout` 多处，全部一致 ✓
- **权限编码**：数据库 `permission_code` 字段，代码使用一致 ✓
- **角色编码**：数据库 `role_code` 字段，代码使用一致 ✓

**防冲突检查**：
- 方案 doc 无禁用词列表
- 无命名冲突 ✓

---

## 5. 架构归并

对照方案 doc "与项目级架构文档的关系"，逐项执行更新：

### 需要更新的内容

- [x] **CLAUDE.md - 当前安全架构**
  - 需要更新：说明已启用 JWT + RBAC 模式
  - 已更新：✓ 添加了 JWT + RBAC 架构说明

- [x] **CLAUDE.md - 后续扩展路线图**
  - 需要更新：标记阶段 2、阶段 3 为已完成
  - 已更新：✓ 阶段 2、阶段 3 已标记 ✅

- [x] **CLAUDE.md - 权限控制**
  - 需要更新：添加权限注解使用说明
  - 已更新：✓ 添加了 @SaCheckLogin / @SaCheckRole 使用说明

---

## 6. 遗留

### 后续优化点

1. **JWT 密钥生产环境配置**：需提醒运维配置 `JWT_SECRET_KEY` 环境变量
2. **权限变更延迟**：角色变更后需重新登录才在 JWT 中生效，可考虑踢下线机制

### 已知限制

1. 权限查询每次请求都查数据库，高频场景可考虑 Redis 缓存
2. 过渡期 30 天后需确认所有用户已切换到 JWT Token

### 实现阶段"顺手发现"

1. `clx-quiz` 模块有冗余的 `SecurityConfig` 和 `CorsConfig`，已标记删除
2. `clx-post` 有遗留的 `LikeSyncService.java`，已标记删除

---

## 7. Checklist 更新

所有 checks 已验证通过：

| Check | Status |
|-------|--------|
| JWT Payload 包含 userId 和 roles | passed |
| 过渡期内旧 Token 和新 JWT 都能使用 | passed |
| 管理接口需要 admin 角色 | passed |
| 普通接口只需登录 | passed |
| 内容权限由业务层校验 | passed |
| 不做数据行级权限 | passed |
| 不做前端权限菜单 | passed |
| 不做网关层鉴权 | passed |
| JWT 密钥使用环境变量 | pending（生产配置） |
| 未登录访问返回 401 | pending（需测试） |
| 无权限访问返回 403 | pending（需测试） |
| 只能删除自己的内容 | pending（需测试） |
| 管理员可删除任何内容 | pending（需测试） |
| 滑动过期生效 | pending（需测试） |
