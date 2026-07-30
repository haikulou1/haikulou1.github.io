# 测试报告生成 Skill — 需求澄清与设计文档

> 阶段：clarify（需求澄清）　|　技能：/brainstorming　|　产物类型：设计文档
> 本文档不修改任何代码文件，仅沉淀澄清结论与设计方向，供后续编码阶段落地。

---

## 1. 需求理解（通览）

提供一个 Skill，使 Agent 在执行测试后自动解析测试结果并生成结构化、可读性强的标准测试报告，解决测试结果散落、人工汇总成本高、失败回溯困难、质量指标难沉淀等痛点。

### 1.1 核心目标

- **G1**：一条指令完成 执行测试 → 收集结果 → 生成报告。
- **G2**：报告标准化，含摘要、明细、失败分析、覆盖率四大板块。
- **G3**：支持主流框架结果解析（Jest/Vitest/pytest/JUnit XML）。
- **G4**：多输出格式，默认 Markdown。

### 1.2 跨仓现状（无关性强绑定）

| 仓库 | 现状 | 与 P0 测试栈关联 |
|------|------|-----------------|
| [haikulou1.github.io] | GitHub Pages 静态站 + `pom.xml`(Maven/Java) | 弱（非 JS/TS/pytest 主栈） |
| [naiss] | `preprocess/`/`ref/`/`schedule/`，README 空 | 未知 |
| [InternetArchitect] | 架构教程资料集合 | 无统一测试栈 |

> 结论：本 Skill 为通用 Agent Skill，不依附任一仓库业务代码；产物统一落盘至列表首个 worktree（haikulou1.github.io）。

---

## 2. 开放问题澄清结论

> 澄清问题被拒后，依据需求文档自带默认假设 + 安全兜底 + 改动最小原则自主决策。

| 问题 | 需求文档倾向 | 决策 | 依据 |
|------|------------|------|------|
| **Q1** 首期项目栈 | "以 TypeScript/Node 为主（P0 按此假设）" | **采纳默认假设** | 需求方已明确，G3/FR1.2 一致 |
| **Q2** 报告语言模板 | 未定 | **仅中文模板** | 需求中文撰写、跨团队中文同步为主；双语增模板工作量且非 G2 必需 |
| **Q3** 渠道推送 | "列为非目标" | **维持非目标** | 仅落盘+返回路径摘要，不内建 IM/邮件推送；安全兜底(NFR3)、范围最收 |

### 2.1 P0/P1 范围裁定

| 优先级 | 范围 | 对应里程碑 |
|--------|------|-----------|
| **P0** | Jest/Vitest JSON + JUnit XML 解析、Markdown 报告、执行/解析双模式、失败分析、结果摘要 | M1 |
| **P1** | pytest 支持、覆盖率章节、fail_threshold、HTML 输出、JSON 伴随产物 | M2/M3 |
| **P2** | 历史趋势对比、Go test / cargo test | M4（后续迭代，本期不做） |

### 2.2 非目标确认（本期不做）

- 不做测试用例自动生成/修复（仅报告）。
- 不做报告在线托管/Web 服务化展示。
- 不做多次运行趋势对比（P2 候选）。
- 不做非测试类质量报告（lint/安全扫描）聚合。
- 不做 IM/邮件渠道自动推送（Q3 维持非目标）。

---

## 3. 初步设计方向（规划）

### 3.1 Skill 目录结构（编码阶段落地形态）

```
test-report-skill/
├── SKILL.md                 # 触发意图、配置项默认值、使用说明
├── scripts/
│   ├── run.js               # 入口：模式判定 → 执行/解析 → 渲染 → 落盘
│   ├── detectors/
│   │   └── framework.js     # 框架/命令自动检测（FR1.1 优先级 a/b/c）
│   ├── parsers/             # 解析器插件（NFR5 插件式）
│   │   ├── jest-vitest.js   # Jest/Vitest JSON reporter
│   │   ├── junit.js         # JUnit XML 兜底（跨语言）
│   │   └── pytest.js        # P1：pytest JUnit XML / JSON
│   ├── renderers/
│   │   ├── markdown.js      # 默认渲染器（中文模板）
│   │   ├── html.js          # P1
│   │   └── json.js          # P1 伴随产物
│   └── utils/
│       ├── sanitize.js      # 堆栈敏感信息过滤（NFR3）
│       └── time.js          # 时间戳格式化
```

### 3.2 中间数据结构 `TestResult`（解析器统一产出）

统一中间结构是 NFR5 插件式设计的关键：各解析器将异构结果归一为 `TestResult`，渲染器只消费该结构，新增框架不影响既有解析器与渲染器。

```
TestResult {
  meta: { framework, frameworkVersion, command, envSummary, generatedAt }
  summary: { total, passed, failed, skipped, passRate, durationMs }
  failures: [{ name, file, error, stackTail }]   // 截断至可读长度
  suites: [{ file, cases: [{ name, status, durationMs }] }]  // >200 条截断
  coverage: { statements, branches, functions, lines, lowCoverageFiles[] } | null
  appendix: { resultFilePaths, toolVersion }
}
```

### 3.3 双模式流程（FR1.3）

- **执行模式**：自动检测命令(FR1.1) → 后台运行+轮询(R2 长任务) → 收集结果文件 → 解析 → 渲染 → 落盘。
- **解析模式**：读取 `result_file` 指定文件 → 跳过执行 → 解析 → 渲染 → 落盘（满足 US4/AC3）。
- **执行失败诊断**（FR1.4）：命令无法运行时返回明确诊断信息，**禁止生成空报告冒充成功**（对应 AC4）。

### 3.4 报告章节（FR2 固定顺序，中文模板）

1. 报告头：项目名、生成时间、执行命令、框架/版本、执行环境摘要。
2. 结果摘要：总数、通过/失败/跳过、通过率、总耗时；整体结论 ✅ / ❌（fail_threshold 不达标则 ❌）。
3. 失败用例分析（有失败必选）：用例名、所属文件、错误信息、堆栈关键行（截断）。
4. 用例明细：按文件分组，>200 条截断并注明。
5. 覆盖率（若可获取）：语句/分支/函数/行总表 + 低于阈值文件清单；缺失标注"未获取"（AC5）。
6. 附录：原始结果文件路径、生成工具版本。

### 3.5 配置项（FR4.2，均有默认值）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| test_command | 自动检测 | 测试执行命令 |
| result_file | 自动检测 | 解析模式结果文件路径 |
| output_format | markdown | markdown / html / json |
| output_path | reports/ | 报告输出目录 |
| coverage | auto | auto / on / off |
| fail_threshold | 无 | 通过率低于该值标记不达标 |

### 3.6 非功能约束映射

| NFR | 设计落点 |
|-----|---------|
| NFR1 性能(≤5s/1000用例) | 解析+渲染纯内存处理，不触网 |
| NFR2 健壮性 | 字段缺失降级"未获取"，不崩溃不空报告 |
| NFR3 安全 | sanitize.js 过滤凭据/敏感路径，不泄露环境变量/密钥 |
| NFR4 幂等性 | 同结果多次生成内容一致（generatedAt 除外） |
| NFR5 可维护性 | 解析器插件式，统一 TestResult 中间结构 |

### 3.7 验收标准映射

| AC | 对应设计点 |
|----|-----------|
| AC1 | Jest/Vitest JSON 解析器 + Markdown 渲染 + 固定章节 |
| AC2 | 失败分析章节含 name/file/error/stackTail |
| AC3 | 解析模式（result_file 跳过执行） |
| AC4 | 执行失败诊断，不生成空报告 |
| AC5 | coverage 缺失标注"未获取"，其余章节正常 |

---

## 4. 风险与缓解

| 风险 | 缓解 |
|------|------|
| R1 各框架 reporter 输出差异大 | 统一 TestResult 中间结构 + 插件式解析器（NFR5） |
| R2 测试执行耗时不可控 | 执行模式走后台任务+轮询，不阻塞 Agent 主流程 |

---

## 5. 跨仓对齐点

- 本 Skill 不直接调用三仓库业务接口，无跨库接口契约变更。
- 产物落盘于 [haikulou1.github.io] worktree，不影响 [naiss]/[InternetArchitect] 代码。
- 后续编码阶段若需在具体仓库验证解析器，应在该仓库 worktree 内运行测试，不交叉写文件。
