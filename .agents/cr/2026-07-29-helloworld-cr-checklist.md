# Code Review Checklist

> **Change** `helloworld (编码实现 stage: coding)` · **分支/Commit** `AI/ta***` / `7299978` · **日期** `2026-07-29`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。
>
> **预扫结果**：`bash scan-all-rules.sh HelloWorld.java` → `No findings. 52/222 rules scanned`

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `HelloWorld.java` | REQ-1 / 需求"写一个helloworld" | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |

- 由 `git diff --name-only HEAD~1 HEAD` 展开：`HelloWorld.java`（1 文件，+24 行）。
- **Java 守卫**：通过（含 `.java` 文件）。
- **收口**：每文件各 Sn/Gn 列均非 `⬜`；HelloWorld.java 仅含 `main` 打印硬编码常量，无并发/资源/事务/MQ/SQL/异常/日期/集合/线程构造，G1–G17、S1–S10 整节与当前文件无关，统一标 `N/A(无对应构造)`。

---

## Step 2 — 功能（产物 B）

> REQ 来源：需求原文"写一个helloworld"。不臆造 change 外功能点。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given 程序编译运行 / When 执行 `main` / Then 标准输出打印 `Hello, World!` | 需求原文"写一个helloworld" | `HelloWorld.java` | ✅ | `HelloWorld.java:14` `GREETING_MESSAGE = "Hello, World!"` + `HelloWorld.java:22` `System.out.println(GREETING_MESSAGE)` |

---

## Step 3 — 可读性检查（产物 C）

> 对照 `references/readability-checklist.md` A1–A7 逐节核销（预扫无命中，LLM 复核）：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | `HelloWorld.java:1` package 声明规范，文件编码/行尾一致 |
| A2 | 源文件结构/import 顺序 | ✅ | 无 import（本示例不需要）；结构 package → 类声明 |
| A3 | 代码样式 | ✅ | 4 空格缩进，K&R 大括号风格，`HelloWorld.java:9/21` |
| A4 | 命名规范 | ✅ | 类名 `HelloWorld` 大驼峰（L9）；常量 `GREETING_MESSAGE` 全大写下划线（L14）；方法 `main` 标准签名（L21） |
| A5 | 编码实践 | ✅ | 问候语提取为 `private static final` 常量，避免魔法值（L14），注释已说明意图 |
| A6 | 特定元素样式 | ✅ | 单一类、单一方法，结构清晰 |
| A7 | Javadoc 规范 | ✅ | 类 Javadoc 含 `@author`/`@date`（L3-8）；常量注释（L11-13）；方法 Javadoc 含 `@param args`（L16-20） |

---

## Step 4 — 可靠性检查（产物 D）

> 预扫 `scan-all-rules.sh`：`No findings. 52/222 rules scanned`。LLM 按 ID 逐条复核。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> HelloWorld.java 仅含 `System.out.println(常量)`，无数组/集合/日期/线程池/BigDecimal/异常/递归/格式化/反射/IO 等构造，B/M/I 全部 N/A。

| ID | 状态 | 备注 |
|----|------|------|
| B001–B081 | N/A | HelloWorld.java 无对应代码构造（parse/of/数组/集合/日期/线程/BigDecimal/异常/递归等），预扫无命中 |
| M001–M027 | N/A | 同上，无 Major 级 Bug 模式相关构造 |
| I001–I010 | N/A | 同上，无 Info 级 Bug 模式相关构造 |

### 4.2 可靠性（`reliability-checklist.md`）

> HelloWorld.java 无并发/资源释放/事务/MQ/超时重试/限流/边界条件/灰度监控等构造，G1–G18 全部 N/A。

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发控制构造（G1 并发） |
| G2.1–G2.3 | N/A | 无资源释放构造（G2 资源） |
| G3.1–G3.2 | N/A | 无事务边界构造（G3 事务） |
| G4.1–G4.4 | N/A | 无幂等构造（G4 幂等） |
| G5.1 | N/A | 无边界条件构造（G5 边界） |
| G6.1–G6.2 | N/A | 无超时/重试构造（G6 超时重试） |
| G7.1–G7.2 | N/A | 无限流构造（G7 限流） |
| G8.1–G8.7 | N/A | 无 MQ/异步构造（G8 MQ） |
| G9.1–G9.3 | N/A | 无缓存构造（G9 缓存） |
| G10.1–G10.3 | N/A | 无序列化构造（G10 序列化） |
| G11.1–G11.4 | N/A | 无外部 IO/网络构造（G11 IO） |
| G12.1–G12.2 | N/A | 无配置/开关构造（G12 配置） |
| G13.1 | N/A | 无监控/埋点构造（G13 监控） |
| G14.1–G14.4 | N/A | 无日志/可观测构造（G14 日志） |
| G15.1–G15.3 | N/A | 无灰度/发布构造（G15 灰度） |
| G16.1–G16.4 | N/A | 无异常处理构造（G16 异常） |
| G17.1–G17.3 | N/A | 无应急/降级构造（G17 应急） |
| G18.1–G18.3 | N/A | 无安全补强相关构造（G18 安全补强） |

### 4.3 安全（`security-checklist.md`）

> HelloWorld.java 无 SQL/Web/认证/密钥/反序列化/文件上传/CSRF 等构造，S1–S10 全部 N/A。`System.out.println` 输出硬编码常量 `Hello, World!`，无敏感信息泄露。

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL 操作（S1 SQL 注入） |
| S2.1–S2.3 | N/A | 无 XSS/输出转义构造（S2 XSS） |
| S3.1–S3.3 | N/A | 无认证/授权构造（S3 认证授权） |
| S4.1–S4.2 | N/A | 无密钥/凭证构造（S4 密钥泄露） |
| S5.1–S5.2 | N/A | 无反序列化构造（S5 反序列化） |
| S6.1–S6.x | N/A | 无文件上传/路径构造（S6 文件操作） |
| S7.x–S10.x | N/A | 无 SSRF/重定向/CSRF/CORS 等构造（S7–S10） |

---

## Step 5 — 自定义扩展检查（产物 E）

> `references/customized-checklist.md` 为空或全为示例项。

| 域 | 参考 | 状态 | 备注 |
|----|------|------|------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | N/A(未启用自定义规则) |

---

## 收口

- **队列核销**：执行队列待审项 = 0（1/1 文件已审，状态 `✅`）。
- **Step 2 一致性**：REQ-1 章节级 `✅` 与逐文件结论一致。
- **Step 3/4/5 跨文件合并**：A1–A7 `✅`；G1–G18/S1–S10/B/M/I `N/A`；自定义 `N/A`，均已核销。
- **report 审查范围文件数** = 1（与已审队列一致）。
