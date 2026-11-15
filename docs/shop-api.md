# 店铺 API：预制店铺与零件

测试店铺是公开 API 的使用示例。它通过 `api` 提供配置、调用运行入口和处理玩法效果；店铺运行、顾客管理及订单交付的接线由内部实现负责。

这条边界也适用于模组自己的玩法代码。缺少能力时补充公开合同，避免在玩法里直接引用 `core`。API 的内部实现仍负责装配和调用核心服务。

## 两类入口

| 入口 | 当前提供的能力 |
| --- | --- |
| `api.preset.ShopPresets` | `horizontalQueue(...)`：配好默认行为的排队店铺 |
| `api.component.menu` | `MenuSource` 菜单来源接口、`MenuListing` 手动定价单品 |
| `api.component.menu.ShopcoreMenus` | 标签菜单注册、候选商品查询与匹配 |
| `api.component.consumer` | `QueueConsumers` 顾客组配置、交互和归属接口 |
| `api.component.order` | 订单周期与有效期、成功交付通知 |
| `api.component.economic` | 价格查询、结账计算、货币操作及其公开数据类型 |
| `api.shop` | 店铺定义、宿主、运行接口、状态快照与结算账户操作 |

预制入口返回普通 `ShopDefinition`，使用的就是同一组公开零件。自行组装也创建这个定义，再交给相同的运行入口。定义描述配置，每个宿主各自持有运行状态；共享定义不会共享订单、营业请求、账户或顾客名单。自定义 `MenuSource` 如果有可变状态，应自行保证它能被多个宿主调用。

当前零件覆盖现有测试店铺的完整配置链路。`QueueConsumers` 明确选择 Easy NPC 人形顾客、横向队列和延迟自动点单；它还不是任意顾客后端或任意移动策略的通用接口。复杂菜单、需求池、自定义订单策略及其他顾客适配器仍需后续开放。当前没有新增 KubeJS 注册事件或脚本绑定。

## 使用预制店铺

现有示例见 `src/main/java/com/y271727uy/shopcore/text/TestShopBlockEntity.java`。核心配置：

```java
ShopDefinition definition = ShopPresets.horizontalQueue(
        ResourceLocation.fromNamespaceAndPath("example", "fruit_shop"),
        MenuSource.fixed(
                new MenuListing("minecraft:apple", 4, 4),
                new MenuListing("minecraft:sweet_berries", 3, 4)),
        "ShopConsumers",
        "example.delivery");
```

菜单项参数依次为物品或已配置菜单的引用、手动单价、每单最大数量。解析沿用原菜单候选解析器。该首批接口使用普通商品需求分类和单品订单。

最后两个参数是顾客名单的存档键名，以及交付反馈翻译键的前缀。后者需要宿主提供与现有 `test.shopcore.delivery.*` 对应的翻译条目。定义 ID 标识配置/开店规则，不是实体店铺的唯一 ID；每家实体店仍创建自己的 UUID。

方块实体继承公开的 `api.shop.ShopBlockEntity`，构造时传入定义。服务端 ticker 调用 `shopRuntime().tick(serverLevel)`；拆除时调用 `shopRuntime().removed(serverLevel)`。朝向通过覆盖 `shopFacing()` 提供。宿主基类负责存档和客户端状态同步，顾客交互、删除通知及孤立顾客恢复通过公开宿主合同接入现有事件。

`ShopcoreShops.createRuntime(host, definition, facing, feedback)` 也允许现有方块实体使用组合方式。此时宿主实现 `ShopRuntimeHost`，自行转发 tick、拆除、存档与同步；不要遗漏这些生命周期入口。运行实例与宿主一一对应，不在静态字段中共享。

## 自行组合零件

```java
ShopDefinition definition = new ShopDefinition(
        ResourceLocation.fromNamespaceAndPath("example", "custom_shop"),
        MenuSource.fixed(new MenuListing("minecraft:apple", 8, 2)),
        new QueueConsumers(60L, Component.literal("正在点单..."), "ShopConsumers"),
        new OrderSettings(40L, 2400L),
        100L, // 店铺运行检查间隔
        1,    // 开店所需的最少有效商品栏位
        "example.delivery");
```

这里直接使用零件设置点单等待、订单生成周期和有效期，不需要引用 `core`。预制店铺使用同一个构造过程。

`MenuSource.listings()` 在菜单没有有效条目时提供默认商品，保留测试店铺原来的空菜单回填行为。已经保存的有效菜单优先；修改定义不直接覆盖存档菜单。暂不提供运行中编辑复杂菜单的公开接口。

## 查询、操作与玩法回调

- `ShopcoreShops.snapshot(blockEntity)` 返回只读快照。新宿主尚未初始化时返回空，不因查询创建店铺。
- `shopRuntime().status()` 获取营业请求、实际营业状态、订单/顾客数量及最近一次诊断；需要时会初始化运行数据。
- `shopRuntime().toggleOpenRequested()` 切换营业请求；开店校验仍决定实际营业状态。
- `shopRuntime().deliver(player, consumer, input)` 复用订单校验、消耗输入物品、结算、保存通知及顾客离场流程。
- `ShopcoreShops.bindSettlementAccount(...)` / `unbindSettlementAccount(...)` 操作结算账户，银行卡也使用这一入口。
- `ShopFeedback.arrived` / `departing` / `delivered` 提供玩法反馈；测试店铺在这些回调中处理粒子、日报记录和声音。

`DeliveryReceipt` 仅在成功交付后发出，携带交付前物品的防御性副本和本次消耗数量；不暴露内部订单/结算对象。订单和保存通知已提交，反馈回调不要重复结算或再次消耗物品。

运行修改在所属服务端线程执行；每次传入当前宿主所在的 `ServerLevel`。客户端只应用同步数据和读取快照。新 API 的公开方法、构造器和继承关系使用 API 类型或 Minecraft/Java 类型；内部适配实现位于 `core.shop.runtime.ConfiguredShopRuntime`。

## 行为与迁移约束

测试店铺保留 100 tick 运行检查、每 tick 顾客更新、40 tick 点单等待、20 tick 订单生成调度和 1200 tick 订单有效期。开关店、拆店、订单终止通知及提示清理由原有协调组件执行。

存档沿用 `ShopRuntime`、`TestConsumers`、`TestConsumersOrderBindings` 和原实体记忆字段；加载后的首次 tick 仍按现有策略清理旧顾客再生成。新 API 没有切换移动实现或修改粒子参数。

API 版本为 `3.0.0`。旧经济、旧菜单包及无调用的兼容壳已经删除；这是源码/二进制接口的不兼容更新，现有扩展需按下表迁移并重新编译。存档字段继续沿用原格式。

| 旧接口 | 当前用法 |
| --- | --- |
| `api.economic.*` | `api.component.economic.*` |
| `PriceProvider`、`ShopcorePrices.getPrice()` | `ShopcorePrices.findPrice()` 返回 `Optional<PriceQuote>` |
| `ShopcoreReputation` | 单品声望使用 `PriceQuote.reputationValue()`；结账声望使用 `CheckoutInput.finalReputation()` 或 `CheckoutResult.reputation()`；汇总由调用方遍历明确的物品/报价集合 |
| `checkout(input, null)` | `ShopcoreCheckout.calculate(input)`，只计算面额与声望 |
| `checkout(Price, quantity, multiplier)` | 用公开的 `CheckoutInput` 明确传入金额、声望、数量与系数 |
| `CheckoutResult.currencyReward()` / `reputationReward()` | `currency()` / `reputation()` |
| `ShopcoreCurrency.increase(account, ItemStack)` | `ShopcoreCurrency.settle(account, paymentStack)`；金额增减仍使用 `increase` / `decrease` / `adjust` |
| `api.shop_menu.ShopcoreMenus` | `api.component.menu.ShopcoreMenus` |
| `registerMenu(id, tag)`、`getMenu(id)` | `registerTag(id, tag)`、`findTag(id)`；查询返回标签的 `ResourceLocation`，不再返回内部菜单对象 |
| `bindMenu(Object, id)`、`MenuProvider`、`MenuBinding` | 通过 `ShopDefinition` 的 `MenuSource` 提供商品，每个 `MenuListing.reference` 指向所需菜单；不再维护任意对象绑定表 |
| `core.shop.blockentity.AbstractShopBlockEntity` | 继承公开 `api.shop.ShopBlockEntity`，或实现 `ShopRuntimeHost` 使用组合方式 |
| 解绑时的 `UNINITIALIZED` 结果 | `NO_ACCOUNT_BOUND`，准确表示尚未绑定结算账户 |
| `core` 下的顾客交互/归属兼容接口 | 使用 `api.component.consumer` 中的同名接口 |
| `TestShopCustomerInteraction` 事件转发壳 | 实现公开顾客交互接口，由已有 `ConsumerInteractionEvents` 统一派发 |

`CheckoutInput`、`CheckoutResult`、`CurrencyDenomination`、`CurrencyPayout`、`CurrencyStackFactory` 和 `CurrencyOperationResult` 也已从 `core.economic` 移到 `api.component.economic`。核心结算引擎与 SDM 适配器直接使用这些公开合同，不再额外维护一份内部同名数据类型。

价格 Tooltip、订单价格选择、订单结算和出货箱货币操作已经使用新入口。结账结果的物品堆在写入与读取时复制，面额顺序与付款明细列表不可由外部调用方修改。

## 菜单与经济零件示例

```java
// 在模组菜单初始化之后注册别名，再将别名放入店铺的菜单来源。
ShopcoreMenus.registerTag("example:wood", "#minecraft:logs");
MenuSource supplies = MenuSource.fixed(new MenuListing("example:wood", 8, 4));

// 只计算奖励；不会创建货币物品或向账户入账。
CheckoutResult reward = ShopcoreCheckout.calculate(new CheckoutInput(10, 3, 2, 3, 1.5));
// reward.totalValue() == 59；reward.reputation() == 9。
```

`ShopcoreMenus.getCandidateItems(reference)` 与订单生成共用解析器。解析顺序为 PriceSetting 已配置菜单、注册的标签别名、物品 ID、物品标签；同名 PriceSetting 菜单优先。`canAccept(reference, stack)` 按相同候选集合检查物品及 NBT。标签内容在查询时解析，随游戏标签数据更新。

`ShopcoreCheckout.checkout(input)` 创建默认货币物品；`checkout(input, factory)` 使用调用方提供的 `CurrencyStackFactory`。物品结果和账户入账是不同操作，入账通过 `ShopcoreCurrency` 执行；`settle`/`settleVerified` 读取付款堆的价值，不负责从背包移除它，调用方负责一次性提交物品消耗。

## 验证

编译继续使用现有 D 盘 Gradle：

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
.\gradlew.bat --no-daemon -I gradle/consumer-regression.init.gradle verifyShopApi verifyConsumerRuntime verifyDeliveryInteraction
```

`verifyShopApi` 自动扫描整个 API 包的公开签名，检查旧兼容类型确已删除，以及测试店铺、银行卡、Jade 和价格 Tooltip 是否直接依赖 `core`；同时验证店铺实例隔离、账户绑定、存档/同步往返、旧顾客名单、结算金额守恒、货币工厂和菜单注册。顾客与交付回归继续覆盖原有移动、生命周期、订单和提示行为。这些检查不启动游戏；真实世界中的生成、交付、标签候选解析和视觉表现仍需游戏内验证。

## 结构化订单与交付结果

`ShopRuntime.orders()` 返回订单簿当前保留的所有订单，`activeOrders()` 只返回 PENDING/PARTIAL，`findOrder(UUID)` 按 ID 查询。`OrderSnapshot` 包含店铺与订单 ID、位置、顾客类型、状态、创建/到期游戏刻、品质奖励和商品明细。每行提供商品（包括 NBT）、需求量、已交付量、剩余量和单价。列表不可修改，ItemStack 在构造和读取时复制。

订单簿不是永久历史；已清理订单的查询返回 Optional.empty。订单快照描述履约状态，不包含历史银行交易记录。

```java
var result = runtime.deliver(player, consumer, player.getMainHandItem());
int delivered = result.deliveredCount();
var order = result.order();
var settlement = result.settlement();
boolean paymentFailed = settlement.isPresent() && !settlement.get().settled();
player.displayClientMessage(result.message(), false);
```

`DeliveryResult` 提供 `DeliveryStatus`、本次交付量/价值、操作后的实际手持物品副本、交付后的订单快照，以及可选的 `SettlementResult`。早期拒绝可能没有订单信息；没有结算结果表示未尝试结算。创造模式保留手持物品，交付量仍表示订单实际接收的数量。

`COMPLETED` 只表示订单商品已交齐；结算是否成功必须读取 `settlement().settled()`。结算数据包括方式、税前/税后金额、税额、声望奖励、结账结果及银行操作结果。本次改造只公开现有结果，不新增失败重试。`message()` 保留预制提示以便直接展示，业务逻辑应读取状态和数值。

迁移：删除 `activeOrderDescriptions()`，改用 `activeOrders()` 并由界面格式化；`deliver(...)` 返回类型改为 `DeliveryResult`，纯展示调用改为 `deliver(...).message()`。这属于当前 API 3.0 重构的一部分，调用方需重新编译。新增能力标识 `shop.orders.snapshots` 和 `shop.delivery.results`。

验证：无界面 `verifyShopApi` 检查公开类型边界；`verifyDeliveryInteraction` 检查状态和银行结算结果映射。`src/verification/java/com/y271727uy/shopcore/core/consumer/OrderSnapshotGameChecks.java` 的 `run()` 需要已启动的 Minecraft/Forge 环境，用于物品副本和交付快照检查，未纳入无界面入口，本次尚未在游戏环境执行。
