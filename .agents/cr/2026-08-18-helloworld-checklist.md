# Code Review Checklist

> **Change** helloworld · **分支/Commit** `AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-818534c8-a10d-4d51-` / `1591c66` · **日期** 2026-08-18
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

---

## Step 1 — 执行队列（产物 A）

> **Step4 列语义**：每个 **Sn / Gn** 表示「**本文件**在 Step4 审查中，对 `reliability-checklist.md` 第 **G*n*** 节、`security-checklist.md` 第 **S*n*** 节的扫描结论」。**Bug 模式（B/M/I）** 不在本表分列，在下方 **§4.1** 按清单 ID 核销（可与 `scan-all-rules.sh` 预扫结果对照）。与变更无关填 `N/A`；已扫无命中填 `✅`；命中风险填 `⚠️` 或 `❌`（并在 Step 4 明细表与 report 中写清 `Gx.x` / `Sx.x` + `path:line`）。

**列说明（与 references 章节对齐）**

| 列组 | 列名 | 对应清单章节 |
|------|------|----------------|
| 可靠性 | **G1** … **G17**（+ **G18** 仅明细表） | `reliability-checklist.md` — G1 并发 … G17 可应急；**G18** 安全补强在 Step 4.2 逐条核销，Step 1 可不单列 |
| 安全 | **S1** … **S10** | `security-checklist.md` — S1 SQL 注入 … S10 CSRF/CORS/跳转 |

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/test/java/com/example/helloworld/HelloWorldTest.java` | REQ-1 / TEST | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ |
| 2 | `.agents/impl-report.md` | 非 Java | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 |
| 3 | `docs/ARCHITECTURE.md` | 非 Java | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 |
| 4 | `docs/modules/helloworld/README.md` | 非 Java | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 |

- 由 `git diff HEAD~1 --name-only` 展开；**禁止 glob**；非 Java 标 `跳过`。
- **守卫**：存在 `.java` 文件 → 继续审查。
- **收口**：每文件各 **Sn/Gn** 列均非 `⬜` 后，再与下方 Step 4 **逐条 ID 表** 核对一致。

---

## Step 2 — 功能（产物 B）

> 仅从 spec/tasks 提 **REQ**，勿臆造。不符 spec 标 **P0**。
> 每个 REQ 都必须填写 **spec 证据** 与 **关联文件**；若命中 P0，代码证据需落到 `path:line`、测试或接口行为。

**需求原文**: 「写一个helloworld」

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 提供问候语 `getGreeting()` 返回 "Hello, World!" | 需求: 「写一个helloworld」 | `HelloWorldTest.java` | ✅ | `HelloWorldTest.java:20-25` — `should_returnDefaultGreeting_when_getGreeting()` 断言 `assertEquals(EXPECTED_GREETING, greeting)` |
| REQ-2 | main 入口可正常执行不抛异常 | 需求: 「写一个helloworld」 | `HelloWorldTest.java` | ✅ | `HelloWorldTest.java:29-32` — `should_outputGreeting_when_mainInvoked()` 调用 `HelloWorld.main(new String[]{})` |
| REQ-3 | 多次调用 `getGreeting()` 返回值一致 | 需求: 「写一个helloworld」 | `HelloWorldTest.java` | ✅ | `HelloWorldTest.java:36-43` — `should_returnSameGreeting_when_calledMultipleTimes()` 两次调用并断言相等 |
| REQ-4 | `getGreeting()` 返回值非空 | 需求: 「写一个helloworld」 | `HelloWorldTest.java` | ✅ | `HelloWorldTest.java:47-52` — `should_returnNonNullGreeting()` 断言 `assertNotNull(greeting)` |

---

## Step 3 — 可读性检查（产物 C）

> 无 Java：**整节 N/A**。

对照 `references/readability-checklist.md` A1–A7 逐节核销：

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名 `HelloWorldTest.java` = 类名 + Test，UTF-8，空格合规 |
| A2 | 源文件结构/import 顺序 | ✅ | package → import → 类，无 `import *`，import 分组且 ASCII 序 |
| A3 | 代码样式 | ✅ | K&R 大括号，缩进 4 空格，行宽 ≤ 120，运算符空格合规 |
| A4 | 命名规范 | ✅ | 类名 `UpperCamelCase`，方法名 `lowerCamelCase`，常量 `UPPER_SNAKE_CASE`，测试类后缀 Test |
| A5 | 编码实践 | ✅ | 静态方法 `HelloWorld.getGreeting()` 类名调用，无空 catch |
| A6 | 特定元素样式 | ✅ | 数组方括号类型风格 `String[]`，修饰符顺序合规 |
| A7 | Javadoc 规范 | ✅ | 类有 Javadoc，方法有 Javadoc，块标记顺序合规 |

**预扫备注**: `scan-all-rules.sh` 对目标文件扫描无命中。

---

## Step 4 — 可靠性检查（产物 D）

> **逐条核销（强制）**：G/S 每个 ID **独占一行**，禁止合并为区间。**Bug 模式** 按 `bug-pattern-checklist.md` 中 **每条 B*/M*/I*** 独占一行核销（120 条）**；无关变更可对该 ID 标 `N/A` 并写原因。报告等级：**Blocker→P0、Major→P1、Info→P2**。

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 已运行 `references/script/scan-all-rules.sh` 对变更路径扫描，结果：**无命中**。以下为 LLM 逐条核销。

| ID | 状态 | 备注（命中写 `path:line`；预扫可粘贴脚本摘要） |
|----|------|--------------------------------------------------|
| B001 | N/A | 无 parse/of 调用 |
| B002 | N/A | 无数组 equals 比较 |
| B003 | N/A | 无 Arrays.fill 调用 |
| B004 | N/A | 无数组 toString 调用 |
| B005 | N/A | 无 Arrays.asList 调用 |
| B006 | ✅ | `HelloWorldTest.java:25` — `assertEquals(EXPECTED_GREETING, greeting)` 参数顺序正确（expected 在前） |
| B007 | N/A | 无 catch Throwable |
| B008 | N/A | 无 Executors 调用 |
| B009 | N/A | 无移位操作 |
| B010 | N/A | 无 BigDecimal |
| B011 | N/A | 无包装类型 == 比较 |
| B012 | N/A | 无 Calendar 操作 |
| B013 | N/A | 无 Calendar 操作 |
| B014 | N/A | 无集合查询 |
| B015 | N/A | 无 toArray 调用 |
| B016 | N/A | 无 Comparable 实现 |
| B017 | N/A | 无 this == null |
| B018 | N/A | 无条件表达式数值提升 |
| B019 | N/A | 无 Money 类操作 |
| B020 | N/A | 无常量溢出风险 |
| B021 | N/A | 无 Jedis 使用 |
| B022 | N/A | 无 DateFormat 调用 |
| B023 | N/A | 无异常实例创建 |
| B024 | N/A | 无 Thread 创建 |
| B025 | N/A | 无双括号初始化 |
| B026 | N/A | 无 equals(null) 调用 |
| B027 | N/A | 无 equals 重写 |
| B028 | N/A | 无 DateUtil 调用 |
| B029 | N/A | 无 setter 赋值错误 |
| B030 | N/A | 无浮点 == 比较 |
| B031 | N/A | 无 String.format 调用 |
| B032 | N/A | 无注解 getClass 调用 |
| B033 | N/A | 无 Unsafe 操作 |
| B034 | N/A | 无 Hashtable 操作 |
| B035 | N/A | 无恒等二元表达式 |
| B036 | N/A | 无 IdentityHashMap |
| B037 | N/A | 无可变参数条件表达式 |
| B038 | N/A | 无递归调用 |
| B039 | N/A | 无 indexOf 调用 |
| B040 | N/A | 无 isInstance 调用 |
| B041 | N/A | 无 JDBC 操作 |
| B042 | N/A | JUnit4 测试，非 JUnit3 |
| B043 | N/A | 无内部类 @Test |
| B044 | N/A | 仅 JUnit4 注解，无混用 |
| B045 | N/A | 无锁操作 |
| B046 | N/A | 无循环 |
| B047 | N/A | 无 compare 调用 |
| B048 | N/A | 无 Math.round 调用 |
| B049 | N/A | 无日期格式字符串 |
| B050 | N/A | 无日期格式字符串 |
| B051 | N/A | 无 Boolean.getBoolean 调用 |
| B052 | N/A | 无日期格式字符串 |
| B053 | N/A | 无 try-catch 预期异常模式 |
| B054 | N/A | 无 EqualsTester |
| B055 | N/A | 无 Mockito 使用 |
| B056 | N/A | 无 Arrays.asList 操作 |
| B057 | N/A | 无增强 for 循环修改 |
| B058 | N/A | 无集合自操作 |
| B059 | N/A | 无 nCopies 调用 |
| B060 | N/A | 无条件表达式拆箱 |
| B061 | N/A | 无 BASE64Encoder |
| B062 | N/A | 无 ClassLoader 转型 |
| B063 | N/A | 无 javax.xml 操作 |
| B064 | N/A | 无 Optional |
| B065 | N/A | 无 setter 自赋值 |
| B066 | N/A | 无 Math.random 调用 |
| B067 | N/A | 无 Random 调用 |
| B068 | N/A | 无自赋值 |
| B069 | N/A | 无 compareTo 自比较 |
| B070 | N/A | 无 equals 自比较 |
| B071 | N/A | 无 size() >= 0 |
| B072 | N/A | 无 Stream.toString |
| B073 | N/A | 无 StringBuilder 构造 |
| B074 | N/A | 无 substring 调用 |
| B075 | N/A | 无 for 循环 |
| B076 | N/A | 无 @Transactional |
| B077 | N/A | 无 catch Throwable 测试模式 |
| B078 | N/A | 无 Truth 断言 |
| B079 | N/A | 无 @Mock |
| B080 | ✅ | 每个测试方法均有断言：`assertEquals` / `assertNotNull`，`should_outputGreeting_when_mainInvoked` 通过无异常隐式断言 |
| B081 | N/A | 无集合排序操作 |
| M001 | N/A | 无条件判断 |
| M002 | N/A | 无 instanceof |
| M003 | N/A | 无包装类构造器 |
| M004 | N/A | 无 printStackTrace |
| M005 | N/A | 无内部类 |
| M006 | N/A | 无条件布尔表达式 |
| M007 | N/A | 无空 catch 块 |
| M008 | N/A | 无 equals 重写 |
| M009 | N/A | 无 equals 跨类型比较 |
| M010 | N/A | 无比特运算 |
| M011 | N/A | 无 switch |
| M012 | N/A | 无 finally |
| M013 | N/A | 无类型转换 |
| M014 | N/A | 无枚举 getClass |
| M015 | N/A | 无继承 |
| M016 | N/A | 无时间操作 |
| M017 | ✅ | 所有测试方法均有 `@Test` 注解 |
| M018 | N/A | 无 Lock 操作 |
| M019 | N/A | 无 switch 枚举 |
| M020 | N/A | 无重写方法 |
| M021 | N/A | 无 equals 重写 |
| M022 | N/A | 无 Optional |
| M023 | N/A | 无 toString 未重写对象 |
| M024 | N/A | 无 Optional |
| M025 | N/A | 无 final 类 protected 成员 |
| M026 | N/A | 无 @Mock |
| M027 | N/A | 无 ThreadLocal |
| I001 | N/A | 无异常捕获测试，使用直接断言 |
| I002 | N/A | 无 @Mock |
| I003 | N/A | 无 @Mock |
| I004 | N/A | 无 java.util.Date |
| I005 | N/A | 纯 JUnit4，无 JUnit3 混用 |
| I006 | N/A | 无 @Before |
| I007 | N/A | 无 @After |
| I008 | N/A | 无 DataProvider |
| I009 | N/A | 统计类规则，不适用 |
| I010 | N/A | 无 @RunWith |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1 | N/A | 无并发/事务操作，纯静态方法测试 |
| G1.2 | N/A | 无锁操作 |
| G1.3 | N/A | 无乐观锁 |
| G1.4 | N/A | 无多资源加锁 |
| G2.1 | N/A | 无写接口/消息消费 |
| G2.2 | N/A | 无重试/定时任务 |
| G2.3 | N/A | 无幂等键约定 |
| G3.1 | N/A | 无事务操作 |
| G3.2 | N/A | 无 @Transactional |
| G4.1 | N/A | 无 SQL 操作 |
| G4.2 | N/A | 无 SQL 操作 |
| G4.3 | N/A | 无 SQL 操作 |
| G5.1 | N/A | 无 MQ 操作 |
| G6.1 | N/A | 无缓存操作 |
| G6.2 | N/A | 无缓存操作 |
| G7.1 | N/A | 无调度任务 |
| G7.2 | N/A | 无调度任务 |
| G8.1 | N/A | 无异常处理 |
| G8.2 | N/A | 无核心链路依赖 |
| G8.3 | N/A | 无 I/O 流操作 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1 | N/A | 无外部调用 |
| G9.2 | N/A | 无外部调用 |
| G9.3 | N/A | 无重试逻辑 |
| G10.1 | N/A | 无接口字段 |
| G10.2 | N/A | 无契约变更 |
| G11.1 | ✅ | `HelloWorldTest.java` — 4 个测试方法均有断言（`assertEquals` / `assertNotNull` / 无异常隐式断言） |
| G11.2 | ✅ | 覆盖了正常返回值、main入口、多次调用一致性、非空边界 |
| G11.3 | N/A | 无入参校验场景（`getGreeting()` 无参） |
| G11.4 | N/A | 无数值运算 |
| G12.1 | N/A | 无资金相关场景 |
| G12.2 | N/A | 无资金相关场景 |
| G13.1 | N/A | 无日志操作 |
| G14.1 | N/A | 无金额操作 |
| G14.2 | N/A | 无多租户 |
| G14.3 | N/A | 无时区操作 |
| G14.4 | N/A | 无 SimpleDateFormat |
| G15.1 | N/A | 无表结构变更 |
| G15.2 | N/A | 无接口共存 |
| G15.3 | N/A | 无开关控制 |
| G16.1 | N/A | 无核心链路埋点 |
| G16.2 | N/A | 无异常路径 |
| G16.3 | N/A | 无日志输出 |
| G16.4 | N/A | 无空 catch |
| G17.1 | N/A | 无功能开关 |
| G17.2 | N/A | 无降级预案 |
| G17.3 | N/A | 无数据变更 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1 | N/A | 无 SQL 操作 |
| S1.2 | N/A | 无 SQL 操作 |
| S1.3 | N/A | 无 SQL 操作 |
| S2.1 | N/A | 无 HTML/JS 输出 |
| S2.2 | N/A | 无富文本 |
| S2.3 | N/A | 无模板引擎 |
| S3.1 | N/A | 无外部 URL 请求 |
| S3.2 | N/A | 无外部 URL 请求 |
| S3.3 | N/A | 无外部 URL 请求 |
| S4.1 | N/A | 无系统命令 |
| S4.2 | N/A | 无文件操作 |
| S5.1 | N/A | 无 XML 解析 |
| S5.2 | N/A | 无 XML 解析 |
| S6.1 | N/A | 无反序列化 |
| S6.2 | N/A | 无反序列化 |
| S6.3 | N/A | 无反序列化 |
| S7.1 | N/A | 无文件上传 |
| S7.2 | N/A | 无文件上传 |
| S7.3 | N/A | 无文件上传 |
| S8.1 | N/A | 无鉴权接口 |
| S8.2 | N/A | 无 HTTP 接口 |
| S8.3 | N/A | 无数据 ID |
| S8.4 | N/A | 无 Cookie |
| S9.1 | N/A | 无密钥配置 |
| S9.2 | N/A | 无日志敏感信息 |
| S9.3 | N/A | 无传输加密 |
| S9.4 | N/A | 无随机数 |
| S10.1 | N/A | 无 CSRF |
| S10.2 | N/A | 无 CORS |
| S10.3 | N/A | 无 URL 跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

> 按 `customized-checklist.md` 逐条核销；若未启用可整节写 `N/A(未启用自定义规则)`。

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 示例项，未启用自定义规则 |
| U1.2 | N/A | 未启用自定义规则 |
| U1.3 | N/A | 未启用自定义规则 |
| U2.1 | N/A | 未启用自定义规则 |
| U2.2 | N/A | 未启用自定义规则 |
| U2.3 | N/A | 未启用自定义规则 |

**整体**: N/A(未启用自定义规则)

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（允许 `N/A`，但有原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（允许 `N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`