# 内部方块绑定层

实现位于 `com.y271727uy.shopcore.core.capability`。这是内部基础设施，尚未开放为 KubeJS 或稳定公共 API，也不包含商店定义、营业逻辑或界面。

## 分层与访问范围

- `BlockBindingRegistry`：包级私有注册入口，保存方块类型到实体类型、能力工厂的映射。
- `BoundCapabilityProvider`：包级私有；每个 BlockEntity 独立持有一个能力实例和 LazyOptional。
- `BlockBindingHooks`：仅供模组初始化和跨包 Mixin 调用的 public 桥接类，不是公共 API。内部 carrier、创建分派对象和注册记录均为私有嵌套类。
- `BoundBlockFlags`：混入 `Block` 的内部 duck 接口，注册时同步写入"是否有宿主 / 是否有能力绑定"标记。`hasBlockEntity`、`getTicker`、`AttachCapabilitiesEvent` 等对所有方块生效的热路径只读这个字段，不查 Map。
- Mixin：接入原生方块实体声明、创建、合法性检查、ticker、移除及世界生成路径。
- 能力分发：沿用 Forge 标准的 `AttachCapabilitiesEvent`。装了 ModernFix 时由其 `perf.faster_capabilities` 生成分发类，ShopCore 不自行生成字节码；所有绑定共用 `BoundCapabilityProvider` 一个类，避免 ModernFix 按类型组合缓存的生成类数量膨胀。

Java 顶层类不能声明为 `private`，因此内部实现采用包级访问权限。后续商店绑定适配代码放进同一包，由上层传递商店定义，不应把业务规则写入这些 Mixin。

## 注册方式

在同包内部引导代码的 `FMLCommonSetupEvent.enqueueWork` 中注册。客户端和服务端必须安装完全相同的绑定配置。`FMLLoadCompleteEvent` 时冻结注册表，不支持世界运行期间绑定、解绑或热重载。

```java
BlockBindingRegistry.bindBlockEntity(
    myCounterBlock,
    MY_BLOCK_ENTITY_TYPE::get,
    MyBlockEntity::serverTick,
    null // 不需要客户端 ticker
);

BlockBindingRegistry.bindCapability(
    myCounterBlock,
    ResourceLocation.fromNamespaceAndPath("shopcore", "my_component"),
    MY_CAPABILITY,
    owner -> new MyComponent(owner::setChanged),
    MyComponent::save,
    MyComponent::load,
    side -> true // side == null 表示无方向查询
);
```

绑定 BlockEntity 使用已经注册的 `BlockEntityType` 及其创建工厂；不接收共享的实体对象。多个方块类型可以使用同一实体类型，每个位置仍有独立的实例。双方都使用同一个类型工厂，所以放置和 NBT 恢复的构造方式一致。ticker 必须显式提供；实体类型本身没有统一的 tick 方法。

能力类型仍由所属业务通过 Forge 注册。绑定入口只接受 `shopcore` 命名空间的 provider ID；它是私有调用入口，不是向脚本开放任意第三方 Capability 的机制。能力实例在首次查询、保存或加载时创建，避免在 BlockEntity 基类构造过程中访问尚未初始化的子类字段。

## 三种宿主

1. 普通方块绑定指定 BlockEntity：实际加入原生区块的实体表，可通过 `level.getBlockEntity(pos)` 获取。
2. 普通方块仅绑定 Capability：自动使用注册的 `shopcore:capability_carrier` 作为最小宿主。
3. 已有原生 BlockEntity 的方块：保留其实体，使用 Forge `AttachCapabilitiesEvent<BlockEntity>` 附加能力。

为已有原生实体的方块重新指定实体类型会报错，避免破坏其原有交互中的类型转换。能力不能覆盖宿主原来优先返回的同类型能力；这里面向独立的 ShopCore 能力。

仅 capability 绑定与显式实体绑定可以组合；最终每个位置始终只有一个 BlockEntity。空气、重复实体绑定、重复 provider ID、同一方块重复绑定同一种能力以及冻结后的注册都会被拒绝。

## 生命周期与边界

- 保存恢复走原生 BlockEntity NBT 和 ForgeCaps；可变能力必须在变化时调用宿主 `setChanged()`。
- 同一方块类型的状态变化保留实例并更新 state/ticker；替换为其他方块或拆除会移除实例并失效能力句柄。
- 区块卸载失效旧句柄；恢复时从 NBT 创建新实例。不会把卸载自动解释成业务销毁。
- 世界生成的 DUMMY NBT、普通区块的即时创建和 pending NBT 恢复均接入绑定工厂。
- 不扫描既有世界。新增绑定后，旧方块可在原生即时查询时创建实体，但未被访问的旧位置不保证自动开始 tick。需要立即启用既有位置时，应由后续上层迁移工具显式初始化。
- 原版活塞对有 BlockEntity 的方块采用原生限制；不提供跨模组搬运协议。
- 客户端遵循所绑定实体自己的 `getUpdateTag` / `getUpdatePacket`。自定义 Capability 不会自动向客户端广播，尤其不能默认同步全部私有存档数据。
- 方块不会自动获得界面、库存、游戏事件监听器、特殊渲染或商店交互。这些需要上层适配。
- 所绑定实体必须兼容宿主的方块属性和行为。例如会读取 `FACING` 的实体不能直接用于没有该属性的方块；该基础层不能修复实体内部硬编码的方块转换。
- 修改已有世界的实体类型绑定需要显式数据迁移，不应直接换类型后期待旧 NBT 自动兼容。

创建路径仅对存在绑定的方块调用注册类型的工厂，从而避开原版 `EntityBlock` 强制转换。世界生成路径保留写入范围检查、方块变化通知及后处理；不替换方块注册对象、模型或 ID。普通区块放置路径在原版创建分支之前建立绑定实体。

## 验证

正常编译：

```powershell
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon compileJava
```

可选 GameTest：

```powershell
$env:JAVA_HOME = 'D:\java\jdk17'
$env:GRADLE_USER_HOME = 'D:\document\dev\gradle\.gradle'
.\gradlew.bat --no-daemon -I gradle/block-bindings-gametest.init.gradle runGameTestServer
```

测试代码和结构位于 `src/verification/gametest*`，此 init script 使用独立的 `blockBindingGameTest` source set 加载它们，不进入正常产物；正常构建不会绑定金块、钻石块、干草块或箱子。测试世界位于 `build/block-bindings-gametest`。
