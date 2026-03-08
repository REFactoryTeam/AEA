# [AEA]

这是一个应用能源2 (AE2) 的附属与体验优化 (QoL) 模组，旨在增强样板编码、改善合成UI，并提供与其他流行模组的无缝联动。

## ✨ 核心特性

### 🧩 样板与编码增强
* **样板编码修改器：**
    * 支持独立的输入和输出黑名单。
    * 可调整全局输入/输出（I/O）倍率。
    * 自定义材料合并规则：`全局 (Global)` -> `相邻 (Adjacent)` -> `无 (None)`。
* **灵活的样板轮换：** 可以在编码界面中轻松地正向/反向轮换主输入材料，以及反向轮换主输出产物。
* **可染色样板：** 现在可以对样板进行染色以便于分类管理。此外，染色样板现在支持自循环（递归）配方计算。
* **镜像样板供应器：** 新增了一个“镜像”样板供应器，用于复制和同步样板的操作。

### 🖥️ AE2 系统与 UI 体验优化 (QoL)
* **增强的合成状态：** 为 AE2 合成监控器添加了更详细的状态显示，包括 **“阻塞 (Blocked)”** 状态和 **“样板次数 (Pattern Times)”**（发配次数）。
* **扩展样板管理终端 (EAE)：** 为扩展样板管理终端的 UI 添加了酷炫的彩虹边框效果。
* **石英切割刀实用功能：** 现在你可以使用石英切割刀快速将物品或方块的名称复制到剪贴板。

### 📡 无线连接
* **无线连接供应器：** 可以无线连接到任意 AE 网络节点。
* **普通版本：** 相当于一根致密线缆。
* **扩展版本：** 提供 6 路连接（每面一个）。
* **高级版本：** 按频道连接；每个频道相当于一根致密线缆。

### 🤝 模组联动
* **JEI 物品管理器交互：** 将 JEI 书签的操作直接转发到 AE2 终端。现在点击 JEI 书签的行为与直接在 AE2 UI 中点击物品的效果完全一致。
* **建筑小帮手 2 (Building Gadgets 2) 联动：** 轻松将 BG2 “复制粘贴工具”中所需的材料清单直接转换为 AE2 的处理样板。
* **FTB 连锁挖矿 & 记忆卡兼容：** 使用 AE2 记忆卡时新增了对 FTB Ultimine 的支持，允许你一次性“连锁粘贴”多个机器的配置。

### 🛠️ 修复与渲染
* **JEI 渲染修复：** 修复了 JEI 中通用包裹堆栈（Wrapped Generic Stacks）的渲染问题 (AE/JEI)。
* **压印器槽位语义：** 修复并纠正了压印器配方的槽位语义，以确保在编码时正确处理配方。

## 📦 依赖模组
为了体验本模组的所有功能，请确保安装了以下模组：
* [应用能源 2 (Applied Energistics 2)](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) (必需)
* [扩展AE (ExtendedAE / EAE)](https://www.curseforge.com/minecraft/mc-mods/ex-pattern-provider) (用于彩虹边框效果)
* [JEI 物品管理器 (Just Enough Items)](https://www.curseforge.com/minecraft/mc-mods/jei) (强烈推荐)
* [建筑小帮手 2 (Building Gadgets 2)](https://www.curseforge.com/minecraft/mc-mods/building-gadgets) (用于复制粘贴工具联动)
* [FTB 连锁挖矿 (FTB Ultimine)](https://www.curseforge.com/minecraft/mc-mods/ftb-ultimine-forge) (用于连锁复制配置功能)
