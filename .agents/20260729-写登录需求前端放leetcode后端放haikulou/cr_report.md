# Code Review Report — 用户登录功能（前端 leecode / 后端 haikulou）

> **Change** `登录功能编码实现 + Round 1 问题修复` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-7bcc388b-855b-47f7-` · **Commit** `3dd20e9` (haikulou) / `2f075b4` (leecode) · **日期** `2026-07-29`
>
> **审查技能**：dtazziboot-java-code-review · **审查模式**：SDD 范式结构化审查（逐文件 LLM 复核 + 跨仓接口契约对齐）
>
> **系分基线**：`.agents/20260729-写登录需求前端放leetcode后端放haikulou/design.md`
>
> **评审轮次**：Round 2（Round 1 修复后复审）

---

## 审查结论摘要

| 指标 | Round 1 | Round 2 |
|------|---------|---------|
| **Blocker (P0)** | 4 | **0** |
| Major (P1) | 3 | 2 |
| Info (P2) | 6 | 6 |
| 变更文件总数 | 16（后端 13 + 前端 3） | 同左 |

> **合并建议**：✅ **可合并** — Round 1 的 4 个 P0 Blocker 已全部修复并验证通过。Round 2 未发现新的 P0 级阻塞问题。2 个 P1 Major 建议在生产部署前修复，不阻塞当前合并。

---

## Round 1 修复验证

| # | Round 1 Blocker | 规则 | 文件 | 行号 | 修复方案 | Round 2 验证 |
|---|----------------|------|------|------|---------|-------------|
| 1 | H2 内存库连接关闭后销毁，登录首次后永久失败 | H2-lifecycle | `UserRepository.java` | 25 | JDBC URL 添加 `;DB_CLOSE_DELAY=-1` | ✅ **已修复** — `JDBC_URL = "jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1"` 已到位，H2 内存库在最后一个连接关闭后保持存活 |
| 2 | verifyToken catch 块无日志记录 | G16.2 | `AuthService.java` | 59-61 | 添加 `LOG.log(Level.WARNING, ...)` | ✅ **已修复** — catch 块已含 `LOG.log(Level.WARNING, "Token 验证失败: {0}", e.getMessage())` |
| 3 | getCurrentUser catch 块无日志记录 | G16.2 | `AuthService.java` | 80-82 | 添加 `LOG.log(Level.WARNING, ...)` | ✅ **已修复** — catch 块已含 `LOG.log(Level.WARNING, "获取当前用户失败: {0}", e.getMessage())` |
| 4 | isExpired catch 块无日志记录 | G16.2 | `JwtUtil.java` | 95-97 | 添加 Logger 并记录异常级别信息 | ✅ **已修复** — catch 块已含 `LOG.log(Level.WARNING, "Token 解析失败，判定为已过期: {0}", e.getMessage())` |

> **结论**：4 个 P0 Blocker 全部修复验证通过，无回归。

---

## Step 1 — 执行队列

| # | 文件（仓库相对路径） | 归属原因 | Step2 REQ | Step3 可读性 | Step4 可靠性 | 总状态 |
|---|---------------------|---------|-----------|-------------|-------------|--------|
| 1 | `server/pom.xml` | 构建配置 | ✅ | ✅ | ✅ | ✅ |
| 2 | `server/.../ApiResponse.java` | 统一响应封装 | ✅ | ✅ | ✅ | ✅ |
| 3 | `server/.../AuthService.java` | 认证业务层 | ✅ | ✅ | ✅ | ✅ |
| 4 | `server/.../CorsFilter.java` | 跨域过滤器 | ✅ | ✅ | ⚠️ P1-S10.2 | ⚠️ |
| 5 | `server/.../CurrentUserServlet.java` | 当前用户接口 | ✅ | ✅ | ✅ | ✅ |
| 6 | `server/.../JwtUtil.java` | JWT 工具 | ✅ | ✅ | ⚠️ P1-signWith + P2-I004 | ⚠️ |
| 7 | `server/.../LoginResult.java` | 登录结果 DTO | ✅ | ✅ | ✅ | ✅ |
| 8 | `server/.../LoginServlet.java` | 登录入口 | ✅ | ✅ | ✅ | ✅ |
| 9 | `server/.../LogoutServlet.java` | 登出入口 | ✅ | ✅ | ✅ | ✅ |
| 10 | `server/.../User.java` | 用户实体 | ✅ | ✅ | ✅ | ✅ |
| 11 | `server/.../UserInfo.java` | 用户视图对象 | ✅ | ✅ | ✅ | ✅ |
| 12 | `server/.../UserRepository.java` | 数据访问层 | ✅ | ✅ | ✅ | ✅ |
| 13 | `server/.../web.xml` | 部署描述 | ✅ | ✅ | ✅ | ✅ |
| 14 | `web/login/index.html` | 前端页面 | ✅ | ✅ | ✅ | ✅ |
| 15 | `web/login/login.css` | 前端样式 | ✅ | ✅ | ✅ | ✅ |
| 16 | `web/login/login.js` | 前端逻辑 | ✅ | ✅ | ⚠️ P2 | ⚠️ |

---

## Step 2 — 功能核对（REQ 绑定）

> 系分文档：`.agents/20260729-写登录需求前端放leetcode后端放haikulou/design.md`

| REQ ID | 需求项 | 实现文件 | 实现状态 | 验收 |
|--------|--------|---------|---------|------|
| R-01 | 用户登录页面 | [leecode] `web/login/index.html` | ✅ 已实现 | AC-01 ✅ |
| R-02 | 登录认证接口 | [haikulou] `LoginServlet.java` | ✅ 已实现 | AC-02/03 ✅ |
| R-03 | 登录状态保持 | [haikulou] `JwtUtil.java` + [leecode] `login.js` | ✅ 已实现 | AC-06 ✅ |
| R-04 | 登录失败提示 | [leecode] `login.js` + [haikulou] `LoginServlet.java` | ✅ 已实现 | AC-03/04 ✅ |
| R-05 | 登出功能 | [haikulou] `LogoutServlet.java` + [leecode] `login.js` | ✅ 已实现 | AC-07 ✅ |
| R-06 | 密码安全存储 | [haikulou] `UserRepository.java` (BCrypt) | ✅ 已实现 | AC-05 ✅ |

**额外实现（系分 API-03 设计，代码已实现）**：

| API ID | 接口 | 实现文件 | 状态 |
|--------|------|---------|------|
| API-01 | `POST /api/login` | `LoginServlet.java` | ✅ |
| API-02 | `POST /api/logout` | `LogoutServlet.java` | ✅ |
| API-03 | `GET /api/user/current` | `CurrentUserServlet.java` | ✅ |

---

## Step 3 — 可读性检查

| 检查项 | 结果 | 备注 |
|--------|------|------|
| 类/方法 Javadoc 覆盖率 | ✅ 良好 | 所有 public 类和方法均有 Javadoc，含 `@param`/`@return` |
| 命名规范一致性 | ✅ 良好 | 包名 `cn.haikulou.auth`，类名大驼峰，常量大写蛇形 |
| 方法行数 ≤80 | ✅ 通过 | 最长方法 `LoginServlet.doPost` 约 40 行 |
| 注释质量 | ✅ 良好 | 关键逻辑有行内注释（如双检锁、错误码区分） |

### §3.1 发现（Info 级）

| ID | 严重性 | 位置 | 描述 | 确认 |
|----|--------|------|------|------|
| I-3 | P2 (Info) | `CurrentUserServlet.java:62-67` + `LogoutServlet.java:59-64` | `extractToken` 方法与 `BEARER_PREFIX` 常量在两个 Servlet 中完全重复 | ✅ 确认 |

**修复建议**：抽取 `TokenExtractor` 工具类或基类 `BaseAuthServlet`，消除重复。

---

## Step 4 — 可靠性检查（G1–G17）

### §4.1 G16.2 — CatchWithoutLogging（Round 1 修复验证）

逐文件审查所有 catch 块，确认日志记录完整性：

| 文件 | catch 块行号 | 是否含日志 | Round 2 状态 |
|------|------------|-----------|-------------|
| `AuthService.java` | 59-61 (verifyToken) | `LOG.log(Level.WARNING, ...)` | ✅ 已修复 |
| `AuthService.java` | 80-82 (getCurrentUser) | `LOG.log(Level.WARNING, ...)` | ✅ 已修复 |
| `JwtUtil.java` | 95-97 (isExpired) | `LOG.log(Level.WARNING, ...)` | ✅ 已修复 |
| `LoginServlet.java` | 78-81 (JsonSyntaxException) | `LOG.log(Level.WARNING, ...)` | ✅ 原本已有 |
| `LoginServlet.java` | 82-85 (Exception) | `LOG.log(Level.SEVERE, ...)` | ✅ 原本已有 |
| `UserRepository.java` | 68-77 (initDefaultUser) | `LOG.log(Level.INFO/SEVERE, ...)` | ✅ 原本已有 |
| `UserRepository.java` | 96-98 (findByUsername) | `LOG.log(Level.SEVERE, ...)` | ✅ 原本已有 |
| `UserRepository.java` | 118-120 (findById) | `LOG.log(Level.SEVERE, ...)` | ✅ 原本已有 |

> **G16.2 全量验证通过**：8 处 catch 块全部含日志记录，0 处遗漏。

### §4.2 S10.2 — CorsWildcard（P1，未修复）

| ID | 严重性 | 规则 | 文件 | 行号 | 描述 | 确认 |
|----|--------|------|------|------|------|------|
| P1-2 | P1 (Major) | S10.2 | `CorsFilter.java` | 33 | `Access-Control-Allow-Origin: *` 配合 `Access-Control-Allow-Headers: Content-Type, Authorization`，允许任意源携带 Authorization 头发起跨域请求 | ✅ 确认 |

**分析**：`Allow-Origin: *` 意味着任何域名的前端页面均可向本服务发送带 Bearer Token 的请求。结合 localStorage 存储 Token 的前端方案，存在 CSRF/跨站 Token 盗用风险。代码注释已声明"开发期全允许，生产期替换为前端域名白名单"，作为 MVP 开发期配置可接受，但生产部署前必须修复。

**修复建议**：生产环境将 `*` 替换为具体前端域名（如 `https://leetcode.example.com`），或通过 `web.xml` `<init-param>` 注入可配置白名单。

### §4.3 JwtUtil signWith String 重载误用（P1，新发现）

| ID | 严重性 | 规则 | 文件 | 行号 | 描述 | 确认 |
|----|--------|------|------|------|------|------|
| P1-1 | P1 (Major) | 自定义-JWT-API | `JwtUtil.java` | 67 | `signWith(SignatureAlgorithm.HS256, getSecret())` 使用 jjwt 0.9.1 的 String 重载，该重载将参数视为 Base64 编码并调用 `TextCodec.BASE64.decode()` 解码 | ✅ 确认 |

**详细分析**：

jjwt 0.9.1 的 `signWith(SignatureAlgorithm.HS256, String)` 重载内部调用 `DatatypeConverter.parseBase64Binary(secret)` 对字符串进行 Base64 解码。`DEFAULT_SECRET = "haikulou-dev-secret-key-20260729"` 含 4 个连字符 `-`，不属于标准 Base64 字母表（`A-Za-z0-9+/`）。

验证结果（Python `base64.b64decode(validate=True)`）：
- `"haikulou-dev-secret-key-20260729"` → **解码失败**：`Error: Only base64 data is allowed`
- `"haikuloudevsecretkey20260729"`（去连字符）→ **解码成功**：21 字节

jjwt 0.9.1 内部使用 JAXB `DatatypeConverter`（宽松解码器），不会抛异常但会**静默跳过**非法字符。实际效果：
1. **不会运行时崩溃**（宽松解码器跳过连字符）
2. **实际密钥长度约 21 字节（168 bit）**，低于代码注释声称的"32 字节，满足 HS256 最低 256-bit 要求"
3. 签发与解析使用同一 `getSecret()`，故 sign/verify 周期一致，功能不受影响
4. 但密钥强度低于预期，存在安全隐患

**修复建议**：改用 byte[] 重载，将 String 直接转为 UTF-8 字节：
```java
.signWith(SignatureAlgorithm.HS256, getSecret().getBytes(StandardCharsets.UTF_8))
```
同时 `parseToken` 中的 `setSigningKey(getSecret())` 也需改为 `setSigningKey(getSecret().getBytes(StandardCharsets.UTF_8))`。

### §4.4 其他可靠性发现

| ID | 严重性 | 规则 | 文件 | 行号 | 描述 | 确认 |
|----|--------|------|------|------|------|------|
| I-1 | P2 (Info) | I004 | `JwtUtil.java` | 61,66 | 使用 `java.util.Date` 而非 `java.time` 新日期 API | ✅ 确认 |
| I-2 | P2 (Info) | 自定义-DoS | `LoginServlet.java` | 92-99 | `parseRequestBody` 无请求体大小限制，可被大 payload DoS | ✅ 确认 |
| I-4 | P2 (Info) | 自定义-DeadCode | `login.js` | 12 | `CODE_AUTH_FAILED = 40101` 已定义但未在代码中使用 | ✅ 确认 |
| I-5 | P2 (Info) | 自定义-Secret | `JwtUtil.java` | 24 | `DEFAULT_SECRET` 硬编码在源码中，虽有环境变量覆盖机制，但默认值已进入版本库 | ✅ 确认 |
| I-6 | P2 (Info) | 自定义-DI | `AuthService.java` | 21 | `new UserRepository()` 硬编码依赖，不利于单元测试 | ✅ 确认 |

**修复建议**：
- I-1：迁移到 `java.time.Instant` / `java.time.temporal.ChronoUnit`（非阻塞）
- I-2：在 `parseRequestBody` 中限制读取字节数（如 4KB），超限返回 400
- I-4：移除未使用常量或在前端错误码分支中使用
- I-5：生产环境通过 `JWT_SECRET` 环境变量覆盖，默认值仅限开发期
- I-6：后续迭代引入依赖注入框架或工厂方法

---

## Step 5 — 自定义扩展检查

### §5.1 安全性检查

| 检查项 | 文件 | 结果 | 备注 |
|--------|------|------|------|
| SQL 注入防护 | `UserRepository.java` | ✅ 通过 | 全部使用 `PreparedStatement` 参数化查询 |
| XSS 防护 | `login.js:202` | ✅ 通过 | 使用 `textContent` 而非 `innerHTML` |
| 密码明文传输 | `design.md` + `LoginServlet.java` | ✅ 可接受 | 明文 HTTPS 下安全，MVP 阶段可接受 |
| 密码哈希存储 | `UserRepository.java:59` | ✅ 通过 | BCrypt `gensalt(10)`，cost=10 合理 |
| 密码哈希泄露 | `User.java:98-105` `toString()` | ✅ 通过 | `toString()` 不含 `passwordHash` 字段 |
| 敏感字段屏蔽 | `UserInfo.java` | ✅ 通过 | 对外 VO 不含 `passwordHash`，`fromUser()` 只取 id/username/nickname |
| JWT Secret 管理 | `JwtUtil.java:43-50` | ⚠️ P1-1 | 见 §4.3，String 重载误用导致密钥强度不足 |
| CORS 安全 | `CorsFilter.java:33` | ⚠️ P1-2 | 见 §4.2，`Allow-Origin: *` 生产需替换 |

### §5.2 并发安全检查

| 检查项 | 文件 | 结果 | 备注 |
|--------|------|------|------|
| 双检锁正确性 | `UserRepository.java:46-51` | ✅ 通过 | `volatile` + `synchronized` 双检锁，`initialized` 标记 volatile 保证可见性 |
| Servlet 线程安全 | `LoginServlet/LogoutServlet/CurrentUserServlet` | ✅ 通过 | 无实例变量状态（`Gson`/`AuthService` 为线程安全或无状态） |
| H2 连接管理 | `UserRepository.java:88-99,108-122` | ✅ 通过 | try-with-resources 保证 Connection/PreparedStatement/ResultSet 自动关闭 |

### §5.3 前端安全检查

| 检查项 | 文件 | 结果 | 备注 |
|--------|------|------|------|
| Token 存储方式 | `login.js:120` | ✅ 可接受 | localStorage 存储，MVP 阶段可接受；生产建议改用 HttpOnly Cookie |
| CSRF 防护 | N/A | ✅ 不适用 | Bearer Token 方案天然防 CSRF（不依赖 Cookie 自动携带） |
| 表单 autocomplete | `index.html:22,34` | ✅ 通过 | `autocomplete="username"` / `autocomplete="current-password"` 正确设置 |
| 输入 maxlength | `index.html:23,35` | ✅ 通过 | username maxlength=32，password maxlength=64，与后端校验一致 |

---

## 跨仓接口契约对齐检查

> 后端仓库：haikulou1.github.io（commit `3dd20e9`）
> 前端仓库：leecode（commit `2f075b4`）

| # | 检查点 | 后端 | 前端 | 对齐 |
|---|--------|------|------|------|
| 1 | 登录 URL | `web.xml:28` `/api/login` | `login.js:81` `API_BASE + '/api/login'` | ✅ |
| 2 | 登录 HTTP Method | `LoginServlet.doPost` | `login.js:82` `method: 'POST'` | ✅ |
| 3 | 登录请求 Content-Type | `LoginServlet` Gson 解析 JSON | `login.js:84` `'Content-Type': 'application/json'` | ✅ |
| 4 | 登录请求字段 username | `LoginRequest.username` | `login.js:87` `username: username` | ✅ |
| 5 | 登录请求字段 password | `LoginRequest.password` | `login.js:88` `password: password` | ✅ |
| 6 | 响应结构 code/message/data | `ApiResponse<T>` | `login.js:98` `result.body.code` / `result.body.message` / `result.body.data` | ✅ |
| 7 | 成功码 | `ApiResponse.success` → `code=0` | `login.js:11` `CODE_SUCCESS = 0` | ✅ |
| 8 | 认证失败码 | `LoginServlet:70` `CODE_AUTH_FAILED = 40101` | `login.js:12` `CODE_AUTH_FAILED = 40101` | ✅ |
| 9 | 登录响应 data.token | `LoginResult.token` | `login.js:119` `data.token` | ✅ |
| 10 | 登录响应 data.expiresIn | `LoginResult.expiresIn` | （前端未使用，但字段存在） | ✅ |
| 11 | 登录响应 data.user | `LoginResult.user` (UserInfo) | `login.js:122` `data.user` | ✅ |
| 12 | user.nickname | `UserInfo.nickname` | `login.js:123` `user.nickname` | ✅ |
| 13 | 登出 URL | `web.xml:38` `/api/logout` | `login.js:155` `API_BASE + '/api/logout'` | ✅ |
| 14 | 登出 Authorization 头 | `LogoutServlet.extractToken` `Bearer ` 前缀 | `login.js:158` `'Authorization': 'Bearer ' + token` | ✅ |
| 15 | CORS 预检 | `CorsFilter:39` OPTIONS → 204 | `login.js` fetch 自动处理预检 | ✅ |

> **跨仓对齐结论**：15/15 检查点全部通过，前后端接口契约完全一致。

---

## 验收标准覆盖

| AC ID | 验收标准 | 验证结果 |
|-------|---------|---------|
| AC-01 | 登录页面展示用户名/密码输入框与登录按钮 | ✅ `index.html` 含完整表单 |
| AC-02 | 输入 admin/123456 可成功登录并返回 Token | ✅ `LoginServlet` → `AuthService.login` → BCrypt 校验 → JWT 签发 |
| AC-03 | 错误密码返回认证失败提示 | ✅ HTTP 401 + `code=40101` + `message="用户名或密码错误"` |
| AC-04 | 空用户名/密码返回参数错误提示 | ✅ HTTP 400 + `code=40001` |
| AC-05 | 密码以 BCrypt 哈希存储，非明文 | ✅ `UserRepository.initDefaultUser` 使用 `BCrypt.hashpw` |
| AC-06 | Token 存储在 localStorage，后续请求携带 | ✅ `login.js:120` 存储 + `login.js:158` 携带 |
| AC-07 | 登出清除本地 Token | ✅ `login.js:166` `localStorage.removeItem` |

> **验收结论**：7/7 验收标准全部通过。

---

## 审查范围

| 仓库 | 分支 | Commit | 评审文件数 | 代码行数 |
|------|------|--------|-----------|---------|
| haikulou1.github.io | `AI/task-DEV-...` | `3dd20e9` | 13 | 1081 |
| leecode | `AI/task-DEV-...` | `2f075b4` | 3 | 377 |
| **合计** | | | **16** | **1458** |

---

## 最终结论

### 评审结果：✅ 通过（可合并）

- **P0 Blocker**：0（Round 1 的 4 个 P0 已全部修复并验证）
- **P1 Major**：2（JwtUtil signWith String 重载误用、CorsFilter 通配符，建议生产部署前修复）
- **P2 Info**：6（均为非阻塞改进建议）

### 生产部署前必须修复项

1. **P1-1**：`JwtUtil.java:67` 改用 `getSecret().getBytes(StandardCharsets.UTF_8)` 的 byte[] 重载
2. **P1-2**：`CorsFilter.java:33` 将 `*` 替换为具体前端域名

### 后续迭代建议

1. I-1：JwtUtil 迁移到 `java.time` API
2. I-2：LoginServlet 添加请求体大小限制
3. I-3：抽取 TokenExtractor 消除 Servlet 间重复代码
4. I-4：清理 login.js 未使用常量
5. I-5：JwtUtil DEFAULT_SECRET 移出源码（通过配置文件或环境变量）
6. I-6：AuthService 引入依赖注入，提升可测试性
