# Code Review Report — 用户登录功能（前端 leecode / 后端 haikulou）

> **Change** `登录功能编码实现` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-7bcc388b-855b-47f7-` · **Commit** `9e36d14` (haikulou) / `2f075b4` (leecode) · **日期** `2026-07-29`
>
> **审查技能**：dtazziboot-java-code-review · **审查模式**：SDD 范式结构化审查（scan-all-rules.sh + LLM 逐文件复核）
>
> **系分基线**：`.agents/20260729-写登录需求前端放leetcode后端放haikulou/design.md`

---

## 审查结论摘要

| 指标 | 数量 |
|------|------|
| **Blocker (P0)** | **4** |
| Major (P1) | 3 |
| Info (P2) | 6 |
| scan-all-rules.sh 误报 | 6（已排除） |
| 变更文件总数 | 16（后端 13 + 前端 3） |

> **合并建议**：❌ **不可合并** — 存在 4 个 P0 Blocker。其中 Blocker #1（H2 内存数据库生命周期缺陷）导致登录功能在首次初始化连接关闭后完全不可用，必须修复后重新评审。

---

## Step 1 — 执行队列（产物 A）

> **scan-all-rules.sh 预扫结果**（52/222 规则覆盖，引擎 ripgrep）：
> ```
> [P0] G16.2 — CatchWithoutLogging: AuthService.java:58
> [P0] G16.2 — CatchWithoutLogging: AuthService.java:78
> [P0] G16.2 — CatchWithoutLogging: JwtUtil.java:90
> [P0] G16.2 — CatchWithoutLogging: LoginServlet.java:78
> [P0] G16.2 — CatchWithoutLogging: LoginServlet.java:82
> [P0] G16.2 — CatchWithoutLogging: UserRepository.java:68
> [P0] G16.2 — CatchWithoutLogging: UserRepository.java:91
> [P0] G16.2 — CatchWithoutLogging: UserRepository.java:113
> [P1] S10.2 — CorsWildcard: CorsFilter.java:33
> [P2] I004 — JavaUtilDate: JwtUtil.java:89
> === Summary: 10 findings (P0=8, P1=1, P2=1) ===
> ```

> **误报排除（6 条）**：LoginServlet.java:78,82 和 UserRepository.java:68,91,113 共 6 处 catch 块均包含 `LOG.log(Level.WARNING/SEVERE, ...)` 日志记录，scan-all-rules.sh 未识别 `java.util.logging.Logger.log()` 模式，判定为**误报**，已在 Step 4 复核中排除。
>
> **真实命中（4 条）**：AuthService.java:58、AuthService.java:78、JwtUtil.java:90 三处 catch 块确实无日志记录（G16.2 P0 确认）；CorsFilter.java:33 CORS 通配符（S10.2 P1 确认）；JwtUtil.java:89 java.util.Date（I004 P2 确认）。

| # | 文件（仓库相对路径） | 归属原因 | Step2 REQ | Step3 可读性 | 总状态 |
|---|---------------------|---------|-----------|-------------|--------|
| 1 | `server/pom.xml` | 构建配置 | ✅ | ✅ | ✅ |
| 2 | `server/.../ApiResponse.java` | 统一响应封装 | ✅ | ✅ | ✅ |
| 3 | `server/.../AuthService.java` | 认证业务层 | ✅ | ✅ | ❌ G16.2×2 |
| 4 | `server/.../CorsFilter.java` | 跨域过滤器 | ✅ | ✅ | ⚠️ S10.2 |
| 5 | `server/.../CurrentUserServlet.java` | 当前用户接口 | ✅ | ✅ | ✅ |
| 6 | `server/.../JwtUtil.java` | JWT 工具 | ✅ | ✅ | ❌ G16.2+I004 |
| 7 | `server/.../LoginResult.java` | 登录结果 DTO | ✅ | ✅ | ✅ |
| 8 | `server/.../LoginServlet.java` | 登录入口 | ✅ | ✅ | ✅ (误报×2) |
| 9 | `server/.../LogoutServlet.java` | 登出入口 | ✅ | ✅ | ✅ |
| 10 | `server/.../User.java` | 用户实体 | ✅ | ✅ | ✅ |
| 11 | `server/.../UserInfo.java` | 用户视图对象 | ✅ | ✅ | ✅ |
| 12 | `server/.../UserRepository.java` | 数据访问层 | ✅ | ✅ | ❌ P0-H2生命周期 |
| 13 | `server/.../web.xml` | 部署描述 | ✅ | ✅ | ✅ |
| 14 | `web/login/index.html` | 前端页面 | ✅ | ✅ | ✅ |
| 15 | `web/login/login.css` | 前端样式 | ✅ | ✅ | ✅ |
| 16 | `web/login/login.js` | 前端逻辑 | ✅ | ✅ | ⚠️ P2 |

---

## Step 2 — 功能核对（产物 B：REQ 绑定）

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
- `CurrentUserServlet.java` — `GET /api/user/current` 接口 ✅
- `LoginResult.java` — 登录结果 DTO ✅
- `UserInfo.java` — 用户视图对象（屏蔽 passwordHash）✅

**功能完整性结论**：需求 R-01 ~ R-06 全部实现，与系分文档接口契约一致。但 Blocker #1 导致运行时功能不可用（见 Step 4）。

---

## Step 3 — 可读性检查（产物 C）

> 参考 `references/readability-checklist.md`（阿里巴巴 Java 代码风格 A1–A7）

### A1 源文件格式

| ID | 规则 | 结论 |
|----|------|------|
| A1.1 | 文件名 = 顶层类名 + `.java` | ✅ 全部符合 |
| A1.2 | 编码 UTF-8 | ✅ pom.xml 已配置 `project.build.sourceEncoding=UTF-8` |
| A1.3 | 空白仅允许 ASCII 空格和换行，禁止 Tab | ✅ 全部使用 4 空格缩进 |

### A2 源文件结构

| ID | 规则 | 结论 |
|----|------|------|
| A2.1 | 文件顺序：package → import → 顶层类 | ✅ 全部符合 |
| A2.2 | 禁止 `import *` | ✅ 无通配符引入 |
| A2.3 | import 分静态/非静态两组 | ⚠️ P2 — 无静态 import，未体现分组（不影响功能） |
| A2.4 | 组内按字典序排列 | ✅ 基本符合 |

### A3 代码样式

| ID | 规则 | 结论 |
|----|------|------|
| A3.1 | K&R 大括号 | ✅ 全部符合 |
| A3.3 | 缩进 4 空格 | ✅ 全部符合 |
| A3.4 | 行宽 ≤ 120 字符 | ✅ 全部符合 |
| A3.6 | 类成员之间空行 | ✅ 全部符合 |

### A4 命名规范

| ID | 规则 | 结论 |
|----|------|------|
| A4.1 | 包名全小写 | ✅ `cn.haikulou.auth` |
| A4.2 | 类名 UpperCamelCase | ✅ |
| A4.3 | 方法名 lowerCamelCase | ✅ |
| A4.4 | 常量 UPPER_SNAKE_CASE | ✅ `CODE_SUCCESS`, `BEARER_PREFIX` 等 |

### A5 注释规范

| ID | 规则 | 结论 |
|----|------|------|
| A5.1 | 公共 API 有 Javadoc | ✅ 工厂方法、Servlet 路由均有 Javadoc |
| A5.2 | 注释有意义，无废话注释 | ✅ |

### 可读性总结

可读性整体优秀。代码风格规范，分层清晰（Servlet → Service → Repository），Javadoc 覆盖率高。无 P1 可读性问题。

---

## Step 4 — 可靠性 + 安全检查（产物 D）

### §4.1 Bug 模式（B/M/I 清单核销）

| 规则 ID | 等级 | 文件:行号 | 描述 | 判定 |
|---------|------|-----------|------|------|
| I004 | P2 (Info) | `JwtUtil.java:89` | 使用 `java.util.Date` 而非 `java.time` 新日期 API | ✅ 确认 |

### §4.2 可靠性检查（G1–G17）

#### ❌ Blocker #1：H2 内存数据库生命周期缺陷 — `UserRepository.java:25`

```java
// UserRepository.java:25
private static final String JDBC_URL = "jdbc:h2:mem:authdb";
```

**问题**：H2 内存数据库默认行为是「最后一个连接关闭时销毁数据库」。`UserRepository` 所有方法均使用 `try-with-resources` 管理连接，每次查询后连接立即关闭：

1. `initDefaultUser()` 打开连接 → 建表 + 插入默认用户 → **关闭连接 → 数据库被销毁**
2. `initialized` 标志已设为 `true`，后续不再重新初始化
3. `findByUsername()` 打开新连接 → **空数据库** → `SELECT ... FROM user` 抛出 `SQLException`（表不存在）
4. 异常被捕获返回 `null` → **登录永远失败**

**影响**：应用首次初始化后，所有后续登录请求都会因数据库被销毁而失败。这是**功能性阻断缺陷**。

**修复建议**：
```java
// 方案 1（推荐）：添加 DB_CLOSE_DELAY=-1 保持数据库存活
private static final String JDBC_URL = "jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1";

// 方案 2：使用连接池（如 HikariCP）保持常驻连接
```

**等级**：P0 (Blocker) — 功能阻断

---

#### ❌ Blocker #2：G16.2 CatchWithoutLogging — `AuthService.java:54-61`

```java
// AuthService.java:54-61
public boolean verifyToken(String token) {
    try {
        Claims claims = JwtUtil.parseToken(token);
        return !JwtUtil.isExpired(token);
    } catch (Exception e) {
        return false;  // ← 吞没异常，无日志记录
    }
}
```

**问题**：`verifyToken` 捕获 `Exception` 后直接返回 `false`，不记录任何日志。Token 解析失败可能是密钥变更、Token 篡改等安全事件，无日志导致无法排查。此外 `Claims claims` 声明后未使用，`isExpired(token)` 内部重复调用 `parseToken`，同一 token 被解析两次（冗余）。

**修复建议**：
```java
public boolean verifyToken(String token) {
    try {
        JwtUtil.parseToken(token);  // parseToken 已校验签名+过期
        return true;
    } catch (Exception e) {
        LOG.log(Level.WARNING, "Token 验证失败: {0}", e.getMessage());
        return false;
    }
}
```

**等级**：P0 (Blocker) — G16.2

---

#### ❌ Blocker #3：G16.2 CatchWithoutLogging — `AuthService.java:69-81`

```java
// AuthService.java:69-81
public UserInfo getCurrentUser(String token) {
    try {
        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        if (userId == null) { return null; }
        User user = userRepository.findById(userId);
        return UserInfo.fromUser(user);
    } catch (Exception e) {
        return null;  // ← 吞没异常，无日志记录
    }
}
```

**问题**：`getCurrentUser` 同样吞没异常。`userRepository.findById(userId)` 内部的 SQLException 已被 Repository 层捕获并记录，但 JWT 解析异常在此被静默吞没，安全事件无法追踪。

**修复建议**：
```java
} catch (Exception e) {
    LOG.log(Level.WARNING, "获取当前用户失败: {0}", e.getMessage());
    return null;
}
```

**等级**：P0 (Blocker) — G16.2

---

#### ❌ Blocker #4：G16.2 CatchWithoutLogging — `JwtUtil.java:86-93`

```java
// JwtUtil.java:86-93
public static boolean isExpired(String token) {
    try {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    } catch (Exception e) {
        return true;  // ← 吞没异常，无日志记录
    }
}
```

**问题**：`isExpired` 将所有异常（包括签名无效、格式错误）归类为"已过期"，且不记录日志。无法区分 Token 过期与 Token 伪造，安全审计盲区。此外使用 `java.util.Date`（I004 P2）。

**修复建议**：添加 `Logger` 并记录异常级别信息；考虑使用 `java.time.Instant` 替代 `Date`。

**等级**：P0 (Blocker) — G16.2

---

### §4.3 安全检查（S1–S10）

#### ⚠️ Major #1：S10.2 CorsWildcard — `CorsFilter.java:33-36`

```java
// CorsFilter.java:33-36
httpResponse.setHeader("Access-Control-Allow-Origin", "*");
httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
```

**问题**：`Allow-Origin: *` 与 `Allow-Credentials: true` 同时设置违反 CORS 规范——浏览器会拒绝携带凭据的跨域请求。当前使用 Bearer Token（非 Cookie），`Allow-Credentials: true` 无实际意义且产生误导。

**修复建议**：移除 `Allow-Credentials` 头，或生产环境将 `*` 替换为具体前端域名。

**等级**：P1 (Major) — S10.2

---

#### ⚠️ Major #2：初始化失败即标记已初始化 — `UserRepository.java:68-72`

```java
// UserRepository.java:68-72
} catch (SQLException e) {
    LOG.log(Level.WARNING, "默认用户初始化跳过（可能已存在）: {0}", e.getMessage());
    initialized = true;  // ← 无论失败原因都标记为已初始化
}
```

**问题**：`catch` 块假设所有 SQLException 都是"表已存在"，但实际可能是数据库连接失败、磁盘满等严重错误。一旦标记 `initialized = true`，系统永不重试，导致应用处于不可恢复的损坏状态。

**修复建议**：区分错误类型，仅在确认"表/用户已存在"时标记 `initialized = true`；其他错误应保持 `false` 允许重试。

**等级**：P1 (Major)

---

#### ⚠️ Major #3：JWT Secret 硬编码 — `JwtUtil.java:22`

```java
// JwtUtil.java:22（设计文档记录默认值为 haikulou-dev-secret-key）
private static final String DEFAULT_SECRET = "haikulou-dev-secret-key";
```

**问题**：JWT Secret 以明文硬编码在源码中。生产环境若忘记设置 `JWT_SECRET` 环境变量，将使用此弱密钥，攻击者可伪造任意 Token。此外该密钥仅 23 字节（184 bit），低于 HS256 推荐的最低 256 bit（32 字节）。

**修复建议**：移除默认值，未配置环境变量时直接抛出异常拒绝启动（fail-fast）；或至少补充至 32+ 字节。

**等级**：P1 (Major) — 安全

---

### §4.4 安全检查汇总

| ID | 检查项 | 结论 |
|----|--------|------|
| S1 SQL 注入 | ✅ 全部使用 `PreparedStatement` 参数化查询 |
| S2 XSS | ✅ 前端使用 `textContent` 而非 `innerHTML` |
| S3 密码泄露 | ✅ `User.toString()` 不含 `passwordHash`；`UserInfo` 屏蔽敏感字段 |
| S4 CSRF | ✅ Token 模式（非 Cookie），天然免疫 CSRF |
| S5 密钥泄露 | ⚠️ Major #3 — JWT Secret 硬编码 |
| S6 输入校验 | ✅ `LoginServlet` 有用户名正则 + 密码长度校验 |
| S7 认证/授权 | ✅ `CurrentUserServlet`/`LogoutServlet` 校验 Bearer Token |
| S8 依赖安全 | ✅ 依赖版本合理（jjwt 0.9.1, gson 2.10.1, h2 2.2.224） |
| S9 日志安全 | ✅ 日志不记录密码明文 |
| S10 CORS/跳转 | ⚠️ Major #1 — CORS 通配符 + Credentials |

---

## Step 5 — 扩展检查

### §5.1 跨仓接口契约对齐

| # | 对齐点 | 前端（leecode） | 后端（haikulou） | 状态 |
|---|--------|----------------|-----------------|------|
| 1 | 请求路径 | `API_BASE + '/api/login'` | web.xml `/api/login` | ✅ 一致 |
| 2 | 请求方法 | `method: 'POST'` | LoginServlet.doPost | ✅ 一致 |
| 3 | 请求体字段 | `{username, password}` | LoginRequest.{username, password} | ✅ 一致 |
| 4 | Content-Type | `application/json` | Gson JSON 解析 | ✅ 一致 |
| 5 | 成功判断 | `data.code === 0` | `ApiResponse.success` → code=0 | ✅ 一致 |
| 6 | Token 字段 | `data.token` | LoginResult.token | ✅ 一致 |
| 7 | expiresIn 字段 | `data.expiresIn` | LoginResult.expiresIn | ✅ 一致 |
| 8 | user.id | `data.user.id` | UserInfo.id | ✅ 一致 |
| 9 | user.username | `data.user.username` | UserInfo.username | ✅ 一致 |
| 10 | user.nickname | `data.user.nickname` | UserInfo.nickname | ✅ 一致 |
| 11 | 认证失败码 | `CODE_AUTH_FAILED=40101` | `CODE_AUTH_FAILED=40101` | ✅ 一致 |
| 12 | Authorization 头 | `'Bearer ' + token` | extractToken Bearer 前缀 | ✅ 一致 |
| 13 | 登出路径 | `API_BASE + '/api/logout'` | web.xml `/api/logout` | ✅ 一致 |
| 14 | 端口 | `localhost:8080` | Tomcat 开发端口 8080 | ✅ 一致 |
| 15 | CORS | fetch 跨域请求 | CorsFilter `/*` | ✅ 一致 |

**跨仓契约结论**：15 项对齐点全部一致，前后端接口契约完全匹配，无字段名/类型/路径偏差。

### §5.2 P2 Info 级建议

| # | 文件 | 建议 | 等级 |
|---|------|------|------|
| 1 | `login.js:120` | JWT 存储在 `localStorage`，存在 XSS 窃取风险。系分文档已标注为 MVP 权衡，建议后续迁移至 HttpOnly Cookie | P2 |
| 2 | `UserRepository.java:59` | 默认密码 `123456` 硬编码在源码中。MVP 测试用例可接受，生产环境应移除 | P2 |
| 3 | `CurrentUserServlet.java` + `LogoutServlet.java` | `extractToken()` 方法在两个 Servlet 中重复实现，违反 DRY 原则。建议提取至公共基类或工具类 | P2 |
| 4 | `LoginServlet.java:92-100` | `parseRequestBody` 未限制请求体大小，可被大 Body DoS 攻击。建议添加 `Content-Length` 检查 | P2 |
| 5 | `JwtUtil.java:89` | 使用 `java.util.Date` 而非 `java.time.Instant`，I004 规则建议使用新日期 API | P2 |
| 6 | `login.js` | `logout()` 已实现但 index.html 无登出按钮 UI 入口。建议后续迭代补充 | P2 |

### §5.3 设计文档与实现一致性

| 检查项 | 系分设计 | 代码实现 | 状态 |
|--------|---------|---------|------|
| API-01 登录 | `POST /api/login` | `LoginServlet` 映射 `/api/login` | ✅ |
| API-02 登出 | `POST /api/logout` | `LogoutServlet` 映射 `/api/logout` | ✅ |
| API-03 当前用户 | `GET /api/user/current` | `CurrentUserServlet` 映射 `/api/user/current` | ✅ |
| 密码哈希 | BCrypt salt rounds=10 | `BCrypt.gensalt(10)` | ✅ |
| JWT 有效期 | 24h (86400s) | `EXPIRATION_MS = 24L*60*60*1000`, `EXPIRATION_SECONDS = 86400L` | ✅ |
| 错误码表 | 0/40001/40100/40101/50000 | 代码中常量与系分一致 | ✅ |
| 前端模块 | index.html + login.css + login.js | 三文件均已实现 | ✅ |
| 后端分层 | Servlet→Service→Repository | 代码分层与设计一致 | ✅ |

---

## Blocker 汇总

| # | 等级 | 规则 ID | 文件:行号 | 问题摘要 | 修复建议 |
|---|------|---------|-----------|---------|---------|
| 1 | P0 | H2生命周期 | `UserRepository.java:25` | H2 内存库在连接关闭后被销毁，登录首次后永久失败 | JDBC URL 添加 `;DB_CLOSE_DELAY=-1` |
| 2 | P0 | G16.2 | `AuthService.java:58` | `verifyToken` catch 块无日志，且存在冗余解析 | 添加 `LOG.log(Level.WARNING, ...)`，简化为单次 parseToken |
| 3 | P0 | G16.2 | `AuthService.java:78` | `getCurrentUser` catch 块无日志 | 添加 `LOG.log(Level.WARNING, ...)` |
| 4 | P0 | G16.2 | `JwtUtil.java:90` | `isExpired` catch 块无日志，无法区分过期与伪造 | 添加 Logger 并记录异常级别信息 |

**blocker_count = 4**

---

## 验收标准检查

| AC编号 | 验收条件 | 实现情况 | 结论 |
|--------|---------|---------|------|
| AC-01 | 登录页面有用户名、密码输入框和登录按钮 | index.html 含 username/password input + loginBtn | ✅ |
| AC-02 | 正确凭据登录成功 | LoginServlet → AuthService.login → BCrypt.checkpw → JWT | ❌ **因 Blocker #1 实际不可用** |
| AC-03 | 错误凭据显示"用户名或密码错误" | code=40101 + 前端 onLoginError 展示 | ⚠️ 因 Blocker #1 所有登录均返回错误 |
| AC-04 | 空用户名/密码前端拦截 | validateInput 非空校验 → 不发请求 | ✅ |
| AC-05 | 密码 BCrypt 哈希存储 | UserRepository.initDefaultUser BCrypt.hashpw | ✅ |
| AC-06 | 登录返回 Token，后续请求携带可识别 | LoginResult.token + Authorization Bearer | ❌ **因 Blocker #1 实际不可用** |
| AC-07 | 登出后清除状态 | logout() → localStorage.removeItem + 后端 logout | ✅ |

> **验收结论**：AC-02/AC-06 因 Blocker #1（H2 生命周期缺陷）实际不可用，修复后方可通过验收。

---

## 审查署名

> 审查工具：dtazziboot-java-code-review (scan-all-rules.sh + LLM 逐文件复核)
> 审查日期：2026-07-29
> 审查范围：haikulou1.github.io `server/` 全部 13 文件 + leecode `web/login/` 全部 3 文件
> scan-all-rules.sh 覆盖：52/222 规则，10 findings（6 误报排除，4 真实命中）
