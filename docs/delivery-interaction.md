# 顾客交互与手持交付积木

## 接入点

店铺按需实现 `ConsumerInteractionTarget`。`ConsumerInteractionEvents` 统一处理服务端、主手交互，通过顾客记忆中的店铺位置寻找实现该能力的方块实体。无需为每家店另写 Forge 事件。

返回 `InteractionResult.PASS` 时不拦截原实体交互；其他结果会被设置为事件结果并取消后续实体交互。未绑定店铺、店铺不存在或未实现此能力时直接放行。当前入口仍只处理主手，保持测试店铺原来的交互范围。

旧 `TestShopCustomerInteraction` 仅保留不自动注册的兼容转发方法。不要同时手动注册旧入口和新入口，以免重复处理。

## 默认手持交付

每家店创建一个 `HeldItemDeliveryInteraction`，提供：

- 店铺运行状态持有者与顾客记忆。
- 初始化、保存/同步回调。
- `CustomerOrderPresentation`：交付后的订单提示展示/清理；默认 `OVERHEAD` 复用原头顶提示。
- 消息格式器：`DeliveryMessages(prefix)` 使用既有交付消息结构；也可以传入自定义函数。
- 成功交付回调：`BiConsumer<ServerPlayer, DeliveryReceipt>`，用于日报、音效、任务等玩法。

测试店铺的配置示例：

```java
private final HeldItemDeliveryInteraction delivery = new HeldItemDeliveryInteraction(
        this, consumerMemory, this::initializeRuntime, this::setChangedAndSyncShopRuntime,
        CustomerOrderPresentation.OVERHEAD,
        new DeliveryMessages("test.shopcore.delivery"),
        this::onDelivered);

@Override
public InteractionResult interactWithConsumer(ServerPlayer player, Entity customer, InteractionHand hand) {
    player.displayClientMessage(delivery.deliver(player, customer, player.getItemInHand(hand))
            .withStyle(ChatFormatting.AQUA), false);
    return InteractionResult.CONSUME;
}
```

不使用默认手持交付时，可以直接在能力方法里实现自己的交互流程，或者调用已有订单服务。新能力不要求店铺必须继承某个具体测试店铺类。

## 结果回调与快照

`DeliveryReceipt.inputBefore()` 是交付前物品的防御性副本，保留物品类型、数量及 NBT；每次读取也返回副本。`deliveredCount()` 是订单实际接受的数量。创意模式虽然不扣手持物品，交付统计仍使用实际接受数量，沿用原规则。

```java
private void onDelivered(ServerPlayer player, DeliveryReceipt receipt) {
    ItemStack delivered = receipt.inputBefore();
    // 这里读取品质、保鲜期等 NBT；不要再次读取可能已经清空的手持物品。
    recordDelivery(delivered, receipt.deliveredCount());
}
```

回调仅在 `DELIVERED` 或 `COMPLETED` 时触发。订单交付完成与银行成功到账不是同一件事；到账失败仍可通过 `receipt.result().settlementResult()` 识别。此次未修改既有到账失败后的订单结束规则。

回调应只处理自己的玩法反馈，不再次扣除物品或结算。实际交付变更通过 `OrderLifecycleCommit` 统一保存/同步并发送订单状态通知，随后才执行可选玩法回调；因此回调抛异常不会抑制之前的保存通知。异常继续向调用方传播，不会自动重试回调。

日报修复：此前先交付扣除手持堆，再把同一堆传给 `recordDelivery`。整堆交完变为空堆后，日报直接跳过记录。现在日报和腐败检测都使用交付前快照，并使用实际交付数量。

## 兼容范围

默认组件保留原先的前置判断优先级：空手 → 没有绑定订单 → 等待点单 → 调用既有交付服务。`CustomerDeliveryEligibility` 提供无状态、无副作用的前置检查；订单是否有效和物品是否匹配仍由原服务判断。

物品匹配/扣除、创意模式行为、结算、声望和市场记录沿用原规则。`OrderInteractionService` 新增统一提交范围，实际变更才通知保存/同步；无变化的拒绝不再重复发送整份店铺数据。订单结束后的顾客提示善后通过公共状态通知接入，详见 `order-lifecycle.md`。

`DeliveryMessages` 支持更换翻译前缀和订单摘要函数。使用默认格式时需要提供以下后缀的语言条目：`input_empty`、`no_order`、`not_recorded`、`not_active`、`no_match`、`not_allowed`、`unchanged`、`delivered`、`completed`、`completed_account`、`completed_account_failed`。测试店铺沿用已有语言条目，无需新增翻译。

## 验证方式

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
.\gradlew.bat --no-daemon -I gradle/consumer-regression.init.gradle verifyConsumerRuntime verifyDeliveryInteraction
```

无游戏进程回归覆盖原有顾客移动/管理，以及所有队列阶段的交付前置检查、错误消息和现金/银行成功/银行失败的结果消息。它不模拟真实玩家库存或银行服务。

游戏内验收场景：空手、未点单、错误物品、部分交付、整堆交付、创意模式、银行入账失败；特别检查整堆腐败食物交付后，日报仍能记录数量和腐败标记，并确认一次右键仅交付一次。
