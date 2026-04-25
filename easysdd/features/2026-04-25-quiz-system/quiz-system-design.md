---
doc_type: feature-design
feature: 2026-04-25-quiz-system
status: approved
summary: 刷题系统第一期 - 题库管理 + 基础刷题（不含编程题/AI推荐/考试系统）
tags: [quiz, subject, practice, question-bank]
---

# 刷题系统 Design

> Stage 1 | 2026-04-25 | 状态：draft

## 0. 术语约定

| 术语 | 定义 | 防冲突结论 |
|------|------|-----------|
| 题目(Subject) | 一道具体的问答题，含题干、选项/答案、解析、难度、标签 | 复用 jc-club 命名，与 CLX 现有代码无冲突 |
| 题库(Question Bank) | 所有题目的集合，支持分类/标签/难度筛选 | 无冲突 |
| 分类(Category) | 题目的业务分类，如"后端"、"前端"、"算法" | 无冲突，与 clx-post 的 Category 概念类似但独立 |
| 标签(Label) | 题目的技术标签，如"Redis"、"MySQL"、"Java" | 无冲突，与 clx-post 的 Tag 概念类似但独立 |
| 练习(Practice) | 用户做题的行为记录，含答题状态、用时、正确率 | 无冲突 |
| 错题本(WrongBook) | 用户答错题目的集合，用于复习 | 无冲突 |
| 专项练习(SpecialPractice) | 按标签组题，用户选择标签后系统随机抽题 | 无冲突 |

**grep 防冲突检查**：
- `subject` 在 CLX 中无业务使用（仅在搜索模块有 ES 文档字段，无冲突）
- `quiz` / `practice` / `category` / `label` 均无冲突

---

## 1. 决策与约束

### 1.1 需求摘要

**做什么**：为 CLX 社区平台增加刷题功能，第一期聚焦题库管理 + 基础刷题。

**为谁**：
- 社区用户：刷题学习、查漏补缺
- 社区运营：管理题库、发布题目

**成功标准**：
- 用户能按分类/标签筛选题目并答题
- 用户能查看自己的错题本
- 管理员能增删改查题目

**明确不做**：
- ❌ 编程题（在线判题）→ 第二期
- ❌ AI智能推荐 → 第五期
- ❌ 考试系统（场次、限时、成绩、排名）→ 第三期
- ❌ 防作弊（摄像头、屏幕录制）→ 第四期
- ❌ 社区深度整合（题目讨论区、解题分享、积分徽章）→ 第六期
- ❌ 每日挑战（依赖推荐算法）→ 后续
- ❌ 证书生成 → 第三期

### 1.2 关键决策

| 决策点 | 选择 | 理由 | 被拒方案 |
|--------|------|------|---------|
| 模块划分 | 新建 `clx-quiz` 一个模块 | 题目和练习紧密耦合，拆两个模块增加 Feign 调用复杂度 | `clx-subject` + `clx-practice` 两模块 |
| 端口 | 9600 | 顺延现有端口分配（9100-9500 已用） | - |
| 数据库 | 新建 `clx_quiz` | 独立数据域，便于后续扩展 | 复用 `clx_post` |
| 题型处理 | 策略模式 | 四种题型（单选/多选/判断/简答）的答案结构不同，策略模式解耦 | if-else 分支 |
| 分类/标签 | 独立表，不复用 clx-post | 刷题分类体系与帖子不同，后续会扩展难度、知识点图谱等 | 复用帖子分类/标签 |
| 难度等级 | 1-5 级 | 参考 LeetCode 难度体系，1简单/2中等/3较难/4困难/5专家 | 3级（简单/中等/困难） |

### 1.3 主流程概述

**用户刷题主流程**：
1. 进入题库首页 → 选择分类/标签筛选
2. 开始练习 → 系统按条件随机抽取题目
3. 逐题作答 → 提交答案 → 显示正确答案和解析
4. 练习结束 → 查看本次练习统计（正确率、用时）
5. 错题自动进入错题本

**管理员管理题库流程**：
1. 创建/编辑题目 → 选择题型 → 填写题干、选项、答案、解析
2. 选择分类和标签 → 保存

**异常/边界**：
- 题库无符合条件的题目时，提示"暂无相关题目"
- 简答题由用户自评对错（系统不自动判题）
- 多选题必须全部选对才算正确

---

## 2. 接口契约

### 2.1 后端 API

#### 2.1.1 题目管理

**新增题目** `POST /quiz/subject/add`

```json
// 请求
{
  "subjectName": "Redis支持哪几种数据类型？",
  "subjectType": 4,           // 1单选 2多选 3判断 4简答
  "subjectDifficult": 1,      // 1-5难度
  "subjectScore": 5,
  "subjectParse": "Redis支持String、List、Hash、Set、Sorted Set五种数据类型...",
  "categoryIds": [1, 2],      // 分类ID列表
  "labelIds": [1, 2],         // 标签ID列表
  "optionList": [             // 选项列表（简答题为空，存储答案文本）
    {
      "optionType": 1,        // 选项类型：1A 2B 3C 4D
      "optionContent": "String、List、Hash、Set、Sorted Set",
      "isCorrect": 1          // 是否正确：0否 1是
    }
  ]
}

// 响应
{
  "code": 200,
  "message": "success",
  "data": true
}
```
// 来源：参考 jc-club SubjectController.add()

**分页查询题目** `POST /quiz/subject/page`

```json
// 请求
{
  "categoryId": 1,
  "labelId": 1,
  "keyword": "Redis",
  "pageNo": 1,
  "pageSize": 10
}

// 响应
{
  "code": 200,
  "data": {
    "total": 100,
    "list": [
      {
        "id": 100,
        "subjectName": "Redis支持哪几种数据类型？",
        "subjectType": 4,
        "subjectDifficult": 1,
        "categoryName": "缓存",
        "labelNames": ["Redis", "数据类型"]
      }
    ]
  }
}
```

**查询题目详情** `POST /quiz/subject/detail`

```json
// 请求
{ "id": 100 }

// 响应（含选项和答案，用于编辑或答题后的解析展示）
{
  "code": 200,
  "data": {
    "id": 100,
    "subjectName": "Redis支持哪几种数据类型？",
    "subjectType": 4,
    "subjectDifficult": 1,
    "subjectScore": 5,
    "subjectParse": "...",
    "categoryIds": [1],
    "labelIds": [1],
    "optionList": [
      { "optionType": 1, "optionContent": "...", "isCorrect": 1 }
    ]
  }
}
```

**删除题目** `POST /quiz/subject/delete`

```json
// 请求
{ "id": 100 }

// 响应
{ "code": 200, "data": true }
```

#### 2.1.2 分类管理

**查询分类列表** `GET /quiz/category/list`

```json
// 响应
{
  "code": 200,
  "data": [
    { "id": 1, "categoryName": "后端", "parentId": 0 },
    { "id": 2, "categoryName": "缓存", "parentId": 1 },
    { "id": 3, "categoryName": "前端", "parentId": 0 }
  ]
}
```

**新增分类** `POST /quiz/category/add`

```json
// 请求
{ "categoryName": "数据库", "parentId": 1 }

// 响应
{ "code": 200, "data": true }
```

#### 2.1.3 标签管理

**查询标签列表** `GET /quiz/label/list`

```json
// 请求
{ "categoryId": 1 }  // 可选，按分类筛选

// 响应
{
  "code": 200,
  "data": [
    { "id": 1, "labelName": "Redis", "categoryId": 1, "sortNum": 1 },
    { "id": 2, "labelName": "MySQL", "categoryId": 1, "sortNum": 2 }
  ]
}
```

#### 2.1.4 练习流程

**开始练习（随机组题）** `POST /quiz/practice/start`

```json
// 请求
{
  "labelIds": [1, 2],      // 按标签组题
  "count": 10              // 题目数量
}

// 响应
{
  "code": 200,
  "data": {
    "practiceId": 12345,   // 本次练习ID
    "totalCount": 10,
    "subjectIds": [100, 101, 102, ...]
  }
}
```

**获取练习题目** `POST /quiz/practice/subject`

```json
// 请求
{
  "practiceId": 12345,
  "subjectId": 100,
  "subjectType": 4
}

// 响应（不含答案，用于答题）
{
  "code": 200,
  "data": {
    "subjectId": 100,
    "subjectName": "Redis支持哪几种数据类型？",
    "subjectType": 4,
    "subjectDifficult": 1,
    "optionList": [
      { "optionType": 1, "optionContent": "..." }
      // 注意：不含 isCorrect
    ]
  }
}
```

**提交答案** `POST /quiz/practice/submit`

```json
// 请求
{
  "practiceId": 12345,
  "subjectId": 100,
  "subjectType": 4,
  "answerContent": "String、List、Hash、Set、Sorted Set"  // 用户答案
}

// 响应（返回正确答案和解析）
{
  "code": 200,
  "data": {
    "isCorrect": 1,        // 0错 1对 2部分对（简答题由用户自评）
    "correctAnswer": "String、List、Hash、Set、Sorted Set",
    "subjectParse": "...",
    "needSelfJudge": false  // 简答题为 true，需用户自评
  }
}
```

**简答题自评** `POST /quiz/practice/self-judge`

```json
// 请求
{
  "practiceId": 12345,
  "subjectId": 100,
  "isCorrect": 1          // 用户自评：0错 1对
}

// 响应
{ "code": 200, "data": true }
```

**结束练习** `POST /quiz/practice/finish`

```json
// 请求
{ "practiceId": 12345 }

// 响应
{
  "code": 200,
  "data": {
    "totalCount": 10,
    "correctCount": 8,
    "correctRate": 0.8,
    "timeUsed": "00:15:30",  // 用时
    "wrongSubjectIds": [102, 105]  // 错题ID列表
  }
}
```

#### 2.1.5 错题本

**查询错题本** `POST /quiz/wrong-book/list`

```json
// 请求
{
  "pageNo": 1,
  "pageSize": 10
}

// 响应
{
  "code": 200,
  "data": {
    "total": 25,
    "list": [
      {
        "subjectId": 102,
        "subjectName": "什么是缓存穿透？",
        "subjectType": 4,
        "wrongCount": 3,      // 累计错题次数
        "lastWrongTime": "2026-04-25 10:30:00"
      }
    ]
  }
}
```

**从错题本移除** `POST /quiz/wrong-book/remove`

```json
// 请求
{ "subjectId": 102 }

// 响应
{ "code": 200, "data": true }
```

### 2.2 前端组件

#### 2.2.1 组件拆分

```
src/features/quiz/
├── pages/
│   ├── QuizHomePage.tsx        # 题库首页（分类/标签筛选）
│   ├── PracticePage.tsx        # 答题页面
│   ├── PracticeResultPage.tsx  # 练习结果页
│   └── WrongBookPage.tsx       # 错题本页面
├── components/
│   ├── SubjectCard.tsx         # 题目卡片（列表展示）
│   ├── SubjectForm.tsx         # 题目表单（新增/编辑）
│   ├── AnswerPanel.tsx         # 答题面板（选项/输入框）
│   ├── CategorySelector.tsx    # 分类选择器
│   └── LabelSelector.tsx       # 标签选择器
└── store/
    └── quizStore.ts            # 刷题状态管理
```

#### 2.2.2 核心组件契约

**SubjectForm 题目表单**

```tsx
// Props
interface SubjectFormProps {
  mode: 'create' | 'edit';
  initialValues?: Subject;  // 编辑模式初始值
  onSubmit: (data: SubjectFormData) => void;
  onCancel: () => void;
}

// 示例
<SubjectForm
  mode="create"
  onSubmit={(data) => createSubject(data)}
  onCancel={() => navigate('/quiz')}
/>

// Events
// - onSubmit: 表单提交，包含题目完整数据
// - onCancel: 取消操作
```

**AnswerPanel 答题面板**

```tsx
// Props
interface AnswerPanelProps {
  subjectType: 1 | 2 | 3 | 4;  // 题型
  options: Option[];            // 选项列表（不含答案）
  onSubmit: (answer: string) => void;
  showResult?: boolean;         // 是否显示结果
  correctAnswer?: string;       // 正确答案（showResult=true时）
  subjectParse?: string;        // 解析（showResult=true时）
}

// 示例 - 单选题
<AnswerPanel
  subjectType={1}
  options={[
    { optionType: 1, optionContent: "选项A" },
    { optionType: 2, optionContent: "选项B" }
  ]}
  onSubmit={(answer) => submitAnswer(answer)}
/>

// 示例 - 简答题（答题后显示解析）
<AnswerPanel
  subjectType={4}
  options={[]}
  showResult={true}
  correctAnswer="参考答案..."
  subjectParse="解析内容..."
  onSubmit={(answer) => submitAnswer(answer)}
/>
```

#### 2.2.3 状态归属

| 状态 | 归属层级 | 说明 |
|------|---------|------|
| 分类/标签列表 | 组件内部 | 页面加载时获取，不跨页面共享 |
| 当前练习信息（practiceId、题目列表、当前题号） | 全局 store | 跨页面共享（答题页 → 结果页） |
| 用户答案 | 组件内部 | 答题过程中临时状态 |
| 错题本列表 | 组件内部 | 错题本页面独立获取 |

---

## 3. 实现提示

### 3.1 改动计划

#### 新增模块

| 文件 | 说明 |
|------|------|
| `clx-quiz/pom.xml` | 新建模块，端口 9600 |
| `clx-quiz/src/main/java/com/clx/quiz/ClxQuizApplication.java` | 启动类 |
| `clx-quiz/src/main/resources/application.yml` | 配置文件 |
| `clx-quiz/src/main/resources/application-dev.yml` | 开发环境配置 |

#### 新增实体

| 文件 | 说明 |
|------|------|
| `entity/Subject.java` | 题目主表实体 |
| `entity/SubjectCategory.java` | 分类实体 |
| `entity/SubjectLabel.java` | 标签实体 |
| `entity/SubjectRadio.java` | 单选题选项实体 |
| `entity/SubjectMultiple.java` | 多选题选项实体 |
| `entity/SubjectJudge.java` | 判断题实体 |
| `entity/SubjectBrief.java` | 简答题实体 |
| `entity/Practice.java` | 练习记录实体 |
| `entity/PracticeDetail.java` | 练习详情实体 |
| `entity/WrongBook.java` | 错题本实体 |
| `entity/SubjectMapping.java` | 题目-分类-标签关联实体 |

#### 新增 Mapper

| 文件 | 说明 |
|------|------|
| `mapper/SubjectMapper.java` | 题目 Mapper |
| `mapper/SubjectCategoryMapper.java` | 分类 Mapper |
| `mapper/SubjectLabelMapper.java` | 标签 Mapper |
| `mapper/SubjectRadioMapper.java` | 单选题 Mapper |
| `mapper/SubjectMultipleMapper.java` | 多选题 Mapper |
| `mapper/SubjectJudgeMapper.java` | 判断题 Mapper |
| `mapper/SubjectBriefMapper.java` | 简答题 Mapper |
| `mapper/PracticeMapper.java` | 练习 Mapper |
| `mapper/PracticeDetailMapper.java` | 练习详情 Mapper |
| `mapper/WrongBookMapper.java` | 错题本 Mapper |
| `mapper/SubjectMappingMapper.java` | 关联 Mapper |

#### 新增 Service

| 文件 | 说明 |
|------|------|
| `service/SubjectService.java` | 题目服务接口 |
| `service/impl/SubjectServiceImpl.java` | 题目服务实现 |
| `service/SubjectCategoryService.java` | 分类服务接口 |
| `service/impl/SubjectCategoryServiceImpl.java` | 分类服务实现 |
| `service/SubjectLabelService.java` | 标签服务接口 |
| `service/impl/SubjectLabelServiceImpl.java` | 标签服务实现 |
| `service/PracticeService.java` | 练习服务接口 |
| `service/impl/PracticeServiceImpl.java` | 练习服务实现 |
| `service/WrongBookService.java` | 错题本服务接口 |
| `service/impl/WrongBookServiceImpl.java` | 错题本服务实现 |
| `service/handler/SubjectTypeHandler.java` | 题型处理策略接口 |
| `service/handler/RadioTypeHandler.java` | 单选题处理策略 |
| `service/handler/MultipleTypeHandler.java` | 多选题处理策略 |
| `service/handler/JudgeTypeHandler.java` | 判断题处理策略 |
| `service/handler/BriefTypeHandler.java` | 简答题处理策略 |
| `service/handler/SubjectTypeHandlerFactory.java` | 策略工厂 |

#### 新增 Controller

| 文件 | 说明 |
|------|------|
| `controller/SubjectController.java` | 题目管理 API |
| `controller/SubjectCategoryController.java` | 分类管理 API |
| `controller/SubjectLabelController.java` | 标签管理 API |
| `controller/PracticeController.java` | 练习流程 API |
| `controller/WrongBookController.java` | 错题本 API |

#### 新增 VO/DTO

| 文件 | 说明 |
|------|------|
| `dto/SubjectCreateRequest.java` | 新增题目请求 |
| `dto/SubjectUpdateRequest.java` | 更新题目请求 |
| `dto/SubjectQueryRequest.java` | 查询题目请求 |
| `dto/PracticeStartRequest.java` | 开始练习请求 |
| `dto/PracticeSubmitRequest.java` | 提交答案请求 |
| `vo/SubjectVO.java` | 题目列表 VO |
| `vo/SubjectDetailVO.java` | 题目详情 VO |
| `vo/PracticeResultVO.java` | 练习结果 VO |
| `vo/WrongBookVO.java` | 错题本 VO |

#### 新增配置

| 文件 | 说明 |
|------|------|
| `config/SecurityConfig.java` | Security 配置（放行） |
| `config/CorsConfig.java` | CORS 配置 |

#### 数据库脚本

| 文件 | 说明 |
|------|------|
| `doc/sql/quiz_schema.sql` | 刷题模块表结构 |
| `doc/sql/quiz_init_data.sql` | 初始化数据 |

#### 前端文件

| 文件 | 说明 |
|------|------|
| `clx-web/src/features/quiz/pages/QuizHomePage.tsx` | 题库首页 |
| `clx-web/src/features/quiz/pages/PracticePage.tsx` | 答题页面 |
| `clx-web/src/features/quiz/pages/PracticeResultPage.tsx` | 练习结果页 |
| `clx-web/src/features/quiz/pages/WrongBookPage.tsx` | 错题本页面 |
| `clx-web/src/features/quiz/components/*.tsx` | 刷题相关组件 |
| `clx-web/src/features/quiz/store/quizStore.ts` | 状态管理 |

### 3.2 实现风险与约束

1. **题型策略扩展性**：四种题型使用策略模式，新增题型只需新增 Handler，不修改现有代码
2. **简答题判题**：简答题无法自动判题，需用户自评对错
3. **多选题判题**：必须全部选对才算正确，不支持部分得分
4. **随机组题性能**：标签组合查询时注意索引优化，避免全表扫描
5. **错题本同步**：答题结束后异步写入错题本，不影响主流程性能

### 3.3 推进顺序

#### Step 1: 搭建 clx-quiz 模块骨架

**改动**：
- 新建 `clx-quiz/pom.xml`
- 新建启动类 `ClxQuizApplication.java`
- 新建配置文件 `application.yml`、`application-dev.yml`
- 新建 `SecurityConfig.java`、`CorsConfig.java`
- 新建数据库脚本 `doc/sql/quiz_schema.sql`
- 更新根 `pom.xml` 添加 module

**退出信号**：`mvn compile` 成功，服务能在 9600 端口启动

#### Step 2: 实现分类和标签管理

**改动**：
- 新建实体 `SubjectCategory.java`、`SubjectLabel.java`
- 新建 Mapper 及 XML
- 新建 Service 及实现
- 新建 Controller `SubjectCategoryController.java`、`SubjectLabelController.java`
- 初始化数据 `doc/sql/quiz_init_data.sql`

**退出信号**：调用 `GET /quiz/category/list` 返回分类列表，`GET /quiz/label/list` 返回标签列表

#### Step 3: 实现题目管理（含题型策略）

**改动**：
- 新建实体 `Subject.java`、`SubjectRadio.java`、`SubjectMultiple.java`、`SubjectJudge.java`、`SubjectBrief.java`、`SubjectMapping.java`
- 新建 Mapper 及 XML
- 新建题型策略接口 `SubjectTypeHandler.java` 及四个实现
- 新建策略工厂 `SubjectTypeHandlerFactory.java`
- 新建 Service `SubjectService.java` 及实现
- 新建 Controller `SubjectController.java`
- 新增/查询/删除题目 API

**退出信号**：
- 调用 `POST /quiz/subject/add` 能新增四种题型
- 调用 `POST /quiz/subject/page` 能分页查询
- 调用 `POST /quiz/subject/detail` 能获取详情含选项

#### Step 4: 实现练习流程

**改动**：
- 新建实体 `Practice.java`、`PracticeDetail.java`
- 新建 Mapper 及 XML
- 新建 Service `PracticeService.java` 及实现
- 新建 Controller `PracticeController.java`
- 开始练习/获取题目/提交答案/结束练习 API

**退出信号**：
- 调用 `POST /quiz/practice/start` 返回练习ID和题目列表
- 调用 `POST /quiz/practice/subject` 返回题目（不含答案）
- 调用 `POST /quiz/practice/submit` 返回判题结果和解析
- 调用 `POST /quiz/practice/finish` 返回练习统计

#### Step 5: 实现错题本

**改动**：
- 新建实体 `WrongBook.java`
- 新建 Mapper 及 XML
- 新建 Service `WrongBookService.java` 及实现
- 新建 Controller `WrongBookController.java`
- 练习结束时自动写入错题本（在 PracticeService 中调用）
- 查询错题本/移除错题 API

**退出信号**：
- 答错题目后能在错题本中看到
- 调用 `POST /quiz/wrong-book/remove` 能移除

#### Step 6: 前端 - 题库首页和答题页面

**改动**：
- 新建 `clx-web/src/features/quiz/` 目录结构
- 实现题库首页 `QuizHomePage.tsx`
- 实现答题页面 `PracticePage.tsx`
- 实现练习结果页 `PracticeResultPage.tsx`
- 实现相关组件
- 实现状态管理 `quizStore.ts`
- 更新路由配置

**退出信号**：
- 访问 `/quiz` 能看到题库首页
- 能选择分类/标签筛选并开始练习
- 能逐题作答并查看结果

#### Step 7: 前端 - 错题本页面

**改动**：
- 实现错题本页面 `WrongBookPage.tsx`
- 更新路由配置

**退出信号**：
- 访问 `/quiz/wrong-book` 能看到错题列表
- 能移除错题

#### Step 8: 补充测试

**改动**：
- 新建 `clx-quiz/src/test/java/` 测试类
- SubjectServiceTest
- PracticeServiceTest
- WrongBookServiceTest
- 各题型 Handler 测试

**退出信号**：`mvn test` 全部通过

### 3.4 测试设计

#### 3.4.1 题目管理测试

| 功能点 | 测试约束 | 验证方式 |
|--------|---------|---------|
| 新增单选题 | 必须有一个正确选项 | 插入后查询验证 |
| 新增多选题 | 必须有至少两个正确选项 | 插入后查询验证 |
| 新增判断题 | isCorrect 必须是 0 或 1 | 插入后查询验证 |
| 新增简答题 | answer 不能为空 | 插入后查询验证 |
| 分页查询 | 支持分类/标签/关键词筛选 | 构造测试数据验证 |
| 删除题目 | 软删除，is_deleted = 1 | 删除后查询验证 |

#### 3.4.2 练习流程测试

| 功能点 | 测试约束 | 验证方式 |
|--------|---------|---------|
| 随机组题 | 按标签筛选，数量符合要求 | Mock 数据验证返回题目数 |
| 获取题目 | 返回不含答案 | 验证响应无 isCorrect 字段 |
| 单选题判题 | 选对返回正确，选错返回错误 | 构造答案验证 |
| 多选题判题 | 全对才算对，少选/错选都算错 | 构造答案验证 |
| 判断题判题 | 正确/错误二选一 | 构造答案验证 |
| 简答题判题 | 返回 needSelfJudge=true | 验证响应字段 |
| 练习统计 | 正确率、用时、错题列表 | Mock 完整流程验证 |

#### 3.4.3 错题本测试

| 功能点 | 测试约束 | 验证方式 |
|--------|---------|---------|
| 自动入错题本 | 答错自动记录 | Mock 答错流程验证 |
| 累计错误次数 | 同一题多次答错，次数累加 | 多次答错后查询验证 |
| 移除错题 | 移除后不再显示 | 移除后查询验证 |

---

## 4. 与项目级架构文档的关系

### 4.1 关联架构文档

- `CLAUDE.md` - 需更新服务端口表、模块结构

### 4.2 架构文档补充

**CLAUDE.md 需补充**：

```markdown
| clx-quiz | 9600 | 刷题服务，题库管理、练习流程、错题本 |

数据库：
- `clx_quiz`（题目、分类、标签、练习记录、错题本）
```

**后续扩展预留**：
- 编程题：预留 `subject_type = 5`，答案存储在 `subject_code` 表
- 考试系统：预留 `exam_info`、`exam_detail` 表结构设计
- AI推荐：预留用户做题记录分析表 `user_subject_stat`
