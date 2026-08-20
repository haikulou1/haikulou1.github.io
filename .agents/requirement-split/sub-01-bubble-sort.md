# SUB-01：教学示例 — 冒泡排序算法

## 结果/用户价值
提供一个可独立运行的冒泡排序教学示例，包含完整的代码实现和注释，用于向学习者演示冒泡排序的核心思想（相邻元素比较与交换）。学习者可通过 main 方法观察排序过程，通过单元测试验证正确性。

## 范围与仓库影响

### 新增文件
| 文件 | 说明 |
|------|------|
| `src/main/java/com/example/sort/BubbleSort.java` | 冒泡排序实现类，含 `sort(int[])` 方法和 `main` 演示入口 |
| `src/test/java/com/example/sort/BubbleSortTest.java` | 冒泡排序单元测试，覆盖多种输入场景 |

### 包结构
- 新建包 `com.example.sort`
- 与现有 `com.example.helloworld` 包同级
- 无需修改任何现有文件

### 实现要求
- **类名：** `BubbleSort`
- **核心方法：** `public static void sort(int[] arr)` — 原地排序
- **演示入口：** `public static void main(String[] args)` — 输出排序前后的数组
- **注释要求：** 类 Javadoc 说明算法原理，方法 Javadoc 说明参数和逻辑，关键步骤行内注释
- **代码风格：** 遵循 Java 8 命名规范，与现有 `com.example.helloworld` 包风格一致

## 验收标准
1. `BubbleSort.sort()` 对任意整数数组执行升序排序，结果正确
2. `main` 方法可独立运行，在标准输出打印排序前和排序后的数组
3. 代码注释完整，包含算法时间复杂度说明（O(n²)）
4. 单元测试覆盖以下场景：
   - 正常未排序数组（如 `{5, 1, 4, 2, 8}` → `{1, 2, 4, 5, 8}`）
   - 已排序数组（不应出错）
   - 逆序数组
   - 空数组（`{}`）
   - 单元素数组（`{1}`）
   - 含有重复元素的数组

## 依赖
- 无。SUB-01 可独立实施，不依赖任何其他子需求。
- 技术依赖：Java 8 + JUnit 4.13.2（已在 `pom.xml` 中声明）

## 测试证据
- 测试框架：JUnit 4.13.2
- 测试类：`BubbleSortTest.java`
- 测试方法：每个验收场景对应一个 `@Test` 方法
- 验证方式：`assertArrayEquals` 比较预期数组与实际结果

## 实施顺序
- 优先级：1（建议优先实施，作为流程验证）
- 可独立实施，无需等待其他子需求
