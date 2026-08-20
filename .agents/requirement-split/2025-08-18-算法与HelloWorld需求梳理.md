# 需求梳理：HelloWorld、冒泡排序、最短路径算法

> **生成日期**：2025/08/18
> **来源**：requirement-complexity-splitter 技能 · 完整报告（阶段一→二→三→四）
> **前置任务**：已完成 `QuickSort` 快速排序实现（`AI/task-DEV-72ed08cb-78db-11f1-8f3f-75954cb1c56f-9ee704da-0b11-4114-96e3-42789808994b`）
> **澄清结果**：练习任务 | Dijkstra 算法 | 顺序交付（HelloWorld→冒泡排序→Dijkstra）| 包路径由工具方决定

---

## 当前理解

### 需求原文
> 测试 写一个helloworld，冒泡排序，最短路径算法

### 仓库上下文
- **仓库**：`haikulou1.github.io` — 个人技术博客（Hexo + NexT 主题），包含 Java 算法演示子项目
- **已有成果**：`src/main/java/com/digital/algorithm/QuickSort.java` + `QuickSortTest.java`，已完成快速排序的编码和测试
- **项目结构**：Maven 工程（`quicksort-demo`），JDK 8，JUnit 5 + AssertJ
- **当前分支**：`AI/task-DEV-966dcd0a-7905-11f1-9649-3b4281182f10-810c52d2-7aa2-405c-b94b-10344809e353`

### 已知事实
| 条目 | 说明 |
|------|------|
| 开头"测试" | 可能是"测试用的需求"或"测试驱动开发"，确切含义待确认 |
| HelloWorld | 标准入门程序，通常在 `main` 方法中输出字符串 |
| 冒泡排序 | 经典排序算法，与已完成的 QuickSort 同属排序算法模块 |
| 最短路径算法 | 图论经典算法（如 Dijkstra、Bellman-Ford、Floyd-Warshall 等），未指定具体算法 |
| 延续性 | 与上一个任务（QuickSort）属于同一算法演示系列 |

---

## 待回答的关键问题

以下问题会影响复杂度评估和拆分方案，请逐一答复。

### 问题 1：需求性质与目标

**问题：** 这个需求的业务目标是什么？"测试"二字是指"用测试驱动开发"（先写测试再写实现）、"给测试团队用的需求"、还是"这是一个测试/练习性质的任务，不需要上线"？

**为什么重要：** 决定验收标准、交付方式以及是否需要特性开关/分阶段发布。

**选项：**
1. **练习/学习任务** — 纯粹为了练习编码，不需要上线到生产环境，代码通过编译和单测即可
2. **博客内容补充** — 将实现代码作为博客文章的技术示例/演示，需考虑代码可读性和博客展示
3. **生产级代码** — 需要完整的文档、异常处理、性能优化和上线流程

**建议：** 从仓库已有 QuickSort 的上下文看，似乎是练习/学习任务（延续之前的算法实现系列），但请确认。

---

### 问题 2：最短路径算法的具体选择

**问题：** 最短路径算法有很多种，具体要实现哪一种？

**为什么重要：** 不同算法在复杂度、适用场景和实现难度上差异很大，直接影响评分和拆分方案。

**选项：**
1. **Dijkstra（迪杰斯特拉）** — 单源最短路径，非负权图，最经典常用
2. **Floyd-Warshall（弗洛伊德）** — 全源最短路径，O(n³)，实现简洁
3. **Bellman-Ford（贝尔曼-福特）** — 单源最短路径，支持负权边
4. **A\*（A星）** — 启发式搜索，用于路径规划

**建议：** 如果延续 QuickSort 的风格，建议选择 **Dijkstra**（最经典，与排序算法同属基础算法系列）。

---

### 问题 3：三个子任务的交付顺序和依赖关系

**问题：** HelloWorld、冒泡排序、最短路径算法这三个任务，是否应当按特定顺序交付？它们之间是否有依赖？

**为什么重要：** 决定拆分方案和交付顺序。

**选项：**
1. **按顺序逐一交付**：HelloWorld → 冒泡排序 → 最短路径算法（从简单到复杂）
2. **并行独立交付**：三者互不依赖，可同时实现
3. **先冒泡排序（与 QuickSort 同类）再最短路径，最后 HelloWorld**

**建议：** 三者功能独立，无代码依赖，建议按 ①HelloWorld → ②冒泡排序 → ③最短路径算法 的顺序，从简单到复杂逐步交付。

---

### 问题 4：代码存放位置与包名约定

**问题：** 新代码应放在哪个包路径下？是否沿用 QuickSort 的 `com.digital.algorithm` 包？

**为什么重要：** 决定仓库影响范围，避免重构冲突。

**选项：**
1. **沿用 `com.digital.algorithm`** — 统一放在已有算法包下
2. **新建子包** — 如 `com.digital.algorithm.sort`（排序）和 `com.digital.algorithm.graph`（图算法）
3. **按任务分开** — 不同任务使用不同包结构

**建议：** 建议沿用 `com.digital.algorithm` 包，冒泡排序可与 QuickSort 同级，最短路径可新建 `com.digital.algorithm.graph` 子包。

---

## 暂定影响地图

> ⚠️ 以下为基于需求描述的**推断**，置信度**低**。待关键问题澄清后会更新。

| 领域 | 预期变化 | 置信度 |
|------|---------|:------:|
| `src/main/java/com/digital/algorithm/` — 新增类文件 | `HelloWorld.java`（入口演示） | 中 |
| `src/main/java/com/digital/algorithm/` — 新增类文件 | `BubbleSort.java`（冒泡排序实现） | 高 |
| `src/main/java/com/digital/algorithm/` 或 `graph/` — 新增类文件 | `Dijkstra.java` 或 `ShortestPath.java`（最短路径算法） | 中（算法未定） |
| `src/test/java/com/digital/algorithm/` — 新增测试文件 | 各算法的单元测试（参照 QuickSortTest 风格） | 高 |
| `pom.xml` | 预计无需修改，已有 JUnit 5 + AssertJ 依赖 | 高 |
| 博客文章（`source/_posts/` 目录） | 可能新增博客文章展示算法实现（若为博客内容补充） | 低 |

---

## 建议下一步

1. **请回答以上 4 个澄清问题**，特别是问题 1（需求性质）和问题 2（最短路径算法选择），它们是后续评分和拆分的输入基础。

2. 在问题澄清后，我将进入 **阶段二（复杂度评分）** 和 **阶段三（拆分设计）**，输出：
   - 七维度复杂度评分
   - 分类结论（简单/中等/复杂）
   - 有序子需求列表（每个子需求包含：用户价值、验收标准、仓库影响、依赖关系）

3. 若判定为"简单"需求（0-6 分），则保持为一个需求整体交付；若为"中等"或"复杂"，则拆分为多个可独立验收的子需求。

---

## 阶段二：逐项复杂度评分

### 子需求 1 — HelloWorld

| 维度 | 分值 | 说明 |
|:----|:---:|:----|
| ① 业务领域复杂度 | 0 | 练习任务，无业务逻辑 |
| ② 需求模糊度 | 0 | 已澄清：标准 HelloWorld 程序 |
| ③ 技术实现复杂度 | 0 | 单行输出，零技术门槛 |
| ④ 依赖与接口复杂度 | 0 | 无外部依赖，无接口调用 |
| ⑤ 数据复杂度 | 0 | 无数据输入/处理 |
| ⑥ 非功能性需求 | 0 | 无性能/安全要求 |
| ⑦ 验收与测试复杂度 | 0 | 验证标准输出内容即可 |
| **总分** | **0** | **分类：简单** |

### 子需求 2 — 冒泡排序

| 维度 | 分值 | 说明 |
|:----|:---:|:----|
| ① 业务领域复杂度 | 0 | 练习任务，无业务逻辑 |
| ② 需求模糊度 | 0 | 已澄清：标准冒泡排序算法 |
| ③ 技术实现复杂度 | 1 | 经典排序，双层循环，实现简单 |
| ④ 依赖与接口复杂度 | 0 | 无外部依赖 |
| ⑤ 数据复杂度 | 1 | 整数数组输入/输出 |
| ⑥ 非功能性需求 | 0 | 无性能要求 |
| ⑦ 验收与测试复杂度 | 1 | 需覆盖空数组、单元素、正序、逆序、重复值 |
| **总分** | **3** | **分类：简单** |

### 子需求 3 — Dijkstra 最短路径

| 维度 | 分值 | 说明 |
|:----|:---:|:----|
| ① 业务领域复杂度 | 0 | 练习任务，无业务逻辑 |
| ② 需求模糊度 | 0 | 已澄清：Dijkstra 单源最短路径 |
| ③ 技术实现复杂度 | 2 | 需图结构（邻接表）、优先队列（最小堆），实现中等 |
| ④ 依赖与接口复杂度 | 0 | 无外部依赖，使用 Java 标准库 `PriorityQueue` |
| ⑤ 数据复杂度 | 2 | 图结构数据（节点+边+权重） |
| ⑥ 非功能性需求 | 0 | 无性能要求 |
| ⑦ 验收与测试复杂度 | 2 | 需构建测试图、验证路径距离和路径节点 |
| **总分** | **6** | **分类：简单（临界值）** |

---

## 阶段三：拆分设计

### 包路径约定

| 约定项 | 决定 |
|:------|:-----|
| 基础包 | `com.digital.algorithm`（沿用 QuickSort 所在包） |
| 排序算法 | 直接放在 `com.digital.algorithm` 下（与 QuickSort 同级） |
| 图算法 | 放在 `com.digital.algorithm.graph` 子包下 |
| 入口演示 | 放在 `com.digital.algorithm` 下 |

### 拆分为 3 个独立子需求

| ID | 子需求名称 | 总分 | 产出物 | 交付顺序 |
|:--:|:----------|:---:|:-------|:--------:|
| **SR-1** | HelloWorld 实现 | 0 | `HelloWorld.java` + `HelloWorldTest.java` | 第 1 位 |
| **SR-2** | 冒泡排序实现 | 3 | `BubbleSort.java` + `BubbleSortTest.java` | 第 2 位 |
| **SR-3** | Dijkstra 最短路径实现 | 6 | `graph/Dijkstra.java` + `graph/DijkstraTest.java` | 第 3 位 |

### 子需求详细设计

#### SR-1：HelloWorld 实现

| 项目 | 内容 |
|:----|:------|
| **用户故事** | 作为练习者，我希望能运行一个 HelloWorld 程序，验证 Java 开发环境正常工作 |
| **验收标准** | ① 编译通过无错误；② 运行 `main` 方法打印 `"Hello, World!"`；③ 单元测试验证输出内容为 `"Hello, World!"` |
| **仓库影响** | 新增 `src/main/java/com/digital/algorithm/HelloWorld.java` |
| | 新增 `src/test/java/com/digital/algorithm/HelloWorldTest.java` |
| **依赖关系** | 无 |
| **技术提示** | 使用 `System.out.println` 输出；测试使用 `System.setOut` 捕获输出流或使用 `ByteArrayOutputStream` |

#### SR-2：冒泡排序实现

| 项目 | 内容 |
|:----|:------|
| **用户故事** | 作为练习者，我希望能对整数数组进行冒泡排序，理解排序算法的工作原理 |
| **验收标准** | ① 编译通过无错误；② `sort(int[] array)` 方法对任意整数数组升序排序；③ 测试覆盖：空数组、单元素数组、已正序数组、逆序数组、含重复值数组；④ 与 `Arrays.sort()` 结果一致 |
| **仓库影响** | 新增 `src/main/java/com/digital/algorithm/BubbleSort.java` |
| | 新增 `src/test/java/com/digital/algorithm/BubbleSortTest.java` |
| **依赖关系** | 无（独立于 SR-1） |
| **技术提示** | 经典双层循环实现；可参考 `QuickSort.java` 的接口风格（`sort` 静态方法）；遵循 QuickSort 的异常处理模式（`IllegalArgumentException` for null） |

#### SR-3：Dijkstra 最短路径实现

| 项目 | 内容 |
|:----|:------|
| **用户故事** | 作为练习者，我希望能对加权有向图运行 Dijkstra 算法，找到从源节点到所有节点的最短路径 |
| **验收标准** | ① 编译通过无错误；② 实现 `Dijkstra.computeShortestPaths(Graph, int source)` 返回距离和路径；③ 测试覆盖：连通图（验证正确距离和路径）、不连通节点（距离为无穷大）、单个节点图、含多条相同最短路径的图；④ 输入校验（null 图、无效源节点抛出异常） |
| **仓库影响** | 新增 `src/main/java/com/digital/algorithm/graph/Dijkstra.java` |
| | 新增 `src/test/java/com/digital/algorithm/graph/DijkstraTest.java` |
| **依赖关系** | 无（独立于 SR-1/SR-2） |
| **技术提示** | 使用邻接表表示图；使用 `java.util.PriorityQueue` 实现最小堆；可设计内部 `Node` 或 `Edge` 辅助类；建议封装 `Graph` 类或提供静态方法 |

### 仓库影响矩阵

| 文件路径 | 操作 | 归属 |
|:---------|:---:|:----:|
| `src/main/java/com/digital/algorithm/HelloWorld.java` | 新增 | SR-1 |
| `src/test/java/com/digital/algorithm/HelloWorldTest.java` | 新增 | SR-1 |
| `src/main/java/com/digital/algorithm/BubbleSort.java` | 新增 | SR-2 |
| `src/test/java/com/digital/algorithm/BubbleSortTest.java` | 新增 | SR-2 |
| `src/main/java/com/digital/algorithm/graph/Dijkstra.java` | 新增 | SR-3 |
| `src/test/java/com/digital/algorithm/graph/DijkstraTest.java` | 新增 | SR-3 |

---

## 阶段四：产物校验

### 自检清单

- [x] 仓库勘察已完成（代码结构、技术栈、已有成果）
- [x] 需求澄清已完成（4 个关键问题已答复）
- [x] 三项子需求各自独立评分已完成
- [x] 拆分设计已完成（3 个独立子需求）
- [x] 每个子需求含：用户故事、验收标准、仓库影响、依赖关系
- [x] 包路径约定已确定
- [x] 仓库影响范围已明确

### 未覆盖的风险

| 风险项 | 说明 |
|:------|:-----|
| Dijkstra 图数据结构设计 | 需确定 Graph 表示方式（邻接表 vs 邻接矩阵 vs 边列表），建议邻接表以降低复杂度 |
| 最短路径输出格式 | 需确定返回距离数组、路径信息或两者皆有，建议返回 `Map<Integer, PathResult>` |
| 博客文章同步 | 若需在博客展示算法，需额外创建 Hexo 文章（不在当前需求范围内） |
| Maven artifactId | 当前为 `quicksort-demo`，新增算法后无需改名 |

---

## 完整报告总结

### 需求概要

| 项目 | 内容 |
|:----|:------|
| 需求名称 | 算法与 HelloWorld 练习任务 |
| 需求类型 | 练习任务（3 个独立子需求） |
| 技术栈 | Java 8, Maven, JUnit 5, AssertJ |
| 拆分总数 | **3 个独立子需求** |
| 交付顺序 | SR-1 HelloWorld → SR-2 冒泡排序 → SR-3 Dijkstra 最短路径 |
| 预估影响文件数 | 6 个新增文件（3 实现 + 3 测试） |
| 包路径 | `com.digital.algorithm`（排序/演示） / `com.digital.algorithm.graph`（图算法） |

### 子需求一览

| ID | 名称 | 评分 | 复杂度 | 文件数 | 依赖 |
|:--:|:-----|:---:|:------:|:-----:|:----:|
| SR-1 | HelloWorld 实现 | 0 | 简单 | 2 | 无 |
| SR-2 | 冒泡排序实现 | 3 | 简单 | 2 | 无 |
| SR-3 | Dijkstra 最短路径 | 6 | 简单（临界） | 2 | 无 |

### 交付路径

```
SR-1: HelloWorld.java + HelloWorldTest.java  [评分 0]
    ↓（独立，无依赖）
SR-2: BubbleSort.java + BubbleSortTest.java  [评分 3]
    ↓（独立，无依赖）
SR-3: graph/Dijkstra.java + graph/DijkstraTest.java  [评分 6]
    ↓
✅ 3 个独立子需求全部交付完成
```

---

*本文档由 DTCoder 基于 requirement-complexity-splitter 技能生成*