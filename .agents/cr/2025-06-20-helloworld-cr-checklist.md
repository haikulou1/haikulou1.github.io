# Code Review Checklist

> **Change** helloworld · **分支/Commit** AI/task-DEV-72ed08cb-78db-11f1-8f3f-75954cb1c56f-56baae98-6379-4f6f-9424-8e1f59891bab · **日期** 2025-06-20
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|--------|
| 1 | `src/main/java/com/example/HelloWorldService.java` | REQ-1 接口定义 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 2 | `src/main/java/com/example/HelloWorldServiceImpl.java` | REQ-2 实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 3 | `src/main/java/com/example/HelloWorldApplication.java` | REQ-3 入口 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/test/java/com/example/HelloWorldServiceImplTest.java` | REQ-4 测试 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | 定义 HelloWorldService 接口，含 greet(String) 方法 | impl.md:21 — "服务接口" | HelloWorldService.java | ✅ | `HelloWorldService.java:9-17` — 接口定义含 `String greet(String name)` |
| REQ-2 | 实现 greet 方法，null/空串时用默认名称 "World" | impl.md:22 — "服务实现"; READ: null/空串边界 | HelloWorldServiceImpl.java | ✅ | `HelloWorldServiceImpl.java:27-29` — `(name == null \|\| name.isEmpty()) ? DEFAULT_NAME : name` |
| REQ-3 | 提供 main 入口，调用服务输出问候语 | impl.md:23 — "程序入口" | HelloWorldApplication.java | ✅ | `HelloWorldApplication.java:16-18` — `main` 方法创建 `HelloWorldServiceImpl` 并调用 `greet("World")` |
| REQ-4 | 4 个测试方法：正常路径、null、空串、个性化问候 | impl.md:17-18 — "测试方法数：4 / 覆盖场景：正常路径 ✓, 边界值(null/空串) ✓, 个性化问候 ✓" | HelloWorldServiceImplTest.java | ✅ | 4 个 `@Test` 方法 at lines 20, 33, 46, 59 |

---

## Step 3 — 可读性检查（产物 C）

> 预扫结果：[P2] A2.2 — WildcardImport: `src/test/java/com/example/HelloWorldServiceImplTest.java:5`

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 所有文件 UTF-8、文件名 = 类名 + .java |
| A2 | 源文件结构/import 顺序 | ⚠️ | **A2.2** `HelloWorldServiceImplTest.java:5` — `import static org.junit.Assert.*` 使用通配符导入（预扫命中 P2） |
| A3 | 代码样式 | ✅ | K&R 大括号、4 空格缩进、行宽 ≤120、运算符空格均合规 |
| A4 | 命名规范 | ✅ | 包名全小写、类名 UpperCamelCase、方法名 lowerCamelCase、常量 UPPER_SNAKE_CASE、测试类名 `*Test` |
| A5 | 编码实践 | ✅ | `@Override` 已加（`HelloWorldServiceImpl.java:26`）、无空 catch、静态方法无实例调用 |
| A6 | 特定元素样式 | ✅ | 数组方括号 `String[] args` 合规、无 switch、修饰符顺序正确 |
| A7 | Javadoc 规范 | ✅ | 所有 public 类/方法均有 Javadoc、`@param`/`@return` 顺序正确 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫已运行 `scan-all-rules.sh`，除 A2.2 外无其他命中。

| ID | 状态 | 备注 |
|----|------|------|
| B001–B081 | N/A | 均为集合操作、异常处理、并发、资源等复杂场景规则，本次变更仅含简单字符串拼接 + 空判断，不触发 |
| M001–M027 | N/A | 同上，为代码质量/可维护性中等规则，HelloWorld 极简代码不触发 |
| I001–I010 | N/A | 同上，为信息级建议规则，不适用 |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 | N/A | 无并发/锁场景 |
| G2.1–G2.3 | N/A | 无写接口/消息消费 |
| G3.1–G3.2 | N/A | 无事务/数据库 |
| G4.1–G4.3 | N/A | 无 SQL/数据库 |
| G5.1 | N/A | 无 MQ |
| G6.1–G6.2 | N/A | 无缓存 |
| G7.1–G7.2 | N/A | 无调度任务 |
| G8.1 | ✅ | `HelloWorldServiceImpl.java:28` — 非 happy path 仅处理 null/空串，为简单逻辑无异常路径，可接受 |
| G8.2 | N/A | 无外部依赖 |
| G8.3 | N/A | 无 I/O 流/连接/锁 |
| G8.4 | N/A | 无线程池 |
| G8.5 | N/A | 无 ThreadLocal |
| G8.6 | N/A | 无线程池 |
| G9.1–G9.3 | N/A | 无外部调用 |
| G10.1–G10.2 | N/A | 无接口契约场景 |
| G11.1 | ✅ | 4 个测试方法均有断言（`assertNotNull` + `assertEquals`） |
| G11.2 | ✅ | 已覆盖 null、空串、正常值、个性化值 |
| G11.3 | ✅ | `HelloWorldServiceImpl.java:28` — `name == null \|\| name.isEmpty()` 已做防御性空值校验 |
| G11.4 | N/A | 无数值运算 |
| G12.1–G12.2 | N/A | 无资金场景 |
| G13.1 | N/A | 无日志输出 |
| G14.1–G14.4 | N/A | 无金额/多租户/时区 |
| G15.1–G15.3 | N/A | 无 DB 变更/接口共存 |
| G16.1–G16.4 | N/A | 无核心链路/异常日志 |
| G17.1–G17.3 | N/A | 无应急场景 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 | N/A | 无 SQL |
| S2.1–S2.3 | N/A | 无 Web 输出 |
| S3.1–S3.3 | N/A | 无外部 URL 请求 |
| S4.1–S4.2 | N/A | 无命令执行 |
| S5.1–S5.2 | N/A | 无 XML |
| S6.1–S6.3 | N/A | 无反序列化 |
| S7.1–S7.3 | N/A | 无文件上传/下载 |
| S8.1–S8.4 | N/A | 无 Web 接口 |
| S9.1–S9.4 | N/A | 无密钥/凭证/加密 |
| S10.1–S10.3 | N/A | 无 CSRF/CORS/跳转 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1 | N/A | 仅示例项，无 Controller 层 |
| U2.1 | N/A | 未定义业务红线 |

> **整节**：N/A(未启用自定义规则) — `customized-checklist.md` 仅含示例项 U1.1，无实际业务/团队自定义规则。

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`
- [x] Step 5 全部 U* ID 均非 `⬜`
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`