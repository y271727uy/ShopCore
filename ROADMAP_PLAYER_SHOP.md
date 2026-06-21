# 模拟经营店铺开发路线图（技术细化版）

> 目标：让玩家能在 Minecraft 中开店经营，NPC 进店消费，价格动态调整。
> **本文件侧重技术细节**：数据流、事件监听、网络同步、持久化、UI 交互。

---

## 一、店铺菜单系统（Player 设置菜单）

### 现状分析

`TooltipMenuCreate` 目前只做了**按 tag 定义菜单**，且只注册了 `menu_one` → `#minecraft:logs`。

```java
// 当前能力：仅代码注册
TooltipMenuCreate.registerMenu("menu_one", "#minecraft:logs");

// 当前API：只能按 tag 匹配，不支持多个 tag
public record MenuDefinition(String menuId, TagKey<Item> itemTag)
```

而 `MenuBinding.java` 是一个空的 record，没有绑定到任何 BlockEntity 的代码。

### 需要补的内容

#### 1.1 菜单数据结构扩展

当前 `MenuDefinition` 只有一个 tag，但一个店铺需要卖多类商品。

```java
// 需要做到：一个菜单可以包含多个 tag + 特定物品 + 排除规则
public class ShopMenu {
    private final String menuId;           // 菜单ID
    private final Component displayName;   // 显示名称
    private final List<TagKey<Item>> includeTags;  // 包含的标签
    private final List<Item> includeItems;         // 包含的特定物品
    private final List<TagKey<Item>> excludeTags;  // 排除的标签
    private final int maxListings;         // 最大上架数
    private final boolean playerPriced;    // 是否允许玩家自主定价
    
    // 查询物品是否在这个菜单范围内
    public boolean accepts(ItemStack stack) { ... }
    
    // 获取所有符合条件的物品列表
    public List<ItemStack> getCandidateItems() { ... }
}
```

#### 1.2 玩家设置菜单的交互流程

```
玩家右键店铺方块 → 打开店铺管理UI
  └─ 切换到"菜单设置"标签页
       ├─ 选择一个 MenuSlot（货架格子）
       │   ├─ 从背包拖入一个物品 → 上架
       │   ├─ 右键已上架物品 → 定价
       │   │   ├─ 自动定价（使用 PriceRegistry）
       │   │   └─ 手动定价（玩家输入）
       │   └─ 空手Shift右键 → 下架
       └─ 保存到 ShopBlockEntity.menuSlots[]
```

#### 1.3 数据持久化

```java
// ShopBlockEntity 中存储
public class ShopBlockEntity extends BlockEntity {
    // 店铺的"菜单"：n 个货架格子
    private final ShopMenuSlot[] menuSlots = new ShopMenuSlot[MAX_SLOTS];
    // 每个格子：卖什么 + 定价模式 + 当前库存
    public record ShopMenuSlot(
        ItemStack item,        // 卖什么
        PricingMode mode,      // AUTO / MANUAL
        int manualPrice,       // 手动定价（MANUAL模式）
        int stock              // 当前库存量
    ) { }
}
```

存 NBT：
```java
@Override
protected void saveAdditional(CompoundTag tag) {
    // 序列化所有 MenuSlot
    ListTag menuList = new ListTag();
    for (ShopMenuSlot slot : menuSlots) {
        CompoundTag slotTag = new CompoundTag();
        // item, pricingMode, manualPrice, stock
        menuList.add(slotTag);
    }
    tag.put("MenuSlots", menuList);
}
```

#### 1.4 网络同步 — 菜单数据 C2S/S2C

| 包 | 方向 | 内容 | 触发时机 |
|----|------|------|---------|
| **ShopMenuUpdateC2SPacket** | C→S | slotIndex, item, pricingMode, price | 玩家确认上架/改价 |
| **ShopMenuSyncS2CPacket** | S→C | 全部 menuSlots 数组 | 玩家打开UI、数据变更后 |

---

## 二、点菜系统（NPC 顾客下单）

### 核心问题

NPC 进店后，不能凭空消失。必须：
1. NPC 浏览货架 → 选中商品 → 提出需求
2. 这个需求变成一张"订单"（Order）
3. 订单展示给玩家看（UI）
4. 玩家把对应物品放入订单交付区 → 完成订单 → 收款

### 2.1 订单数据结构

```java
public class ShopOrder {
    private final UUID orderId;              // 订单唯一ID
    private final UUID shopOwnerId;          // 店主UUID（收款人）
    private final BlockPos shopPos;          // 店铺位置
    private final ItemStack requestedItem;   // 顾客要什么
    private final int requestedCount;        // 要多少
    private final int deliveredCount;        // 已交付多少
    private final long pricePerUnit;         // 单价（已含所有加成）
    private final long totalValue;           // 总价
    private final long orderTime;            // 下单时刻（game time）
    private final OrderStatus status;        // PENDING / PARTIAL / COMPLETED / EXPIRED
    private final @Nullable UUID customerNpcId; // 顾客 NPC 的 UUID
}
```

### 2.2 NPC 点菜决策流程

```
NPC进店 → 扫描 ShopBlockEntity.menuSlots
  ├─ 排除库存为0的格子
  ├─ 按 NPC 偏好（CustomerProfile）过滤
  │   ├─ 预算上限：只看得起价格范围内的
  │   ├─ 标签偏好：#minecraft:logs 类 NPC 不看食物
  │   └─ 品质偏好：高富 NPC 倾向高价商品
  ├─ 对剩余商品算"购买意愿分"
  │   ├─ 价格因子（越低越想要）
  │   ├─ 稀缺因子（库存越少越想要）
  │   └─ 顾客类型权重
  └─ 选最高分的商品 → 生成 ShopOrder
```

### 2.3 订单生成的事件流

```java
// NPC 决策后生成订单
ShopOrder order = new ShopOrder(
    UUID.randomUUID(),
    shopOwner.getUUID(),
    shopPos,
    requestedItem.copy(),
    quantity,
    finalPrice,
    totalValue,
    level.getGameTime(),
    OrderStatus.PENDING,
    npc.getUUID()
);

// 触发 Forge 事件（方便其他模组/KubeJS监听）
Forge.EVENT_BUS.post(new ShopOrderCreatedEvent(order));

// 订单存入 ShopBlockEntity 的待完成列表
shopBlockEntity.addPendingOrder(order);

// NPC 留下订单后离开
// 玩家下次打开 UI 会看到"你有 N 个待完成订单"
```

### 2.4 多 NPC 排队机制

当前 `EazyNpcQueue.java` 是空壳。

```
NPC进店
  ├─ 店铺有空位（maxCrowded内）→ 进入浏览
  └─ 店铺满了 → 进入队列
       ├─ EazyNpcQueue.enqueue(Npc)
       ├─ 前面顾客离开 → dequeue → 进入浏览
       └─ 等太久 → 满意度下降 → 离开
```

需要实现的：
```java
// ShopBlockEntity 中的排队系统
public class ShopBlockEntity {
    private final Queue<UUID> customerQueue = new LinkedList<>();
    private final Set<UUID> activeCustomers = new HashSet<>();
    private int maxConcurrentCustomers = 3;  // 受店铺等级影响
    
    // NPC尝试进店
    public boolean tryEnter(UUID npcId) {
        if (activeCustomers.size() < maxConcurrentCustomers) {
            activeCustomers.add(npcId);
            return true;
        }
        customerQueue.add(npcId);
        return false;
    }
    
    // NPC离开，叫下一个
    public void customerLeave(UUID npcId) {
        activeCustomers.remove(npcId);
        if (!customerQueue.isEmpty()) {
            UUID next = customerQueue.poll();
            activeCustomers.add(next);
            // 通知下一个NPC进店（通过 EasyNPC 或 event）
        }
    }
}
```

---

## 三、监听玩家交付（Player Delivery）

### 核心问题

订单生成后，**玩家把对应物品放到哪里**才算"交付"？怎么监听？

### 3.1 交付流程设计

```
┌─────────────────────────────────────────────────────────┐
│                    店铺 UI                                │
│                                                          │
│  待完成订单                                              │
│  ┌─────────────────────────────────────────┐             │
│  │ 🧑 顾客：村民 Alice                       │             │
│  │ 📦 需求：苹果 ×32                        │             │
│  │ 💰 金额：384 铜币                        │             │
│  │ ├─ 已交付：12/32   进度：████████░░░░░   │             │
│  │ └─ [放入背包中的苹果] → 点击交付            │             │
│  └─────────────────────────────────────────┘             │
└─────────────────────────────────────────────────────────┘
```

### 3.2 交付有两种模式（都需要实现）

#### 模式A：UI内拖拽交付（推荐）

```
玩家打开 ShopBlockEntity 的 UI
  └─ 左侧：待完成订单列表（只读）
  └─ 右侧：一个"交付槽位"（单人格子）
       └─ 玩家从背包拖入物品到交付槽位
            └─ 系统检测物品类型 + 数量
                 ├─ 匹配订单 → 增加 deliveredCount
                 │   ├─ 满了 → OrderStatus.COMPLETED
                 │   │   └─ 触发结账流程（CustomerCheckout）
                 │   │       └─ 钱直接入玩家SDM账户
                 │   └─ 没满 → OrderStatus.PARTIAL
                 └─ 不匹配 → 退回玩家背包 + 提示"这不是订单所需物品"
```

需要：
- `ShopDeliverySlot` — 一个特殊的 `ItemStackHandler`（1格），监听 `onContentsChanged`
- 放入时校验是否匹配任一 pending order
- 匹配则自动扣减并增加 deliveredCount
- 需要网络包同步：`ShopDeliveryC2SPacket`

```java
// 交付槽位逻辑
public class DeliverySlotHandler extends ItemStackHandler {
    private final ShopBlockEntity shop;
    
    @Override
    protected void onContentsChanged(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack.isEmpty()) return;
        
        // 找匹配的 PENDING 订单
        for (ShopOrder order : shop.getPendingOrders()) {
            if (!order.matches(stack)) continue;
            
            int needed = order.getRemainingCount();
            int available = stack.getCount();
            int toDeliver = Math.min(needed, available);
            
            order.addDelivered(toDeliver);
            stack.shrink(toDeliver);
            
            if (order.isCompleted()) {
                // 触发结账
                processCompletedOrder(order);
            }
            break;
        }
        // 没匹配 → 退回物品
        setStackInSlot(slot, ItemStack.EMPTY);
    }
}
```

#### 模式B：管道输入（自动化用）

通过 `ForgeCapabilities.ITEM_HANDLER` 接受漏斗/管道输入，存入店铺的"待处理交付缓存区"。

```java
// ShopBlockEntity.getCapability()
@Override
public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
        // 暴露一个"输入管道"：从侧面漏斗进来的物品自动匹配订单
        return deliveryInputCap.cast();
    }
    return super.getCapability(cap, side);
}
```

### 3.3 事件监听清单（需要新建的 Forge Event）

| 事件 | 触发时机 | 用途 |
|------|---------|------|
| `ShopOrderCreatedEvent` | NPC 生成订单 | KubeJS 监听、统计、通知玩家 |
| `ShopOrderCompletedEvent` | 玩家交付完成 | 通知玩家收款、刷新 UI |
| `ShopOrderExpiredEvent` | 订单超时（24h未交付） | 自动取消、影响 NPC 满意度 |
| `ShopDeliveryEvent` | 玩家放入交付槽 | KubeJS 修改交付逻辑 |
| `ShopCustomerEnterEvent` | NPC进店 | 排队管理、统计 |
| `ShopCustomerLeaveEvent` | NPC离开 | 排队管理、叫下一个 |

---

## 四、交易流水线（Transaction Pipeline）

### 4.1 完整流程图

```
NPC进店 → 浏览货架 → 点菜 → ShopOrderCreatedEvent
                                          │
                                    玩家看到订单
                                          │
                                    玩家放苹果到交付槽
                                          │
                                     ┌────▼────┐
                                     │ 校验匹配  │
                                     └────┬────┘
                                          │ 匹配
                                     ┌────▼──────────┐
                                     │ 累计 delivered │
                                     │ 满了?          │
                                     └────┬──────────┘
                                          │ 完成
                                     ┌────▼──────────────────┐
                                     │ ShopOrderCompletedEvent │
                                     └────┬──────────────────┘
                                          │
                          ┌───────────────┼───────────────┐
                          │               │               │
                     ┌────▼────┐    ┌────▼────┐    ┌────▼──────────┐
                     │ Tax     │    │ Checkout│    │ Market Record │
                     │ 算税    │    │ 结账     │    │ 记录市场销售   │
                     └────┬────┘    └────┬────┘    └────┬──────────┘
                          │               │               │
                     ┌────▼────────────── ▼ ──────────────▼──────┐
                     │           Assembly Line                    │
                     │                                           │
                     │  ① Tax.calculate(totalValue, taxExempt)   │
                     │  ② CustomerCheckout.checkout(input, fac)  │
                     │  ③ ShopcoreCurrency.increase(owner, net)  │
                     │  ④ SellingBinGroupManager.recordSale(...) │
                     │  ⑤ MicroPriceAdjustmentHelper.record(...) │
                     │  ⑥ EquilibriumController.update(...)      │
                     └───────────────────────────────────────────┘
```

### 4.2 管道编排器：ShopTransactionPipeline

之前所有的概念都散落在各处，需要一个**统一的编排器**：

```java
public class ShopTransactionPipeline {
    
    public static TransactionResult processCompletedOrder(
        ServerLevel level, ShopOrder order, ShopBlockEntity shop
    ) {
        // 1. 算税
        boolean taxExempt = shop.isTaxExempt();
        Tax.TaxResult tax = Tax.calculate(order.getTotalValue(), taxExempt);
        
        // 2. 结账（拆面额）
        CheckoutInput input = new CheckoutInput(
            order.getTotalValue(), 0, 0, 1, 1.0
        );
        CheckoutResult checkout = CustomerCheckout.checkout(input, 
            DefaultCurrencyStackFactory.INSTANCE);
        
        // 3. 打钱
        ShopcoreCurrency.increase(order.getShopOwnerId(), 
            (double) tax.netAmount());
        
        // 4. 市场记录
        boolean marketChanged = SellingBinGroupManager.recordSale(
            level, recipe, order.getRequestedItem(), order.getDeliveredCount());
        
        // 5. 微价格调整（权重 + 纳什均衡）
        MicroPriceAdjustmentHelper helper = /* 从某处获取或创建 */;
        if (helper != null) {
            PriceAdjustmentResult adjusted = helper.recordSale(
                order.getShopOwnerId(), order.getRequestedItem(), 
                order.getDeliveredCount());
        }
        
        // 6. 纳什均衡更新（按策略组）
        MarketSignal signal = new MarketSignal(
            StrategyKey.of(order.getRequestedItem()),
            order.getDeliveredCount(),
            checkout.totalValue(),
            1.0  // 当前调节器
        );
        EquilibriumController.INSTANCE.update(List.of(signal));
        
        // 通知客户端
        notifyPlayer(level, order);
        
        return new TransactionResult(checkout, tax, marketChanged);
    }
}
```

---

## 五、ShopBlock 方块实现细节

### 5.1 ShopBlock

```java
public class ShopBlock extends BaseEntityBlock {
    // 类似 SellingBinBlock，但额外功能：
    // - 右键打开管理UI（店主专属）
    // - 其他玩家右键 → 显示"这是XX的店"（不可操作）
    // - 破坏时发事件，掉所有未完成订单的物品（作为补偿）
    // - 等级属性（通过 BlockState 或 BE 存储）
}
```

### 5.2 ShopBlockEntity 完整数据结构

```java
public class ShopBlockEntity extends BlockEntity implements MenuProvider {
    
    // 1. 店主信息
    private @Nullable UUID ownerUuid;
    private @Nullable String ownerName;
    
    // 2. 店铺等级
    private int shopLevel = 1;
    
    // 3. 菜单（上架商品）— 持久化
    private final ShopMenuSlot[] menuSlots = new ShopMenuSlot[MAX_SLOTS];
    
    // 4. 待完成订单 — 持久化
    private final List<ShopOrder> pendingOrders = new ArrayList<>();
    
    // 5. 已完成订单历史 — 持久化（统计用）
    private final List<CompletedOrderRecord> orderHistory = new ArrayList<>();
    
    // 6. 交付槽 — 1格，不放持久化（或实时持久化）
    private final DeliverySlotHandler deliverySlot = new DeliverySlotHandler(this);
    
    // 7. NPC排队 — 不持久化（NPCLogin时重建？或者持久化队列）
    private final Queue<UUID> customerQueue = new LinkedList<>();
    
    // 8. 财务统计 — 持久化
    private long totalRevenue = 0;
    private long dailyRevenue = 0;  // 每天重置
    private int totalCustomersServed = 0;
    
    // ---- NBT 序列化 ----
    @Override
    protected void saveAdditional(CompoundTag tag) {
        // ownerUuid, ownerName, shopLevel
        // menuSlots[], pendingOrders[]
        // orderHistory[], totalRevenue, totalCustomersServed
    }
    
    // ---- 核心 tick ----
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        
        // 每 tick 检查：
        // 1. 是否该重置日营收（跨天）
        // 2. 检查是否有过期订单（超24h）
        // 3. 如果有自动补货系统（模式B），处理输入缓存
    }
    
    // ---- 权限校验 ----
    public boolean isOwner(Player player) {
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }
}
```

---

## 六、客户端 UI 细节

### 6.1 ShopScreen 布局

```
┌────────────────────────────────────────────────────────┐
│  🏪 [店铺名]           Lv.2       今日营收: $1,248     │
├────────────────────────────────────────────────────────┤
│  ┌──────┬──────┬──────┬──────┬──────┐                 │
│  │ 菜单  │ 订单  │ 财务  │ 员工  │ 升级 │                 │
│  └──────┴──────┴──────┴──────┴──────┘                 │
│                                                        │
│  === 菜单管理标签页示例 ===                            │
│                                                        │
│  格子1                 格子2                 格子3      │
│  ┌────────┐           ┌────────┐           ┌────────┐  │
│  │ 🍎     │           │        │           │ 🥕     │  │
│  │ 苹果   │           │ 空     │           │ 胡萝卜 │  │
│  │ $12/组  │           │        │           │ $8/组  │  │
│  │ 库存:47 │           │ [上架] │           │ 缺货!  │  │
│  └────────┘           └────────┘           └────────┘  │
│  [调整价格]            [上架物品]            [补货]     │
│                                                        │
│  === 订单标签页示例 ===                                │
│                                                        │
│  ┌─────────────────────────────────────────────┐       │
│  │ 📋 待完成订单 (3)                            │       │
│  │                                             │       │
│  │ ① 🧑 村民Alice   🍎苹果×32   进度: 12/32   │       │
│  │    💰 384铜币    ⏰ 剩余22h    [交付]        │       │
│  │                                             │       │
│  │ ② 🧑 村民Bob     🥕胡萝卜×16 进度: 0/16    │       │
│  │    💰 128铜币    ⏰ 剩余23h    [交付]        │       │
│  └─────────────────────────────────────────────┘       │
└────────────────────────────────────────────────────────┘
```

### 6.2 UI 与服务器的网络交互

| 操作 | 网络包 | 方向 | 响应 |
|------|--------|------|------|
| 打开UI | — | C→S | S→C: `ShopDataSyncS2CPacket`（全部店铺数据） |
| 上架物品 | `ShopMenuUpdateC2SPacket` | C→S | S→C: 更新后的 MenuSlot |
| 手动定价 | `ShopMenuPriceC2SPacket` | C→S | S→C: 确认 |
| 放入交付槽 | `ShopDeliveryC2SPacket` | C→S | S→C: 订单状态更新 + 收款通知 |
| 切换标签 | 纯客户端 | — | — |
| 升级店铺 | `ShopUpgradeC2SPacket` | C→S | S→C: 成功/失败 + 新等级 |

### 6.3 需要注册的 MenuType / Screen

```java
// ModMenus.java 新增
public static final DeferredRegister<MenuType<?>> MENUS = ...;
public static final RegistryObject<MenuType<ShopMenu>> SHOP = 
    MENUS.register("shop", () -> new MenuType<>(ShopMenu::new));

// ShopcoreClientEvents 新增
MenuScreens.register(ModMenus.SHOP.get(), ShopScreen::new);
```

---

## 七、网络包清单（新增）

| 包名 | 方向 | 内容 | 类似参考 |
|------|------|------|---------|
| `ShopDataSyncS2CPacket` | S→C | menuSlots[], pendingOrders[], stats | `SellingBinPriceSyncS2CPacket` |
| `ShopMenuUpdateC2SPacket` | C→S | slotIndex, item, pricingMode, price | 新建 |
| `ShopMenuPriceC2SPacket` | C→S | slotIndex, newPrice | 新建 |
| `ShopDeliveryC2SPacket` | C→S | slotIndex | 新建 |
| `ShopOrderUpdateS2CPacket` | S→C | 订单状态变更（完成/过期） | 新建 |
| `ShopUpgradeC2SPacket` | C→S | — | 新建 |
| `ShopNotificationS2CPacket` | S→C | 文字通知（"收到 $384 铜币"） | 参考 `revenue_notice` 消息 |

---

## 八、代码修改清单（按文件）

### 新建文件

| 路径 | 文件 | 说明 |
|------|------|------|
| `block/ShopBlock.java` | 店铺方块 | |
| `block/entity/ShopBlockEntity.java` | 店铺方块实体 | 核心，含菜单/订单/交付槽 |
| `block/entity/ShopMenuSlot.java` | 菜单格子 record | |
| `block/entity/ShopOrder.java` | 订单 record | |
| `block/entity/DeliverySlotHandler.java` | 交付槽 ItemStackHandler | |
| `block/entity/ShopTransactionPipeline.java` | 交易管线编排器 | |
| `event/ShopEvents.java` | 店铺 Forge Events | ShopOrderCreatedEvent 等 |
| `network/Shop*.java` | 网络包 6 个 | 见上一节 |
| `client/menu/ShopMenu.java` | 店铺 GUI 容器 | |
| `client/screen/ShopScreen.java` | 店铺 GUI 屏幕 | |
| `consumer/QueueManager.java` | 排队系统 | 从空壳 EazyNpcQueue 抽出 |
| `gameplay/shop/ShopLevelManager.java` | 店铺等级配置 | |

### 修改文件

| 文件 | 改动 |
|------|------|
| `all/ModBlock.java` | 注册 `shop` 方块 |
| `all/ModBlockEntities.java` | 注册 `ShopBlockEntity` |
| `all/ModItem.java` | 注册 `shop` 的 BlockItem |
| `all/ModMenus.java` | 注册 `ShopMenu` |
| `all/ModCreativeModeTabContents.java` | 加入店铺物品 |
| `client/ShopcoreClientEvents.java` | 注册 ShopScreen |
| `network/ModMessages.java` | 注册 6 个新网络包 |
| `economic/shopmenu/TooltipMenuCreate.java` | 扩展 MenuDefinition，支持多 tag + 排除规则 |
| `ShopcoreEconomicBootstrap.java` | 注册更多默认商品价格 |
| `consumer/eazy_npc/EazyNpcDineIn.java` | (替换) 改为 NPC 顾客进店消费逻辑 |
| `consumer/eazy_npc/EazyNpcQueue.java` | (替换) 引用 QueueManager |
| `resources/assets/shopcore/lang/zh_cn.json` | 新增店铺相关翻译 |
| `resources/assets/shopcore/lang/en_us.json` | 同上 |

---

## 九、技术难点清单

| 难点 | 说明 | 建议方案 |
|------|------|---------|
| **NPC 行为接管** | EasyNPC 的 NPC 如何被"请"进店、浏览、下订单？ | 检查 EasyNPC 是否提供 AI 覆盖接口或事件。如果不提供，可能需要 Mixin 注入 NPC 的 AI 切换逻辑 |
| **交付槽的并发安全** | 玩家多人同时操作同一店铺 | 加锁 sync 或在 BE tick 中批处理 |
| **订单超时判定** | 跨世界重载后，orderTime vs currentGameTime | `SellingBinGroupManager.refreshForElapsedDays()` 的 day 计算方式可复用 |
| **排队系统的跨 tick 持久化** | 服务器重启后 NPC 队列丢失 | 队列不持久化（重启后NPC重新生成），或者持久化队列中的NPC ID |
| **小游戏满意度** | NPC 等待太久会降低满意度，影响店铺声誉 | 每个 NPC 维护一个 `patience` 值，每次 tick 递减 |
