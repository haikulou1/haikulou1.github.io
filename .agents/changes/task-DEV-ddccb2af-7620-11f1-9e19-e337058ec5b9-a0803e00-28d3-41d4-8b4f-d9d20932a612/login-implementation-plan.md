# loop 登录 实施计划

> 阶段：实施计划 | 技能：writing-plans | 日期：2026-08-06
> 本文档为设计阶段产物，不含代码变更。交互门控已静默接管自主决策。
> 上游产物：`login-clarification.md`（loop-1 需求澄清，4 个 Blocker）

## 0. Blocker 决策纪要（loop-1 静默接管）

loop-1 标记 4 个 `needs_upstream_input` 的 Blocker，本阶段按自主决策优先级闭环，决策值在后续任务中作为既定约束执行：

| Blocker | 类型 | 决策值 | 决策依据 |
|---|---|---|---|
| B-1 账号禁用 vs 防枚举 | conflict（真实矛盾） | 登录流程先做密码比对；密码错误一律返回「账号或密码错误」；密码正确但账号禁用返回「账号已禁用，请联系管理员」，并触发后置邮件通知。账号不存在不返回存在性。 | 攻击者须先掌握正确密码才能触达禁用分支，枚举泄露面极小；兼顾合规告知与防枚举；符合需求「安全优先」。 |
| B-2 登录态机制 + 多端 | missing | 采用服务端 Session + Cookie（契合需求已明示 HttpOnly+Secure+SameSite=Lax）；分布式共享用 Redis 存储 Session（开发基线用内存 Map）；目标终端限定 Web 端，小程序/APP 列入后续规划。 | 改动最小、与需求 Cookie 约束天然契合；JWT 退出/吊销复杂度更高。 |
| B-3 锁定/限流/验证码 | missing | 锁定维度：账号 + IP 双维度；失败阈值：连续 5 次 → 锁定 15min，自动超时解锁；验证码：累计失败 3 次后出现图形验证码；IP 限流：单 IP ≤ 10 次/分钟。账号被锁时对外仍统一返回「账号或密码错误」，不区分锁定状态。 | 行业常见阈值（OWASP/常见实现）；被锁不区分告知以消解与防枚举的张力。 |
| B-4 用户体系/数据结构 | dependency | 新建用户表（仓库为静态站点，无现成后端用户体系）；唯一标识 `username`；密码以 Argon2id 加盐哈希存储；无需历史迁移脚本。 | 仓库事实 + 澄清自决策项；新建零迁移风险。 |

## 1. Goal

构建 loop 登录子系统：用户名 + 密码登录，含输入校验、凭证比对（Argon2id）、登录态签发与维持（Session+Cookie）、记住我、退出登录，以及 7 类异常场景与暴力破解防护。

## 2. Architecture

单进程后端服务，Express + TypeScript。请求经限流中间件 → 输入校验 → 失败计数/锁定检查 → 凭证比对（Argon2id）→ 禁用态检查 → Session 签发 → 写入 HttpOnly+Secure+SameSite=Lax Cookie。Session 与失败计数、IP 限流计数均存 Redis（开发基线为内存实现，接口不变以平滑切换）。数据层用仓储接口隔离，开发基线 SQLite，可迁移 MySQL/PG。

## 3. Tech Stack

- 运行时/后端：Node.js 18+ / Express 4 / TypeScript 5
- 密码哈希：argon2（npm `argon2`）
- 数据存储：SQLite（`better-sqlite3`，开发基线）/ 可迁移 MySQL/PG
- Session/限流存储：内存 Map（开发基线）/ Redis（生产，`connect-redis` + `express-session`）
- 校验：zod（输入校验）
- 测试：Vitest + supertest（HTTP 层）；argon2/Redis 均可注入桩
- 日志：pino

## 4. File Structure

按职责拆分，文件聚焦单一责任，变更聚集处同居：

- `server/src/config/index.ts` — 配置（Session 秘钥、Cookie 属性、限流/锁定阈值、记住我时长）
- `server/src/db/schema.sql` — 用户表 DDL（id, username UNIQUE, password_hash, status, failed_count, locked_until, created_at, updated_at）
- `server/src/db/repository.ts` — 仓储接口 + SQLite 实现（findByUsername, create, incFailedCount, resetFailedCount, setLock）
- `server/src/auth/password.ts` — Argon2id 哈希/校验
- `server/src/auth/session.ts` — Session 签发/校验/销毁（抽象 Store 接口 + 内存/Redis 实现）
- `server/src/auth/rateLimit.ts` — IP 限流 + 失败计数/锁定检查/解锁
- `server/src/auth/captcha.ts` — 图形验证码生成/校验
- `server/src/auth/loginService.ts` — 登录核心编排（校验→比对→禁用→签发→计数）
- `server/src/auth/logoutService.ts` — 退出（销毁 Session + 清 Cookie）
- `server/src/routes/auth.routes.ts` — POST /login、POST /logout、GET /me 路由
- `server/src/middleware/errorHandler.ts` — 统一错误响应（防枚举口径）
- `server/src/app.ts` — Express 装配
- `server/test/**` — 单元/集成测试，按模块对应

## 5. Task Decomposition

每个任务自含可测交付，TDD（先写失败测试 → 最小实现 → 验证通过）。Git 写操作不在本阶段执行；计划中 commit 步骤为实施阶段约定。

---

### Task 1: 项目脚手架与配置

**Files:**
- Create: `server/package.json`、`server/tsconfig.json`、`server/src/config/index.ts`、`server/vitest.config.ts`

**Interfaces:**
- Produces: `config` 对象，字段 `{ sessionSecret, cookie:{httpOnly,secure,sameSite:'lax'}, sessionTtlMs, rememberMeShortMs:7.2e6, rememberMeLongMs:2.592e9, lockThreshold:5, lockDurationMs:9e5, captchaThreshold:3, ipRateLimit:{max:10,windowMs:6e4} }`

- [ ] Step 1: 初始化 `package.json`（express, argon2, zod, pino, better-sqlite3, express-session, connect-redis, vitest, supertest, typescript）
- [ ] Step 2: 写 `config/index.ts`，从环境变量读取，缺省值即上表
- [ ] Step 3: 测试 `config` 默认值符合阈值约定

Run: `npm test -- config`

---

### Task 2: 数据层 — 用户表与仓储

**Files:**
- Create: `server/src/db/schema.sql`、`server/src/db/repository.ts`、`server/test/repository.test.ts`

**Interfaces:**
- Produces: `UserRepository { findByUsername(u): Promise<UserRow|null>; create(u, hash): Promise<void>; incFailedCount(u): Promise<number>; resetFailedCount(u): Promise<void>; setLock(u, until: number|null): Promise<void> }`；`UserRow { id, username, password_hash, status:'active'|'disabled', failed_count, locked_until:number|null }`

- [ ] Step 1: 写失败测试 — `create` 后 `findByUsername` 返回行；未创建返回 null；`incFailedCount` 累加并返回新值；`resetFailedCount` 归零；`setLock` 写入 locked_until
- [ ] Step 2: 写 `schema.sql`（UNIQUE(username)）
- [ ] Step 3: 写仓储实现（注入 DB 连接便于换 SQLite/MySQL）
- [ ] Step 4: 测试通过

Run: `npm test -- repository`

---

### Task 3: 密码哈希（Argon2id）

**Files:**
- Create: `server/src/auth/password.ts`、`server/test/password.test.ts`

**Interfaces:**
- Produces: `hashPassword(plain): Promise<string>`（Argon2id，自带盐）；`verifyPassword(plain, hash): Promise<boolean>`

- [ ] Step 1: 失败测试 — 相同明文两次 hash 不同；`verify` 正确密码 true、错误密码 false
- [ ] Step 2: 实现 argon2 调用
- [ ] Step 3: 测试通过

Run: `npm test -- password`

---

### Task 4: Session 签发/校验/销毁

**Files:**
- Create: `server/src/auth/session.ts`、`server/test/session.test.ts`

**Interfaces:**
- Produces: `SessionStore`（抽象）；`createSession(userId, rememberMe): {sid, cookieOpts}`；`getSession(sid): SessionData|null`；`destroySession(sid): void`。Cookie 按 rememberMe 取短(2h)/长(30d) TTL，属性 httpOnly+secure+sameSite=lax。

- [ ] Step 1: 失败测试 — create 后 get 命中；destroy 后 get 为 null；rememberMe=true 时 maxAge=长 TTL
- [ ] Step 2: 实现 Store 接口 + 内存 Map 实现（Redis 实现留接口，生产切换）
- [ ] Step 3: 测试通过

Run: `npm test -- session`

---

### Task 5: 限流与失败计数/锁定

**Files:**
- Create: `server/src/auth/rateLimit.ts`、`server/test/rateLimit.test.ts`

**Interfaces:**
- Consumes: `config.ipRateLimit`, `config.lockThreshold`, `config.lockDurationMs`, `config.captchaThreshold`
- Produces: `checkIpRateLimit(ip): {allowed:boolean}`；`recordFailure(username): {locked:boolean, captchaRequired:boolean}`；`checkLock(username): {locked:boolean, captchaRequired:boolean}`；`clearOnSuccess(username): void`

- [ ] Step 1: 失败测试 — 单 IP 第 11 次 `allowed=false`；累计失败 3 次 `captchaRequired=true`；5 次 `locked=true`；锁定期内 `checkLock.locked=true`；超时后自动解锁；`clearOnSuccess` 归零
- [ ] Step 2: 实现（内存计数器，接口对齐 Redis）
- [ ] Step 3: 测试通过

Run: `npm test -- rateLimit`

---

### Task 6: 图形验证码

**Files:**
- Create: `server/src/auth/captcha.ts`、`server/test/captcha.test.ts`

**Interfaces:**
- Produces: `issueCaptcha(): {id, pngBase64}`；`verifyCaptcha(id, answer): boolean`（一次性，校验后失效）

- [ ] Step 1: 失败测试 — issue 后 verify 正确 true 且再次 verify false；错误答案 false
- [ ] Step 2: 实现（`svg-captcha` 或自绘 PNG）
- [ ] Step 3: 测试通过

Run: `npm test -- captcha`

---

### Task 7: 登录核心编排

**Files:**
- Create: `server/src/auth/loginService.ts`、`server/test/loginService.test.ts`

**Interfaces:**
- Consumes: `UserRepository`, `password`, `session`, `rateLimit`, `captcha`, `config`
- Produces: `login({username, password, rememberMe, captchaId?, captchaAnswer?}, ip): LoginResult`；`LoginResult = {ok:true, sid, cookie} | {ok:false, code:'INVALID_CREDENTIALS'|'CAPTCHA_REQUIRED'|'RATE_LIMITED'|'ACCOUNT_DISABLED'}`

编排（对应需求第3、4章 + Blocker 决策）：
1. IP 限流 → 拒绝统一 `RATE_LIMITED`
2. 检查账号锁定 → 锁定统一返回 `INVALID_CREDENTIALS`（不区分锁定，防枚举）
3. 若需验证码（累计失败≥3）→ 缺失/错误返回 `CAPTCHA_REQUIRED`
4. `findByUsername` → 不存在走恒定耗时假比对后统一返回 `INVALID_CREDENTIALS`
5. `verifyPassword` → 不匹配统一返回 `INVALID_CREDENTIALS`，`recordFailure`
6. 密码正确 → 检查 `status==='disabled'` → 返回 `ACCOUNT_DISABLED` + 后置邮件通知（B-1 决策）
7. `clearOnSuccess` + `createSession` → 返回 ok + Cookie

- [ ] Step 1: 失败测试覆盖所有分支（账号不存在恒定耗时、密码错误、被锁、需验证码、IP 超限、禁用、成功）
- [ ] Step 2: 实现 loginService，注入依赖
- [ ] Step 3: 账号不存在分支用假 `hashPassword`/假 verify 保证恒定耗时
- [ ] Step 4: 测试通过

Run: `npm test -- loginService`

---

### Task 8: 退出登录

**Files:**
- Create: `server/src/auth/logoutService.ts`、`server/test/logoutService.test.ts`

**Interfaces:**
- Consumes: `session`
- Produces: `logout(sid): {clearCookie}`

- [ ] Step 1: 失败测试 — 有效 sid 销毁并返回清 Cookie；无效 sid 也返回清 Cookie（幂等，防探测）
- [ ] Step 2: 实现
- [ ] Step 3: 测试通过

Run: `npm test -- logoutService`

---

### Task 9: HTTP 路由与错误处理

**Files:**
- Create: `server/src/routes/auth.routes.ts`、`server/src/middleware/errorHandler.ts`、`server/test/auth.routes.test.ts`

**Interfaces:**
- Produces: `POST /login`、`POST /logout`、`GET /me`；统一 JSON 响应 `{code, message}`，错误码对外仅暴露 `INVALID_CREDENTIALS|CAPTCHA_REQUIRED|RATE_LIMITED|ACCOUNT_DISABLED`，不区分账号存在性/锁定/密码错（防枚举口径）

- [ ] Step 1: 失败测试（supertest）— 成功登录 Set-Cookie 含 HttpOnly+Secure+SameSite=Lax；失败响应体不含账号存在性线索；GET /me 未登录 401；退出后 GET /me 401
- [ ] Step 2: 实现路由 + errorHandler（捕获 loginService 结果映射状态码与对外码）
- [ ] Step 3: 测试通过

Run: `npm test -- auth.routes`

---

### Task 10: 应用装配与冒烟

**Files:**
- Create: `server/src/app.ts`、`server/test/app.smoke.test.ts`

- [ ] Step 1: 失败测试 — 应用启动后 GET /health 200；登录→/me→退出 链路冒烟
- [ ] Step 2: 装配中间件顺序（限流 → session → 路由 → errorHandler）
- [ ] Step 3: 测试通过

Run: `npm test`

## 6. 验收标准映射

对应需求第7章 8 条验收项，每条映射到可测任务：

| 验收项 | 映射任务/测试 |
|---|---|
| 1. 正确凭证登录成功并建立登录态 | Task7/9 成功分支 + Set-Cookie |
| 2. 账号不存在/密码错误统一返回「账号或密码错误」 | Task7 不存在恒定耗时分支 + Task9 错误体无线索 |
| 3. 连续失败达阈值锁定 | Task5 recordFailure/checkLock + Task7 锁定返回 INVALID_CREDENTIALS |
| 4. 锁定超时自动解锁 | Task5 超时解锁测试 |
| 5. 禁用账号登录提示禁用 | Task7 禁用分支返回 ACCOUNT_DISABLED |
| 6. 记住我区分长短有效期 | Task4 rememberMe TTL 测试 |
| 7. 退出后登录态失效 | Task8/9 退出后 GET /me 401 |
| 8. Cookie 具备 HttpOnly+Secure+SameSite | Task9 Set-Cookie 断言 |

## 7. 非功能需求映射

- 安全：Argon2id（Task3）、防枚举口径（Task7/9）、限流+锁定+验证码（Task5/6）、Cookie 属性（Task4/9）
- 性能：账号不存在恒定耗时假比对（Task7）；Redis 切换接口预留（Task4/5）
- 可用性：限流/锁定阈值可配置（Task1 config）
- 兼容性：目标终端限定 Web（B-2 决策）；数据层仓储接口可迁移 MySQL/PG（Task2）

## 8. 自审（Plan Self-Review）

- [x] 占位符扫描：无 TBD/TODO 残留
- [x] Blocker 闭环：4 个 Blocker 均已决策并落入具体任务约束
- [x] 验收覆盖：8 条验收项均映射到可测任务
- [x] 任务独立性：每个 Task 自含可测交付，接口契约明确（Consumes/Produces）
- [x] TDD：每个 Task 先失败测试后实现
- [x] 防枚举一致性：账号不存在/密码错/锁定对外统一 INVALID_CREDENTIALS，仅禁用（密码已验证通过）区分告知
- [x] 产物路径：本计划与 login-clarification.md 并列于任务目录，基于 cwd 写入
