# 订单状态变化与善后

## 职责与入口

`OrderLifecycleService` 继续负责单笔订单的创建检查、刷新、取消规则。`OrderBookLifecycleService` 统一处理整本订单的刷新、创建和批量取消，并通过 `OrderLifecycleAccounting` 维护营业场次统计。`ShopRuntimeTickService` 和 `ShopSessionDriver` 共用这套实现，避免两条入口各自维护一套统计规则。

- 创建：仅成功加入的新订单增加创建次数；已存在的订单 ID 不重复插入或计数。
- 过期：原规则判断状态，再更新订单本与过期次数；再次刷新已过期订单不会重复计数。
- 取消：仅取消 PENDING/PARTIAL，补记取消次数；重复关店/拆店不会重复计数。
- 完成：仍由原交付与结算代码处理，成功结算负责完成次数、销量和收入。轮询到 COMPLETED 状态、监听结束通知都不再次结算或增加完成次数。银行失败时保留原来的结束订单及统计规则。

`ShopSessionDriver` 是纯计算入口；调用方提交结果时可以使用下面的公共提交入口。

## 统一提交和通知

```java
var result = OrderBookLifecycleService.cancelActive(
        holder.shopcore$shopInstance(), holder.shopcore$orderBook());
OrderLifecycleCommit.apply(holder, result.shop(), result.orderBook());
```

`OrderLifecycleCommit` 在状态与统计写入宿主后，比较前后结果。内容无变化时不通知；有变化时先调用 `shopcore$runtimeChanged()` 保存/同步，再发送 `shopcore$ordersChanged(changes)`。默认宿主回调为空，方块店铺基类实现保存/同步；纯数据宿主可以按自身需要接入。

`OrderStateChange` 只携带订单 ID 和前后状态，不依赖玩家、物品或 NPC。相同状态不会重复通知；部分交付仍会因订单内容变化通知保存，但不伪造状态变化；清理已结束订单历史也不会重新发出完成通知。

交付流程使用同一个提交范围包住订单写入、库存扣除和原结算调用，在本次处理离开范围时统一保存/发送通知。兼容原交付构造器传入的保存回调，避免再由交付外壳重复保存。即便本次处理在写入后抛异常，也会尝试保存已经改变的状态。

这个范围不是库存或银行的回滚事务，不提供跨系统重试或崩溃补偿。订单已经完成之后不会因为状态通知而再次发钱。观察回调负责反馈或组件善后，不能再次扣物品或调用结算。

## 顾客与提示

测试店铺把 `shopcore$ordersChanged` 中的有效订单结束通知转交 `ShopConsumerRuntime.ordersEnded(population, orderIds)`。

- 已加载顾客清理生命周期提示/点单计时；排队、关店逐个离场和正常移动仍由所选脚本控制。
- 暂时卸载的顾客释放名单和订单占位，返回后由已有归属检查接管离场。
- 已结算顾客即使清除了实体上的订单绑定，管理器也能通过自己的顾客—订单关系完成提示善后。

手动关店与拆店仍先取消活动订单，再走原组件关闭/交接流程。测试菜单、日报内容和粒子音效由玩法负责。

## 验证

`verifyConsumerRuntime` 包含新增 `OrderLifecycleRegression`，验证状态通知去重、统计归属、提交范围的无变化/重复关闭/异常保存/自定义回调、空订单本及错误归属检查。顾客回归补充验证订单结束只清理对应提示、保留移动控制权、卸载顾客的归属释放与保存通知。

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --offline --no-daemon compileJava -I gradle/consumer-regression.init.gradle verifyConsumerRuntime verifyDeliveryInteraction
```

这些检查不启动 Minecraft，不模拟真实库存或银行操作；实际物品交付、银行失败、关店/拆店及重新加载仍需在游戏中验收。
