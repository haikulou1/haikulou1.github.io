# Code Review Report

> **Change** `quicksort-demo(coding)` · **分支/Commit** `AI/task-DEV-72ed08cb-78db-11f1-8f3f-75954cb1c56f-9ee704da-0b11-4114-96e3-42789808994b` / `9fb6080` · **日期** `2026-08-18` · **审查者** AI
>
> **AI**：等级 **P0 / P1 / P2**；G/S 以 checklist 行内定义为准；Bug 模式以 `bug-pattern-checklist.md` 表头为准（Blocker→P0、Major→P1、Info→P2）。已运行 `scan-all-rules.sh`（无命中，52/222 规则），要点并入 §5，再写 LLM 结论。问题含 `path:line` 或清单 ID。本次仅 2 个 `⚠️`（P2），见 §7.1。

---

## 1. 审查范围

| 项 | 值 |
|----|-----|
| `.java` 文件数 | `2` |
| 变更行数 | `+398 / -0`（本次 change 全新增） |

| 类/接口 | 路径 | 角色（可选） |
|---------|------|--------------|
| `QuickSort` | `src/main/java/com/digital/algorithm/QuickSort.java` | 快速排序工具类（泛型、原地、final） |
| `QuickSortTest` | `src/test/java/com/digital/algorithm/QuickSortTest.java` | 单元测试（JUnit5 + AssertJ，10 例） |

> 非文件 `pom.xml`、`.agents/quicksort/impl.md` 标 `跳过(非 Java)`，不参与逐文件审查。

---

## 2. 问题计数

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 0 | 2 |

---

## 3. Step 2 — 功能（REQ）

### REQ-1: 原地升序快速排序

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| Given 任意可比较数组，When 调用 sort，Then 原地升序 | ✅ | impl.md「提供基于比较的泛型快速排序工具方法（原地排序）」 | `QuickSort.java:26-31`；`QuickSortTest.java:23-32` | 实现满足 |

### REQ-2: 工具类不可实例化

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| final + 私有构造 | ✅ | impl.md「关键类 QuickSort（final 工具类，私有构造）」 | `QuickSort.java:17`、`:98` | 满足 |

### REQ-3: null 入参抛 IllegalArgumentException

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| Given null，Then 抛 IllegalArgumentException | ✅ | impl.md「null 入参抛 IllegalArgumentException」 | `QuickSort.java:27-29`；`QuickSortTest.java:142-148` | 满足 |

### REQ-4: 中间元素 pivot 规避最坏退化

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| 已排序/逆序输入不退化 | ✅ | impl.md「原地 Lomuto 分区，选取中间元素为 pivot 并交换到末端，规避已排序/逆序输入的最坏退化」 | `QuickSort.java:61-76`；测试 `:36-58` | 满足 |

### REQ-5: 泛型支持任意可比较类型

| Scenario | 结果 | Spec证据 | 代码证据 | 说明 |
|----------|------|----------|----------|------|
| `<T extends Comparable<? super T>>` | ✅ | impl.md「泛型约束 `<T extends Comparable<? super T>>`」 | `QuickSort.java:26`；`QuickSortTest.java:100-110`（字符串） | PECS 用法正确 |

> 功能性结论：5 个 REQ 全部满足，无 P0。

---

## 4. Step 3 — 可读性检查

| 结果 | 说明（违规写 Ax.x 与 `path:行`） |
|------|--------------------------------|
| ⚠️ | A1–A6 全 ✅；A7 Javadoc 结构 ✅，但存在 2 处测试描述准确性问题（P2）：`QuickSortTest.java:61` "稳定升序"误导（quicksort 非稳定排序）；`QuickSortTest.java:126` "较大规模随机数组" 实为 20 个固定元素，非随机非大规模 |

---

## 5. Step 4 — 可靠性检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「已扫无命中」） |
|----|------|------|------|-------------------------------------|
| 可靠性 | `reliability-checklist.md` G1–G17 | ✅/N/A | — | G8.1/G8.3/G11.1/G11.2/G11.3 ✅（null 防御、无吞异常、单测含断言、边界覆盖）；G1–G7、G9–G10、G12–G17 均 N/A（无并发/事务/SQL/MQ/缓存/调度/网络/资损/监控/灰度/应急场景） |
| 安全 | `security-checklist.md` S1–S10 | N/A | — | 纯算法无 Web/SQL/反序列化/文件/命令/密钥场景，S1–S10 均 N/A |
| Bug 模式 | `bug-pattern-checklist.md` B/M/I（120） | ✅/N/A | — | 预扫 `scan-all-rules.sh` 无命中（52/222）；LLM 复核 B002/B006/B016/B035/B046/B053/B068-B070/B075/B077/B078/B080/B081 均无命中，其余不相关 N/A；M001-M027/I001-I010 无命中 |

---

## 6. Step 5 — 自定义扩展检查

| 域 | 参考 | 结果 | 等级 | 说明（列命中 ID 或「未启用自定义规则」） |
|----|------|------|------|------------------------------------------|
| 自定义扩展 | `customized-checklist.md` U* | N/A | — | `N/A(未启用自定义规则)`：清单仅含示例项（U1.1 标注"示例项"，U2 为空） |

---

## 7. 结论

- **合并建议**：通过
- **P0**：无
- **P1/P2**：
  1. **P2** `QuickSortTest.java:61` — `@DisplayName("含重复元素的数组排序后应稳定升序")`，"稳定"措辞误导：quicksort 为非稳定排序，无法保证相等元素的相对顺序。
  2. **P2** `QuickSortTest.java:126` — `@DisplayName("较大规模随机数组排序后应与系统排序结果一致")`，实际测试数据为 20 个固定硬编码元素，既非"较大规模"也非"随机"。
- **一句话**：功能、可靠性、安全、Bug 模式均无阻塞或隐患；仅测试描述措辞有 2 处可选改进项，建议合并后择机修正以避免误导维护者。

---

## 7.1 问题片段（必填）

### P2 — 测试描述"稳定升序"误导 `QuickSortTest.java:61`

- **等级 + 规则ID**：P2 / 可读性（A7 Javadoc/描述准确性）
- **定位**：`src/test/java/com/digital/algorithm/QuickSortTest.java:61`
- **问题说明**：quicksort 是非稳定排序，`@DisplayName` 中"稳定升序"措辞会误导维护者认为本实现保证稳定性。

片段范围：`src/test/java/com/digital/algorithm/QuickSortTest.java:60-71`

```java
L60|     @Test
L61|     @DisplayName("含重复元素的数组排序后应稳定升序")
L62|     void should_sortAscending_when_duplicateElements() {
L63|         // Arrange (Given)
L64|         Integer[] array = {3, 1, 2, 3, 1, 2};
L65|
L66|         // Act (When)
L67|         QuickSort.sort(array);
L68|
L69|         // Assert (Then)
L70|         assertThat(array).containsExactly(1, 1, 2, 2, 3, 3);
L71|     }
```

### P2 — 测试描述"较大规模随机数组"与实际数据不符 `QuickSortTest.java:126`

- **等级 + 规则ID**：P2 / 可读性（A7 Javadoc/描述准确性）
- **定位**：`src/test/java/com/digital/algorithm/QuickSortTest.java:126`
- **问题说明**：`@DisplayName` 称"较大规模随机数组"，但数据为 20 个固定硬编码元素，非随机、非大规模，描述与实现不符。

片段范围：`src/test/java/com/digital/algorithm/QuickSortTest.java:125-139`

```java
L125|     @Test
L126|     @DisplayName("较大规模随机数组排序后应与系统排序结果一致")
L127|     void should_matchSystemSort_when_largeRandomArray() {
L128|         // Arrange (Given)
L129|         Integer[] array = {9, 7, 8, 5, 6, 3, 4, 1, 2, 0,
L130|                 15, 13, 14, 11, 12, 19, 17, 18, 16, 10};
L131|         Integer[] expected = array.clone();
L132|         Arrays.sort(expected);
L133|
L134|         // Act (When)
L135|         QuickSort.sort(array);
L136|
L137|         // Assert (Then)
L138|         assertThat(array).containsExactly(expected);
L139|     }
```

---

## 8. 修复任务列表

> 无阻塞项；以下为可选改进，按 P2 列出。

### P0

- 无待修复项。

### P1

- 无待修复项。

### P2（可选）

- [ ] **P2** `src/test/java/com/digital/algorithm/QuickSortTest.java:61` — 将 `@DisplayName` 中"稳定升序"改为"升序"，避免暗示 quicksort 稳定性。
- [ ] **P2** `src/test/java/com/digital/algorithm/QuickSortTest.java:126` — 修正 `@DisplayName`：要么改写描述为"中等规模固定数组"，要么改用 `Random`/`ThreadLocalRandom` 生成较大随机数据以匹配描述。
