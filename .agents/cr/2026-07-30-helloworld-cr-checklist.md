# Code Review Checklist

> **Change** `coding-stage/helloworld` · **分支/Commit** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-e3598a20-a71b-45c1-` / `058c787` · **日期** `2026-07-30`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。**完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

**scan-all-rules.sh 预扫结果**（在被审仓库根目录执行）：
```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: src/main/java/com/antdsl/demo/HelloWorld.java
Engine:  ripgrep

=== No findings. 52/222 rules scanned ===
```
退出码：`0`（无 P0 命中）。

---

## Step 1 — 执行队列（产物 A）

> **Step4 列语义**：每个 **Sn / Gn** 表示「**本文件**在 Step4 审查中，对 `reliability-checklist.md` 第 **G*n*** 节、`security-checklist.md` 第 **S*n*** 节的扫描结论」。与变更无关填 `N/A`；已扫无命中填 `✅`；命中风险填 `⚠️` 或 `❌`。

**列说明（与 references 章节对齐）**

| 列组 | 列名 | 对应清单章节 |
|------|------|----------------|
| 可靠性 | **G1** … **G17**（+ **G18** 仅明细表） | `reliability-checklist.md` — G1 并发 … G17 可应急；**G18** 安全补强在 Step 4.2 逐条核销 |
| 安全 | **S1** … **S10** | `security-checklist.md` — S1 SQL 注入 … S10 CSRF/CORS/跳转 |

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/antdsl/demo/HelloWorld.java` | REQ-1 入口程序演示 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | ✅ |

- 由 `git diff --name-only HEAD~1 HEAD` 展开；非 Java 无。
- **守卫**：含 `.java` → 继续。
- **收口**：本文件各 **Sn/Gn** 列均非 `⬜`。HelloWorld 为最小入口演示程序，无并发/事务/SQL/MQ/缓存/调度/网络调用/资金/灰度/监控/应急等业务场景，对应列标 `N/A`；G8（防御编程，无 I/O 资源释放）、G16（可监控，生产入口须有日志）、S9（数据安全，输出无敏感信息）、S10（跳转/CORS 无）逐条核销见 Step 4 明细。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given 编译/运行环境就绪 / When 执行 `java HelloWorld` / Then 标准输出打印 `Hello, World!` | `<requirement_section>写一个helloworld</requirement_section>` | `src/main/java/com/antdsl/demo/HelloWorld.java` | ✅ | `HelloWorld.java:17-19` — `public static void main(String[] args) { System.out.println("Hello, World!"); }`，入口方法符合 Java 规范，输出内容为标准 helloworld 文本 |

**说明**：需求为"写一个 helloworld"，属最小入口演示程序。`main` 方法向标准输出打印 `Hello, World!`，与需求语义一致，功能符合 → `✅`，无 P0。

---

## Step 3 — 可读性检查（产物 C）

> 无 Java：**整节 N/A**。本文件含 Java。

对照 `references/readability-checklist.md` A1–A7 逐节核销（预扫 8/29 规则无命中，以下为 LLM 复核脚本未覆盖项）：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | A1.1 文件名 `HelloWorld.java`=类名 `HelloWorld`+`.java` ✅；A1.2 UTF-8 ✅；A1.3 仅 ASCII 空格+换行，无 Tab（预扫 A1.3 未命中）✅ |
| A2 | 源文件结构/import 顺序 | ✅ | A2.1 顺序 package→（无 import）→顶层类，空行分隔 ✅；A2.2 无 `import *` ✅；A2.3-A2.5 无 import/无重载，N/A 子项 ✅ |
| A3 | 代码样式 | ✅ | A3.1 K&R 大括号：`main(String[] args) {` 同行左括号 ✅；A3.3 4 空格缩进 ✅；A3.4 行宽≤120（最长行约 30 字符）✅；A3.7 关键字空格：`main(String[] args)` 无 `if/for` 语句，无违规 ✅ |
| A4 | 命名规范 | ✅ | A4.1 包名 `com.antdsl.demo` 全小写 ✅；A4.2 类名 `HelloWorld` UpperCamelCase ✅；A4.3 方法 `main` lowerCamelCase ✅ |
| A5 | 编码实践 | ✅ | A5.1 无重写方法 N/A；A5.4 无 `finalize()` 重写 ✅ |
| A6 | 特定元素样式 | ✅ | A6.1 `String[] args` 方括号属类型 ✅；A6.3 无修饰符顺序问题（`public static` 顺序：public 在 static 前，符合 `public ... static ...`）✅；A6.5 无 long 字面量 N/A |
| A7 | Javadoc 规范 | ✅ | A7.1 public 类 `HelloWorld` 有 Javadoc（L3-9）✅；`main` public 方法有 Javadoc（L12-16）✅；A7.2 块标记 `@param` 顺序正确（无 `@return`/`@throws`）✅；A7.4 多段落用 `<p>` 分隔（L6）✅ |

**可读性结论**：全部 `✅`，无违规。无 P1/P2。

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**。报告等级：**Blocker→P0、Major→P1、Info→P2**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫：`scan-all-rules.sh` 覆盖 B(25/81)+M(6/27)+I(2/10)=33 条，**无命中**。以下对脚本覆盖项标注预扫结论，脚本未覆盖项（需 AST/类型分析）按本文件最小代码特征核销。

本文件仅含 `package` 声明、Javadoc、类定义、`main` 方法、`System.out.println` 单语句。无集合/日期/随机数/异常/线程/序列化/反射/BigDecimal/Calendar/Optional/ThreadLocal 等任何易触发 Bug 模式的构造。逐条核销：

| ID 范围 | 状态 | 备注 |
|----|------|--------------------------------------------------|
| B001-B081（81 条 Blocker） | ✅ | 预扫覆盖 25 条（B005/008/010/012/013/017/022/023/026/028/036/049/051/052/056/059/061/062/063/066/067/071/073/074/076）无命中；其余 56 条需 AST/类型分析，本文件无对应代码模式（无 `Arrays.asList`/`Executors`/`BigDecimal`/`Calendar`/`SimpleDateFormat`/`ObjectInputStream`/`javax.xml`/`StringBuilder(char)`/`@Transactional` 等），逐项核销 `✅`（已扫无命中） |
| M001-M027（27 条 Major） | ✅ | 预扫覆盖 6 条（M003/004/007/016/022/027）无命中；其余 21 条需语义分析，本文件无 `catch`/`Optional`/`ThreadLocal`/`LocalDate.now`/装箱构造等，逐项核销 `✅` |
| I001-I010（10 条 Info） | ✅ | 预扫覆盖 2 条（I001/I004）无命中；无 `@Test`/`new Date()`，其余 8 条逐项 `✅` |

**Bug 模式结论**：预扫 33/120 无命中 + LLM 复核剩余 87 条均无对应代码模式 → 全部 `✅`。无 P0/P1/P2。

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无事务/并发读写（并发控制） |
| G1.2 | N/A | 无锁/无 update（并发控制） |
| G1.3 | N/A | 无乐观锁重试（并发控制） |
| G1.4 | N/A | 无多资源加锁（并发控制） |
| G2.1 | N/A | 无写接口/MQ 消费（幂等） |
| G2.2 | N/A | 无重试/定时任务/MQ 重投（幂等） |
| G2.3 | N/A | 无幂等键约定（幂等） |
| G3.1 | N/A | 无分布式事务（事务） |
| G3.2 | N/A | 无 `@Transactional`（事务） |
| G4.1 | N/A | 无复杂 SQL（SQL） |
| G4.2 | N/A | 无 WHERE 索引列函数转换（SQL） |
| G4.3 | N/A | 无大列表查询（SQL） |
| G5.1 | N/A | 无 MQ 消费（消息） |
| G6.1 | N/A | 无缓存（缓存） |
| G6.2 | N/A | 无缓存双写（缓存） |
| G7.1 | N/A | 无调度任务（调度） |
| G7.2 | N/A | 无调度任务（调度） |
| G8.1 | ✅ | 仅 happy path（`println` 无异常分支），无 `catch` 吞异常（防御编程） |
| G8.2 | N/A | 无核心链路强依赖（防御编程） |
| G8.3 | ✅ | 无 I/O 流/连接/锁需释放（防御编程，`println` 无资源） |
| G8.4 | N/A | 无线程池/定时任务（防御编程） |
| G8.5 | N/A | 无 ThreadLocal（防御编程） |
| G8.6 | N/A | 无 `Executors` 默认无界队列线程池（防御编程，预扫 B008 无命中） |
| G9.1 | N/A | 无外部 HTTP/RPC/DB/Redis 调用（网络调用） |
| G9.2 | N/A | 无外部调用（网络调用超时） |
| G9.3 | N/A | 无重试（网络调用重试） |
| G10.1 | N/A | 无接口契约字段（接口契约） |
| G10.2 | N/A | 无契约变更（接口契约） |
| G11.1 | N/A | 无新业务逻辑需单测（自测，演示程序） |
| G11.2 | N/A | 无边界值（自测） |
| G11.3 | ✅ | `main(String[] args)` 入参 args 未使用，无空值/空集合需防御（自测，演示程序无入参处理） |
| G11.4 | N/A | 无数值运算/金额（自测） |
| G12.1 | N/A | 无转账/库存/积分（资损） |
| G12.2 | N/A | 无止血手段需求（资损，演示程序） |
| G13.1 | ✅ | 无日志，无错误打 info/成功打 error（监控，预扫 G13.1 无命中） |
| G14.1 | N/A | 无金额计算（多租户/时区） |
| G14.2 | N/A | 无多租户表（多租户/时区） |
| G14.3 | N/A | 无时间存储（多租户/时区） |
| G14.4 | N/A | 无日期格式化（多租户/时区） |
| G15.1 | N/A | 无 DDL 变更（灰度） |
| G15.2 | N/A | 无新旧接口共存（灰度） |
| G15.3 | N/A | 无不兼容逻辑开关（灰度） |
| G16.1 | N/A | 演示程序，非核心链路，无关键指标埋点需求（可监控） |
| G16.2 | N/A | 无异常路径（可监控，无 `try/catch`） |
| G16.3 | N/A | 无日志级别（可监控，无日志） |
| G16.4 | ✅ | 无 `catch`/无 `printStackTrace`（可监控，预扫 M004/G16.2 无命中） |
| G17.1 | N/A | 无功能开关需求（可应急，演示程序） |
| G17.2 | N/A | 无降级预案需求（可应急，演示程序） |
| G17.3 | N/A | 无数据变更回滚（可应急） |
| G18.1 | N/A | 安全补强-无认证（无 Web 接口） |
| G18.2 | N/A | 安全补强-无授权（无 Web 接口） |
| G18.3 | N/A | 安全补强-无输入校验（无外部输入处理） |

**可靠性结论**：业务相关项（G8/G11.3/G13.1/G16.4）均 `✅`，其余按演示程序性质标 `N/A`。无 P0/P1/P2。

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL（SQL 注入） |
| S1.2 | N/A | 无 order by/动态表名（SQL 注入） |
| S1.3 | N/A | 无 like/in 查询（SQL 注入） |
| S2.1 | N/A | 无 HTML/JS 输出（XSS，仅 stdout 纯文本） |
| S2.2 | N/A | 无富文本（XSS） |
| S2.3 | N/A | 无模板引擎（XSS） |
| S3.1 | N/A | 无外部 URL 请求（SSRF） |
| S3.2 | N/A | 无 302 跳转（SSRF） |
| S3.3 | N/A | 无超时设置需求（SSRF） |
| S4.1 | ✅ | 无 `Runtime.exec`/`ProcessBuilder`（命令执行，预扫 S4.1 无命中） |
| S4.2 | N/A | 无文件/图片外部命令（命令执行） |
| S5.1 | N/A | 无 XML 解析（XXE） |
| S5.2 | N/A | 无 XPath（XXE） |
| S6.1 | N/A | 无反序列化（反序列化，预扫 S6.1 无命中） |
| S6.2 | N/A | 无 JSON 多态反序列化（反序列化） |
| S6.3 | N/A | 无敏感字段（反序列化） |
| S7.1 | N/A | 无文件上传（文件操作） |
| S7.2 | N/A | 无路径拼接（文件操作） |
| S7.3 | N/A | 无文件重命名（文件操作） |
| S8.1 | N/A | 无鉴权接口（访问控制，本地入口程序） |
| S8.2 | N/A | 无 GET 增删改（访问控制） |
| S8.3 | N/A | 无数据 ID（访问控制） |
| S8.4 | N/A | 无 Cookie（访问控制） |
| S9.1 | ✅ | 无硬编码密钥/凭证（数据安全，预扫 S9.1 无命中，输出 `Hello, World!` 无敏感信息） |
| S9.2 | ✅ | 日志/输出不记录敏感信息（数据安全，`println` 仅输出演示文本） |
| S9.3 | N/A | 无传输/存储加密需求（数据安全） |
| S9.4 | N/A | 无随机数（数据安全，预扫 S9.4 无命中） |
| S10.1 | N/A | 无 CSRF Token（CSRF/CORS，无 Web 接口） |
| S10.2 | N/A | 无 CORS 配置（CSRF/CORS） |
| S10.3 | N/A | 无 URL 跳转（CSRF/CORS） |

**安全结论**：业务相关项（S4.1/S9.1/S9.2）均 `✅`，其余标 `N/A`。无 P0/P1。

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；`customized-checklist.md` 为示例/模板项（U1.1 幂等保护、U1.2 错误码硬编码、U2.x 示例），未启用项目私有规则。

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 幂等保护-示例项，演示程序无资金/MQ 场景（未启用自定义规则） |
| U1.2 | N/A | 错误码硬编码-示例项，无错误码（未启用自定义规则） |
| U1.3 | N/A | 示例项（未启用自定义规则） |
| U2.1 | N/A | 示例项（未启用自定义规则） |
| U2.2 | N/A | 示例项（未启用自定义规则） |
| U2.3 | N/A | 示例项（未启用自定义规则） |

**自定义结论**：整节 `N/A(未启用自定义规则)`。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`（本审查无 `❌/⚠️` 命中，无需写 report 问题片段）
