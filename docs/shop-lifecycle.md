# 店铺运行与生命周期协调

公开宿主接入方式见 [店铺 API](shop-api.md)。测试店铺通过 API 运行入口使用该协调器，以下说明其内部调用约定。

`ShopLifecycleController<C>` 是每家店独立持有的协调积木。`C` 为调用时传入的运行上下文；控制器不会保存世界或实体引用。无顾客的店铺可以不实现组件回调。

测试店铺以 `ServerLevel` 为上下文，通过 `Hooks` 接入已有订单运行、顾客管理和保存通知。菜单、顾客类型、交付规则仍由原组件负责。本轮只统一协调顺序，不同时重做点单、默认菜单或统计。

## 调用约定

- 服务端 tick 调用 `tick(context, gameTime)`：首次立即更新运行，随后按世界时间整除 `intervalTicks` 的时刻更新；每次都在运行更新之后调用 `tickComponents`。
- `toggleOpenRequested(context)`：初始化、翻转营业请求、要求下次更新；开店调用重开回调，关店先取消订单再调用关闭回调，最后通知保存同步。
- 无服务端上下文时可传 null 切换请求，保持旧实现只更新请求及同步通知的行为。
- 拆店调用 `removed(context)`：先取消订单，再转交组件离场/清理。
- 加载完店铺及顾客 NBT 后调用 `requestRuntimeTick()`；首次服务端 tick 刷新，保留加载得到的营业请求。

`tickRuntime` 回调负责调用已有店铺运行桥；其初始化、缺失数据检查、结果保存逻辑沿用原实现。控制器负责何时调用，避免再由每个店铺维护 `forceRuntimeTick` 和取模判断。

`cancelActiveOrders` 现在委托公共 `OrderBookLifecycleService`，取消订单同时更新取消统计，并通过 `OrderLifecycleCommit` 通知保存与顾客提示清理，详见 `order-lifecycle.md`。

控制器负责玩家显式开关请求的顺序；营业策略导致的自动状态变化仍由原订单/顾客 tick 处理。不要额外重复发送 close/reopen。组件回调也不要重复取消订单或自行递归调用控制器。

测试店铺保留 100 tick 的店铺运行周期、每 tick 顾客更新、相同重开/拆除处理、原 NBT 及其加载顺序。强制刷新不会把周期改为“刷新后再等 100 tick”。

## 验证

使用 D 盘 Gradle 配置运行 `compileJava`；原 `verifyConsumerRuntime` 入口现在也执行 `ShopLifecycleRegression`，覆盖周期、强制刷新、组件调用顺序、开关店、拆除及读档刷新。

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
.\gradlew.bat --no-daemon -I gradle/consumer-regression.init.gradle verifyConsumerRuntime verifyDeliveryInteraction
```

这些是无游戏进程的协调及行为回归，不替代真实存档中的视觉验收。
