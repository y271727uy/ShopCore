# ShopCore (Farmer's Shop Core) 项目结构说明书

> 一个 Minecraft 1.20.1 Forge 模组，整合了经济模拟 + 树木种植 + 作物品质三大玩法模块。

---

## 目录

- [顶层源码结构](#顶层源码结构)
- [Java 源码 —— 核心框架层 (src/main/java)](#java-源码--核心框架层-srcmainjava)
  - [ShopcoreMod.java](#shopcoremodjava--模组入口)
  - [all/ —— 注册中心](#all--注册中心)
  - [api/ —— 公开 API 门面](#api--公开-api-门面)
  - [block/ —— 方块](#block--方块)
  - [client/ —— 客户端逻辑](#client--客户端逻辑)
  - [consumer/ —— NPC 集成消费方（空壳）](#consumer--npc-集成消费方空壳)
  - [economic/ —— 经济基础设施层](#economic--经济基础设施层)
  - [economic/micromachinelearning/ —— 机器学习调控](#economicmicromachinelearning--机器学习调控)
  - [economic/shopmenu/ —— 商店菜单框架](#economicshopmenu--商店菜单框架)
  - [event/ —— Forge 事件](#event--forge-事件)
  - [gameplay/ —— 游戏玩法（仅 sellingbin）](#gameplay--游戏玩法仅-srcmainjavacom-y271727uy-shopcore-gameplay)
  - [integration/ —— 外部模组集成](#integration--外部模组集成)
  - [item/ —— 物品](#item--物品)
  - [mixin/ —— 混入注入](#mixin--混入注入)
  - [network/ —— 网络通信](#network--网络通信)
  - [recipe/ —— 配方系统](#recipe--配方系统)
- [Kotlin 源码 —— 玩法层 (src/main/kotlin)](#kotlin-源码--玩法层-srcmainkotlin)
  - [gameplay/tree/ —— 树木种植系统](#gameplaytree--树木种植系统)
  - [gameplay/quality/ —— 作物品质系统](#gameplayquality--作物品质系统)
  - [integration/jade/ —— Jade 集成](#integrationjade--jade-集成)
- [资源文件 (src/main/resources)](#资源文件-srcmainresources)
- [构建文件](#构建文件)
- [各模块关系图](#各模块关系图)

---

## 顶层源码结构

整个模组分两大语言：

```
src/main/java/    → Java：核心框架层（经济、API、注册、集成桥接）
src/main/kotlin/  → Kotlin：玩法层（树木种植、作物品质）
```

Java 和 Kotlin 在编译时合并（build.gradle 配置了 `compileJava.dependsOn compileKotlin`），Kotlin 编译后生成 `.class` 给 Java 调用。

---

## Java 源码 —— 核心框架层 (src/main/java)

### ShopcoreMod.java —— 模组入口

| 路径 | `com.y271727uy.shopcore.ShopcoreMod` |
|------|---------------------------------------|
| 作用 | 模组主类，MODID = `"shopcore"` |
| 初始化流程 | 注册所有 DeferredRegister → `commonSetup` 里启动经济系统 |

关键流程：
```java
ModBlock.BLOCKS.register(modEventBus);
ModBlockEntities.register(modEventBus);
ModItem.register(modEventBus);
ModMenus.register(modEventBus);
ModRecipes.register(modEventBus);
ModMessages.register();

// commonSetup:
ShopcoreEconomicBootstrap.bootstrap();  // 注册价格、tooltip
MenuCreate.clear();
MenuCreate.registerAll();               // 注册商店菜单
```

---

### all/ —— 注册中心

统一管理所有 Forge 注册。

| 文件 | 注册内容 |
|------|----------|
| **ModBlock.java** | 方块: `selling_bin`, `tree_compost`, `tree_stump` |
| **ModItem.java** | 物品: `selling_bin`(BlockItem), `bank_card`, `premium_bank_card`, `tree_compost`, `tree_stump`, `equals`(调试发光物品) |
| **ModBlockEntities.java** | 方块实体: `SellingBinBlockEntity`, `TreeCompostBlockEntity`(类型用 `BlockEntity` 而非具体类型), `TreeStumpBlockEntity`(同上) |
| **ModMenus.java** | GUI 容器: `SellingBinMenu` + 快捷打开 `open()` |
| **ModRecipes.java** | 配方类型: `SellingBinRecipe`, 序列化器 `Serializer` |
| **ModCreativeModeTabContents.java** | 将模组物品放入 `list:list` 创造模式标签页 |

> ⚠️ `ModCreativeModeTabContents` 硬绑定到 List mod 的标签页，没有自己的创造模式标签。

---

### api/ —— 公开 API 门面

其他模组或 KubeJS 通过这层调用经济功能。

| 文件 | 作用 |
|------|------|
| **PriceProvider.java** | 价格查询接口 |
| **ShopcorePrices.java** | 通过 `PriceRegistry` 查询物品价格 |
| **ShopcoreCheckout.java** | 结账入口，使用 `DefaultCurrencyStackFactory.INSTANCE` |
| **ShopcoreCurrency.java** | 货币操作，桥接到 `SdmCurrencyHelperBridge` → SDM Economy |
| **ShopcoreReputation.java** | 声望查询和计算 |

---

### block/ —— 方块

| 文件 | 作用 |
|------|------|
| **SellingBinBlock.java** | 出货箱方块。`BaseEntityBlock`，带朝向属性、盖子动画、30s配方执行周期。只有绑定自己的玩家才能打开。 |
| **entity/SellingBinBlockEntity.java** | 出货箱方块实体。核心方法 `tick()` → `runAllRecipes()` 循环: 匹配配方 → 计算产出 → 绑定则走转账，否则入箱。 |

---

### client/ —— 客户端逻辑

| 文件 | 作用 |
|------|------|
| **PriceTooltipHandler.java** | 物品悬浮提示显示 `基础价格 / 附加价格 / 声望` |
| **TooltipTitleRegistry.java** | 按物品标签显示不同的 tooltip 标题（如木材显示"木材经营售价"） |
| **ShopcoreTooltipEntries.java** | 注册 tooltip 标题映射（当前只有 `#minecraft:logs`） |
| **ShopcoreClientEvents.java** | 注册客户端事件: GUI容器绑定、BlockEntityRenderer、Tooltip组件、价格缓存清理 |
| **sellingbin/SellingBinClientPriceCache.java** | 客户端价格缓存，接收服务端同步的4层价格加成 |
| **sellingbin/SellingBinClientPriceHelper.java** | 客户端出货箱 tooltip 辅助：查找配方、计算显示输出、价格文本 |
| **screen/SellingBinScreen.java** | 出货箱 GUI 屏幕 |
| **menu/SellingBinMenu.java** | 出货箱 GUI 容器逻辑 |
| **render/blockentity/SellingBinBlockEntityRenderer.java** | 出货箱方块实体渲染器（盖子动画） |
| **render/model/SellingBinModel.java** | 出货箱模型定义（Java 模型） |
| **render/sellingbin/** | 出货箱额外渲染相关 |

---

### consumer/ —— NPC 集成消费方（空壳）

| 文件 | 状态 |
|------|------|
| **eazy_npc/EazyNpcDineIn.java** | 空类，未实现。预留给 Easy NPC 做"点餐"场景 |
| **eazy_npc/EazyNpcQueue.java** | 空类，未实现。预留给 Easy NPC 做"排队"场景 |
| **maid/** | 空目录。预留给车万女仆模组 |

---

### economic/ —— 经济基础设施层

这是全模组最核心的模块，不依赖任何业务逻辑，提供纯经济计算能力。

| 文件 | 作用 |
|------|------|
| **Price.java** | 不可变价格数据: `basicPrice`, `addPrice`, `reputation` |
| **PriceDefinition.java** | 价格定义规则: 按物品/标签/自定义匹配器匹配，带优先级排序 |
| **PriceRegistry.java** | 全局价格注册中心，支持 `registerItem` / `registerTag` / `registerCustom` |
| **ShopcorePriceEntries.java** | 默认价格条目（当前 4 条: apple, logs, diamond, gold_ingot） |
| **ShopcoreEconomicBootstrap.java** | 启动引导: 注册 tooltip 标题 + 价格条目 |
| **CheckoutInput.java** | 结账输入: `basicPrice + addPrice`, `quantity`, `multiplier` → 计算终价 |
| **CheckoutResult.java** | 结账输出: 货币堆、面额拆分、总价值、声望 |
| **CustomerCheckout.java** | 纯结算引擎: 计算终价 → 贪心算法拆分为面额 → 转换为物品 |
| **CurrencyDenomination.java** | 货币面额枚举: DOGE(131072) → COPPER(1)，共 9 级 |
| **CurrencyPayout.java** | 单面额支付输出 |
| **CurrencyOperationResult.java** | 货币操作结果: 成功/失败 + 余额回查 |
| **CurrencyStackFactory.java** | 函数式接口: 逻辑面额 → 实际物品堆 |
| **DefaultCurrencyStackFactory.java** | 默认实现: `list:copper_gt_credit` 等物品 |
| **Tax.java** | 累进税率计算: ≤1000 免税, ≤5000 5%, >5000 15% |

> ⚠️ `CustomerCheckout` 目前未被 `depositBoundRevenue()` 调用，后者走的是更直接的方式（识别面额→扣税→SDM充值）。

---

### economic/micromachinelearning/ —— 机器学习调控

独立于业务模块的算法库，目前**未接驳**到任何实际流程。

#### nashequilibrium/ —— 纳什均衡价格调节

| 文件 | 作用 |
|------|------|
| **EquilibriumController.java** | 核心控制器：采集市场信号 → 计算份额偏差 → 带惯性平滑的新调节器 → 均衡评分 |
| **EquilibriumConfig.java** | 可调参数: 敏感度(0.65)、惯性(0.80)、最大步长(0.08)、范围(-0.5~0.5)、非活跃奖励(0.04) |
| **MarketSignal.java** | 市场信号: 策略ID, 成交量, 单位收入, 当前调节器 |
| **PriceAdjustment.java** | 价格调整结果: 目标/实际份额、新旧调节器、乘数、失衡方向 |
| **StrategyKey.java** | 策略标识: `(marketKey, strategyKey)` 如 `("crop", "minecraft:apple")` |
| **EquilibriumScore.java** | 均衡评分: 集中度(60%) + 收入差(30%) + 波动率(10%) |
| **EquilibriumSnapshot.java** | 单次均衡更新的完整快照 |

#### weight/ —— 玩家权重惩罚系统

| 文件 | 作用 |
|------|------|
| **PlayerWeightSpace.java** | 接口: 玩家-物品权重空间 |
| **DefaultPlayerWeightSpace.java** | 内存实现: 权重随时间线性衰减，LRU淘汰溢出条目 |
| **WeightConfig.java** | 权重参数: 衰减速度、最大权重、压力抵抗等 |
| **WeightSnapshot.java** | 权重快照: 当前权重、压力值、惩罚乘数 |
| **SellPriceWeightModifier.java** | 纯函数: 权重快照 → 价格乘数 |
| **ItemTagWeightAmountPolicy.java** | 按标签计算权重增量 |
| **WeightAmountPolicy.java** | 权重增量策略接口 |
| **WeightPenaltyFeatures.java** | 特征向量构造: 用于评分模型学习 |
| **WeightPenaltyScoreModel.java** | 评分驱动模型: 自动选择最优惩罚乘数 |
| **WeightPenaltyFeatures.java** | 特征: 压力%×soldAmount, 惩罚乘数, 权重 etc. |
| **WeightTags.java** | 权重相关的物品标签定义 |

#### model/ —— 在线学习模型

| 文件 | 作用 |
|------|------|
| **OnlineModel.java** | 在线学习接口 |
| **OnlineLinearRegressor.java** | 在线线性回归: 单样本 SGD 训练 |
| **LinearModelConfig.java** | 学习率、L2惩罚、最大权重绝对值 |
| **FeatureVector.java** | 特征向量 |
| **LearningSample.java** | 训练样本: 特征 + 目标值 + 样本权重 |
| **ScoreDrivenSelector.java** | 评分驱动选择器: 预测各候选的分数，选最优 |
| **ScoreCandidate.java** | 候选: 值 + 特征 |
| **ScoreChoice.java** | 选择结果 |

#### helper/ —— 微价格辅助层

| 文件 | 作用 |
|------|------|
| **MicroPriceAdjustmentHelper.java** | 外部模块 Facade: 记录销售 → 查权重 → 选乘数 → 算调整价 |
| **MicroPriceAdjustmentConfig.java** | 配置: 是否启用评分模型 |
| **PriceAdjustmentRequest.java** | 调整请求 |
| **PriceAdjustmentResult.java** | 调整结果 |
| **PriceAdjustmentFeedback.java** | 学习反馈 |

> ⚠️ `MicroPriceAdjustmentHelper` 已完整实现但**没有任何地方实例化或调用**。

---

### economic/shopmenu/ —— 商店菜单框架

| 文件 | 作用 |
|------|------|
| **MenuCreate.java** | 核心: 按标签定义菜单、将目标绑定到菜单ID、检查是否能接收物品 |
| **MenuDefinition.java** | 菜单定义: `(menuId, itemTag)` |
| **MenuBinding.java** | 菜单绑定对象 |
| **MenuBootstrap.java** | 遗留占位(已弃用) |

当前只注册了 `registerMenu("menu_one", "#minecraft:logs")`，没有实体调用过 `bindMenu()`。

---

### event/ —— Forge 事件

| 文件 | 作用 |
|------|------|
| **SellingBinEvents.java** | 每 tick 刷新市场天数 → 价格同步到客户端 / 数据包重载时重置缓存 / 玩家登录时同步 |
| **CommandEvents.java** | 注册 `/shopcore sellingbin sell` 命令(OP 2级) |
| **CommonSellingEvet.java** | 几乎空文件（怀疑拼写错误，应为 Event） |

---

### gameplay/ —— 游戏玩法（仅 `src/main/java/.../gameplay`）

Java 侧只包含 `sellingbin/` 子模块，`tree/` 和 `quality/` 在 Kotlin 侧。

| 文件 | 作用 |
|------|------|
| **sellingbin/SellingBinGroup.java** | 配方分组: 按 `recipe.group` 分组，派发每日浮动 |
| **sellingbin/SellingBinGroupManager.java** | 核心: 每日市场刷新、价格加成计算(浮动+虚拟库存+季节+长期)、销售记录 |
| **sellingbin/SellingBinMarketSavedData.java** | 市场数据持久化: 4维价格 + 虚拟库存 + 携带阶段 + 长期R值 |
| **sellingbin/SellingBinSeasonalPriceRules.java** | 季节价格规则注册: Builder 模式，默认注册 `winterCrop +1` |

---

### integration/ —— 外部模组集成

#### integration/sdm/ —— SDM Shop 集成

| 文件 | 作用 |
|------|------|
| **SdmCurrencyHelperBridge.java** | SDM Economy 桥接: 读/写玩家货币余额 |
| **card/BankCardItem.java** | 银行卡物品: 右键绑定/解绑出货箱、切换交易通知、存取出货箱位置 |
| **card/PremiumBankCardItem.java** | 高级银行卡: 继承自 BankCardItem，`isTaxExempt() = true`(免税) |

#### integration/jei/ —— JEI 集成

| 文件 | 作用 |
|------|------|
| **ShopcoreJeiPlugin.java** | JEI 插件注册 |
| **sdmshop/SdmShopDataBridge.java** | **核心**: 通过反射扫描 SDM Shop 模组的类/字段/方法，提取商店条目数据到 `SdmShopJeiEntry` |
| **sdmshop/SdmShopJeiEntry.java** | JEI 展示条目: `(ItemStack, Price, quantity, shopName, locked)` |
| **sdmshop/SdmShopJeiPlugin.java** | SDM Shop JEI 分类注册 |
| **sdmshop/SdmShopCurrencyItems.java** | 货币物品引用: 铜币 |
| **sdmshop/SdmShopRuntimeBridge.java** | 运行时桥接: 反射创建 SDM ShopEntry 实例 |
| **sdmshop/SdmShopUIUtils.java** | UI 工具 |
| **sdmshop/FtbQuestUtils.java** | FTB Quest 完成状态查询 |
| **sdmshop/category/SdmShopCategory.java** | JEI 分类定义 |
| **sdmshop/event/ShopDataLoadedEvent.java** | 商店数据加载事件 |
| **tooltip/SellingBinTooltipComponent.java** | 出货箱 tooltip 组件(逻辑侧) |
| **tooltip/SellingBinClientTooltipComponent.java** | 出货箱 tooltip 组件(客户端渲染) |

#### integration/sereneseasons/ —— SereneSeasons 集成

| 文件 | 作用 |
|------|------|
| **SereneSeasonsCompat.java** | 获取当前季节ID、检查季节是否允许树木生长 |

---

### item/ —— 物品

| 文件 | 作用 |
|------|------|
| **SellingBinBlockItem.java** | 出货箱 BlockItem（简单的 extends BlockItem） |
| **GlowingItem.java** | `isFoil()` 始终返回 true 的发光物品（调试/装饰用） |

---

### mixin/ —— 混入注入

| 文件 | 注入目标 | 作用 |
|------|----------|------|
| **AbstractShopTabMixin.java** | `net.sixik.sdmshoprework.api.shop.AbstractShopTab.createShopEntry` | SDM商店创建条目时触发 `ShopDataLoadedEvent`，刷新 JEI 数据 |
| **GuiGraphicsQualityOverlayMixin.java** | `GuiGraphics.renderItemDecorations` | 在物品渲染后叠加品质星星图标 |
| **ItemRendererQualityOverlayMixin.java** | `ItemRenderer.render` | 在世界中渲染物品时叠加品质星星 |

---

### network/ —— 网络通信

| 文件 | 作用 |
|------|------|
| **ModMessages.java** | 网络通道注册 |
| **SellingBinPriceSyncS2CPacket.java** | 服务端 → 客户端同步4层价格加成数据 |

---

### recipe/ —— 配方系统

| 文件 | 作用 |
|------|------|
| **SellingBinRecipe.java** | 出货箱配方: Ingredient输入 → ItemStack输出，支持随机范围(base/max)、分组(group)、贸易平衡(trade_balance)、S回归(s-regression)、季节限定(season) |

JSON 示例（`data/shopcore/recipes/selling_bin/apple_to_copper_credit.json`）:
```json
{
  "type": "shopcore:selling_bin",
  "input": { "item": "minecraft:apple", "count": 5 },
  "output": { "item": "list:copper_gt_credit" },
  "base": 1, "max": 3,
  "group": "crop",
  "trade_balance": true,
  "s-regression": false,
  "season": "winter",
  "season_base": 1, "season_max": 2,
  "season_only": false
}
```

---

## Kotlin 源码 —— 玩法层 (src/main/kotlin)

### gameplay/tree/ —— 树木种植系统

完整的自定义树木种植玩法：树坑播种 → 树木生长 → 开花结果。

#### 核心数据

| 文件 | 作用 |
|------|------|
| **TreeDefinitions.kt** | 树木定义中心: 3种树形(Shape)、肥料等级、树种映射表(支持 Manors Bounty 13种 + Fruits Delight 3种)、树木创建/清除/叶子状态推进 |
| **FruitsDelightTreeManager.kt** | Fruits Delight 专用管理器: 精确叶子状态(LEAVES→FLOWERS→FRUITS)、开花/结果 tick 逻辑 |
| **TreeTickConstants.kt** | `TREE_TICK_INTERVAL_TICKS = 400` (20秒) |

#### 方块

| 文件 | 作用 |
|------|------|
| **block/TreeCompostBlock.kt** | 树坑方块: BaseEntityBlock，接受种子播种 |
| **block/TreeStumpBlock.kt** | 树桩方块: BaseEntityBlock，树木生长后生成，破坏时清除整棵树 |

#### 方块实体

| 文件 | 作用 |
|------|------|
| **block/entity/TreeCompostBlockEntity.kt** | 树坑BE: 存储树种/形状/dy/生长倒计时，serverTick 推进生长 |
| **block/entity/TreeStumpBlockEntity.kt** | 树桩BE: 管理肥力(0-25)/含水量(0-50)/杂枝(0-25)，维护评分(0-100)，开花结果推进 |

#### 事件

| 文件 | 作用 |
|------|------|
| **event/TreeCompostEvents.kt** | 破坏树坑时同时破坏上方的树苗 |
| **event/TreeGrowthEvents.kt** | 骨粉催熟: 拦截对树坑上方树苗的骨粉使用，改为快速成熟 |
| **event/TreeInteractionEvents.kt** | 右键交互: 骨粉、水瓶、斧头(修剪杂枝) |
| **event/TreePlacementEvents.kt** | 在树坑上方放置种子: 消耗种子、设置生长计时器 |
| **event/TreeVanillaGrowthEvents.kt** | 拦截原版树苗生长(防止被管理的树苗自然生长) |
| **event/EnvironmentVariables/SeasonVariables.kt** | 季节环境变量: 判断树木生长是否允许(春夏可生长) |
| **event/EnvironmentVariables/GrassVariables.kt** | 草地变量(空壳，仅声明) |
| **event/EnvironmentVariables/WeaterVariables.kt** | 天气变量(空壳，仅声明) |

---

### gameplay/quality/ —— 作物品质系统

独立于 Quality Food/Crops 模组的品质系统，支持 3 档品质（Iron/Gold/Diamond），通过 NBT 标记。

| 文件 | 作用 |
|------|------|
| **Quality.kt** | 品质枚举: IRON(1)/GOLD(2)/DIAMOND(3)，每档有价格加成范围 |
| **QualityNbt.kt** | NBT 读写: 支持 `quality1`/`quality2`/`quality3` 布尔标记 + 旧版 `quality` 整数字段兼容 |
| **client/QualityOverlayRenderer.kt** | 品质星星渲染: GUI 叠加 + 世界渲染 |
| **event/harvest/StandardCrop.kt** | 标准作物 (CropBlock): 收获时概率赋予品质 |
| **event/harvest/BerryHarvestEvents.kt** | 浆果类: 支持 `age`/`berries`/`type=FRUITS` 多种状态的收获检测 |
| **event/harvest/MDHarvestEvents.kt** | Manors Bounty 作物: 检测 `can_fruit` + `age` 双条件 |
| **event/harvest/NonStandardCropEvents.kt** | 非标准作物: BreakEvent 时赋予品质 |
| **util/WhiteList.kt** | 白名单接口 |
| **util/StandardCropWhitelist.kt** | 标准作物白名单 |
| **util/BerryFruitHarvestWhitelist.kt** | 浆果类白名单 |
| **util/NonStandardCropWhitelist.kt** | 非标准作物白名单 |
| **util/MDWhiteList.kt** | Manors Bounty 白名单 |

品质赋予概率:
| 品质 | 标准/MD/NonStd | 浆果 |
|------|----------------|------|
| IRON | 35% | 25% |
| GOLD | 10% | 15% |
| DIAMOND | 5% | 5% |

---

### integration/jade/ —— Jade 集成

| 文件 | 作用 |
|------|------|
| **provider/TreeStumpTooltipProvider.kt** | 树桩 Jade 信息悬浮窗: 显示肥力/含水/杂枝/维护评分 |

---

## 资源文件 (src/main/resources)

### 语言文件

| 文件 | 语言 |
|------|------|
| `assets/shopcore/lang/zh_cn.json` | 简体中文(71条) |
| `assets/shopcore/lang/en_us.json` | 英文(70条) |

涵盖: 价格 tooltip、季节加成、银行卡、出货箱绑定消息、Jade 信息、树桩信息。

### 方块状态 / 模型

| 文件 | 内容 |
|------|------|
| `blockstates/selling_bin.json` | 出货箱朝向变体(4方向) |
| `blockstates/tree_compost.json` | 树坑 |
| `blockstates/tree_stump.json` | 树桩 |
| `models/block/selling_bin.json` | 出货箱方块模型 |
| `models/item/*.json` | 各物品模型: bank_card, premium_bank_card, selling_bin, tree_compost, tree_stump, equals |

### 战利品表

| 文件 | 方块 |
|------|------|
| `loot_tables/blocks/selling_bin.json` | 出货箱 |
| `loot_tables/blocks/tree_compost.json` | 树坑 |
| `loot_tables/blocks/tree_stump.json` | 树桩 |

### 配方

| 文件 | 内容 |
|------|------|
| `data/shopcore/recipes/selling_bin/apple_to_copper_credit.json` | 出货箱配方示例: 5苹果 → 1-3铜币 |

### 物品标签

| 文件 | 标签 |
|------|------|
| `data/shopcore/tag/items/seven_to_eleven.json` | 权重相关标签 |
| `data/shopcore/tags/items/weight_*.json` | 权重标签(5个文件: weight_four~weight_nine) |

### 混合配置

| 文件 | 内容 |
|------|------|
| `shopcore.mixins.json` | 3个mixins: 1 universal + 2 client |

### 模组元数据

| 文件 | 内容 |
|------|------|
| `templates/META-INF/mods.toml` | mod 描述文件(模板，编译时替换版本变量) |

### 其他模组的语言文件

| 文件 | 内容 |
|------|------|
| `assets/manors_bounty/lang/en_us.json` | Manors Bounty 中文化 |
| `assets/manors_bounty/lang/zh_cn.json` | Manors Bounty 中文化 |

### META-INF

| 文件 | 内容 |
|------|------|
| `META-INF/services/mezz.jei.api.IModPlugin` | JEI 插件服务注册 |

---

## 构建文件

### 顶层

| 文件 | 作用 |
|------|------|
| `build.gradle` | 构建脚本: NeoForged Legacy 2.0.91 + Kotlin 2.2.0 + Lombok |
| `settings.gradle` | 项目设置 |
| `gradle.properties` | 版本号、模组元数据、Gradle JVM参数 |
| `gradle/libs.versions.toml` | 版本目录(Registrate + JEI) |

### 依赖清单 (build.gradle)

| 依赖类型 | 模组 |
|----------|------|
| 核心 | JEI, Farmers Delight, KubeJS, Architectury, Jade |
| 商店/经济 | SDM Shop, SDM UI Lib, FTB Library, SDM Economy |
| 季节 | Serene Seasons (+Fix), SeasonHUD, GlitchCore |
| 拓展 | Some Assembly Required, Avaritia Neo, List Core, Touhou Little Maid, Fruits Delight |
| 开发 | ProbeJS, Kotlin for Forge, Geckolib, Manors Bounty |
| NPC | Easy NPC |

---

## 各模块关系图

```
                    ┌──────────────────────────────────────┐
                    │          economic/ (基础设施)          │
                    │  Price / Checkout / Tax / Currency    │
                    └──────────┬───────────────────────────┘
                               │ 依赖
                ┌──────────────┼──────────────────┐
                │              │                   │
         ┌──────▼──────┐ ┌────▼────┐       ┌──────▼──────┐
         │  api/       │ │network/ │       │ event/      │
         │  对外门面    │ │ 价格同步 │       │ 市场刷新/命令│
         └──────┬──────┘ └─────────┘       └──────┬──────┘
                │                                 │
         ┌──────▼──────────────────────────────────▼──────┐
         │              gameplay/sellingbin/               │
         │  GroupManager / MarketSavedData / SeasonalRules │
         └──────────┬─────────────────────────────────────┘
                    │ 调用
         ┌──────────▼──────────┐     ┌──────────────────────┐
         │ block/SellingBin    │     │   recipe/            │
         │  + BlockEntity      │     │   SellingBinRecipe   │
         └──────────┬──────────┘     └──────────────────────┘
                    │
         ┌──────────▼──────────────────────────────────────┐
         │        Kotlin 玩法层                              │
         │  ┌─────────────────┐  ┌──────────────────────┐  │
         │  │ gameplay/tree/  │  │ gameplay/quality/    │  │
         │  │ 树木种植系统     │  │ 作物品质系统          │  │
         │  └────────┬────────┘  └──────────┬───────────┘  │
         │           │                      │              │
         │  ┌────────▼──────────────────────▼───────────┐  │
         │  │        integration/jade/                  │  │
         │  │        树桩 Jade 悬浮窗                    │  │
         │  └───────────────────────────────────────────┘  │
         └─────────────────────────────────────────────────┘

          ┌─────────────────────────────────────────────┐
          │  integration/ (外部模组桥接)                  │
          │  sdm/ → SDM Economy 货币                     │
          │  jei/ → JEI 配方展示 + SDM Shop 数据反射      │
          │  sereneseasons/ → 季节系统兼容                │
          │  sdm/card/ → 银行卡物品                       │
          └─────────────────────────────────────────────┘

          ┌─────────────────────────────────────────────┐
          │  micromachinelearning/ (智能调控,未接驳)     │
          │  nashequilibrium/ → 纳什均衡价格调节          │
          │  weight/ + model/ → 玩家权重惩罚 + 在线学习   │
          │  helper/ → MicroPriceAdjustmentHelper Facade │
          └─────────────────────────────────────────────┘

          ┌─────────────────────────────────────────────┐
          │  consumer/ (空壳,未实现)                     │
          │  eazy_npc/EazyNpcDineIn                     │
          │  eazy_npc/EazyNpcQueue                      │
          │  maid/ (空目录)                              │
          └─────────────────────────────────────────────┘
```
