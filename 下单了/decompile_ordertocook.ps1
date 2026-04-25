<#
批量反编译脚本 (PowerShell)
用途：下载 CFR（如未存在），反编译仓库中 `下单了/cn/breezeth/ordertocook` 下的 .class 文件，
并把若干关键类的反编译结果合并到根目录的 `下单了_decompiled_core.txt`。

先决条件：已安装 JDK（可在命令行使用 java -version）。
运行：在仓库根目录（含本脚本）执行：
    powershell -ExecutionPolicy Bypass -File .\decompile_ordertocook.ps1

注意：此脚本会从互联网下载 CFR 反编译器 jar，如果机器无法联网，请手动把 CFR jar 放到 tools\cfr.jar。
#>

set -e
Push-Location -LiteralPath (Split-Path -Path $MyInvocation.MyCommand.Definition -Parent)

$modDir = "下单了"
$classRoot = Join-Path $modDir "cn\breezeth\ordertocook"
if (-not (Test-Path $classRoot)) {
    Write-Error "找不到 class 目录: $classRoot"
    Pop-Location
    exit 1
}

$toolsDir = "tools"
New-Item -ItemType Directory -Force -Path $toolsDir | Out-Null
$cfrJar = Join-Path $toolsDir "cfr.jar"

if (-not (Test-Path $cfrJar)) {
    Write-Host "未找到 CFR 反编译器，正在下载到 $cfrJar ..."
    $cfrUrl = 'https://www.benf.org/other/cfr/cfr-0.152.jar'
    try {
        Invoke-WebRequest -Uri $cfrUrl -OutFile $cfrJar -UseBasicParsing -ErrorAction Stop
        Write-Host "下载完成。"
    } catch {
        Write-Error "无法下载 CFR，请手动下载并放置到 $cfrJar。错误: $_"
        Pop-Location
        exit 1
    }
} else {
    Write-Host "已存在 CFR: $cfrJar"
}

# 输出目录
$outDir = "decompiled_src\ordertocook"
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $outDir
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Write-Host "开始反编译 class 文件到: $outDir"

# 使用 CFR 对每个 class 文件反编译
$classFiles = Get-ChildItem -Path $classRoot -Recurse -Filter "*.class"
$total = $classFiles.Count
Write-Host "发现 $total 个 .class 文件。逐个反编译中（可能需要几分钟）..."

$i = 0
foreach ($f in $classFiles) {
    $i++
    Write-Host "[$i/$total] 反编译: $($f.FullName)"
    & java -jar $cfrJar --outputdir $outDir --silent true $f.FullName | Out-Null
}

Write-Host "反编译完成。现在收集关键类到下单了_decompiled_core.txt"

$targetClasses = @(
    'cn.breezeth.ordertocook.core.OrderGenerator',
    'cn.breezeth.ordertocook.core.OrderNpcManager',
    'cn.breezeth.ordertocook.core.RestaurantPersistentState',
    'cn.breezeth.ordertocook.core.PrestigeManager',
    'cn.breezeth.ordertocook.core.WashingTableManager',
    'cn.breezeth.ordertocook.registry.OrderNpcRegistry'
)

$outputFile = Join-Path (Get-Location) '下单了_decompiled_core.txt'
"反编译合并输出 - 下单了 (ordertocook) - $(Get-Date)" | Out-File -Encoding UTF8 $outputFile
"来源目录: $classRoot" | Out-File -Encoding UTF8 -Append $outputFile
"" | Out-File -Encoding UTF8 -Append $outputFile

foreach ($fqcn in $targetClasses) {
    $relPath = ($fqcn -replace '\.', '\') + '.java'
    $srcPath = Join-Path $outDir $relPath
    if (Test-Path $srcPath) {
        "==================== $fqcn ====================" | Out-File -Encoding UTF8 -Append $outputFile
        Get-Content -Raw -Encoding UTF8 $srcPath | Out-File -Encoding UTF8 -Append $outputFile
        "`n`n" | Out-File -Encoding UTF8 -Append $outputFile
    } else {
        "!!!! 未找到反编译源码: $fqcn (预期路径: $srcPath)" | Out-File -Encoding UTF8 -Append $outputFile
    }
}

Write-Host "合并完成：$outputFile"
Write-Host "完成。您可以打开该文件查看关键类的反编译源码。"

Pop-Location

