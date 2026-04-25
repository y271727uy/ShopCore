使用说明（反编译 `下单了` Mod 的 class 并提取关键类）

概述：
  - 本仓库包含 `下单了` Mod 的解压目录（含大量 .class 文件）。
  - 脚本 `decompile_ordertocook.ps1` 会下载 CFR 反编译器（如果本地缺失），对 `下单了/cn/breezeth/ordertocook` 下的所有 .class 文件逐个反编译，
    并把若干关键类的反编译 Java 源合并到仓库根目录的 `下单了_decompiled_core.txt`。

前提：
  - 需要在有网络的机器上运行以便下载 CFR（或请手动将 CFR jar 放到 tools\cfr.jar）。
  - 需要安装 JDK（可以运行 java 命令）。

运行步骤（在仓库根目录）：

1) 打开 Powershell（Windows）并 cd 到仓库根目录（含本脚本）。

2) 运行脚本（允许执行策略临时绕过）：
```
powershell -ExecutionPolicy Bypass -File .\decompile_ordertocook.ps1
```

脚本会执行：
  - 下载 CFR 到 tools\cfr.jar（如果缺失）
  - 反编译 下单了/cn/breezeth/ordertocook 下的所有 .class 文件到 decompiled_src\ordertocook
  - 将脚本中列出的关键类（可在脚本内修改）合并到 `下单了_decompiled_core.txt`

输出文件：
  - decompiled_src/ordertocook/... (反编译出的 Java 源文件)
  - 下单了_decompiled_core.txt （合并的关键类源码，用 UTF-8 编码）

如果你希望我在拿到反编译后的源码后，进一步抽取核心逻辑（整理为更精简的解释、伪代码或将关键函数注释中文化），
请把反编译后的 Java 文件上传或允许我在本环境运行脚本（如果你希望我代为运行并环境允许的话）。

常见问题：
  - 无法下载 CFR：请手动将 CFR jar 下载到 tools\cfr.jar（推荐版本 cfr-0.152.jar），然后重复运行脚本。
  - java: 未找到命令：请安装 JDK 并把 java 加入 PATH。

安全与许可：请确保你有权反编译该 mod（你已表示允许）。反编译仅用于分析/学习目的。

