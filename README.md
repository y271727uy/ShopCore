
Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
The MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

MDG Legacy:
==========
This template uses [ModDevGradle Legacy](https://github.com/neoforged/ModDevGradle). Documentation can be found [here](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

SDM Economy Currency API
==========
ShopCore now exposes a thin adapter around `net.sixik.sdm_economy.api.CurrencyHelper`.

Use `com.y271727uy.shopcore.api.economic.ShopcoreCurrency` for common actions:

- `ShopcoreCurrency.increase(player, amount)` to add currency
- `ShopcoreCurrency.decrease(player, amount)` to subtract currency
- `ShopcoreCurrency.adjust(player, delta)` for signed changes
- `ShopcoreCurrency.balance(player)` to query the current balance when supported

The adapter resolves the helper methods reflectively so it can tolerate small API differences between SDM Economy releases.

**出货箱数据结构如下:**

>{    
"type": "shopcore:selling_bin",     
"input": {       
"item": "minecraft:apple",     
"count": 5     
},    
"output": {     
"item": "list:copper_gt_credit"     
},   
"base": 1,    
"max": 3,     
"group": "crop",       
"trade_balance": true,       
"s-regression": false,       
"season":"winter",   
"season_base": 1,     
"season_max": 3,   
"season_only": false     
>}

### type
- **类型**：`String`
- **必填**：是
- **约束**：固定值 `shopcore:selling_bin`
- **说明**：数据包类型标识符

### input
- **类型**：`Object`
- **必填**：是
- **子字段**：
    - `item`（`String`，必填）：物品 ID 或 Tag，支持命名空间格式（如 `minecraft:apple`）
    - `count`（`Integer`，必填）：交易数量，必须为正整数
- **说明**：输入物品配置，支持 `item` 字段的同时也支持 `tag` 字段

**Quality 作物兼容**    
由 ShopCore 独立实现的 Quality 作物 (带有品质的作物) 已内置兼容，无需单独配置。  
由于 ShopCore 独立实现了 Quality 作物支持，因此不内置 Quality Food 和 Quality Crops 模组的兼容，也不会主动兼容这两个模组。如需使用这些模组的物品，请在数据包中自行配置。

### output
- **类型**：`Object`
- **必填**：是
- **子字段**：
    - `item`（`String`，必填）：输出物品 ID 或 list 的物品 ID（如 `list:copper_gt_credit`）
- **说明**：输出物品配置

### base
- **类型**：`Number`
- **必填**：是
- **约束**：必须为正数，不得为零或负数
- **说明**：物品的基础售价

### max
- **类型**：`Number`
- **必填**：是
- **约束**：必须为正数，不得为零或负数
- **说明**：物品的最大售价

### group
- **类型**：`String`
- **必填**：否
- **说明**：价格浮动与继承的核心字段。若未填写，则无法进行价格浮动、季节经济与继承

### trade_balance
- **类型**：`Boolean`
- **必填**：否
- **默认值**：`false`
- **说明**：贸易平衡模块开关。通过引入 S-R 回归来实现：短期内出售多则降价，反之则涨价。`true` 表示启用贸易平衡

### s-regression
- **类型**：`Boolean`
- **必填**：否
- **默认值**：`false`
- **说明**：S 回归方向控制。`false` 表示正回归（S 回归速度越来越快）；`true` 表示反回归（S 回归速度越来越慢）

### season
- **类型**：`String`
- **必填**：否
- **说明**：指定该物品会在哪一个季节涨价

**季节机制说明**     
目前仅支持季节涨价机制，暂不会添加非合法季节降价的机制。

### season_only
- **类型**：`Boolean`
- **必填**：否
- **默认值**：`false`
- **说明**：季节独占开关。`true` 表示该物品只在指定季节出售

### season_base
- **类型**：`Number`
- **必填**：否
- **约束**：必须为正数，不得为零或负数
- **说明**：季节物品的基础价格加成

### season_max
- **类型**：`Number`
- **必填**：否
- **约束**：必须为正数，不得为零或负数
- **说明**：季节物品的最大价格加成
