# SUB-02：教学示例 — 最短路径算法（Dijkstra）

## 结果/用户价值
提供一个可独立运行的最短路径教学示例，包含完整的图数据结构和 Dijkstra 算法实现，用于向学习者演示单源最短路径的计算过程。学习者可通过 main 方法观察从起点到图中各节点的最短路径及距离，通过单元测试验证算法正确性。

## 范围与仓库影响

### 新增文件
| 文件 | 说明 |
|------|------|
| `src/main/java/com/example/graph/Graph.java` | 图数据结构（邻接表实现），支持添加边和节点 |
| `src/main/java/com/example/graph/Dijkstra.java` | Dijkstra 最短路径算法实现，含 `computeShortestPaths(int source)` 方法和 `main` 演示入口 |
| `src/test/java/com/example/graph/DijkstraTest.java` | Dijkstra 算法单元测试，覆盖多种图结构 |

### 包结构
- 新建包 `com.example.graph`
- 与现有 `com.example.helloworld` 包同级
- 无需修改任何现有文件

### 实现要求
- **Graph 类：**
  - 使用邻接表存储图结构（`Map<Integer, List<Edge>>`）
  - 内部类 `Edge` 包含目标节点 `to` 和权重 `weight`
  - 方法：`addEdge(int from, int to, int weight)`、`addNode(int node)`、`getNeighbors(int node)`
- **Dijkstra 类：**
  - 核心方法：`public static Map<Integer, Integer> shortestPaths(Graph graph, int source)` — 返回从源点到各节点的最短距离
  - 辅助方法：`public static Map<Integer, List<Integer>> shortestPathWithRoute(Graph graph, int source)` — 返回最短路径和路径节点列表（可选，用于演示）
  - 演示入口：`public static void main(String[] args)` — 构建示例图并输出最短路径结果
- **注释要求：** 类 Javadoc 说明算法原理（贪心策略、松弛操作），方法 Javadoc 说明参数和返回值，关键步骤行内注释
- **使用标准库：** `java.util.*`（PriorityQueue 实现最小堆优化）

## 验收标准
1. `Dijkstra.shortestPaths()` 对给定有向带权图返回正确的单源最短路径距离
2. `main` 方法可独立运行，输出从源点到各节点的最短路径距离和路径
3. 代码注释完整，包含算法时间复杂度说明（O((V+E)logV)）
4. 单元测试覆盖以下场景：
   - 简单连通图（教科书标准示例）
   - 存在多条路径的图（验证最短路径选择）
   - 存在不可达节点的图（距离应为 `Integer.MAX_VALUE`）
   - 单节点图
   - 图中包含负权边时应如何处理（可选：注明 Dijkstra 不适用于负权边）

## 依赖
- 无。SUB-02 可独立实施，不依赖任何其他子需求。
- 技术依赖：Java 8 + JUnit 4.13.2（已在 `pom.xml` 中声明）

## 测试证据
- 测试框架：JUnit 4.13.2
- 测试类：`DijkstraTest.java`
- 测试方法：每个验收场景对应一个 `@Test` 方法
- 验证方式：`assertEquals` 比较预期距离值

## 实施顺序
- 优先级：2（建议在 SUB-01 之后实施，或并行实施）
- 可独立实施，无需等待其他子需求
- 注意：若与 SUB-01 并行实施，需确保两个开发分支不冲突（文件路径完全不同）
