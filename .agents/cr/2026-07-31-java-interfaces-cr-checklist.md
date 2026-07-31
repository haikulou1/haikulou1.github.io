# Code Review Checklist

> **Change** `java-interfaces` · **分支/Commit** `AI/task-DEV-ddccb2af` / `5f5b0ec` · **日期** `2026-07-31`
>
> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **完成标准**：所有核销项必须从 `⬜` 变为其他状态；`N/A` 需写原因。

> **scan-all-rules.sh 预扫结果**：`No findings. 52/222 rules scanned`（ripgrep 引擎，目标 `java-interfaces/`）

---

## Step 1 — 执行队列（产物 A）

| # | 文件（仓库相对路径） | 归属原因 | Step2 | Step3 | G1-G17 | S1-S10 | 总状态 |
|---|----------------------|----------|-------|-------|--------|--------|--------|
| 1 | `java-interfaces/.../DemoApplication.java` | 启动类 | ✅ | ✅ | N/A | N/A | ✅ |
| 2 | `java-interfaces/.../GlobalExceptionHandler.java` | 异常处理 | N/A | ✅ | ✅G4/G16 | N/A | ✅ |
| 3 | `java-interfaces/.../controller/HelloWorldController.java` | REQ-1 | ✅ | ✅ | N/A | ✅S6 | ✅ |
| 4 | `java-interfaces/.../controller/BubbleSortController.java` | REQ-2/3 | ✅ | ✅ | ✅G14 | ✅S6 | ✅ |
| 5 | `java-interfaces/.../controller/QuickSortController.java` | REQ-4 | ✅ | ✅ | ✅G14 | ✅S6 | ✅ |
| 6 | `java-interfaces/.../dto/SortRequest.java` | DTO | N/A | ✅ | N/A | N/A | ✅ |
| 7 | `java-interfaces/.../dto/SortResponse.java` | DTO | N/A | ✅ | N/A | N/A | ✅ |

> `pom.xml` 为构建配置，非 `.java`，标 `跳过`。Java 守卫：7 个 `.java` 文件 > 0，通过。

---

## Step 2 — 功能（产物 B）

| REQ | Scenario | Spec证据 | 关联文件 | 状态 | 代码证据 |
|-----|----------|----------|----------|------|----------|
| REQ-1 | GET /api/helloworld→{"message":"Hello World"} | §5.1 已定案；F2 | `HelloWorldController.java` | ✅ | `:23-26` |
| REQ-2 | POST /api/bubble-sort {"arr"}→{"sorted"} | §5.1 已定案；F3-F5 | `BubbleSortController.java` | ✅ | `:36-47` |
| REQ-3 | 冒泡算法/边界（空/单元素/null→400/超长→400） | §5.3 算法约束 | `BubbleSortController.java` | ✅ | `:37-47` + `GlobalExceptionHandler:33-36` |
| REQ-4 | 第三个接口（快排） | §3.2 U1 已定案（类 Javadoc 标注）；§5.1 | `QuickSortController.java` | ✅ | `:38-49`；spec→code 可追溯性已补齐 |

---

## Step 3 — 可读性检查（产物 C）

| ID | 检查项 | 状态 | 备注 |
|----|--------|------|------|
| A1 | 源文件格式 | ✅ | package 声明正确，UTF-8 无 BOM |
| A2 | 源文件结构/import 顺序 | ✅ | 项目包→Spring→JDK，分组合理 |
| A3 | 代码样式 | ✅ | 4 空格缩进一致 |
| A4 | 命名规范 | ✅ | PascalCase 类名，camelCase 方法名 |
| A5 | 编码实践 | ✅ | 标准 getter/setter；魔法值已提取为常量 `MAX_ARRAY_LENGTH` |
| A6 | 特定元素样式 | N/A | 无常量/枚举等特定元素 |
| A7 | Javadoc 规范 | ✅ | `HelloWorldController:18-22`、`BubbleSortController:30-35`、`QuickSortController:32-37` 公开方法已补方法级 Javadoc（`@param`/`@return`）；私有方法亦有 Javadoc |

---

## Step 4 — 可靠性检查（产物 D）

### 4.1 Bug 模式（`bug-pattern-checklist.md`）

> `scan-all-rules.sh` 预扫：**No findings. 52/222 rules scanned**。
> 本 demo 为无状态 REST 接口，无 SQL/MQ/并发/反射/序列化/IO 等场景，B001-B081 / M001-M027 / I001-I010 中涉及上述场景的规则均 N/A。

| ID 范围 | 状态 | 备注 |
|---------|------|------|
| B001-B081 | ✅/N/A | 预扫无命中；LLM 复核无 NPE/越界/资源泄漏；其余涉及 SQL/反射/IO 等场景项 N/A(本 demo 无相关场景) |
| M001-M027 | ✅/N/A | 预扫无命中；涉及并发/事务/MQ 项 N/A(无状态接口) |
| I001-I010 | ✅/N/A | 预扫无命中；信息级建议项 N/A(无相关场景) |

### 4.2 可靠性（`reliability-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| G1.1-G1.4 | N/A | 无共享可变状态，无状态 Controller，无并发场景 |
| G2.1-G2.3 | N/A | 无 closeable 资源/流/连接 |
| G3.1-G3.2 | N/A | 无数据库事务 |
| G4.1 | ⚠️ | `GlobalExceptionHandler:17` 仅处理 IllegalArgumentException，HttpMessageNotReadableException 未捕获返回 500(P2) |
| G4.1 | ✅ | `GlobalExceptionHandler:45-49` 增补 `@ExceptionHandler(HttpMessageNotReadableException.class)`→400；原 P2-G4 已修复 |
| G4.2-G4.4 | N/A | 无重试/超时/限流场景 |
| G5.1 | N/A | 无幂等要求 |
| G6.1-G6.2 | N/A | 无资源清理 |
| G7.1-G7.2 | N/A | 无线程池 |
| G8.1-G8.7 | N/A | 无缓存 |
| G9.1-G9.3 | N/A | 无 MQ |
| G10.1-G10.3 | N/A | 无定时任务 |
| G11.1-G11.4 | N/A | 无灰度发布 |
| G12.1-G12.2 | N/A | 无监控埋点（demo 范围） |
| G13.1 | N/A | 无降级 |
| G14.1 | ✅ | 冒泡边界：空/单元素/null/超长 正确处理 |
| G14.2 | ✅ | `QuickSortController:75-88,98-112` 改用三数取中 Lomuto 分区，规避最坏递归深度；原 P2-G14 已修复 |
| G14.3-G14.4 | ✅ | 数组下标循环边界正确 |
| G15.1-G15.3 | N/A | 无应急/回滚场景 |
| G16.1 | ✅ | `GlobalExceptionHandler:25,35,47` 引入 SLF4J Logger，异常记录 warn 级日志；原 P2-G16 已修复 |
| G16.2-G16.3 | N/A | 无业务日志场景 |
| G17.1-G17.3 | N/A | 无应急预案 |
| G18.1-G18.3 | N/A | 安全补强归入 S 节，此处无额外项 |

### 4.3 安全（`security-checklist.md`）

| ID | 状态 | 备注 |
|----|------|------|
| S1.1-S1.3 | N/A | 无 SQL |
| S2.1-S2.3 | N/A | JSON API 无 HTML 渲染，无 XSS |
| S3.1-S3.3 | N/A | 无命令执行 |
| S4.1-S4.2 | N/A | 无认证授权 |
| S5.1-S5.2 | N/A | 无密钥/凭证 |
| S6.1 | ✅ | `BubbleSortController:41-42`、`QuickSortController:43-44` 增 `MAX_ARRAY_LENGTH=10000` 上限校验→400；原 P2-S6 已修复 |
| S6.2-S6.3 | ✅ | null 已校验→400 |
| S7.1-S7.3 | N/A | 无文件上传 |
| S8.1-S8.4 | N/A | 无 SSRF/外呼 |
| S9.1-S9.4 | N/A | 无反序列化 |
| S10.1-S10.3 | N/A | 无重定向/CSRF（无状态 GET/POST） |

---

## Step 5 — 自定义扩展检查（产物 E）

| ID | 状态 | 备注 |
|----|------|------|
| U1.1-U1.3 | N/A | `customized-checklist.md` 为示例项，未启用自定义规则 |
| U2.1-U2.3 | N/A | 同上 |

---

## 终检（防漏检）

- [x] 执行队列中每个文件 `Step2`、`Step3`、S1-S10 / G1-G17 各列均非 `⬜`（跳过文件除外）
- [x] Step 2 的每个 REQ/Scenario 均非 `⬜`
- [x] Step 3 的 A1-A7 均非 `⬜`
- [x] Step 4 全部 G/S 与 B001-B081/M001-M027/I001-I010 ID 均非 `⬜`（允许 N/A，已写原因）
- [x] Step 5 全部 U* ID 均非 `⬜`（`N/A(未启用自定义规则)`）
- [x] 所有 `❌/⚠️` 已写入 report，且包含 `ID + path:line`

---

## 修订记录

| 版本 | 日期 | Commit | 说明 |
|------|------|--------|------|
| v1 | 2026-07-31 | `5f5b0ec` | 首次 CR，5 项 P2（A7/G4/G14/G16/S6） |
| v2 | 2026-07-31 | `c97007c` | CR 修复后复查：5 项 P2 全部修复，无新增问题，全部 ✅ |
| v3 | 2026-07-31 | `a975ce7` | 第三轮复查：代码无变化，scan 复扫无命中，逐文件复核全部 ✅ |
