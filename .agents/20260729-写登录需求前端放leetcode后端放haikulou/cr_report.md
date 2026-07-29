# 代码评审报告：用户登录功能（跨仓）

> **评审元信息**
>
> | 项目 | 值 |
> |------|-----|
> | 评审主题 | 用户登录功能（前端 leecode / 后端 haikulou1.github.io） |
> | 评审阶段 | review（代码评审） |
> | 使用技能 | dtazziboot-java-code-review |
> | 评审基线 | `.agents/20260729-写登录需求前端放leetcode后端放haikulou/design.md` |
> | 后端提交 | `9e36d14` [auto-dev] 编码实现 (stage: coding, round: 1) |
> | 前端提交 | `2f075b4` [auto-dev] 编码实现 (stage: coding, round: 1) |
> | 评审日期 | 2026-07-29 |
> | Blocker 数 | 0 |
> | Major 数 | 4 |
> | Minor 数 | 4 |
> | 评审结论 | **通过（有改进建议）** — 功能完整，契约对齐，无阻断级缺陷 |

---

## 一、评审范围

### 1.1 变更文件清单

| 仓库 | 文件路径（逻辑前缀） | 类型 | 行数 |
|------|---------------------|------|------|
| [haikulou1.github.io] | `server/pom.xml` | 后端-新增 | 78 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/LoginServlet.java` | 后端-新增 | 142 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/LogoutServlet.java` | 后端-新增 | 66 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/CurrentUserServlet.java` | 后端-新增 | 69 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/AuthService.java` | 后端-新增 | 94 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/UserRepository.java` | 后端-新增 | 137 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/JwtUtil.java` | 后端-新增 | 94 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/User.java` | 后端-新增 | 107 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/UserInfo.java` | 后端-新增 | 59 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/LoginResult.java` | 后端-新增 | 49 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/ApiResponse.java` | 后端-新增 | 83 |
| [haikulou1.github.io] | `server/src/main/java/cn/haikulou/auth/CorsFilter.java` | 后端-新增 | 52 |
| [haikulou1.github.io] | `server/src/main/webapp/WEB-INF/web.xml` | 后端-新增 | 51 |
| [leecode] | `web/login/index.html` | 前端-新增 | 53 |
| [leecode] | `web/login/login.css` | 前端-新增 | 113 |
| [leecode] | `web/login/login.js` | 前端-新增 | 211 |

> 合计：后端 1081 行（13 文件），前端 377 行（3 文件），均为纯新增。

---

## 二、逐文件审查

### 2.1 [haikulou1.github.io] LoginServlet.java

#### 功能核对（对照 design.md API-01）

| 契约项 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| 路由 | `POST /api/login` | web.xml 映射 `/api/login`，doPost 重写 | ✅ 一致 |
| 请求体 | `{username, password}` JSON | Gson 反序列化 LoginRequest DTO | ✅ 一致 |
| 用户名校验 | 3-32 位字母数字下划线 | `Pattern.compile("^[a-zA-Z0-9_]{3,32}$")` | ✅ 一致 |
| 密码校验 | 长度 6-64 | `PASSWORD_MIN_LEN=6, PASSWORD_MAX_LEN=64` | ✅ 一致 |
| 成功响应 | HTTP 200 + `ApiResponse.success(LoginResult)` | `SC_OK` + `ApiResponse.success(result)` | ✅ 一致 |
| 认证失败 | HTTP 401 + code=40101 | `SC_UNAUTHORIZED` + `CODE_AUTH_FAILED=40101` | ✅ 一致 |
| 参数错误 | HTTP 400 + code=40001 | `SC_BAD_REQUEST` + `CODE_PARAM_ERROR=40001` | ✅ 一致 |
| 服务器错误 | HTTP 500 + code=50000 | `SC_INTERNAL_SERVER_ERROR` + `CODE_SERVER_ERROR=50000` | ✅ 一致 |
| 空请求体处理 | — | `gson.fromJson("")` 返回 null → validateInput 拦截 | ✅ 正确 |
| JSON 格式错误 | — | catch `JsonSyntaxException` → 400 | ✅ 正确 |

#### 可读性检查

- ✅ Javadoc 完整，含路由、请求体、响应说明
- ✅ 错误码常量命名语义清晰（`CODE_SUCCESS`/`CODE_PARAM_ERROR`/`CODE_AUTH_FAILED`/`CODE_SERVER_ERROR`）
- ✅ 方法职责单一：`parseRequestBody`/`validateInput`/`toJson` 分离清晰
- ✅ LoginRequest 作为私有静态内部类 DTO，作用域正确

#### 可靠性检查

- ✅ 异常处理层次分明：`JsonSyntaxException`（400）→ `Exception`（500），无异常吞没
- ✅ `serialVersionUID` 显式声明，序列化兼容
- ✅ 日志分级：`WARNING`（解析失败）+ `SEVERE`（服务器异常），不记录密码明文
- ⚠️ **Minor-1**：`PrintWriter out = response.getWriter()` 在 try 块外获取，若 getWriter 抛异常不会被 catch。属 Servlet 标准模式，实际影响低。
- ⚠️ **Minor-2**：design.md flowchart（Step 5.2.6）提及"Content-Type 检查"步骤，代码未显式检查 Content-Type。Gson 容错可接受，但与设计流程图存在细微偏差。

---

### 2.2 [haikulou1.github.io] AuthService.java

#### 功能核对（对照 design.md Step 5.2.2）

| 方法 | 设计要求 | 实现情况 | 结论 |
|------|---------|---------|------|
| `login(username, password)` | 查询用户 → BCrypt 校验 → 生成 JWT → 返回 LoginResult | findByUsername → BCrypt.checkpw → JwtUtil.generateToken → new LoginResult | ✅ 一致 |
| `verifyToken(token)` | 解析并验证 JWT 有效性 | parseToken + isExpired | ✅ 功能正确 |
| `getCurrentUser(token)` | 从 Token 提取 userId → 查询用户 | parseToken → claims.get("userId") → findById → UserInfo.fromUser | ✅ 一致（返回类型 UserInfo 优于设计中的 User） |
| `logout(token)` | 无状态模式，客户端清除即可 | 仅日志记录 token 前缀 | ✅ 一致 |

#### 可读性检查

- ✅ Javadoc 完整，标注业务层定位（协调 UserRepository + JwtUtil）
- ✅ 日志记录登录成功/失败，含 username 上下文
- ✅ logout 中 token 前缀截取（`Math.min(16, token.length())`）避免日志泄露完整 token

#### 可靠性检查

- ✅ `logout` 中 token null 防御：`token == null ? "null" : token.substring(...)`，无 NPE 风险
- ✅ `getCurrentUser` catch Exception 返回 null，降级合理
- ⚠️ **Major-1**：`verifyToken` 中 `Claims claims = JwtUtil.parseToken(token)` 声明后未使用，且 `JwtUtil.isExpired(token)` 内部再次调用 `parseToken`，导致同一 token 被解析两次。功能正确但存在冗余解析与未使用变量。
  - 建议：`verifyToken` 可简化为 `try { JwtUtil.parseToken(token); return true; } catch (Exception e) { return false; }`（parseToken 已校验签名+过期）
- ⚠️ **Minor-3**：`getCurrentUser` catch 块无日志记录，异常被静默吞没。建议补充 `LOG.log(Level.WARNING, "获取当前用户失败", e)`。

---

### 2.3 [haikulou1.github.io] UserRepository.java

#### 功能核对（对照 design.md Step 5.2.4）

| 方法 | 设计要求 | 实现情况 | 结论 |
|------|---------|---------|------|
| `findByUsername(username)` | 按用户名查询 | PreparedStatement 参数化查询 | ✅ 一致 |
| `findById(id)` | 按 ID 查询（getCurrentUser 依赖） | PreparedStatement 参数化查询 | ✅ 新增合理 |
| `initDefaultUser()` | 初始化默认用户 admin/BCrypt("123456") | DDL 建表 + INSERT admin/BCrypt.hashpw("123456", gensalt(10)) | ✅ 一致 |
| SQL 注入防护 | 参数化查询 | 全部 PreparedStatement，无字符串拼接 | ✅ 合规 |
| H2 内存库 | `jdbc:h2:mem:authdb` | JDBC_URL 常量 | ✅ 一致 |

#### 可读性检查

- ✅ SQL 常量提取为静态 final，命名清晰（`SQL_FIND_BY_USERNAME` 等）
- ✅ DDL 内嵌为常量，建表逻辑集中
- ✅ Javadoc 完整，标注双检锁线程安全

#### 可靠性检查

- ✅ `initDefaultUser` 使用 `synchronized` + `volatile initialized` 双检锁，线程安全
- ✅ Connection/PreparedStatement/ResultSet 全部 try-with-resources，无资源泄漏
- ✅ `User.fromResultSet(rs)` 封装列名映射，避免散落映射逻辑
- ✅ BCrypt salt rounds=10，与 design.md 6.1 一致
- ⚠️ **Minor-4**：`initDefaultUser` catch `SQLException` 后无条件设置 `initialized = true`，即使是非"已存在"原因的建表失败也会标记为已初始化。MVP 阶段 H2 内存库首次启动不会冲突，影响低。建议区分错误码（如 `23505` 唯一约束冲突）后决定是否标记初始化。

---

### 2.4 [haikulou1.github.io] JwtUtil.java

#### 功能核对（对照 design.md Step 5.2.3）

| 配置项 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| 算法 | HS256 | `SignatureAlgorithm.HS256` | ✅ 一致 |
| Secret | 环境变量 `JWT_SECRET`，默认 `haikulou-dev-secret-key` | `getSecret()` 读环境变量，fallback DEFAULT_SECRET | ✅ 一致 |
| 有效期 | 24 小时（86400 秒） | `EXPIRATION_MS=24*60*60*1000`, `EXPIRATION_SECONDS=86400` | ✅ 一致 |
| Claims | userId, username, exp | `.claim("userId",..).claim("username",..).setExpiration(..)` | ✅ 一致 |
| `generateToken(userId, username)` | 签发 JWT | builder + compact | ✅ 一致 |
| `parseToken(token)` | 解析 JWT，无效抛异常 | `Jwts.parser().setSigningKey().parseClaimsJws()` | ✅ 一致 |
| `isExpired(token)` | 判断是否过期 | parseToken + getExpiration().before(now) | ✅ 一致 |

#### 可读性检查

- ✅ 工具类 `final` + 私有构造函数，禁止实例化
- ✅ Javadoc 完整，标注环境变量名与生产警告

#### 可靠性检查

- ⚠️ **Major-2**：生产环境未设置 `JWT_SECRET` 环境变量时，静默使用硬编码默认密钥 `haikulou-dev-secret-key`。design.md 6.1 标注"JWT Secret 环境变量注入，禁止硬编码到代码仓库"为 P0，代码中 DEFAULT_SECRET 存在于源码仓库。虽然 design.md 5.2.3 自身允许"默认值仅限开发"，但缺少生产环境 fail-fast 保护（如启动时检测环境并拒绝使用默认值）。
  - 建议：增加启动检查，生产 profile 下未配置 `JWT_SECRET` 时抛异常拒绝启动
- ⚠️ **Major-3**：默认密钥 `haikulou-dev-secret-key` 为 23 字节（184 bit），低于 HS256 推荐的最低 256 bit（32 字节）密钥长度。jjwt 0.9.1 不强制校验密钥长度，但存在弱密钥隐患。
  - 建议：默认密钥补充至 32+ 字节，或使用 `Keys.secretKeyFor(SignatureAlgorithm.HS256)` 生成

---

### 2.5 [haikulou1.github.io] User.java

#### 功能核对（对照 design.md Step 5.2.1）

| 字段 | Java 类型 | DB 字段 | 实现情况 | 结论 |
|------|----------|---------|---------|------|
| id | Long | id | `Long id` + getter/setter | ✅ 一致 |
| username | String | username | `String username` | ✅ 一致 |
| passwordHash | String | password_hash | `String passwordHash` | ✅ 一致 |
| nickname | String | nickname | `String nickname` | ✅ 一致 |
| createdAt | Timestamp | created_at | `Timestamp createdAt` | ✅ 一致 |

#### 可读性检查

- ✅ Javadoc 宇标注 DB 字段下划线命名与 Java 大驼峰映射规则
- ✅ `fromResultSet` 工厂方法封装列读取
- ✅ `toString()` 不输出 passwordHash，避免日志泄露敏感字段

#### 可靠性检查

- ✅ 无异常风险点，纯数据载体

---

### 2.6 [haikulou1.github.io] UserInfo.java

#### 功能核对

- ✅ 视图对象，屏蔽 passwordHash，仅暴露 id/username/nickname
- ✅ `fromUser(user)` 工厂方法含 null 防御
- ✅ 与 design.md API-03 响应 `data.{id, username, nickname}` 字段一致

---

### 2.7 [haikulou1.github.io] LoginResult.java

#### 功能核对

- ✅ 承载 `token` + `expiresIn` + `user(UserInfo)`，与 design.md API-01 成功响应 `data.{token, expiresIn, user}` 一致
- ✅ 与前端 login.js `onLoginSuccess(data)` 解析 `data.token`/`data.user.nickname` 对齐

---

### 2.8 [haikulou1.github.io] ApiResponse.java

#### 功能核对

- ✅ 泛型封装 `{code, message, data}`，与 design.md Step 5.2.5 一致
- ✅ `success(data)` → code=0, message="success"
- ✅ `error(code, message)` → data=null
- ✅ 前端 login.js 判断 `result.body.code === CODE_SUCCESS(0)` 对齐

---

### 2.9 [haikulou1.github.io] CorsFilter.java

#### 功能核对（对照 design.md Step 4.4）

| 响应头 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| Allow-Origin | 开发期 `*` | `"*"` | ✅ 一致 |
| Allow-Methods | `GET, POST, OPTIONS` | `"GET, POST, OPTIONS"` | ✅ 一致 |
| Allow-Headers | `Content-Type, Authorization` | `"Content-Type, Authorization"` | ✅ 一致 |
| Allow-Credentials | `true` | `"true"` | ⚠️ 见 Major-4 |
| OPTIONS 预检 | 返回 204 | `SC_NO_CONTENT` + return | ✅ 一致 |

#### 可靠性检查

- ⚠️ **Major-4**：`Access-Control-Allow-Origin: *` 与 `Access-Control-Allow-Credentials: true` 同时设置，违反 CORS 规范（浏览器要求 Allow-Credentials=true 时 Allow-Origin 不得为 `*`）。当前前端 fetch 未设置 `credentials: 'include'`（默认 same-origin），不携带 Cookie，因此实际不受影响。但此配置组合在语义上矛盾，若后续引入 Cookie 认证会触发浏览器拦截。
  - 建议：开发期移除 `Allow-Credentials` 或改为动态回显 Origin；生产期改为白名单 Origin + `Allow-Credentials: true`

---

### 2.10 [haikulou1.github.io] CurrentUserServlet.java

#### 功能核对（对照 design.md API-03）

| 契约项 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| 路由 | `GET /api/user/current` | web.xml 映射 + doGet | ✅ 一致 |
| 认证 | `Authorization: Bearer <token>` | extractToken 解析 Bearer 前缀 | ✅ 一致 |
| 未认证 | HTTP 401 + code=40100 | `SC_UNAUTHORIZED` + `CODE_UNAUTHORIZED=40100` | ✅ 一致 |
| 成功响应 | `ApiResponse.success(UserInfo)` | `ApiResponse.success(userInfo)` | ✅ 一致 |

#### 可靠性检查

- ✅ token null 防御 + userInfo null 防御，双层校验
- ✅ extractToken 处理 header null 与前缀不匹配

---

### 2.11 [haikulou1.github.io] LogoutServlet.java

#### 功能核对（对照 design.md API-02）

| 契约项 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| 路由 | `POST /api/logout` | web.xml 映射 + doPost | ✅ 一致 |
| 认证 | `Authorization: Bearer <token>` | extractToken + verifyToken | ✅ 一致 |
| 未认证 | HTTP 401 + code=40100 | `SC_UNAUTHORIZED` + `CODE_UNAUTHORIZED=40100` | ✅ 一致 |
| 成功响应 | `ApiResponse.success(null)` | `ApiResponse.success(null)` | ✅ 一致 |

#### 可靠性检查

- ✅ token null 防御 + verifyToken 校验，双重保障

---

### 2.12 [haikulou1.github.io] pom.xml

#### 功能核对（对照 design.md Step 5.2.7）

| 依赖 | 设计要求 | 实现情况 | 结论 |
|------|---------|---------|------|
| javax.servlet-api | 3.1.0 (provided) | 3.1.0 (provided) | ✅ 一致 |
| gson | 2.10.1 | 2.10.1 | ✅ 一致 |
| jbcrypt | 0.4 | 0.4 | ✅ 一致 |
| jjwt | 0.9.1 | 0.9.1 | ✅ 一致 |
| h2 | 2.2.224 | 2.2.224 | ✅ 一致 |
| junit | 4.13.2 (test) | 4.13.2 (test) | ✅ 一致 |
| Java 编译目标 | 1.8 | `maven.compiler.source/target=1.8` | ✅ 一致 |
| packaging | war | `<packaging>war</packaging>` | ✅ 一致 |

---

### 2.13 [haikulou1.github.io] web.xml

#### 功能核对

| 映射项 | 设计要求 | 实现情况 | 结论 |
|--------|---------|---------|------|
| CorsFilter | `/*` | `<url-pattern>/*</url-pattern>` | ✅ 一致 |
| LoginServlet | `/api/login` | `/api/login` | ✅ 一致 |
| LogoutServlet | `/api/logout` | `/api/logout` | ✅ 一致 |
| CurrentUserServlet | `/api/user/current` | `/api/user/current` | ✅ 一致 |
| web-app version | — | 3.1 | ✅ 合理 |

---

### 2.14 [leecode] index.html

#### 功能核对（对照 design.md Step 5.1.1）

| 元素 | 设计要求 | 实现情况 | 结论 |
|------|---------|---------|------|
| 用户名输入框 | `<input type="text" id="username">` | `type="text" id="username" maxlength="32" required` | ✅ 一致 |
| 密码输入框 | `<input type="password" id="password">` | `type="password" id="password" maxlength="64" required` | ✅ 一致 |
| 登录按钮 | `<button id="loginBtn">登录</button>` | `id="loginBtn"` + btn-text/btn-loading | ✅ 一致 |
| 错误提示区 | `<div id="errorMsg">` | `id="errorMsg" role="alert"` | ✅ 一致 |
| 加载指示器 | — | `<span class="btn-loading" hidden>登录中...</span>` | ✅ 一致 |
| 回车提交 | 绑定 keypress | form submit 事件天然支持回车 | ✅ 更优 |
| autocomplete | — | `username`/`current-password` | ✅ 合理 |
| novalidate | — | `novalidate` 属性 | ✅ 自定义校验 |

---

### 2.15 [leecode] login.css

#### 功能核对

- ✅ 居中卡片式布局（flexbox），与 design.md 5.1.2 一致
- ✅ 响应式适配 `@media (max-width: 480px)`
- ✅ 加载状态样式（btn-loading）

---

### 2.16 [leecode] login.js

#### 功能核对（对照 design.md Step 5.1.3）

| 函数 | 设计要求 | 实现情况 | 结论 |
|------|---------|---------|------|
| `handleLogin()` | 获取输入 → 校验 → sendLoginRequest | trim → validateInput → sendLoginRequest | ✅ 一致 |
| `validateInput(username, password)` | 非空 + 长度校验 | 空值 + 3-32 + 6-64 | ✅ 一致 |
| `sendLoginRequest(username, password)` | fetch POST /api/login | fetch + JSON.stringify | ✅ 一致 |
| `onLoginSuccess(data)` | 存储 Token + 提示 | localStorage.setItem + textContent | ✅ 一致 |
| `onLoginError(message)` | 展示错误 | showError(message) | ✅ 一致 |
| `getToken()` | 从 localStorage 读取 | localStorage.getItem | ✅ 一致 |
| `logout()` | 调用 /api/logout + 清除 Token | fetch POST + removeItem | ✅ 一致 |
| API_BASE | `http://localhost:8080` | `const API_BASE = 'http://localhost:8080'` | ✅ 一致 |
| CODE_SUCCESS | 0 | `const CODE_SUCCESS = 0` | ✅ 一致 |
| CODE_AUTH_FAILED | 40101 | `const CODE_AUTH_FAILED = 40101` | ✅ 一致 |
| XSS 防护 | textContent | `errorMsgDiv.textContent = message` | ✅ 一致 |
| 重复提交防护 | 按钮禁用 | `setLoading(true/false)` + `loginBtn.disabled` | ✅ 一致 |

#### 可靠性检查

- ✅ fetch catch 处理网络异常，finally 恢复 loading 状态
- ✅ response.json() reject 时（非 JSON 响应体）进入 catch
- ✅ onLoginSuccess 中 nickname fallback：`user.nickname || user.username || '用户'`
- ✅ logout 网络异常时仍清除本地 Token（无状态模式降级）
- ⚠️ **Minor-5**：`logout()` 函数已实现但 index.html 无登出按钮 UI 入口。design.md R-05（登出功能）为 P1 优先级，后端接口已就绪但前端缺少触发入口。建议后续迭代补充登出按钮。
- ⚠️ **Minor-6**：`btnText`/`btnLoading` 在脚本顶层通过 `querySelector` 获取（第25-26行），依赖 script 位于 body 末尾保证 DOM 已解析。当前 index.html 确实将 `<script>` 置于 `</body>` 前，无问题。但若未来调整 script 位置可能引入 NPE。建议使用 `DOMContentLoaded` 或在函数内获取。

---

## 三、跨仓对齐点检查

| # | 对齐点 | 前端（leecode） | 后端（haikulou1.github.io） | 结论 |
|---|--------|---------------|---------------------------|------|
| 1 | 请求路径 | `API_BASE + '/api/login'` | web.xml `/api/login` | ✅ 一致 |
| 2 | 请求方法 | `method: 'POST'` | LoginServlet.doPost | ✅ 一致 |
| 3 | 请求体字段 | `{username, password}` | LoginRequest.{username, password} | ✅ 一致 |
| 4 | Content-Type | `'application/json'` | Gson JSON 解析 | ✅ 一致 |
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

> **跨仓对齐结论**：15 项对齐点全部一致，前后端接口契约完全匹配，无字段名/类型/路径偏差。

---

## 四、问题汇总

### 4.1 Blocker（阻断级）

**数量：0**

本次评审未发现阻断级问题。代码功能完整、安全基线达标（BCrypt 哈希存储、PreparedStatement 参数化查询、textContent 防 XSS、JWT 签名验证），无编译阻断、无安全漏洞级别的硬编码密码、无资源泄漏。

### 4.2 Major（重要级）

| # | 文件 | 问题描述 | 建议 |
|---|------|---------|------|
| Major-1 | AuthService.java | `verifyToken` 中 `Claims claims = parseToken(token)` 声明后未使用，`isExpired(token)` 内部重复解析同一 token，存在冗余 | 简化为 `try { JwtUtil.parseToken(token); return true; } catch(Exception e) { return false; }`，parseToken 已校验签名+过期 |
| Major-2 | JwtUtil.java | 生产环境未设 `JWT_SECRET` 时静默使用硬编码默认密钥，缺少 fail-fast 保护 | 增加启动检查：生产 profile 下未配置环境变量时拒绝启动 |
| Major-3 | JwtUtil.java | 默认密钥 `haikulou-dev-secret-key`（23 字节/184bit）低于 HS256 推荐 256bit 最低长度 | 补充至 32+ 字节或使用 `Keys.secretKeyFor(HS256)` 生成 |
| Major-4 | CorsFilter.java | `Allow-Origin: *` 与 `Allow-Credentials: true` 同时设置，违反 CORS 规范 | 移除 Allow-Credentials 或改为动态回显 Origin；当前 fetch 未用 credentials 故实际无影响 |

### 4.3 Minor（轻微级）

| # | 文件 | 问题描述 | 建议 |
|---|------|---------|------|
| Minor-1 | LoginServlet.java | `PrintWriter out = response.getWriter()` 在 try 块外获取 | 属 Servlet 标准模式，影响低，可选移入 try 内 |
| Minor-2 | LoginServlet.java | design.md flowchart 提及 Content-Type 检查，代码未实现 | Gson 容错可接受，可选补充 Content-Type 校验 |
| Minor-3 | AuthService.java | `getCurrentUser` catch 块无日志记录 | 补充 `LOG.log(Level.WARNING, "获取当前用户失败", e)` |
| Minor-4 | UserRepository.java | `initDefaultUser` catch-all SQLException 后无条件设 initialized=true | 建议区分错误码后决定是否标记初始化 |
| Minor-5 | login.js | `logout()` 已实现但无 UI 登出按钮入口 | 后续迭代补充登出按钮（R-05 P1） |
| Minor-6 | login.js | `btnText`/`btnLoading` 顶层 querySelector 依赖 script 位置 | 可选改用 DOMContentLoaded 或函数内获取 |

---

## 五、验收标准检查

| AC编号 | 验收条件 | 实现情况 | 结论 |
|--------|---------|---------|------|
| AC-01 | 登录页面有用户名、密码输入框和登录按钮 | index.html 含 username/password input + loginBtn | ✅ |
| AC-02 | 正确凭据登录成功 | LoginServlet → AuthService.login → BCrypt.checkpw → JWT | ✅ |
| AC-03 | 错误凭据显示"用户名或密码错误" | code=40101 + 前端 onLoginError 展示 | ✅ |
| AC-04 | 空用户名/密码前端拦截 | validateInput 非空校验 → 不发请求 | ✅ |
| AC-05 | 密码 BCrypt 哈希存储 | UserRepository.initDefaultUser BCrypt.hashpw | ✅ |
| AC-06 | 登录返回 Token，后续请求携带可识别 | LoginResult.token + Authorization Bearer | ✅ |
| AC-07 | 登出后清除状态 | logout() → localStorage.removeItem + 后端 logout | ✅ |

> **验收结论**：7/7 验收标准全部达成。

---

## 六、评审结论

### 6.1 总体评价

本次登录功能跨仓实现**通过评审**。代码分层清晰（Servlet 接入层 → AuthService 业务层 → UserRepository 数据层），与 design.md 设计文档高度对齐，15 项跨仓接口契约全部一致，7 项验收标准全部达成。

### 6.2 安全基线

| 安全项 | 状态 |
|--------|------|
| 密码 BCrypt 哈希存储（salt=10） | ✅ 合规 |
| SQL 注入防护（PreparedStatement） | ✅ 合规 |
| XSS 防护（textContent） | ✅ 合规 |
| JWT 签名验证 | ✅ 合规 |
| 敏感字段屏蔽（UserInfo） | ✅ 合规 |
| 日志不记录密码明文 | ✅ 合规 |
| JWT Secret 环境变量注入 | ⚠️ 有默认值兜底，缺少生产 fail-fast（Major-2） |

### 6.3 改进优先级

| 优先级 | 数量 | 建议 |
|--------|------|------|
| Blocker | 0 | 无阻断，可合入 |
| Major | 4 | 建议下一迭代修复（JwtUtil 密钥强化 + CorsFilter 配置修正 + AuthService 冗余清理） |
| Minor | 6 | 可选改进，不影响功能 |

### 6.4 合入建议

**允许合入**。Blocker=0，功能完整且契约对齐。4 项 Major 问题均为非阻断性改进建议（JWT 密钥强度、CORS 配置语义、冗余解析），不影响当前 MVP 功能正确性，建议在下一迭代中修复。
