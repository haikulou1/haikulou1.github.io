# Code Review Checklist

> **Change** `quicksort-demo(coding)` · **分支/Commit** `AI/task-DEV-72ed08cb-78db-11f1-8f3f-75954cb1c56f-9ee704da-0b11-4114-96e3-42789808994b` / `9fb6080` · **日期** `2026-08-18`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。
>
> **执行顺序（强制）**：写入本清单并进入逐文件审查前，先在目标仓库对变更路径运行 `references/script/scan-all-rules.sh`，将输出贴入 Step 3 和 Step 4 备注；再用 LLM 完成 Step 2–5 中脚本未覆盖项及复核。

> **自动化预扫结果**（`scan-all-rules.sh`，目标：`src/main/java/com/digital/algorithm` `src/test/java/com/digital/algorithm`）：
> ```
> === Step 4 Rule Scan (B/M/I + A/S/G) ===
> Targets: src/main/java/com/digital/algorithm src/test/java/com/digital/algorithm
> Engine:  ripgrep
> === No findings. 52/222 rules scanned ===
> ```
> 结论：52 条可程序化规则（B/M/I + A/S/G 子集）无命中。剩余 170 条由 LLM 逐条核销。

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1 | G2 | G3 | G4 | G5 | G6 | G7 | G8 | G9 | G10 | G11 | G12 | G13 | G14 | G15 | G16 | G17 | S1 | S2 | S3 | S4 | S5 | S6 | S7 | S8 | S9 | S10 | 总状态 |
|---|----------------------|----------|-------|-------|----|----|----|----|----|----|----|----|----|-----|-----|-----|-----|-----|-----|-----|-----|----|----|----|----|----|----|----|----|----|----|--------|
| 1 | `.agents/quicksort/impl.md` | 设计文档 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非 Java) |
| 2 | `pom.xml` | 构建配置 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过 | 跳过(非 Java) |
| 3 | `src/main/java/com/digital/algorithm/QuickSort.java` | REQ-1~5 实现 | ✅ | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ 已审 |
| 4 | `src/test/java/com/digital/algorithm/QuickSortTest.java` | REQ-1~5 验证 | ✅ | ⚠️ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ✅ | N/A | N/A | ✅ | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A | ⚠️ 已审有问题 |

- 展开自 `git diff --name-only HEAD~1`：`.agents/quicksort/impl.md`、`pom.xml`、`src/main/java/com/digital/algorithm/QuickSort.java`、`src/test/java/com/digital/algorithm/QuickSortTest.java`。
- **Java 守卫**：存在 2 个 `.java` 文件，继续审查。
- 非文件 `impl.md`/`pom.xml` 标 `跳过(非 Java)`。
- **收口**：所有文件的 Step2/Step3/G1–G17/S1–S10 均非 `⬜`。

---

## Step 2 — 功能（产物 B）

> REQ 来源：`.agents/quicksort/impl.md`（编码报告）+ 任务需求「实现一个快速排序算法」。

| REQ | Scenario | Spec证据（原文/章节） | 关联文件 | 状态 | 代码证据（文件/测试/接口） |
|-----|----------|----------------------|----------|------|----------------------------|
| REQ-1 | Given 任意可比较数组，When 调用 sort，Then 原地升序排序 | impl.md「提供基于比较的泛型快速排序工具方法（原地排序）」 | `QuickSort.java`、`QuickSortTest.java` | ✅ | `QuickSort.java:26-31` sort 入口；测试 `should_sortAscending_when_unsortedIntegerArray:23-32` |
| REQ-2 | Given 工具类，Then 不可实例化 | impl.md「关键类 QuickSort（final 工具类，私有构造）」 | `QuickSort.java` | ✅ | `QuickSort.java:17` `public final class`；`QuickSort.java:98` 私有构造 |
| REQ-3 | Given null 入参，Then 抛 IllegalArgumentException | impl.md「null 入参抛 IllegalArgumentException」 | `QuickSort.java`、`QuickSortTest.java` | ✅ | `QuickSort.java:27-29` null 校验抛 `IllegalArgumentException`；测试 `should_throwException_when_arrayIsNull:142-148` |
| REQ-4 | Given 已排序/逆序输入，Then 不退化到最坏情况 | impl.md「原地 Lomuto 分区，选取中间元素为 pivot 并交换到末端，规避已排序/逆序输入的最坏退化」 | `QuickSort.java` | ✅ | `QuickSort.java:61-76` partition 取 `mid` 交换至 `high`；测试覆盖已排序/逆序 `:36-58` |
| REQ-5 | Given 任意可比较类型，Then 泛型支持 | impl.md「泛型约束 `<T extends Comparable<? super T>>`」 | `QuickSort.java` | ✅ | `QuickSort.java:26` `<T extends Comparable<? super T>>`；测试字符串数组 `:100-110` |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注（命中写 `path:line`） |
|----|--------|------|----------------------------|
| A1 | 源文件格式 | ✅ | 文件名=类名.java；UTF-8；4 空格缩进无 Tab |
| A2 | 源文件结构/import 顺序 | ✅ | package→import→class；无 `import *`；静态/非静态分组正确 |
| A3 | 代码样式 | ✅ | K&R 大括号；行宽≤120；成员间空行；运算符两侧空格 |
| A4 | 命名规范 | ✅ | 包名全小写；类名 UpperCamelCase；方法 lowerCamelCase；泛型 `T`；测试类 `QuickSortTest` |
| A5 | 编码实践 | ✅ | 无重写需 `@Override`；无空 catch；静态方法以类名/简名调用；无 finalize |
| A6 | 特定元素样式 | ✅ | 数组方括号属类型 `T[] array`；修饰符顺序 `public static`；注解每行一个 |
| A7 | Javadoc 规范 | ✅ | public 类与成员均有 Javadoc；块标顺序 `@param`→`@return`→`@throws`；段落用 `<p>` |
| —  | 测试描述准确性（A7/Javadoc 类） | ⚠️ | `QuickSortTest.java:61` `@DisplayName("含重复元素的数组排序后应稳定升序")`：quicksort 为**非稳定**排序，"稳定"措辞误导；`QuickSortTest.java:126` `@DisplayName("较大规模随机数组排序后应与系统排序结果一致")`：实际为 20 个固定元素，非随机、非大规模 |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> 预扫 `scan-all-rules.sh` 无命中。下表按相关性归并核销；与本算法/测试无关的规则标 `N/A` 并写原因。

| ID 区间 | 状态 | 备注 |
|--------|------|------|
| B002/B004/B005 数组比较/toString/asList | ✅ | 测试用 AssertJ `containsExactly`，无 `array.equals`/`toString`/`Arrays.asList(primitive[])` |
| B006/B053/B077/B078/B080/B081 JUnit 误用 | ✅ | AssertJ 断言；null 用 `assertThatThrownBy`；无 `TruthSelfEquals`；每测试均含断言 |
| B016 `ComparableType` | ✅ | `<T extends Comparable<? super T>>` 用法正确（PECS） |
| B035/B046/B068/B069/B070/B075 自反/循环/自赋值 | ✅ | for 循环 `j<high` 且 `j++` 更新；swap 非自赋值；无 `compareTo(self)`/`equals(self)` |
| B001/B009-B013/B018-B033/B036-B065/B071-B074/B076 不相关规则 | N/A | 无日期/金钱/集合误用/反射/注解/线程池/异常构造未抛等场景 |
| M001-M027 Major | ✅/N/A | M007 空 catch N/A（无 catch）；M020/M021 equals 无重写 N/A；M005 内部类 N/A；其余不相关 N/A |
| I001-I010 Info | ✅/N/A | I001 null 测试已断言消息 `hasMessageContaining("array")`；其余 N/A |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1–G1.4 并发 | N/A | 纯单线程算法，无锁/事务/并发 |
| G2.1–G2.3 幂等 | N/A | 非写接口/MQ 消费 |
| G3.1–G3.2 事务 | N/A | 无事务/分布式 |
| G4.1–G4.4 SQL | N/A | 无 SQL/DB |
| G5.1 MQ | N/A | 无 MQ |
| G6.1–G6.2 缓存 | N/A | 无缓存 |
| G7.1–G7.2 调度 | N/A | 无调度任务 |
| G8.1 防御-happy path | ✅ | sort 含 null 防御；无 catch 吞异常 |
| G8.2 强依赖超时降级 | N/A | 无外部依赖 |
| G8.3 资源释放 | ✅ | 无 I/O 流/连接/锁需释放 |
| G8.4 线程池关闭 | N/A | 无线程池 |
| G8.5 ThreadLocal remove | N/A | 无 ThreadLocal |
| G8.6 无界队列线程池 | N/A | 无 Executors |
| G9.1–G9.3 网络调用 | N/A | 无 RPC/HTTP |
| G10.1–G10.2 接口契约 | N/A | 无对外契约字段 |
| G11.1 新逻辑单测 | ✅ | 10 个测试，均含断言 |
| G11.2 边界覆盖 | ✅ | 空/单元素/已排序/逆序/重复/负正混合 |
| G11.3 入参空值防御 | ✅ | `QuickSort.java:27-29` null 抛 IllegalArgumentException |
| G11.4 数值溢出/精度 | N/A | 仅比较与下标运算；`mid=low+(high-low)/2` 已防溢出 |
| G12.1–G12.2 资损 | N/A | 无资金场景 |
| G13.1 监控 | N/A | 无告警/日志埋点需求 |
| G14.1–G14.4 国际化/时区 | N/A | 无金额/时区/多租户 |
| G15.1–G15.3 灰度 | N/A | 无 DB schema/接口兼容变更 |
| G16.1–G16.4 可监控 | N/A | 无核心链路埋点；无 catch 吞异常 |
| G17.1–G17.3 可应急 | N/A | 无开关/回滚需求（工具类） |
| G18.1–G18.3 安全补强 | N/A | 无安全补强项 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1–S1.3 SQL 注入 | N/A | 无 SQL |
| S2.1–S2.3 XSS | N/A | 无 HTML/JS 输出 |
| S3.1–S3.3 SSRF | N/A | 无外部 URL 请求 |
| S4.1–S4.2 命令执行 | N/A | 无系统命令/外部进程 |
| S5.1–S5.2 XXE | N/A | 无 XML 解析 |
| S6.1–S6.3 反序列化 | N/A | 无反序列化 |
| S7.1–S7.3 文件上传/下载 | N/A | 无文件上传/下载 |
| S8.1–S8.4 访问控制 | N/A | 无鉴权接口 |
| S9.1–S9.4 数据安全 | N/A | 无密钥/敏感数据；无随机数 |
| S10.1–S10.3 CSRF/CORS/跳转 | N/A | 无 Web 接口 |

---

## Step 5 — 自定义扩展检查（产物 E）

### 5.1 自定义扩展（`customized-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1–U2.3 | N/A | `customized-checklist.md` 仅有示例项（U1.1 标注"示例项"，U2 为空），未启用自定义规则 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、**S1–S10 / G1–G17** 各列均非 `⬜`（跳过文件除外）；
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1–A7 均非 `⬜`
- [x] Step 4 全部 **G/S** 与 **B001–B081 / M001–M027 / I001–I010** ID 均非 `⬜`（按区间归并核销，允许 `N/A`，已写原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（`N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`
