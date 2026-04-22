# 手机号登录配置指南

本文档说明如何配置手机号登录功能，包括短信服务商对接。

---

## 1. 功能概述

手机号登录流程：
1. 用户输入手机号 + 图形验证码
2. 后端验证图形验证码，生成短信验证码
3. 短信验证码存入 Redis（5分钟有效）
4. 用户输入短信验证码完成登录
5. 未注册手机号自动创建账号

---

## 2. 当前状态（开发环境）

**已实现**：
- 后端接口：`POST /auth/phone/sms-code`（发送验证码）、`POST /auth/phone/login`（登录）
- 前端页面：`/phone-login`
- 验证码存储：Redis

**开发环境**：验证码直接打印在后端控制台日志中，方便测试。

```log
=== 短信验证码已发送（开发环境）=== phone=13800138000, code=123456
```

---

## 3. 生产环境配置

### 3.1 阿里云短信服务

#### 步骤一：开通服务

1. 登录 [阿里云控制台](https://www.aliyun.com/)
2. 搜索「短信服务」，开通服务
3. 实名认证（个人或企业均可）

#### 步骤二：申请签名和模板

**申请签名**：
- 国内消息 → 签名管理 → 添加签名
- 签名名称：`CLX社区`（需要和营业执照/网站名称一致）
- 签名来源：选择对应类型，上传资质

**申请模板**：
- 国内消息 → 模板管理 → 添加模板
- 模板类型：验证码
- 模板名称：`登录验证码`
- 模板内容：
  ```
  您的验证码为：${code}，5分钟内有效，请勿泄露给他人。
  ```
- 等待审核（通常 1-2 小时）

#### 步骤三：创建 AccessKey

1. 右上角头像 → AccessKey 管理
2. 创建 AccessKey，记录 `AccessKeyId` 和 `AccessKeySecret`
3. **安全提醒**：不要提交到 Git！

#### 步骤四：配置到项目

**方式一：application-dev.yml**

```yaml
# 阿里云短信配置
aliyun:
  sms:
    enabled: true
    access-key-id: 你的AccessKeyId
    access-key-secret: 你的AccessKeySecret
    sign-name: CLX社区                    # 签名名称
    template-code: SMS_123456789         # 模板CODE
    region-id: cn-hangzhou               # 地域
```

**方式二：环境变量（推荐生产环境）**

```bash
export ALIYUN_SMS_ACCESS_KEY_ID=你的AccessKeyId
export ALIYUN_SMS_ACCESS_KEY_SECRET=你的AccessKeySecret
export ALIYUN_SMS_SIGN_NAME=CLX社区
export ALIYUN_SMS_TEMPLATE_CODE=SMS_123456789
```

#### 步骤五：添加 Maven 依赖

```xml
<!-- 阿里云短信 SDK -->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>alibabacloud-dysmsapi20170525</artifactId>
    <version>2.0.24</version>
</dependency>
```

---

### 3.2 腾讯云短信服务

#### 步骤一：开通服务

1. 登录 [腾讯云控制台](https://cloud.tencent.com/)
2. 搜索「短信」，开通服务
3. 创建短信应用

#### 步骤二：申请签名和模板

类似阿里云流程，需要提交资质审核。

#### 步骤三：配置到项目

```yaml
# 腾讯云短信配置
tencent:
  sms:
    enabled: true
    secret-id: 你的SecretId
    secret-key: 你的SecretKey
    app-id: 1400xxxxx
    sign-name: CLX社区
    template-id: 123456
```

---

## 4. 代码改造点

当前代码在 `PhoneLoginServiceImpl.sendSmsCode()` 中有一处 TODO：

```java
// 步骤4：发送短信
// 开发环境：直接打印日志，方便测试
// 生产环境：对接阿里云/腾讯云短信服务
log.info("=== 短信验证码已发送（开发环境）=== phone={}, code={}", phone, smsCode);

// TODO: 生产环境对接短信服务商
// sendSmsToProvider(phone, smsCode);
```

### 改造示例（阿里云）

创建 `AliyunSmsService.java`：

```java
@Service
@ConditionalOnProperty(name = "aliyun.sms.enabled", havingValue = "true")
public class AliyunSmsService {

    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    public void send(String phone, String code) throws Exception {
        Config config = new Config()
            .setAccessKeyId(accessKeyId)
            .setAccessKeySecret(accessKeySecret)
            .setEndpoint("dysmsapi.aliyuncs.com");

        Client client = new Client(config);
        SendSmsRequest request = new SendSmsRequest()
            .setPhoneNumbers(phone)
            .setSignName(signName)
            .setTemplateCode(templateCode)
            .setTemplateParam("{\"code\":\"" + code + "\"}");

        SendSmsResponse response = client.sendSms(request);
        log.info("短信发送结果: phone={}, code={}", phone, response.getBody().getCode());
    }
}
```

修改 `PhoneLoginServiceImpl`：

```java
@Autowired(required = false)
private AliyunSmsService smsService;

// 发送短信
if (smsService != null) {
    smsService.send(phone, smsCode);
} else {
    log.info("=== 短信验证码（开发环境）=== phone={}, code={}", phone, smsCode);
}
```

---

## 5. 配置清单

| 配置项 | 说明 | 必填 | 示例值 |
|--------|------|------|--------|
| `aliyun.sms.enabled` | 是否启用阿里云短信 | 是 | `true` |
| `aliyun.sms.access-key-id` | 阿里云 AccessKey ID | 是 | `LTAI5t...` |
| `aliyun.sms.access-key-secret` | 阿里云 AccessKey Secret | 是 | `xxx...` |
| `aliyun.sms.sign-name` | 短信签名 | 是 | `CLX社区` |
| `aliyun.sms.template-code` | 短信模板 CODE | 是 | `SMS_123456789` |
| `aliyun.sms.region-id` | 地域 | 否 | `cn-hangzhou` |

---

## 6. 测试验证

### 开发环境测试

1. 启动 Redis
2. 启动 clx-auth 服务
3. 访问 `http://localhost:5174/phone-login`
4. 输入手机号，点击「获取验证码」
5. 查看后端控制台日志获取验证码
6. 输入验证码登录

### 生产环境测试

1. 配置阿里云短信服务
2. 使用真实手机号测试
3. 确认收到短信
4. 完成登录流程

---

## 7. 费用说明

| 服务商 | 价格（约） | 说明 |
|--------|-----------|------|
| 阿里云 | ¥0.045/条 | 国内短信，量大有优惠 |
| 腾讯云 | ¥0.045/条 | 国内短信，量大有优惠 |

**注意**：新用户通常有免费额度（100-1000 条），足够开发测试使用。

---

## 8. 安全建议

1. **AccessKey 保密**：不要提交到 Git，使用环境变量
2. **验证码有效期**：当前 5 分钟，可根据需要调整
3. **发送频率限制**：当前已实现 5 分钟内不可重复发送
4. **IP 限流**：建议在网关层添加 IP 维度限流
5. **手机号验证**：前端和后端都验证手机号格式

---

## 9. 常见问题

**Q: 签名审核不通过怎么办？**
A: 确保签名名称和营业执照/网站名称一致，个人开发者可以使用「App 名称」或「网站名称」类型。

**Q: 模板审核不通过怎么办？**
A: 模板内容要简洁明确，不要包含营销内容。验证码模板通常格式：「您的验证码为：${code}，有效期5分钟。」

**Q: 测试环境不想真实发短信？**
A: 当前代码已支持开发模式，不配置短信服务时会自动打印日志。

---

## 10. 相关文件

| 文件 | 说明 |
|------|------|
| `PhoneLoginController.java` | 手机号登录控制器 |
| `PhoneLoginServiceImpl.java` | 手机号登录服务实现 |
| `VerificationCodeService.java` | 验证码存储服务 |
| `PhoneLoginPage.tsx` | 前端手机号登录页面 |
