# Code Review (Round 2 复审) — Java HelloWorld & 哈希算法接口

> **Change** `java-api-demo` · **分支** `AI/task-DEV-ddccb2af-7620-11f1-9e19-e337058ec5b9-3696ddbb-e5a4-4e91-` / commit `196e0e1`（round 2 编码）· **日期** 2026-07-31
> **技能** `/dtazziboot-java-code-review` · **阶段** loop-1 / 代码审查（round 2 复审）
> **前置审查** `docs/reviews/2026-07-31-loop1-code-review.md`（round 1，commit `08f291d`）
> **系分/设计文档** `docs/specs/2026-07-31-java-hello-hash-api-design.md`

> **AI**：唯一进度源；状态仅用 `⬜` `✅` `❌` `⚠️` `N/A`。
> **复审目标**：核验 round 1 提出的 P1/P2 问题是否已闭环，并对 round 2 CR 修复后的全量代码重新预扫 + LLM 复核。

---

## 0. 预扫结果（scan-all-rules.sh，round 2）

执行命令：`bash references/script/scan-all-rules.sh java-api-demo/`

```
=== Step 4 Rule Scan (B/M/I + A/S/G) ===
Targets: java-api-demo/
Engine:  ripgrep

[P0] G16.2 — CatchWithoutLogging: java-api-demo/src/main/java/com/example/demo/service/HashService.java:37
[P2] A2.2 — WildcardImport: java-api-demo/src/test/java/com/example/demo/controller/HelloControllerTest.java:10
[P2] A2.2 — WildcardImport: java-api-demo/src/test/java/com/example/demo/service/HashServiceTest.java:5

=== Summary: 3 findings (P0=1, P1=0, P2=2) | 52/222 rules scanned ===
```

**与 round 1 对比**：round 1 报告记录"52 条规则零命中，退出码 0"。本轮扫描命中 **3 条**（1 P0 + 2 P2）。差异原因分析见 §4。

---

## 1. CR 修复闭环核验

### round 1 问题清单修复状态

| # | round 1 问题 | 等级 | 位置 | 修复 commit | 修复状态 | 证据 |
|---|---|---|---|---|---|---|
| 1 | S5 依赖版本 `3.2.5` CVE 滞后 | P1 | `pom.xml:10` | `196e0e1` | ✅ 已修复 | `<version>3.2.12</version>`（3.2.x 最新补丁） |
| 2 | S3 `input` 无长度上限致 DoS | P2 | `HashController.java:30` | `196e0e1` | ✅ 已修复 | 新增 `MAX_INPUT_LENGTH=1_048_576` 常量（:22）+ 长度校验（:40-43）+ 测试 `hash_oversizedInput_returns400`（HelloControllerTest.java:59-67） |
| 3 | A2.3 静态/非静态 import 组间缺空行 | P2 | `HelloControllerTest.java:7-10` | — | ✅ 已满足 | 当前文件第 7 行（`MockMvc`）与第 9 行（`import static`）间已有空行（第 8 行）；CR diff 未改 import 区，判断 round 1 报告记录可能存在行号偏差 |

**CR 修复闭环结论**：round 1 提出的 3 项问题全部闭环（2 项经 CR 修复，1 项当前已满足）。

---

## 2. round 2 CR 修复 diff 核验

commit `196e0e1` 变更范围（`+20 / -1`，3 文件）：

| 文件 | 变更 | 核验 |
|------|------|------|
| `pom.xml` | `3.2.5` → `3.2.12` | ✅ 版本号正确，3.2.x 最新补丁 |
| `HashController.java` | +`MAX_INPUT_LENGTH` 常量 + 长度校验分支 | ✅ 校验逻辑正确：`input.length() > MAX_INPUT_LENGTH` → 400 + 错误信息；位置在 `isSupported` 校验前，先做长度限制再校验算法，合理 |
| `HelloControllerTest.java` | +`hash_oversizedInput_returns400` 测试 | ✅ 测试断言正确：1_048_577 字符输入 → 400 + `"input exceeds maximum length of 1048576 characters"` |

**新引入问题检查**：CR 修复未引入新的功能缺陷、Bug 模式或安全问题。`MAX_INPUT_LENGTH` 使用 `1_048_576`（1MB）合理，错误信息使用字符串拼接可接受（demo 级别）。

---

## 3. 本轮预扫命中 LLM 逐条复核

### 命中 1：G16.2 CatchWithoutLogging（P0）

| 项 | 值 |
|----|----|
| 规则 ID | G16.2 |
| 规则名 | CatchWithoutLogging |
| 脚本标记等级 | **P0** |
| 位置 | `HashService.java:37` |
| 命中代码 | `} catch (NoSuchAlgorithmException e) { throw new IllegalArgumentException("unsupported algorithm: " + algorithm, e); }` |

**LLM 复核**：
- **命中事实属实**：catch 块确实无日志记录语句。
- **但严重性应降级**，理由：
  1. **异常非静默吞没**：catch 块将 `NoSuchAlgorithmException` 重新抛出为 `IllegalArgumentException`，且携带原始 cause `e`，调用方能完整感知异常链——与 G16.2 防范的"静默吞异常"场景不同。
  2. **Controller 前置校验**：`HashController` 在调用 `hash()` 前已用 `isSupported()` 白名单校验（:46），正常路径不会触发此 catch。
  3. **demo 项目无日志框架**：设计文档 §7 明确"不做日志框架配置"，`pom.xml` 无 slf4j/logback 依赖，无法在 catch 中写 log。
  4. round 1 报告 G8（异常处理）已标记 ✅、G9（日志）标记为 N/A（设计 §7 非目标），结论一致。
- **最终等级**：P0 → **P2**（建议项，不阻塞）
- **建议**：若未来引入日志框架，在 catch 块补充 `log.warn("unsupported algorithm: {}", algorithm, e)` 后再抛出。

| 状态 | ⚠️ P2（降级） |

### 命中 2：A2.2 WildcardImport（P2）— HelloControllerTest.java:10

| 项 | 值 |
|----|----|
| 规则 ID | A2.2 |
| 规则名 | WildcardImport |
| 脚本标记等级 | P2 |
| 位置 | `HelloControllerTest.java:10` |
| 命中代码 | `import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;` |

**LLM 复核**：
- **误报**。此为**静态 import 通配符**（`import static ...*`），阿里巴巴 Java 代码风格指南 A2.2 规则针对**非静态** import 禁止通配符，对**静态 import 通配符允许**（测试代码中 `MockMvcResultMatchers.*`、`Assertions.*` 为惯用写法）。
- scan 脚本程序化匹配不区分静态/非静态 import，导致误报。
- round 1 报告 A2.2 已复核为 ✅，结论一致。

| 状态 | ✅（误报） |

### 命中 3：A2.2 WildcardImport（P2）— HashServiceTest.java:5

| 项 | 值 |
|----|----|
| 规则 ID | A2.2 |
| 规则名 | WildcardImport |
| 脚本标记等级 | P2 |
| 位置 | `HashServiceTest.java:5` |
| 命中代码 | `import static org.junit.jupiter.api.Assertions.*;` |

**LLM 复核**：
- **误报**。同命中 2，静态 import 通配符，阿里规约允许。

| 状态 | ✅（误报） |

---

## 4. round 1 vs round 2 预扫结果差异分析

| 项 | round 1 报告记录 | round 2 实际扫描 |
|----|------------------|------------------|
| G16.2 CatchWithoutLogging | 未记录（"No findings"） | P0 命中 `HashService.java:37` |
| A2.2 WildcardImport ×2 | 未记录（"No findings"） | P2 命中 ×2 |

**差异原因**：round 2 编码（`196e0e1`）仅修改了 `pom.xml`/`HashController.java`/`HelloControllerTest.java`，未触及 `HashService.java:37` 和 `HashServiceTest.java:5`。这两处命中在 round 1 时即已存在。round 1 报告记录的"No findings / 退出码 0 / 52 条零命中"与实际不符，判断 round 1 报告**可能未真正运行 scan-all-rules.sh**，或预扫结果记录有误。

> 注：round 1 报告 §0 记录的扫描输出为 `=== No findings. 52/222 rules scanned ===` / `===EXIT:0===`，但本轮扫描同一目标得到 3 条命中。代码未变的两处命中（`HashService.java:37`、`HashServiceTest.java:5`）在 round 1 时理应同样被扫出。

---

## 5. 整体复审结论

### 问题计数（round 2 复审，LLM 复核后）

| P0 | P1 | P2 |
|----|----|-----|
| 0 | 0 | 1 |

### 问题清单

| # | 等级 | 类型 | 规则 | 位置 | 描述 | LLM 复核结论 |
|---|------|------|------|------|------|-------------|
| 1 | P2 | 可靠性 | G16.2 | `HashService.java:37` | catch 块无日志记录 | 降级 P0→P2：异常被 re-throw（非吞没）+ demo 无日志框架 + Controller 前置校验 |
| 2 | — | 可读性 | A2.2 | `HelloControllerTest.java:10` | 静态 import 通配符 | 误报：阿里规约允许静态 import 通配 |
| 3 | — | 可读性 | A2.2 | `HashServiceTest.java:5` | 静态 import 通配符 | 误报：同上 |

### CR 修复闭环总结

| 维度 | 结论 |
|------|------|
| round 1 P1 修复 | ✅ S5 依赖版本已升级 `3.2.12` |
| round 1 P2 修复 | ✅ S3 输入长度限制已新增 `MAX_INPUT_LENGTH` + 测试覆盖 |
| round 1 P2 风格 | ✅ A2.3 import 空行已满足 |
| CR 新引入问题 | ✅ 无新引入缺陷 |

### 验收标准复核（设计文档 §9）

| # | 验收标准 | 核对 | 状态 |
|---|---------|------|------|
| 1 | 独立可构建 Maven 工程 | `pom.xml` 结构完整，parent `3.2.12` + 依赖 + 插件齐备 | ✅（构建未实跑，受降级协议；静态审查通过） |
| 2 | `GET /api/hello` 返回 `{"message":"Hello World"}` | `HelloController.java:18` | ✅ |
| 3 | `GET /api/hash?algorithm=sha-256&input=abc` 返回正确摘要 | `HelloControllerTest.java:37-38` 断言 `ba7816bf...` | ✅ |
| 4 | 非法 algorithm → 400；缺失 input → 400 | `HashController.java:35-37,46-48` + 测试覆盖 | ✅ |
| 5 | 未修改博客静态文件 | git log 显示仅 `java-api-demo/` + 文档新增 | ✅ |

### 整体评级

**✅ 审查通过**

- **P0 阻塞项：0**（G16.2 经 LLM 复核降级为 P2）— 可合入
- **P1 建议项：0**（round 1 的 P1 已闭环）
- **P2 建议项：1**（G16.2 catch 无日志，demo 非目标范围，建议未来引入日志框架时补充）
- **误报项：2**（A2.2 静态 import 通配 ×2，阿里规约允许）

CR 修复质量良好：P1 依赖升级 + P2 输入长度防御均正确落地，附带完整测试覆盖。无新引入缺陷。唯一留存项 G16.2 为 demo 项目非目标范围（无日志框架），不构成阻塞。

---

## 附：复审范围元数据

- **复审 commit**：`196e0e1`（round 2 编码）
- **CR 修复文件**：3（`pom.xml` / `HashController.java` / `HelloControllerTest.java`）
- **CR 修复行数**：+20 / -1
- **预扫规则**：52/222 程序化规则，命中 3 条（LLM 复核后 1 降级 + 2 误报）
- **LLM 复核规则**：G16.2 / A2.2 ×2
- **Git 只读**：全程未执行任何 Git 写操作 ✅
- **隔离约束**：所有审查限定 `java-api-demo/` 子目录 ✅
