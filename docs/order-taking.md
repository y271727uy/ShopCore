# 点单行为与提示展示

本轮将接单时机、计时存储、提示展示拆为可组合积木。测试店铺继续使用 `OverheadConsumerLifecycle` 预设，它现在由下列组件组成：

- `AutomaticOrderTaking(40, new EntityOrderTakingMemory())`：等待两秒后自动接单。
- `OverheadOrderTakingPresentation`：展示等待文本、订单图标和订单内容，以及清理提示。
- `OrderTakingController`：只为处于 `WAITING_FOR_ORDER` 且绑定有效订单的顾客执行点单行为。
- `OrderTakingLifecycle`：将该组合接入已有顾客运行管理器的等待和清理回调。

## 自己组合

```java
var taking = new OrderTakingController<EazyNpcConsumerActor>(
        new AutomaticOrderTaking(40L, new EntityOrderTakingMemory()),
        new OverheadOrderTakingPresentation<>(Component.literal("等待中...")));
var lifecycle = new OrderTakingLifecycle<>(taking, this::shopcore$orderBook, this::clearShopHud);
// 将 lifecycle 传给 ShopConsumerRuntime。
```

改为人工接单时，使用 `new ManualOrderTaking()`。由玩法的交互入口调用 `taking.request(actor, memory, orderBook)`，或调用 `lifecycle.request(actor, memory)`。只有顾客正在等待点单且订单有效时才会接受；成功后再次请求会返回 false。测试店铺仍保持自动模式，没有新增或改变其按键操作。

替换展示时实现 `OrderTakingPresentation<A>` 的 `waiting`、`accepted`、`clear`。展示组件不得修改顾客阶段。HUD 展示还需要由具体适配器确定接收玩家，本轮未新增 HUD 接收玩家策略。交付后的提示使用已有 `CustomerOrderPresentation`；自定义店铺应为点单与交付选择一致的展示方式。

直接调用行为的 tick/request 前，调用者必须保证顾客和订单满足接单条件；通常应通过控制器调用，以避免重复接单。

## 兼容规则

- 使用原实体 NBT 键 `ShopCoreOverheadOrderRevealGameTime`，保留已有倒计时。
- 第一次发现等待顾客时写入截止时间并显示等待，即便配置等待为零，也在后续 tick 才接单。
- 截止时间之前持续显示等待；到期先显示订单，再清除计时，最后进入 `WAITING_FOR_DELIVERY`。
- 显示订单抛异常时保持计时和原阶段，沿用原处理顺序。
- 离场/关店清理通过生命周期组件同时清理计时与显示。
- 原 `OverheadOrderPromptController` 作为兼容门面保留，其方法委托给新点单组件。
- 原人工入口 `OrderInteractionService.recordNextWaitingOrder` 保留；新人工模式提供按指定顾客接单的组合能力，未修改旧入口的队列选择规则。

底层头顶显示实体、图标、位置、文本和插值参数没有修改；顾客移动、交付、结算也沿用现有实现。

## 验证

```powershell
$env:JAVA_HOME = 'D:\java\jdk17'
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
.\gradlew.bat --no-daemon -I gradle/consumer-regression.init.gradle verifyConsumerRuntime verifyDeliveryInteraction
```

`OrderTakingRegression` 由顾客回归入口执行，验证 0/1/40 tick 等待、保存的截止时间、清理、展示失败时的阶段保护、人工接单以及展示先于状态推进。测试使用模拟计时存储及回调；未进行真实游戏内提示实体和存档视觉验收。
