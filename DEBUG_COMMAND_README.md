# ShopCore 调试命令使用说明

## 新增功能：OP 调试命令

### 命令语法
```
/shopcore sellingbin sell
```

### 功能说明
- **目的**：用于调试售卖箱功能，不需要等待正常的 10 分钟冷却时间
- **效果**：立即让世界上所有的售卖箱完成其配方
- **需求**：OP 权限等级 2 及以上（仅 OP 可以执行）

### 使用场景
1. **快速调试**：在测试售卖箱配方时，无需等待 10 分钟
2. **测试反馈**：快速验证配方是否正确执行
3. **验证库存**：迅速检查物品是否正确售出和获得

### 命令行为
- 执行时遍历所有已加载的区块中的售卖箱
- 找到的每个售卖箱都会立即执行一次配方处理
- 返回成功处理的售卖箱数量
- 如果没有找到售卖箱，会返回相应提示

### 命令反馈示例
- 成功：`§aExecuted recipes on 5 selling bin(s)`
- 未找到：`§cNo selling bins found`

### 技术实现细节
- 新增文件：`CommandEvents.java`
- 修改文件：`SellingBinBlockEntity.java`
- 新增方法：`SellingBinBlockEntity.runAllRecipesBroadcast()`
- 通过 ServerStartingEvent 事件注册命令
- 支持 Brigadier 命令系统

### 注意事项
1. 该命令仅在服务器端执行
2. 只能在服务器启动后使用
3. 需要在游戏内拥有 OP 权限
4. 建议在创意模式或测试世界中使用

