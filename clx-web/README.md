# CLX Web

基于 `stype` 新拟物风格搭建的 React 前端，默认通过 `clx-gateway` 的 `http://localhost:8080` 访问后端接口。

## 启动

```bash
cd clx-web
npm install
npm run dev
```

如需直连其他后端地址，复制 `.env.example` 为 `.env` 并设置：

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 已对接接口

- 认证：登录、注册、退出、当前用户、刷新 Token、验证码、邮箱码、短信码、密码重置、手机号登录。
- OAuth/绑定：授权地址、绑定列表、解绑、绑定回调入口封装。
- 内容：帖子创建、更新、删除、详情、列表、站内搜索、热门帖子。
- 互动：评论列表、发表评论、删除评论、帖子点赞/取消、评论点赞/取消。
- 分类标签：分类列表、标签列表。
- 搜索：聚合搜索、单类搜索、搜索建议、热门关键词。

## 注意

`clx-gateway` 已补充 `/post/**`、`/category/**`、`/tag/**`、`/comment/**`、`/search/**` 路由。后端服务启动后，前端 Vite 代理会把这些路径转到网关。
