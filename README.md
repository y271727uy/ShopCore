我有个鬼点子！！
餐厅可以被所有者设置为团队共有，如此每个成员都可以参与经营！！！
餐厅所有者可以设置属于同一个队伍的玩家到不同的岗位 （队伍通过FTB Team去折腾，并且需要FRMC那边允许一个团队内任务不共享）
餐厅收益可以通过现有的算法进行均衡分配

core/shop/ownership/     ← 所有权积木（新增）
├── OwnershipProvider    ← 接口：谁拥有这个商店？
├── SingleOwnership      ← 实现：单人所有
├── TeamOwnership        ← 实现：团队所有
└── CorporateOwnership   ← 扩展：股份制（未来）

core/shop/revenue/       ← 收益分配积木（新增）
├── RevenueDistributor   ← 接口：如何分配收益？
├── EqualDistributor     ← 实现：平均分配
├── WeightedDistributor  ← 实现：权重分配
├── ShapleyDistributor   ← 实现：Shapley Value
└── CustomDistributor    ← 扩展：KubeJS自定义

core/shop/role/          ← 岗位系统积木（新增）
├── RoleSystem           ← 接口：岗位和权限
├── SimpleRoleSystem     ← 实现：预设岗位
└── CustomRoleSystem     ← 实现：自定义岗位

core/contribution/       ← 贡献追踪积木（新增）
├── ContributionTracker  ← 接口：如何追踪贡
├── OrderCountTracker    ← 实现：按交付订单数
├── TimeBasedTracker     ← 实现：按在线时长
└── HybridTracker        ← 实现：混合模型

四大积木组的完整设计

1️⃣ Ownership（所有权系统）

- OwnershipProvider 接口 - 定义所有权查询和管理
- SingleOwnership - 个人商店（现有行为）
- TeamOwnership - 团队共有（你需要的核心功能）
- CorporateOwnership - 股份制（未来扩展）

2️⃣ Revenue（收益分配系统）

- RevenueDistributor 接口 - 定义分配策略
- EqualDistributor - 平均分配（简单）
- WeightedDistributor - 按权重分配（中等）
- ShapleyDistributor - Shapley Value（高级，完整实现！）
- CustomDistributor - KubeJS 自定义

3️⃣ Role（岗位系统）

- RoleSystem 接口 - 定义岗位和权限
- SimpleRoleSystem - 预设岗位（老板、经理、员工、观察者）
- CustomRoleSystem - 自定义岗位

4️⃣ Contribution（贡献追踪）

- ContributionTracker 接口 - 追踪成员贡献
- OrderCountTracker - 按交付订单数
- TimeBasedTracker - 按在线时长
- HybridTracker - 多维度综合

员工系统（非玩家）的重要性

你提到"员工（非玩家）"是下一个重要内容，这个非常关键！让我理解一下你的设想：

场景A：雇佣NPC员工（自动化）

餐厅所有者：玩家A
员工：NPC（非玩家实体）

工作流程：
1. 玩家雇佣NPC（消耗货币）
2. NPC自动工作（制作食物、交付订单）
3. 订单完成 → 收益分配：
   ├── 老板（玩家A）：60%
   ├── NPC员工1：20%
   ├── NPC员工2：20%
   └── (使用 Shapley Value 或权重分配)

问题：
- NPC员工的"收益"如何处理？
    - 选项1：虚拟账户（NPC有自己的钱包）
    - 选项2：直接扣减（雇佣成本，不分钱给NPC）
    - 选项3：提升NPC"能力"（收益越多，NPC越厉害）

---

场景B：玩家+玩家+NPC混合团队

餐厅团队：
├── 老板（玩家A）
├── 员工（玩家B）← 真实玩家
├── 员工（玩家C）← 真实玩家
└── 雇佣NPC × 2  ← 非玩家实体

收益分配（Shapley Value）：
- 考虑玩家贡献（交付订单数）
- 考虑NPC贡献（自动化劳动）
- 分配时：玩家得真钱，NPC得虚拟工资？
  Week 1: 员工抽象层
  ├── Employee 接口             ← 统一抽象
  ├── PlayerEmployee            ← 玩家实现
  ├── NpcEmployee 基础          ← NPC基础结构
  ├── EmployeeStats             ← 统计系统
  └── 集成到 TeamOwnership      ← 所有权系统识别员工

验证目标：
- 可以添加玩家成员到团队
- 可以雇佣NPC到团队
- 统计数据正确追踪

---

Phase 2：工资和分配

Week 2-3: 收益分配
├── NpcEmployeeWage           ← 工资系统
├── 修改 OrderSettlementService ← 支持NPC分配
├── ShapleyDistributor 集成   ← 玩家池Shapley分配
└── 测试：玩家+NPC混合团队收益分配

验证目标：
- NPC固定工资正确扣除
- 玩家之间Shapley分配正确
- 总收益 = NPC工资 + 玩家收益

---

Phase 3（以后）：NPC自动化

Week 4+: 自动化工作
├── NpcEmployeeCapability     ← 能力系统
├── EmployeeTask              ← 任务系统
├── NpcEmployeeBehavior       ← 行为树
└── 自动接单/制作/交付        ← 全流程自动化

这部分涉及纹理和实体，可以放到后面。

目前员工我打算依然沿用汉堡店的模式，直接永久雇佣因为你说我想要同步吗？我感觉不一定 现在的争议是A脱钩Kubejs B继续现在的情况 目前并不知道未来何去何从
如果沿用汉堡店员工模式则暂时不需要给员工接入NPC工资但是这个确实是可以做
以及是否应该引入ldlib 目前我倾向于引入 因为烹饪QoL mod可能需要引入Ldlib