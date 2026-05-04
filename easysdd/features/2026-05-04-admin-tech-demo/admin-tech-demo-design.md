---
name: admin-tech-demo
feature: admin-tech-demo
doc_type: feature-design
status: approved
summary: 为 clx-admin 模块添加 AOP 操作日志和数据权限控制两个技术展示实例，作为技术能力演示
tags: [admin, aop, data-permission, tech-demo, mybatis-interceptor]
created: 2026-05-04
---

# clx-admin 技术展示实例设计

## 0. 术语约定

| 术语 | 含义 | 代码指针 |
|------|------|----------|
| @OperLog | 操作日志注解 | `clx-admin/.../annotation/OperLog.java` |
| OperLogAspect | 操作日志切面 | `clx-admin/.../aspect/OperLogAspect.java` |
| @DataScope | 数据权限注解 | `clx-admin/.../annotation/DataScope.java` |
| DataScopeInterceptor | 数据权限拦截器 | `clx-admin/.../interceptor/DataScopeInterceptor.java` |
| 操作日志 | 管理员操作行为自动记录 | `sys_oper_log` 表 |
| 数据权限 | 基于组织的数据隔离 | `sys_user.org_id` 字段 |

## 1. 需求摘要

为 clx-admin 模块添加两个**技术展示实例**：

1. **AOP 操作日志**：通过自定义注解 + AOP 切面，自动记录管理员操作
2. **数据权限控制**：通过 MyBatis 拦截器，实现基于组织的数据隔离

**明确不做**：

- 不改造现有的手动日志记录代码（保持兼容）
- 不实现数据权限的管理界面（只做后端能力）
- 不实现多租户隔离（当前仅按组织过滤）
- 不处理子组织继承（仅过滤当前组织）

**决策与约束**：

- 操作日志使用 Spring AOP + 自定义注解，最小侵入
- 数据权限使用 MyBatis 拦截器，对业务代码透明
- 两个功能作为**技术展示**，可独立启用/禁用
- 与现有 `OperLogService` 共存，不破坏已有功能

**被拒方案**：

- ~~Shiro 权限框架~~：项目已使用 sa-Token，保持一致
- ~~MyBatis-Plus 数据权限插件~~：手写拦截器更具技术展示价值
- ~~日志异步写入 MQ~~：当前 `@Async` 已够用，不过度设计

## 2. 系统架构

### 2.1 AOP 操作日志架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Controller                               │
│  @OperLog(module="用户管理", action="查询用户")                  │
│  public R<PageResult> getUserPage(...) { ... }                  │
└───────────────────────────┬─────────────────────────────────────┘
                            │ AOP 代理
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      OperLogAspect                               │
│  1. 解析注解参数                                                 │
│  2. 记录开始时间                                                 │
│  3. 执行目标方法                                                 │
│  4. 捕获响应/异常                                                │
│  5. 异步写入日志表                                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │ @Async
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      sys_oper_log 表                             │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 数据权限架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         Controller                               │
│  @DataScope(orgAlias = "o", userAlias = "u")                    │
│  public R<PageResult> getUserPage(...) {                        │
│      return userMapper.selectPage(...);  // 自动拼接过滤条件    │
│  }                                                               │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                   DataScopeInterceptor                           │
│  1. 拦截 Mapper 方法                                            │
│  2. 解析 @DataScope 注解                                        │
│  3. 获取当前用户的组织 ID                                       │
│  4. 拼接 SQL: WHERE o.org_id = {currentUser.orgId}             │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    MyBatis Executor                              │
│  执行修改后的 SQL                                               │
└─────────────────────────────────────────────────────────────────┘
```

### 2.3 与现有代码的关系

| 现有代码 | 新增代码 | 关系 |
|----------|----------|------|
| `OperLogService.logAsync()` | `@OperLog` 注解 | 共存，注解方式更简洁 |
| 手动记录日志（UserController） | `OperLogAspect` | 可逐步迁移，不强制 |
| `UserQueryDTO` | `@DataScope` 注解 | 注解方式自动过滤 |

## 3. 接口契约

### 3.1 @OperLog 注解使用示例

```java
// 在 Controller 方法上添加注解，自动记录操作日志
@OperLog(module = "用户管理", action = "查询用户列表")
@SaCheckRole("admin")
@PostMapping("/page")
public R<PageResultDTO<UserPageVO>> getUserPage(@RequestBody UserQueryDTO query) {
    return userFeignClient.getUserPage(query);
}

// 支持记录请求参数和响应结果
@OperLog(module = "用户管理", action = "编辑用户", recordParam = true, recordResult = true)
@PutMapping("/{userId}")
public R<Void> updateUser(@PathVariable Long userId, @RequestBody UserUpdateDTO dto) {
    return userFeignClient.updateUser(userId, dto);
}

// 异常自动记录
@OperLog(module = "用户管理", action = "封禁用户")
@PutMapping("/{userId}/ban")
public R<Void> banUser(@PathVariable Long userId) {
    // 抛出异常时，status=1，errorMsg=异常信息
    return userFeignClient.banUser(userId);
}
```

### 3.2 @DataScope 注解使用示例

```java
// 自动拼接: WHERE o.org_id = {当前用户组织ID}
@DataScope(orgAlias = "o", userAlias = "u")
@PostMapping("/page")
public R<PageResultDTO<UserPageVO>> getUserPage(@RequestBody UserQueryDTO query) {
    return userFeignClient.getUserPage(query);
}

// 对应的 Mapper SQL 需要使用别名
// SELECT u.*, o.org_name FROM sys_user u
// LEFT JOIN sys_organization o ON u.org_id = o.org_id
// WHERE 1=1  ← DataScope 会在这里注入条件

// 多表关联时指定多个别名
@DataScope(orgAlias = "o", userAlias = "u")
// 生成的 SQL: WHERE (o.org_id = 1 OR u.org_id = 1)
```

### 3.3 日志查询接口（新增）

**请求**
```
GET /admin/log/oper?page=1&size=10&module=用户管理&startTime=2026-05-01&endTime=2026-05-04
```

**响应**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 1,
        "username": "admin",
        "module": "用户管理",
        "action": "查询用户列表",
        "requestUrl": "/admin/user/page",
        "requestMethod": "POST",
        "requestParams": "{\"page\":1,\"size\":10}",
        "status": "0",
        "costTime": 120,
        "operIp": "127.0.0.1",
        "operTime": "2026-05-04 10:30:00"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 10
  }
}
```

## 4. 实现提示

### 4.1 新建文件清单

```
clx-admin/src/main/java/com/clx/admin/
├── annotation/
│   ├── OperLog.java              # 操作日志注解
│   └── DataScope.java            # 数据权限注解
├── aspect/
│   └── OperLogAspect.java        # AOP 切面
├── interceptor/
│   └── DataScopeInterceptor.java # MyBatis 拦截器
├── config/
│   └── MybatisConfig.java        # 注册拦截器
├── controller/
│   └── OperLogController.java    # 日志查询接口
├── vo/
│   └── OperLogVO.java            # 日志查询 VO
└── dto/
    └── LogQueryDTO.java          # 日志查询条件
```

### 4.2 推进顺序

1. **@OperLog 注解定义** → verify: 编译通过
   - 新建 `annotation/OperLog.java`
   - 定义属性：module、action、recordParam、recordResult

2. **OperLogAspect 切面实现** → verify: 单元测试通过
   - 新建 `aspect/OperLogAspect.java`
   - `@Around` 环绕通知
   - 解析注解、计时、捕获异常
   - 异步调用 `OperLogService`

3. **日志查询接口** → verify: 接口测试通过
   - 新建 `controller/OperLogController.java`
   - 新建 `vo/OperLogVO.java`
   - 新建 `dto/LogQueryDTO.java`
   - 扩展 `OperLogMapper` 添加分页查询

4. **@DataScope 注解定义** → verify: 编译通过
   - 新建 `annotation/DataScope.java`
   - 定义属性：orgAlias、userAlias

5. **DataScopeInterceptor 拦截器** → verify: 单元测试通过
   - 新建 `interceptor/DataScopeInterceptor.java`
   - 实现 `Interceptor` 接口
   - 解析注解、获取用户组织、拼接 SQL

6. **注册拦截器** → verify: 拦截器生效
   - 新建 `config/MybatisConfig.java`
   - 将拦截器添加到 MyBatis 插件链

7. **UserController 改造演示** → verify: 功能正常
   - 添加 `@OperLog` 注解到部分方法
   - 添加 `@DataScope` 注解（需调整 Feign 调用方式）

8. **端到端验证** → verify: 完整流程可操作
   - 登录后台 → 操作用户 → 查看日志列表
   - 不同组织用户登录 → 验证数据过滤

### 4.3 关键实现细节

#### OperLogAspect.java 核心逻辑

```java
@Aspect
@Component
@Slf4j
public class OperLogAspect {

    @Autowired
    private OperLogService operLogService;

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        long start = System.currentTimeMillis();
        String status = "0";
        String errorMsg = null;
        Object result = null;

        try {
            result = point.proceed();
            return result;
        } catch (Throwable e) {
            status = "1";
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - start;
            // 获取请求信息
            HttpServletRequest request = getCurrentRequest();
            // 异步记录日志
            operLogService.logAsync(
                operLog.module(),
                operLog.action(),
                request.getRequestURI(),
                request.getMethod(),
                operLog.recordParam() ? getParams(point) : null,
                operLog.recordResult() ? toJson(result) : null,
                status,
                errorMsg,
                costTime,
                StpUtil.getLoginIdAsLong(),
                StpUtil.getLoginIdAsString(),
                request.getRemoteAddr()
            );
        }
    }
}
```

#### DataScopeInterceptor.java 核心逻辑

```java
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {...}),
    @Signature(type = Executor.class, method = "update", args = {...})
})
@Component
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 1. 获取方法上的 @DataScope 注解
        // 2. 获取当前用户的组织 ID
        // 3. 修改 SQL，拼接 WHERE 条件
        // 4. 执行修改后的 SQL
        return invocation.proceed();
    }
}
```

### 4.4 测试设计

| 功能点 | 测试约束 | 验证方式 |
|--------|----------|----------|
| @OperLog 正常记录 | 日志表新增记录 | 单元测试 |
| @OperLog 异常记录 | status=1，errorMsg 有值 | 单元测试 |
| @OperLog 参数记录 | requestParams 字段有值 | 单元测试 |
| @OperLog 耗时统计 | costTime > 0 | 单元测试 |
| 日志分页查询 | 返回正确数据 | 接口测试 |
| @DataScope SQL 改写 | WHERE 子句正确拼接 | 单元测试 |
| @DataScope 组织过滤 | 只返回当前组织数据 | 集成测试 |
| 无注解方法 | 不受影响 | 回归测试 |

### 4.5 风险与边界

| 风险 | 缓解措施 |
|------|----------|
| AOP 切面影响性能 | 使用 `@Async` 异步记录，不阻塞主流程 |
| SQL 改写错误 | 只处理 SELECT 语句，复杂 SQL 跳过 |
| 组织字段为空 | 空值用户不受数据权限限制（超管） |
| Feign 调用链路 | DataScope 仅在本地 Mapper 生效 |

## 5. 数据权限补充说明

### 5.1 当前数据结构

```sql
-- sys_user 表已有 org_id 字段
CREATE TABLE sys_user (
    ...
    org_id bigint DEFAULT NULL COMMENT '组织ID',
    ...
);

-- sys_organization 表已存在
CREATE TABLE sys_organization (
    org_id bigint NOT NULL COMMENT '组织ID',
    org_name varchar(100) NOT NULL COMMENT '组织名称',
    parent_id bigint DEFAULT '0' COMMENT '父组织ID',
    ...
);
```

### 5.2 用户上下文获取

当前用户组织 ID 的获取方式：

```java
// 方式 1：从 JWT Payload 获取（需要扩展）
Long orgId = StpUtil.getExtra("orgId");

// 方式 2：从 Feign 调用获取
UserInfo user = userFeignClient.getUserById(StpUtil.getLoginIdAsLong());
Long orgId = user.getOrgId();

// 方式 3：缓存到 Redis（推荐）
// 登录时缓存用户组织，拦截器直接读取
String orgId = RedisUtil.get("user:org:" + userId);
```

**假设**：用户登录时，将 `orgId` 存入 JWT Payload 或 Redis 缓存。

## 6. 前端适配

### 6.1 日志查询页面（新增）

在 `clx-admin-web` 添加操作日志查询页面：

```
src/views/log/
├── LogListPage.vue      # 日志列表页
└── LogDetailDialog.vue  # 日志详情弹窗
```

### 6.2 菜单配置

```javascript
// router/index.ts
{
  path: '/log',
  name: 'Log',
  children: [
    {
      path: 'oper',
      name: 'OperLog',
      component: () => import('@/views/log/LogListPage.vue'),
      meta: { title: '操作日志' }
    }
  ]
}
```
