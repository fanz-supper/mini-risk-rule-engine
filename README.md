# Mini Risk Rule Engine

一个用 **纯 Java** 从零实现的轻量级风控规则引擎轮子：

- 领域建模（Rule / Context / Decision）
- 规则配置化 & 可执行化
- 函数式接口（Predicate）
- 注解 + 反射
- 设计模式（策略 / 装饰器）
- 日志体系（SLF4J + Logback）

> 目标定位：不是“大而全的业务系统”，而是**可复用、可解释、可扩展**的“风控规则引擎内核”。

---

## 功能特性

### 1. 配置化规则（JSON 驱动）

- 规则不硬编码在 Java 里，而是存放在 `rules-demo.json` 中。
- 每条规则包含：
  - `id` / `description` / `scene` / `priority`
  - `action`（ALLOW / REJECT / MANUAL_REVIEW）
  - `logicalOp`（AND / OR）
  - `conditions`（字段 + 操作符 + 期望值）

示例：

```json
[
  {
    "id": "R_PAY_001",
    "description": "新用户首单金额过高，且注册时间过短",
    "scene": "PAY",
    "priority": 90,
    "action": "MANUAL_REVIEW",
    "logicalOp": "AND",
    "conditions": [
      { "field": "user.isNew", "op": "==", "value": "true" },
      { "field": "user.historyOrderCount", "op": "==", "value": "0" },
      { "field": "order.amount", "op": ">", "value": "1000" },
      { "field": "user.registerMinutes", "op": "<", "value": "60" }
    ]
  }
]
```

### 2. 规则引擎接口 + 多实现（策略模式）

- 抽象统一接口：

  ```
  public interface RuleEngine {
      DecisionResult evaluate(RiskContext context, List<Rule> rules);
  }
  ```

- 当前提供两种实现：

  - `SimpleRuleEngine`：基础规则执行器
    - 遍历规则 → 通过 `Predicate<RiskContext>` 判断命中
    - 按优先级排序规则
    - 根据动作优先级合成最终决策（REJECT > MANUAL_REVIEW > ALLOW）
  - `ExplainableRuleEngine`：
    - **装饰**一个已有 `RuleEngine`
    - 在不改业务决策逻辑的前提下，额外生成**解释信息**（每条规则、每个条件的实际值与匹配结果）

### 3. 可解释风控（ExplainableRuleEngine）

- 每次决策不仅返回最终动作，还返回：
  - 命中规则列表：`List<Rule>`
  - 规则解释明细：`List<RuleMatchDetail>`
    - 规则是否命中
    - 每个条件的 `ConditionMatch`：
      - 字段路径（如 `order.amount`）
      - 运算符（`>`, `<`, `==`, …）
      - 期望值
      - 实际值
      - 是否匹配
- 方便：
  - 风控策略调优（知道具体是哪条条件导致命中）
  - 线上审计与问题排查

### 4. 注解 + 反射驱动的字段访问层

- 在 `RiskContext` 中使用自定义注解 `@RiskField` 绑定“规则字段路径”和 getter 方法：

  ```
  public class RiskContext {
  
      @RiskField("user.isNew")
      public boolean isNewUser() { ... }
  
      @RiskField("order.amount")
      public double getOrderAmount() { ... }
  
      @RiskField("ip.inBlacklist")
      public boolean isIpInBlacklist() { ... }
  
      // ...
  }
  ```

- `RiskFieldAccessor` 在类加载时通过反射扫描所有带 `@RiskField` 的方法：

  - 构建 `Map<String, Method>` 映射（字段路径 → getter）
  - 执行规则时，根据 JSON 中的 `field` 动态调用相应 getter 取值

- 这样：

  - 新增字段时只需：
    1. 在 `RiskContext` 增加字段 + getter + `@RiskField`
    2. 在 JSON 规则中使用该字段路径
  - 无需在多处写 `switch(field)`，避免硬编码耦合。

### 5. 统一日志体系（SLF4J + Logback + @Slf4j）

- 使用 SLF4J 作为日志门面，Logback 作为日志实现。
- 通过 Lombok `@Slf4j` 简化日志对象注入。
- 配置了 `logback.xml`：
  - 日志输出到控制台
  - 同时输出到 `logs/mini-risk-rule-engine.log`
  - 使用 `TimeBasedRollingPolicy` 按天滚动日志文件，例如：
    - `mini-risk-rule-engine.2025-12-02.log`
    - `mini-risk-rule-engine.2025-12-03.log`

------

## 技术栈

- **语言 & 构建**
  - Java 21
  - Maven
- **JSON 配置解析**
  - Jackson `jackson-databind`
- **日志**
  - SLF4J API
  - Logback Classic
  - Lombok `@Slf4j`
- **核心语言特性**
  - 函数式接口 & Lambda：`Predicate<RiskContext>`
  - 注解（自定义注解 `@RiskField`）
  - 反射（`Method.invoke`）
  - 设计模式：
    - 策略模式（`RuleEngine` + 多个实现）
    - 装饰器模式（`ExplainableRuleEngine` 装饰基础引擎）
    - 简单注册中心（`RuleRegistry` 管理规则集）



## 项目结构

```
src
└─ main
   ├─ java
   │  └─ com/zhangyc/minirisk
   │     ├─ model          # 领域模型：规则、上下文、决策、解释明细等
   │     │  ├─ Rule.java
   │     │  ├─ RuleAction.java
   │     │  ├─ RiskContext.java
   │     │  ├─ DecisionResult.java
   │     │  ├─ ConditionMatch.java
   │     │  └─ RuleMatchDetail.java
   │     │
   │     ├─ engine         # 引擎接口与实现
   │     │  ├─ RuleEngine.java
   │     │  ├─ SimpleRuleEngine.java
   │     │  └─ ExplainableRuleEngine.java
   │     │
   │     ├─ config         # 规则配置模型 + 加载器
   │     │  ├─ ConditionDefinition.java
   │     │  ├─ RuleDefinition.java
   │     │  └─ RuleConfigLoader.java
   │     │
   │     ├─ registry       # 规则注册中心（按场景取规则）
   │     │  └─ RuleRegistry.java
   │     │
   │     ├─ support        # 辅助工具：注解与字段访问器
   │     │  ├─ RiskField.java
   │     │  └─ RiskFieldAccessor.java
   │     │
   │     └─ demo           # 示例入口：构造上下文 & 调用引擎
   │        └─ DemoApplication.java
   │
   └─ resources
      ├─ rules-demo.json   # 规则配置文件（示例）
      └─ logback.xml       # 日志配置
```

------

## 快速开始

### 1. 构建项目

```
mvn clean package
```

或直接在 IDE 中导入 Maven 项目，让它自动构建。

### 2. 运行 Demo 示例

在 IDE（IntelliJ IDEA 等）中，找到：

```
com.zhangyc.minirisk.demo.DemoApplication4
```

右键 → `Run 'DemoApplication4.main()'`。

Demo 会：

1. 从 `rules-demo.json` 加载规则；
2. 按场景（例：`PAY`）获取规则集；
3. 构造一份样例 `RiskContext`（例如：新用户首单金额 1500，注册 30 分钟）；
4. 调用 `ExplainableRuleEngine` 执行决策；
5. 通过 `log.info(...)` 打印决策结果与解释信息。

### 3. 查看日志

运行后，你可以在项目根目录看到：

```
logs/
  ├─ mini-risk-rule-engine.log
  └─ mini-risk-rule-engine.2025-12-02.log   # 按日期滚动产生
```

日志中包含：

- 场景信息（LOGIN / PAY 等）
- 请求上下文 `RiskContext`
- 最终决策（ALLOW / REJECT / MANUAL_REVIEW）
- 命中规则列表
- 每条规则下每个条件的详细解释（实际值 / 阈值 / 是否匹配）

示例日志片段（格式示意）：

```
[2025-12-02 15:30:01.123] [main] INFO  c.z.minirisk.demo.DemoApplication - Scene: PAY
[2025-12-02 15:30:01.125] [main] INFO  c.z.minirisk.demo.DemoApplication - Final Decision: MANUAL_REVIEW
[2025-12-02 15:30:01.128] [main] INFO  c.z.minirisk.demo.DemoApplication - Matched rules:
[2025-12-02 15:30:01.128] [main] INFO  c.z.minirisk.demo.DemoApplication -   - R_PAY_001 | 新用户首单金额过高，且注册时间过短 | action=MANUAL_REVIEW | priority=90 | scene=PAY

[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication - === Explanation Details ===
[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication - Rule R_PAY_001 matched=true (新用户首单金额过高，且注册时间过短)
[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication -     - user.isNew | actual=true  | op== | expected=true  | matched=true
[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication -     - user.historyOrderCount | actual=0 | op== | expected=0 | matched=true
[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication -     - order.amount | actual=1500.0 | op=> | expected=1000 | matched=true
[2025-12-02 15:30:01.130] [main] INFO  c.z.minirisk.demo.DemoApplication -     - user.registerMinutes | actual=30 
```