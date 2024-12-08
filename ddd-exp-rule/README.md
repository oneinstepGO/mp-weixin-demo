# DDD、表达式引擎、规则引擎演示

> DDD、表达式引擎、规则引擎可以很好地结合在一起，实现复杂的业务逻辑。DDD 提供了领域模型的设计方法，表达式引擎提供了表达式的解析和执行功能，规则引擎提供了规则的解析和执行功能。将 DDD、表达式引擎、规则引擎结合在一起，可以实现复杂的业务逻辑，提高软件的灵活性和可维护性。

本项目是一个演示项目，以资金帐户操作为例，演示了如何将 DDD、表达式引擎、规则引擎结合在一起，实现复杂的业务逻辑。

## 1. DDD 领域驱动设计

### 1.1. DDD 的基本概念

什么是 DDD？DDD 是`Domain-Driven Design`的缩写，即领域驱动设计。DDD 是一种软件开发方法，它强调通过与业务专家的合作来开发软件，以便更好地理解业务需求。DDD 的核心思想是将业务需求和软件设计紧密结合，通过领域模型来表达业务需求，从而更好地理解业务需求，更好地设计软件。

### 1.2. DDD 的核心概念

- 领域模型
  - 领域模型是 DDD 的核心，它是对业务需求的抽象和表达。领域模型是通过领域对象、领域服务、领域事件等元素来表达业务需求的。
- 限界上下文
  - 限界上下文是 DDD 的一个重要概念，它是对领域模型的一个划分。限界上下文是一个边界，它定义了**领域模型的范围**，限界上下文内的领域对象、领域服务、领域事件等元素是相互关联的，它们共同表达了业务需求。
- 聚合根
  - 聚合根是领域模型的核心，它是一组相关对象的集合，它负责维护这组对象之间的一致性和完整性。聚合根是领域模型的入口，外部对象只能通过聚合根来访问领域对象。
- 实体
  - 实体是领域模型的基本元素，它是具有**唯一标识**的对象。实体有自己的生命周期，它可以被创建、修改、删除。
- 值对象
  - 值对象是领域模型的另一个基本元素，它是没有唯一标识的对象。值对象是**不可变的**，它的值一旦创建就不会改变。
  - Java 中使用 record 来实现值对象，record 是 Java 16 引入的一种新的数据类型，它是一种**不可变的**、**持久的**、**线程安全的**数据类型。
- 领域服务
  - 领域服务是领域模型的一部分，它是**一组相关的操作的集合**。领域服务是对领域对象的操作的封装，它负责实现**领域对象之间的协作**。
- 领域事件
  - 领域事件是领域模型的一部分，它是**领域对象之间的消息**。领域事件是领域对象之间的通信方式，它用于实现领域对象之间的解耦。

### 1.3 DDD 中的设计模式

- 工厂模式
  - 工厂模式是 DDD 中常用的设计模式，它用于创建领域对象。工厂模式通过工厂类来创建领域对象，从而实现领域对象的创建和管理的解耦。
- 策略模式
  - 策略模式是 DDD 中常用的设计模式，它用于实现不同的算法或行为。策略模式通过定义一组策略接口，然后通过具体的策略实现类来实现不同的算法或行为。
- 模板方法模式
  - 模板方法模式是 DDD 中常用的设计模式，它用于实现算法的骨架，而将具体的实现延迟到子类中。模板方法模式通过定义一个抽象类，然后在抽象类中定义算法的骨架，最后通过具体的子类来实现算法的具体实现。
- 观察者模式
  - 观察者模式是 DDD 中常用的设计模式，它用于实现一对多的依赖关系。观察者模式通过定义一个主题接口，然后通过具体的主题实现类来实现一对多的依赖关系。
- 责任链模式
  - 责任链模式是 DDD 中常用的设计模式，它用于实现请求的处理流程。责任链模式通过定义一个抽象处理类，然后在抽象处理类中定义处理请求的接口，最后通过具体的处理实现类来实现处理请求的具体实现。
- 装饰器模式
  - 装饰器模式是 DDD 中常用的设计模式，它用于实现对象的装饰。装饰器模式通过定义一个装饰器接口，然后通过具体的装饰器实现类来实现装饰器的具体实现。

### 1.4. DDD 的实现方式

步骤：

1. 定义领域模型

- 领域对象的属性、方法、事件等

2. 定义限界上下文
3. 定义聚合根
4. 定义实体
5. 定义值对象
6. 定义领域服务
7. 发布领域事件

### 1.5. DDD 的设计实践

见 4.1 领域驱动设计

## 2. 表达式引擎

### 2.1. 表达式引擎的基本概念

表达式引擎是一种用于解析和执行表达式的工具，它可以将表达式解析为一个抽象语法树，并执行这个抽象语法树，从而得到表达式的结果。表达式引擎通常用于实现规则引擎、计算引擎等功能。

### 2.2 Aviator 表达式引擎

Aviator 是一个**高性能**的表达式引擎，它支持常见的表达式操作，如算术运算、逻辑运算、字符串操作等。Aviator 的语法简单、易用，性能高效，适合用于实现规则引擎、计算引擎等功能。
GitHub 地址：https://github.com/killme2008/aviatorscript

### 2.3. Aviator 表达式引擎的基本用法

Aviator 表达式引擎的基本用法如下：

- 创建表达式

```java
    String expression = "1 + 2 + 3";
    Expression exp = AviatorEvaluator.getInstance().compile(expression);
```

- 执行表达式

  ```java
    Map<String, Object> env = new HashMap<>();
    Object result = exp.execute(env);
    System.out.println(result);
  ```

- 获取表达式的结果

```java
    Object result = exp.execute(env);
    System.out.println(result);
```

## 3. 规则引擎

### 3.1. 规则引擎的基本概念

规则引擎是一种用于实现规则的工具，它可以将规则解析为一个抽象语法树，并执行这个抽象语法树，从而得到规则的结果。规则引擎通常用于实现业务规则、决策规则等功能。

### 3.2. easy-rules 规则引擎

easy-rules 是一个**简单易用**的规则引擎，它支持常见的规则操作，如条件判断、动作执行等。easy-rules 的语法简单、易用，适合用于实现业务规则、决策规则等功能。
GitHub 地址： https://github.com/j-easy/easy-rules

### 3.3. easy-rules 规则引擎的基本用法

easy-rules 规则引擎的基本用法如下：

- 定义规则

```java
    @Rule
    public class BuzzRule {

    /**
     * 条件
     */
    @Condition
    public boolean isBuzz(@Fact("number") Integer number) {
        return number % 7 == 0;
    }
    /**
     * 动作
     */
    @Action
    public void printBuzz() {
        System.out.print("buzz");
    }
    /**
     * 优先级
     */
    @Priority
    public int getPriority() {
        return 2;
    }
}

```

- 创建规则引擎

```java
    Rules rules = new Rules();
    // 注册规则
    rules.register(rule);
    // 创建规则引擎
    RulesEngine rulesEngine = new DefaultRulesEngine();
```

- 执行规则

```java
    // 创建事实
    Facts facts = new Facts();
    facts.put("rain", true);
    // 执行规则
    rulesEngine.fire(rules, facts);
```

## 4. DDD、表达式引擎、规则引擎的整合

DDD、表达式引擎、规则引擎可以很好地结合在一起，实现复杂的业务逻辑。DDD 提供了领域模型的设计方法，表达式引擎提供了表达式的解析和执行功能，规则引擎提供了规则的解析和执行功能。将 DDD、表达式引擎、规则引擎结合在一起，可以实现复杂的业务逻辑，提高软件的灵活性和可维护性。

本项目是一个演示项目，以资金帐户操作为例，演示了如何将 DDD、表达式引擎、规则引擎结合在一起，实现复杂的业务逻辑。

### 4.1 领域驱动设计

在股票资金帐户操作中，需要对资金帐户、钱包、钱包变动流水、资金操作记录等进行管理，这些都可以使用 DDD 的方法来设计。通过 DDD 的方法，可以更好地理解业务需求，更好地设计软件，通过不断完善领域模型，可以更好地应对业务变化，使软件更加灵活、更加易于维护。当然这需要不断实践，不断总结，才能做得更好，DDD 设计的优势才能体现出来。

本示例通过股票资金帐户操作演示了 DDD 的设计实践。

- 限界上下文：股票资金帐户管理
- 聚合根：MoneyAccount - 资金帐户
  - 资金帐户 ID：id
  - 资金帐户类型：accountType
  - 多币种钱包列表：moneyBalances
  - 动作：
    - 存入：deposit
      - 修改钱包当前余额
      - 创建钱包变动流水，可能涉及多个钱包
      - 记录资金操作
      - 发布资金变动事件
    - 取出：withdraw
      - 检查资金是否充足，可能需要通过表达式引擎来计算
      - 修改钱包当前余额
      - 创建钱包变动流水，可能涉及多个钱包
      - 记录资金操作
      - 发布资金变动事件
    - 冻结：freeze
    - 解冻：unfreeze
- 领域对象：
  - 钱包（资金帐户+币种唯一）：MoneyBalance - 核心领域对象
    - 钱包 ID：id
    - 资金帐户 ID：moneyAccountId
    - 币种类型：moneyType
    - 当前余额：currentBalance
    - 冻结金额：frozenAmount
  - 钱包变动流水（钱包+业务来源+变动字段+变动金额）：MoneyBalanceLog
    - 钱包变动流水 ID：id
    - 钱包 ID：moneyBalanceId
    - 业务来源：fromSource
    - 变动金额（大于 0 表示增加，小于 0 表示减少）：chgCurrentBalance、chgFrozenAmount
    - 变动前金额：beforeCurrentBalance、beforeFrozenAmount
    - 变动后金额：afterCurrentBalance、afterFrozenAmount
    - 操作时间：operationTime
  - 资金操作记录（资金帐户+业务来源+操作类型+金额）：MoneyBalanceOperation
    - 资金操作记录 ID：id
    - 资金帐户 ID：moneyAccountId
    - 业务来源：fromSource
    - 操作类型：operationType
    - 金额：amount
- 值对象：
  - 金额：MoneyAmount
  - 币种：Currency
  - 支付方式：PaymentMethod
  - 业务来源：FromSource
- 领域服务：MoneyOperationService

  - 存入：deposit
  - 取出：withdraw
  - 冻结：freeze
  - 解冻：unfreeze

- 领域事件：MoneyBalanceChgEvent
  - 事件 ID：id
  - 资金帐户 ID：moneyAccountId
  - 钱包 ID：moneyBalanceId
  - 变动金额：chgCurrentBalance、chgFrozenAmount
  - 业务来源：fromSource
  - 操作时间：operationTime

### 4.2 表达式引擎

在股票资金帐户操作中，有很多复杂的公式计算，例如计算可取金额，这些公式可以用表达式来表示，然后通过表达式引擎来执行。通过表达式引擎，可以灵活地进行公式计算，使业务逻辑更加清晰，代码更加简洁。另外，如果需要修改公式，只需要修改表达式引擎中的表达式，而无需修改代码。

例如：

- 计算可取金额-现金帐户：[AvaWithdrawalCashCalculator/availableWithdrawalCash.av](src/main/java/com/oneinstep/ddd/asset/formula/AvaWithdrawalCashCalculator.java)、[availableWithdrawalCash.av](src/main/resources/formula/availableWithdrawalCash.av)
- 计算可取金额-统一帐户：[AvaWithdrawalUnitedCashCalculator/availableWithdrawalUnitedCash.av](src/main/java/com/oneinstep/ddd/asset/formula/AvaWithdrawalUnitedCashCalculator.java)、[availableWithdrawalUnitedCash.av](src/main/resources/formula/availableWithdrawalUnitedCash.av)

### 4.3 规则引擎

在资金帐户操作中，需要根据资金帐户的类型、业务类型、操作字段等条件，使用不同的计算公式，计算出不同的结果。这时候，可以使用规则引擎，根据条件和公式，执行不同的操作。这样可以让代码更加清晰，更易于维护，少了很多 if-else。

例如：参见 [CalRuleManager](src/main/java/com/oneinstep/ddd/asset/formula/CalRuleManager.java)、[AbsCalculator](src/main/java/com/oneinstep/ddd/asset/formula/AbsCalculator.java)