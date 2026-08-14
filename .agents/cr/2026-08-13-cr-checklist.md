# Code Review Checklist

> **Change** `auth-login` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-687badc0-d4ca-4945-` · **日期** `2026-08-13`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。

## 预扫说明

- `exec` 工具受 bwrap 沙箱限制（`Operation not permitted`），无法执行 `scan-all-rules.sh`。
- **降级**：改用原生 `rg` 工具对变更 `.java/.xml/.sql` 文件执行等价正则扫描（52 条规则中可程序化部分），脚本未覆盖项由 LLM 逐条补扫。
- 预扫命中：`M016`（AuthServiceImpl:106）、`Math.random`（CaptchaManager:46，脚本 S9.4 仅匹配 `new Random(`，漏报 `Math.random`，LLM 补扫）。

## Step 1 — 执行队列（产物 A）

| # | 文件路径 | 归属 | 状态 |
|---|----------|------|------|
| 1 | src/main/java/com/antdigital/auth/AuthApplication.java | 启动类 | ✅ 已审 |
| 2 | src/main/java/com/antdigital/auth/config/AuthConfiguration.java | 配置 | ✅ 已审 |
| 3 | src/main/java/com/antdigital/auth/common/constant/AuthConstants.java | 常量 | ✅ 已审 |
| 4 | src/main/java/com/antdigital/auth/common/enums/ResponseCodeEnum.java | 枚举 | ✅ 已审 |
| 5 | src/main/java/com/antdigital/auth/common/exception/BusinessException.java | 异常 | ✅ 已审 |
| 6 | src/main/java/com/antdigital/auth/common/model/ApiResponse.java | 响应 | ✅ 已审 |
| 7 | src/main/java/com/antdigital/auth/model/entity/UserDO.java | DO | ✅ 已审 |
| 8 | src/main/java/com/antdigital/auth/model/dto/LoginRequest.java | DTO | ✅ 已审 |
| 9 | src/main/java/com/antdigital/auth/model/dto/LoginResponse.java | DTO | ✅ 已审 |
| 10 | src/main/java/com/antdigital/auth/model/dto/LogoutRequest.java | DTO | ✅ 已审 |
| 11 | src/main/java/com/antdigital/auth/dao/mapper/UserMapper.java | DAO | ✅ 已审 |
| 12 | src/main/java/com/antdigital/auth/manager/cache/SessionManager.java | Manager | ⚠️ 已审有问题 |
| 13 | src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java | Manager | ⚠️ 已审有问题 |
| 14 | src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java | Manager | ⚠️ 已审有问题 |
| 15 | src/main/java/com/antdigital/auth/service/AuthService.java | Service | ✅ 已审 |
| 16 | src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java | ServiceImpl | ⚠️ 已审有问题 |
| 17 | src/main/java/com/antdigital/auth/controller/AuthController.java | Controller | ⚠️ 已审有问题 |
| 18 | src/main/java/com/antdigital/auth/controller/GlobalExceptionHandler.java | Controller | ✅ 已审 |
| 19 | src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java | Test | ❌ 已审有问题 |

> 非 Java 文件（pom.xml / application.yml / schema.sql / UserMapper.xml / .gitignore / docs）不在逐文件 Java 流程内，但其相关安全/配置问题在 Step 4 一并标注。

## Step 2 — 功能性检查（产物 B）

| REQ | 名称 | 结果 | Spec 证据 | 关联文件 |
|-----|------|------|-----------|----------|
| REQ-1 | 登录入口 POST /auth/login | ✅ | 验收标准 1 / README §API | AuthController:47 |
| REQ-2 | 输入校验（用户名/密码非空+长度） | ✅ | 验收标准 2 / 需求§3 输入校验 | LoginRequest:12-18 + GlobalExceptionHandler:38 |
| REQ-3 | 密码 BCrypt 加盐哈希校验 | ✅ | 需求§安全优先 / 验收标准 3 | AuthServiceImpl:98 + AuthConfiguration:20 |
| REQ-4 | 账号不存在与密码错误统一提示（防枚举） | ✅ | 需求§安全优先 / 验收标准 4 | AuthServiceImpl:62-66, 115-116 |
| REQ-5 | 暴力破解锁定（失败次数+锁定时长） | ✅ | 需求§4 异常场景 / 验收标准 5 | LoginAttemptManager + AuthServiceImpl:102-113 |
| REQ-6 | 账号禁用拒绝登录 | ✅ | 需求§4 / 验收标准 6 | AuthServiceImpl:76-81 |
| REQ-7 | Cookie HttpOnly+Secure+SameSite、记住我区分有效期 | ⚠️ | 需求§登录态 / 验收标准 7 | AuthController:55-62（Secure 默认 false，见 §5） |
| REQ-8 | 退出登录清除会话与 Cookie | ✅ | 验收标准 8 | AuthController:75-90 + AuthServiceImpl:132-138 |
| REQ-9 | IP 限流防暴力破解 | ⚠️ | 需求§4 / 非功能§安全 | LoginAttemptManager:129（XFF 可伪造，见 §5） |
| REQ-10 | 验证码（失败次数超阈值后必填） | ⚠️ | 需求§4 / 验收标准 | CaptchaManager:46（Math.random 不安全，见 §5） |

> REQ-7/9/10 功能路径存在但存在安全/可靠性隐患，标 ⚠️；功能性逻辑本身已实现。

## Step 3 — 可读性检查（产物 C）

| 项 | 结果 | 说明 |
|----|------|------|
| A1 源文件格式 | ✅ | 无 Tab 字符 |
| A2 import | ✅ | 无通配符 import |
| A3 行宽/关键字空格 | ✅ | 无 >120 字符行；关键字空格规范 |
| A4 包名 | ✅ | 全小写 |
| A5 finalize | ✅ | 无 finalize 覆盖 |
| A6 修饰符顺序/Long 字面量 | ✅ | 修饰符顺序正确；Long 字面量大写 L |
| A7 命名 | ✅ | 类大驼峰、方法小驼峰、常量全大写下划线 |

## Step 4 — 可靠性检查（产物 D）

| 域 | 结果 | 等级 | 命中 |
|----|------|------|------|
| 可靠性 G | ⚠️ | P2 | G 资源释放：SessionManager/LoginAttemptManager/CaptchaManager 内存 Map 无淘汰（:26/:27/:20）；G 事务边界：AuthServiceImpl login 无 @Transactional（:48）；G 并发：isIpRateLimited check-then-act 非原子（LoginAttemptManager:133-141）；G 一致性：DB lock_until 从不读取（AuthServiceImpl 仅查内存 lockStore） |
| 安全 S | ❌ | P0/P1 | S9.4 InsecureRandom：CaptchaManager:46 Math.random（脚本漏报，LLM 补扫）；S 输入校验：AuthController:120 XFF 可伪造绕过 IP 限流；S9.1 硬编码凭证：application.yml:12 password=root；S Cookie：application.yml:33 cookie-secure=false；S 密钥泄露：AuthServiceImpl:137 日志打印完整 token |
| Bug 模式 B/M/I | ⚠️ | P1 | M016 JavaTimeDefaultTimeZone：AuthServiceImpl:106 LocalDateTime.now()（Major→P1，实际影响低） |
| 编译 | ❌ | P0 | AuthServiceImplTest:122,128 REMEMEMBER_ME_TTL 拼写错误，测试类无法编译 |

## Step 5 — 自定义扩展检查（产物 E）

| 域 | 结果 | 说明 |
|----|------|------|
| 自定义扩展 | N/A | N/A(未启用自定义规则) |

## 收口

- 执行队列 `⬜ 待审` = 0（跳过项 0）；已审 19，其中 ❌1 / ⚠️5 / ✅13。
- 报告：`.agents/cr/2026-08-13-user-auth-code-review.md`
