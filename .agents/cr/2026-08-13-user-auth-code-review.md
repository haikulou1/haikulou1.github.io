# Code Review Report

> **Change** `auth-login` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-687badc0-d4ca-4945-` · **日期** `2026-08-13` · **审查者** AI
>
> 等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式 Blocker→P0、Major→P1、Info→P2。
>
> **预扫说明**：`exec` 工具受 bwrap 沙箱限制（`Operation not permitted`），无法运行 `scan-all-rules.sh`；已降级为原生 `rg` 工具对变更 `.java/.xml/.sql` 执行等价正则扫描，脚本未覆盖项由 LLM 逐条补扫。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | 19 |
| 变更行数 | `+约1100 / -0`（新建工程，全量为新增） |

| 类/接口 | 路径 | 角色 |
|---------|------|------|
| AuthApplication | src/main/java/com/antdigital/auth/AuthApplication.java | 启动类 |
| AuthConfiguration | src/main/java/com/antdigital/auth/config/AuthConfiguration.java | BCrypt Bean |
| AuthConstants | src/main/java/com/antdigital/auth/common/constant/AuthConstants.java | 常量 |
| ResponseCodeEnum | src/main/java/com/antdigital/auth/common/enums/ResponseCodeEnum.java | 错误码 |
| BusinessException | src/main/java/com/antdigital/auth/common/exception/BusinessException.java | 业务异常 |
| ApiResponse | src/main/java/com/antdigital/auth/common/model/ApiResponse.java | 统一响应 |
| UserDO | src/main/java/com/antdigital/auth/model/entity/UserDO.java | 用户 DO |
| LoginRequest | src/main/java/com/antdigital/auth/model/dto/LoginRequest.java | 登录请求 DTO |
| LoginResponse | src/main/java/com/antdigital/auth/model/dto/LoginResponse.java | 登录响应 DTO |
| LogoutRequest | src/main/java/com/antdigital/auth/model/dto/LogoutRequest.java | 退出请求 DTO |
| UserMapper | src/main/java/com/antdigital/auth/dao/mapper/UserMapper.java | 用户 DAO |
| SessionManager | src/main/java/com/antdigital/auth/manager/cache/SessionManager.java | 会话管理 |
| LoginAttemptManager | src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java | 失败计数/锁定/IP 限流 |
| CaptchaManager | src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java | 验证码 |
| AuthService | src/main/java/com/antdigital/auth/service/AuthService.java | 认证接口 |
| AuthServiceImpl | src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java | 认证实现 |
| AuthController | src/main/java/com/antdigital/auth/controller/AuthController.java | 登录/退出入口 |
| GlobalExceptionHandler | src/main/java/com/antdigital/auth/controller/GlobalExceptionHandler.java | 全局异常 |
| AuthServiceImplTest | src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java | 单测 |

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 1 | 6 | 5 |

---

## 3. Step 2 — 功能（REQ）

> REQ 来源：需求§3 功能需求 / §4 异常与边界 / §7 验收标准（impl.md 验收标准映射）。

### REQ-1: 登录入口 POST /auth/login

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 登录接口路径 | ✅ | 验收标准 1 / README §API `POST /api/auth/login` | AuthController:47（context-path /api + /auth/login） | 注：README 示例为 `/api/auth/login`，代码 context-path=/api + /auth → 一致 |

### REQ-2: 输入校验（用户名/密码非空+长度）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| @Valid + Bean Validation | ✅ | 验收标准 2 / 需求§3 输入校验 | LoginRequest:12-18；AuthController:48 @Valid；GlobalExceptionHandler:38 | 用户名 3-50、密码 6-64 |

### REQ-3: 密码 BCrypt 加盐哈希校验

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| BCrypt matches | ✅ | 需求§安全优先 / 验收标准 3 | AuthServiceImpl:98-99；AuthConfiguration:20 | 密码不落明文 |

### REQ-4: 账号不存在与密码错误统一提示（防枚举）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 统一 ACCOUNT_OR_PASSWORD_ERROR | ✅ | 需求§安全优先 / 验收标准 4 | AuthServiceImpl:62-66, 115-116 | 账号不存在与密码错误均返回同一错误码 |

### REQ-5: 暴力破解锁定

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 失败次数累计 + 锁定时长 | ✅ | 需求§4 / 验收标准 5 | AuthServiceImpl:102-113；LoginAttemptManager:94-102 | maxFailCount=5，lock 900s |

### REQ-6: 账号禁用拒绝登录

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| ACCOUNT_DISABLED | ✅ | 需求§4 / 验收标准 6 | AuthServiceImpl:76-81 | status==0 拒绝 |

### REQ-7: Cookie HttpOnly+Secure+SameSite、记住我区分有效期

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| HttpOnly+SameSite | ✅ | 需求§登录态 / 验收标准 7 | AuthController:55-62 | HttpOnly=true，SameSite=Lax |
| Secure | ⚠️ | 需求§登录态「Secure」 | AuthController:58 + application.yml:33 | **Secure 默认 false**，见 §5 S-Cookie |
| 记住我区分有效期 | ✅ | 需求§登录态「记住我区分长短有效期」 | SessionManager:63-70, 99-101 | 1800s / 604800s |

### REQ-8: 退出登录清除会话与 Cookie

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 注销会话 + 清 Cookie | ✅ | 验收标准 8 | AuthController:75-90；AuthServiceImpl:132-138 | maxAge=0 清除 |

### REQ-9: IP 限流防暴力破解

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| IP 每分钟限流 | ⚠️ | 需求§4 / 非功能§安全 | LoginAttemptManager:129-142 | **X-Forwarded-For 可伪造绕过**，见 §5 S-输入校验 |

### REQ-10: 验证码（失败次数超阈值后必填）

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 验证码必填/校验/一次性 | ⚠️ | 需求§4 / 验收标准 | AuthServiceImpl:83-95；CaptchaManager:58-70 | **Math.random 生成不安全**，见 §5 S9.4 |

---

## 4. Step 3 — 可读性检查

| 项 | 结果 | 说明 |
|----|------|------|
| A1 源文件格式 | ✅ | 无 Tab 字符（rg 预扫无命中） |
| A2 import | ✅ | 无通配符 import（A2.2 无命中） |
| A3 行宽/关键字空格 | ✅ | 无 >120 字符行（A3.4 无命中）；关键字空格规范 |
| A4 包名 | ✅ | 全小写（A4.1 无命中） |
| A5 finalize | ✅ | 无 finalize 覆盖（A5.4 无命中） |
| A6 修饰符顺序/Long 字面量 | ✅ | 修饰符顺序正确；Long 字面量大写 L（A6.5 无命中，如 1800L/604800L/300L） |
| A7 命名 | ✅ | 类大驼峰、方法小驼峰、常量全大写下划线 |

> 可读性整体良好，无 P1/P2 命中。

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ⚠️ | P2 | 见 G-资源/G-事务/G-并发/G-一致性行 |
| 安全 | `security-checklist.md` S1–S10 | ❌ | P0/P1 | 见 S9.4/S-输入/S9.1/S-Cookie/S-日志行 |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I | ⚠️ | P1 | M016 AuthServiceImpl:106；编译错误见 P0 |
| SQL 注入 S1.1 | MyBatis `${}` | ✅ | — | UserMapper.xml 全量 `#{}` 参数化，无 `${}` 命中 |
| 其他 B/M/I | B*/M*/I* | ✅ | — | 预扫 B005/B022/B066/M003/M004/M007/M016/M022/M027/I001/I004 等均无命中（除 M016） |

### 命中明细

- **[P0] 编译错误** — `AuthServiceImplTest.java:122,128` 引用 `REMEMEMBER_ME_TTL`（多一个 E），声明处为 `REMEMBER_ME_TTL`（:63），测试类无法编译，阻断构建与单测。
- **[P1] S9.4 InsecureRandom** — `CaptchaManager.java:46` 使用 `Math.random()` 生成验证码，非密码学安全随机数；脚本 S9.4 仅匹配 `new Random(`，对 `Math.random` 漏报，LLM 补扫。验证码可预测，削弱暴力破解防护。
- **[P1] S-输入校验/认证** — `AuthController.java:120` `resolveClientIp` 直接信任 `X-Forwarded-For`/`X-Real-IP` 头，未校验可信代理，攻击者可伪造头绕过 IP 限流。
- **[P1] S9.1 硬编码凭证** — `application.yml:12` `password: root` 数据库口令硬编码于配置文件。
- **[P1] S-Cookie Secure** — `application.yml:33` `cookie-secure: false` 默认非 Secure Cookie，登录态可经中间人窃取；需求§登录态明确要求 Secure。
- **[P1] S-密钥泄露** — `AuthServiceImpl.java:137` `logger.info("退出登录成功, token: {}", token)` 将完整会话令牌写入日志，令牌泄露风险。
- **[P1] M016 JavaTimeDefaultTimeZone** — `AuthServiceImpl.java:106` `LocalDateTime.now()` 使用系统默认时区（Major→P1）；实际仅影响 lockUntil 计算，影响较低。
- **[P2] G-资源释放** — `SessionManager.java:26`、`LoginAttemptManager.java:26-28`、`CaptchaManager.java:20` 内存 Map 无主动淘汰（`cleanupExpiredSessions` 未被任何定时任务调用），长跑内存泄漏。
- **[P2] G-事务边界** — `AuthServiceImpl.java:48` `login` 跨 DB 写（`resetFailStatus`）与内存会话创建（`createSession`）非原子，无 `@Transactional`。
- **[P2] G-并发** — `LoginAttemptManager.java:133-141` `isIpRateLimited` check-then-act 非原子（windowStart 与 ipAttemptStore 两次独立 put），并发下计数可能不准。
- **[P2] G-一致性** — `AuthServiceImpl.java:69` 锁定判定仅查内存 `lockStore`，DB `lock_until` 字段从不被读取用于判定，应用重启后锁定状态丢失。

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明 |
|----|------|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | N/A(未启用自定义规则) |

---

## 7. 结论

- **合并建议**：阻止合并（存在 P0 编译错误）
- **P0**：
  1. `AuthServiceImplTest.java:122,128` 常量名 `REMEMEMBER_ME_TTL` 拼写错误，测试类编译失败，阻断构建。
- **P1**：
  1. `CaptchaManager.java:46` Math.random 生成验证码，应改 SecureRandom（S9.4）。
  2. `AuthController.java:120` resolveClientIp 信任 XFF 可伪造，应校验可信代理或回退 remoteAddr（S-输入）。
  3. `application.yml:12` 数据库 password=root 硬编码，应改环境变量/配置中心（S9.1）。
  4. `application.yml:33` cookie-secure=false，生产应 true（S-Cookie）。
  5. `AuthServiceImpl.java:137` logout 日志打印完整 token，应脱敏或不打印（S-密钥泄露）。
  6. `AuthServiceImpl.java:106` LocalDateTime.now() 默认时区（M016，影响低）。
- **一句话**：功能路径与防枚举/锁定/校验设计完整，但存在阻断编译的测试拼写错误与多处安全配置隐患，须修复 P0 并至少消除登录态相关的 P1 后方可合并。

---

## 7.1 问题片段（必填）

### P0 — AuthServiceImplTest.java:122（编译错误）

- **P0** `AuthServiceImplTest.java:122,128` — 常量 `REMEMEMBER_ME_TTL` 拼写错误（声明为 `REMEMBER_ME_TTL`），导致测试类无法编译，构建阻断。
  片段范围：`src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java:63-128`

```java
L63|    private static final long REMEMBER_ME_TTL = 604800L;
L64|
...
L121|        when(sessionManager.createSession(eq(1L), eq(USERNAME), eq(true))).thenReturn(TOKEN);
L122|        when(sessionManager.getSessionTtl(true)).thenReturn(REMEMEMBER_ME_TTL);  // 问题：REMEMEMBER 多一个 E，符号不存在
...
L128|        assertThat(response.getExpiresIn()).isEqualTo(REMEMEMBER_ME_TTL);        // 同一错误
```

### P1 — CaptchaManager.java:46（不安全随机数）

- **P1** `S9.4` `src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java:46` — 验证码用 `Math.random()` 生成，非密码学安全随机数，可预测，削弱暴力破解防护。脚本 S9.4 仅匹配 `new Random(`，漏报 `Math.random`，LLM 补扫。
  片段范围：`src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java:42-49`

```java
L42|    public String generate(String captchaId) {
L43|        String id = (captchaId == null || captchaId.isBlank())
L44|                ? UUID.randomUUID().toString().replace("-", "")
L45|                : captchaId;
L46|        String text = String.valueOf((int) ((Math.random() * 9000) + 1000));  // 问题：非 SecureRandom
L47|        captchaStore.put(id, new CaptchaEntry(text, Instant.now().plusSeconds(CAPTCHA_TTL_SECONDS)));
L48|        return id;
L49|    }
```

### P1 — AuthController.java:120（XFF 伪造绕过限流）

- **P1** `S-输入校验` `src/main/java/com/antdigital/auth/controller/AuthController.java:120` — `resolveClientIp` 直接信任 `X-Forwarded-For`/`X-Real-IP`，未校验可信代理链，可伪造 IP 绕过 IP 限流。
  片段范围：`src/main/java/com/antdigital/auth/controller/AuthController.java:119-130`

```java
L119|    private String resolveClientIp(HttpServletRequest request) {
L120|        String ip = request.getHeader("X-Forwarded-For");  // 问题：可伪造
L121|        if (ip != null && !ip.isBlank()) {
L122|            int comma = ip.indexOf(',');
L123|            return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
L124|        }
L125|        ip = request.getHeader("X-Real-IP");  // 问题：同样可伪造
L126|        if (ip != null && !ip.isBlank()) {
L127|            return ip.trim();
L128|        }
L129|        return request.getRemoteAddr();
L130|    }
```

### P1 — AuthServiceImpl.java:137（token 写入日志）

- **P1** `S-密钥泄露` `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:137` — logout 将完整会话令牌写入 INFO 日志，令牌泄露风险。
  片段范围：`src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:131-138`

```java
L131|    @Override
L132|    public void logout(String token) {
L133|        if (token == null || token.isBlank()) {
L134|            return;
L135|        }
L136|        sessionManager.invalidate(token);
L137|        logger.info("退出登录成功, token: {}", token);  // 问题：打印完整 token
L138|    }
```

### P1 — AuthServiceImpl.java:106（默认时区）

- **P1** `M016` `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:106` — `LocalDateTime.now()` 使用系统默认时区（Major→P1）；实际仅用于 lockUntil 计算，影响较低。
  片段范围：`src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:102-108`

```java
L102|        if (!passwordMatched) {
L103|            int newFailCount = currentFails + 1;
L104|            boolean locked = loginAttemptManager.applyLockIfExceeded(username, newFailCount);
L105|            LocalDateTime lockUntil = locked
L106|                    ? LocalDateTime.now().plusSeconds(loginAttemptManager.getLockDurationSeconds())  // 问题：默认时区
L107|                    : null;
L108|            userMapper.updateFailStatus(user.getId(), newFailCount, lockUntil);
```

### P1 — application.yml:12 / :33（配置类，非 Java，附说明）

- **P1** `S9.1` `src/main/resources/application.yml:12` — `password: root` 数据库口令硬编码；`S-Cookie` `application.yml:33` — `cookie-secure: false` 默认非 Secure。两者均为配置项，建议改环境变量 / 生产强制 true。`N/A(非 Java)`，不附 Java 片段。

### P2 — LoginAttemptManager.java:129（并发/资源）

- **P2** `G-并发` `LoginAttemptManager.java:133-141` — `isIpRateLimited` check-then-act 非原子；`G-资源` 内存 Map 无淘汰。
  片段范围：`src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java:129-142`

```java
L129|    public boolean isIpRateLimited(String ip) {
L130|        if (ip == null || ip.isBlank()) {
L131|            return false;
L132|        }
L133|        Instant windowStart = ipWindowStart.get(ip);
L134|        Instant now = Instant.now();
L135|        if (windowStart == null || now.isAfter(windowStart.plusSeconds(60))) {
L136|            ipWindowStart.put(ip, now);                 // 问题：与下行非原子
L137|            ipAttemptStore.put(ip, new AtomicInteger(1));
L138|            return false;
L139|        }
L140|        AtomicInteger count = ipAttemptStore.computeIfAbsent(ip, k -> new AtomicInteger(0));
L141|        return count.incrementAndGet() > ipRateLimitPerMinute;
L142|    }
```

---

## 8. 修复任务列表

### P0

- [ ] **P0** `src/test/java/com/antdigital/auth/service/impl/AuthServiceImplTest.java:122,128` — 将 `REMEMEMBER_ME_TTL` 更正为 `REMEMBER_ME_TTL`（与 :63 声明一致），恢复测试类编译。
- [ ] **P0** 验证 — 修复后在本地执行 `mvn clean compile -DskipTests` 与 `mvn test -Dtest=AuthServiceImplTest` 确认 13 个用例通过。

### P1

- [ ] **P1** `src/main/java/com/antdigital/auth/manager/cache/CaptchaManager.java:46` — 改用 `SecureRandom` 生成验证码（注入或 `new SecureRandom()`），替换 `Math.random()`。
- [ ] **P1** `src/main/java/com/antdigital/auth/controller/AuthController.java:120` — `resolveClientIp` 增加可信代理校验，或仅在生产代理后信任 XFF，否则回退 `request.getRemoteAddr()`。
- [ ] **P1** `src/main/resources/application.yml:12` — 数据库 `password` 改为环境变量引用（如 `${DB_PASSWORD:}`），移除硬编码 root。
- [ ] **P1** `src/main/resources/application.yml:33` — `cookie-secure` 生产环境设为 `true`（或通过 profile 区分，默认更安全）。
- [ ] **P1** `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:137` — logout 日志对 token 脱敏（截断/掩码）或移除 token 字段。
- [ ] **P1** `M016` `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:106` — 显式指定时区（如 `LocalDateTime.now(ZoneId.systemDefault())` 注释说明或统一 UTC），消除默认时区歧义。

### P2（可选）

- [ ] **P2** `src/main/java/com/antdigital/auth/manager/cache/SessionManager.java:122` — 为 `cleanupExpiredSessions` 注册 `@Scheduled` 定时清理，并为 LoginAttemptManager/CaptchaManager 增加过期淘汰，避免内存泄漏。
- [ ] **P2** `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:48` — 评估为 `login` 的 DB 写操作加 `@Transactional`，保证失败计数与会话创建的一致性。
- [ ] **P2** `src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java:135-141` — 用原子化结构（如单 entry 包含窗口与计数）重构 IP 限流，消除 check-then-act 竞态。
- [ ] **P2** `src/main/java/com/antdigital/auth/service/impl/AuthServiceImpl.java:69` — 锁定判定同时考虑 DB `lock_until`（或统一以 DB 为准），避免重启后锁定状态丢失。
- [ ] **P2** `src/main/java/com/antdigital/auth/manager/cache/LoginAttemptManager.java:81` — 移除未被调用的死代码 `recordFailure` 方法。
