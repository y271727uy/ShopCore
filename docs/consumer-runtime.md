# 顾客运行积木

公开接入方式见 [店铺 API](shop-api.md)。测试店铺现在通过公开预制入口使用这些组件；下文说明内部组件的职责及行为约定。

本轮以测试店铺当前行为为兼容基准，把顾客名单和运行协调迁入 `core/consumer/common`。订单生成、交付和结算仍由原来的店铺/订单系统负责。

## 组合关系

- `ShopConsumerRuntime<A>`：每家店一个实例，管理顾客 UUID、生成、订单绑定、订单结束后的离场、关店/重开/拆店处理、读档重置和 NBT。
- `ConsumerOrder`：传给管理器的活动订单摘要，仅含 ID 和创建时间；管理器不依赖菜品、价格或结算内容。现有订单本可用 `ConsumerOrder.activeOrders(book)` 转换。
- `ConsumerPopulation<A>`：访问当前世界中的顾客。`EntityConsumerPopulation` 提供实体实现；Easy NPC 可以直接用 `EazyNpcHumanoidConsumers.population(level, pos, memory)`。
- `ConsumerMovement`：决定出生位置、入口是否空闲，以及进店/排队/离店的行为。它不负责实体创建和订单结算。
- `ConsumerNavigator`：执行移动、停止、转向和移除。手动移动与原版寻路是不同的执行方式，选择移动预设不会自动替换导航器。
- `ConsumerLifecycle<A>`：到店、离场、提示清理和等待交互的回调。`OverheadConsumerLifecycle` 组合现有“两秒后自动显示订单”的控制器，也可以使用自定义实现。

`ConsumerMovements.horizontalQueue(pos, facing)` 是测试店铺原来的单服务位横向队列预设。另一种现有行为通过 `counterQueue(layout, spawnPosition, entrancePredicate)` 接入。自定义行为直接实现 `ConsumerMovement`；不需要为每种行为再写顾客名单、生成和清理代码。这里的“脚本”指 Java 行为实现，没有新增脚本语言或 KubeJS 依赖。

## 接入方式

测试店铺是完整示例：`src/main/java/com/y271727uy/shopcore/text/TestShopBlockEntity.java`。

```java
// 每家店长期持有自己的 memory、navigator 和 runtime。
private final EazyNpcConsumerMemory memory = new EazyNpcConsumerMemory();
private final EazyNpcConsumerNavigator navigator = new EazyNpcConsumerNavigator();
private final ShopConsumerRuntime<EazyNpcConsumerActor> consumers =
        new ShopConsumerRuntime<>(memory, navigator,
                new OverheadConsumerLifecycle<>(40L, Component.literal("等待中..."),
                        this::shopcore$orderBook, this::clearShopHud),
                this::setChanged);

// 服务端先更新店铺和订单，再更新顾客。
consumers.tick(
        EazyNpcHumanoidConsumers.population(level, worldPosition, memory),
        ConsumerOrder.activeOrders(shopcore$orderBook()),
        shopcore$shopInstance().isOpen(),
        ConsumerMovements.horizontalQueue(worldPosition, facing),
        level.getGameTime(),
        this::setChangedAndSyncShopRuntime);
```

`clearShopHud` 可以调用 `OverheadConsumerLifecycle.clearNearbyPlayerHud(serverLevel, worldPosition)`。进出场粒子留在玩法的 `arrived` / `departing` 回调中；不需要把粒子规则写进顾客管理器。

生命周期接入：

| 宿主事件 | 调用与顺序 |
| --- | --- |
| 请求关店 | 先取消活动订单，再 `consumers.close(population)`；后续 tick 传入关闭状态，由行为预设安排离场顺序 |
| 重新营业 | `consumers.reopen(population)` 清理旧顾客，下次 tick 按活动订单生成 |
| 店铺被拆除 | 先取消活动订单，再 `consumers.detach(population)`；继续使用现有孤立顾客离场事件 |
| 读取存档 | `consumers.load(tag, 原有顾客列表键名)`；首次服务端 tick 执行旧顾客清理，再尝试生成 |
| 保存存档 | `consumers.save(tag, 原有顾客列表键名)` |

`population` 只在调用时创建，持有当前 level 与店铺位置；不要把旧世界的 population 缓存到静态字段。runtime 和有状态自定义行为应由各店铺独立持有。通过构造器第四个参数传入宿主的 `setChanged` 回调；顾客名单新增、确认移除、重开清空和拆店交接时由管理器通知保存。纯移动和暂时卸载不改变名单，不触发这个通知；不需要为顾客名单变动发送整份店铺同步包。旧的三个参数构造器仍可使用，但不会提供自动保存通知。

## 行为兼容边界

测试店铺保留以下顺序与参数：

1. 读档清理 → 解析顾客 → 按活动订单补生成 → 再解析顾客 → 标记失效订单离场。
2. 移动/队列 tick → 两秒等待和头顶提示 → 离场效果/提示清理/实体移除 → 名单清理。
3. 每 tick 最多成功生成一位顾客；一次生成失败仍可尝试后面的订单。
4. 横向队列本地出生点 `(8, 0, 0.5)`；入口存在本地 `x >= 7` 的顾客时暂停生成，包括离场中的顾客。
5. 横向队列服务起点、间距、离场目标、到位阈值、关店逐个离场规则均沿用 `HorizontalQueue`。
6. `ManualMobConsumerNavigator` 与 `QueueConsumerBehavior` 未改变。Easy NPC 仍使用手动移动；没有切换原版寻路。
7. 原有 `TestConsumers` UUID 列表继续保留，旁边新增可选的 `TestConsumersOrderBindings` 保存顾客与订单关系；`TestDepartureParticlesShown` 和实体顾客记忆键保持原样，点单等待和粒子参数保持原样。

## 卸载、名单与持久化

`ConsumerPopulation.presence(id)` 区分 `LOADED`、`UNLOADED` 和 `REMOVED`。UUID 暂时无法解析时默认视为不可用，不当作确认删除；实体适配器不会为查询强制加载区块。名单中的顾客—订单绑定继续占位，因此一个活动订单不会因为顾客暂时卸载而生成第二位顾客。顾客重新加载后继续使用原有行为和记忆。

`ConsumerRemovalEvents` 监听服务端实体移除：死亡/删除和离开当前维度会通知已加载的所属店铺释放名单；区块卸载、世界保存卸载和单纯停止追踪不释放。宿主实现 `ConsumerOwnershipTarget.consumerRemoved(id)` 并委托 `consumers.consumerRemoved(id)`。管理器也会释放适配器已确认死亡的实体，以及正常走到出口的顾客；重复通知是幂等的。

卸载顾客的订单已不在活动列表时，管理器释放其归属，后续加载由善后流程独立离场，不让失效顾客永久占据名单。关店不生成新顾客，已加载顾客的离场顺序仍由原移动脚本处理。`size()` 表示总名单大小，包括活动订单对应的暂时卸载顾客。

存档保留原 UUID 列表，同时在 `<列表键名>OrderBindings` 保存可选的订单绑定。读取 NBT 不触发保存回调，旧存档不要求包含新字段。当前读档策略明确沿用 **首次 tick 清理旧顾客并重新生成**；新增的卸载恢复仅处理店铺运行期间的顾客区块卸载，没有切换为读档恢复队列。首次读档重置通知仍通过 tick 的 `onLoadReset` 参数执行，与名单保存回调分工不同。

## 顾客善后

拆店后由 `OrphanConsumerDepartureService` 在服务端世界 tick 接管带离场标记的 Mob，支持 Easy NPC、女仆及村民，不依赖已移除的店铺。事件类仅负责转发。沿用原离场步长 0.1、到达距离 0.12、转向公式和粒子参数；已有目标继续使用，缺少目标时补上原来的五格目标。无效坐标直接结束，避免永久滞留。

接管时清除店铺/订单绑定及所选生命周期的提示、等待状态，释放店铺名单；重复拆店不再次接管。原位置的新店清理会跳过已接管的顾客。离场标记和目标保存在实体 NBT 中，因此已接管顾客卸载再加载后仍会继续离场。

重开和首次读档清理通过 `ConsumerPopulation.discardForShop(ids, beforeDiscard)` 调用所配置的清理逻辑；实体实现也会对附近扫描找到的漏记顾客执行回调。正常离场同样先清提示，再移除实体。自定义 population 如果额外扫描顾客，应覆盖这个重载，将回调应用到扫描结果。

顾客重新加载后的恢复由世界 tick 检查：有店铺绑定、但尚无离场标记的顾客，通过 `ConsumerOwnershipTarget` 核对原位置店铺的当前名单。店铺区块未加载时等待，不主动加载区块；区块加载后，店铺已消失、被替换或名单不再包含此顾客时，清除绑定和默认点单计时/提示，转交独立离场流程。同址重建的新店不会认领旧顾客。测试店铺通过委托 `ShopConsumerRuntime.ownsConsumer` 实现接口；其他使用实体顾客的宿主也应实现此接口。

即时清理扫描仍限于已加载实体（名单 + 店铺周围 32 格），扫描外的残留顾客由上述归属恢复流程处理。自定义提示必须在生命周期的 `clearPrompt` 中释放自身资源；跨卸载恢复的兜底清理针对默认头顶提示与点单计时。

## 验证

按项目指定位置执行编译：

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
```

独立回归入口复用现有构建依赖，不添加测试框架：

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon -I gradle/consumer-regression.init.gradle verifyConsumerRuntime
```

轨迹回归用模拟顾客逐 tick 对比两种预设与原实现，覆盖四个朝向、等待交付、离场及关店。管理回归验证生成/绑定、入口阻塞、失败重试、移除、关闭/重开、拆店目标、原有 NBT 格式及读档重置；另覆盖卸载占位、防止重复生成、加载后继续移动、移除确认、失效订单释放、保存回调和新旧存档兼容。

这些检查不代替游戏内视觉验收：应在原测试场景观察走位、朝向、点单提示、粒子、关店顺序和重进存档后的表现。
