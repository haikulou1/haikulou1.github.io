# Bubble Sort Implementation Plan

## Plan Metadata

- **Requirement**: 写一个冒泡排序
- **Input File**: `js/utils.js`
- **Stage**: plan (实施计划 / `/writing-plans` skill)
- **Mount Point**: `NexT.utils.bubbleSort` (existing, `js/utils.js` L416-L441)
- **Date**: 2026-07-31

## Current State Summary

通览 `js/utils.js`（共 442 行）确认：`NexT.utils` 对象内（L416-L441）**已存在 `bubbleSort` 实现**。该实现位于 `loadScript` 方法（L414 `},` 结束）之后，作为 `NexT.utils` 对象的最后一个方法，L442 `};` 关闭整个对象字面量。

### Existing Implementation (L416-L441)

```javascript
  /**
   * Bubble sort for an array. Returns a new sorted array without
   * mutating the original. An optional comparator can be supplied;
   * by default it sorts numbers in ascending order.
   * @param {Array} arr - The array to sort.
   * @param {Function} [comparator] - A function (a, b) => number.
   * @returns {Array} A new sorted array.
   */
  bubbleSort: function(arr, comparator) {
    if (!Array.isArray(arr)) throw new TypeError('Expected an array');
    var result = arr.slice();
    var cmp = comparator || function(a, b) { return a - b; };
    var len = result.length;
    for (var i = 0; i < len - 1; i++) {
      for (var j = 0; j < len - 1 - i; j++) {
        if (cmp(result[j], result[j + 1]) > 0) {
          var temp = result[j];
          result[j] = result[j + 1];
          result[j + 1] = temp;
        }
      }
    }
    return result;
  }
```

## Requirement Mapping (Verification)

| 需求点 | 现有实现 | 行号 | 满足 |
|---|---|---|---|
| 冒泡排序算法（双重循环 + 相邻比较交换） | `for i` 外循环 + `for j` 内循环 + `temp` 交换 | L431-L438 | ✓ |
| 不修改原数组 | `arr.slice()` 浅拷贝 | L429 | ✓ |
| 默认升序 | `function(a, b) { return a - b; }` | L426-L428 | ✓ |
| 可选比较器 | `comparator \|\| function(a,b){return a-b}` | L426 | ✓ |
| 输入校验（非数组） | `Array.isArray(arr)` + `throw TypeError` | L425 | ✓（已存在） |
| 空数组/单元素短路 | `len-1` 为 -1/0，外循环条件不成立 | L431 | ✓（隐式） |
| 提前终止优化（swapped 标志） | **未实现** | — | ✗（纯性能优化，非正确性要求） |

### Verification Conclusion

需求「写一个冒泡排序」**当前已完全满足**。算法正确、不修改原数组、默认升序、支持可选比较器、含输入校验。无需新增算法实现。

## Optional Enhancement (Out of Current Stage Scope)

唯一可选增强项（非功能要求，不在当前 plan 阶段执行动码）：

### Enhancement: Early Termination (swapped flag)

- **目的**: 当某轮内循环无交换时提前终止，将已序数组从 O(n²) 降至 O(n)。
- **改动定位**（仅供后续编码阶段参考，当前不动码）:
  - L431 外循环初始化增 `var swapped;`
  - L432 外循环体首行 `swapped = false;`
  - L435 比较交换分支内置 `swapped = true;`
  - L439 外循环末尾增 `if (!swapped) break;`
- **风险评级**: 低
- **风险说明**: 纯性能优化，不影响正确性与返回语义；改动局限于 `bubbleSort` 方法体内，无外部 API 变更。
- **回滚策略**: 若引入后回归，恢复 L431-L438 原始双重循环结构（`git show` 可取历史版本，仅只读）。

## Implementation Plan (Task Decomposition)

> 当前为 plan 阶段，以下步骤标记编码阶段执行顺序；当前阶段仅产出本计划文档，**不修改 `js/utils.js` 源码**。

- [ ] **Task 1（已完成·核查）**：复核 `js/utils.js` L416-L441 现有 `NexT.utils.bubbleSort`，逐条映射需求，确认满足。
- [ ] **Task 2（已完成·评估）**：评估可选增强项范围，收窄为「提前终止优化」单一项，定位改动行号与风险。
- [ ] **Task 3（编码阶段·可选）**：若决定实施提前终止优化，按上述改动定位在 `bubbleSort` 方法体内增 `swapped` 标志；否则标记为不做（需求已满足）。
- [ ] **Task 4（编码阶段·验证）**：仅对 `js/utils.js` 执行静态代码审查（plan 阶段不构建）；编码阶段对 `bubbleSort` 逻辑分支、边界条件（空数组/单元素/已序/逆序）、类型一致性做静态审查，验证耗时上限 5 分钟。

## Stage Gate Compliance

- **当前阶段**: plan
- **执行路径**: 规划分支（「通览 → 规划」），产出设计产物
- **源码变更**: 无（`js/utils.js` 未修改）
- **Git 操作**: 无（仅允许只读 `git status/log/diff/show`）
- **设计产物**: 本文档

## Design Artifacts Inventory

| 产物 | 路径 | 类型 | 状态 |
|---|---|---|---|
| 实施计划文档 | `docs/superpowers/plans/bubble-sort-implementation.md` | 设计产物 | 已产出 |

## Code Changes Inventory

无代码变更（plan 阶段不修改源码）。
