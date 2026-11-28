# NorPatch 更新日志

版本线：`3.0.x`。`versionName` 取自 `version.properties` 的 `managerVersionPatch`（`3.0.$patch`），`versionCode` 仍由 `managerVersionEpoch(10200) + git 提交数` 自动推导，每次提交都会递增。

本次为从上游基线 `0.1.23` 起的整轮 UI 重构与问题修复，最终版本 **3.0.13**。

---

## 一、核心修复

### 1. 首页卡片冷启动退化为纯色（长期问题，已修复）

**现象**：首次进入首页或反复退出重进后，卡片变成不透明纯色块；手动切换一次布局就恢复正常。

**根因（三层叠加）**：
- 卡片透明度的取值被耦合在「是否启用自定义壁纸」上，没有壁纸时 `BlockBackground` 走 SOLID 分支回退 `alpha = 1f`；
- `GlassCard` 内部把透明度写死成 `fillAlpha`（0.72~0.88），覆盖了全局设置；
- 冷启动时 `BackgroundConfig.load()` 在首次组合（composition）之后才执行，首帧用 JVM 默认值渲染，直到某个重组（切布局）才刷新。

**修复**：
- `MainActivity.onCreate` 在 `setContent` **之前**同步调用 `BackgroundConfig.load(this)`，首帧即用真实配置。
- 新增全局 `cardGlassAlpha`（默认 `0.35`，范围 0.15~0.95），`Theme.kt` 的 `surfaceContainer` / `surfaceContainerHigh` 无论有无壁纸都套用该 alpha，卡片不再退回实色。
- 删除 `GlassCard` 的 `fillAlpha` 参数及所有调用点（`WarningCard` 0.88、`LearnMoreCard` 0.80 等）传值，透明度统一由全局设置决定。
- `BlockBackground` 的 IMAGE 分支补上白色垫底层，图片本身叠加 `.alpha(alpha * frostedAlpha)`，不再用不透明图片盖满整卡。

### 2. 卡片描边（边框）不生效

- 首页 `BlockCard` 原先读的是每张卡的 `HomePrefs.blockBorder*`，而设置页写的是全局 `BackgroundConfig.cardBorderEnabled`，两套配置互不相通。改为**取并集**（`全局 || 块级`），圆角/阴影/内边距也统一优先走全局值，块级值仅在偏离默认值时覆盖。
- **描边色隐形**：默认描边色为纯白 `0xFFFFFFFF`，画在浅色卡片上完全看不见。新增 `globalCardBorderColor()`：当存值为纯白时视为「自动」，改用 `colorScheme.outline.copy(alpha = 0.7f)`，深浅色背景下都可见。
- **另两个布局无描边**：Gridu / Dashboard 用的是 Material3 原生 `Card`，从未接入全局样式。在 `GlassCard.kt` 新增全局卡片样式辅助函数 `globalCardShape()` / `globalCardBorder()` / `globalCardBorderColor()` / `globalCardElevation()`，并接入 `HomeGridu.kt`（2 处 Card）与 `HomeDashboardV4.kt`（4 处 Card：英雄卡、错误态卡、AndroidPatch 卡、Tonal 卡）。

### 3. 移除诊断代码

删除 v3.0.8 排查期加的全部临时诊断信息：`Home.kt` 顶部黄底诊断横幅、`GlassCard` `BlockCard` 内的红色 `dbg α=...` 调试文本，以及随之多余的 import。

### 4. 底部导航栏与应用其他区域的透明度割裂

`BottomBar` 原先在「自定义背景关闭」时直接回退 `NavigationBarDefaults.containerColor`（实色）。改为始终使用 `surfaceContainer.copy(alpha = BackgroundConfig.cardGlassAlpha)`，玻璃观感在全状态下保持一致。

### 5. SuperUser 授权卡顿与重复点击竞态

- `PkgConfig.changeConfig` 由 `thread {}` 改为 `suspend` + `withContext(Dispatchers.IO)`，调用方可按顺序串接「写配置 → grant/revoke → setUidExclude」。
- `SuperUserViewModel` 中 `setRootGranted` / `setExcluded` 改为主线程乐观更新 UI（开关立刻响应），内核侧操作放到 `viewModelScope.launch(Dispatchers.Default)`；失败时回滚 UI 状态并记日志。
- 新增 `pendingUids` 集合与 `isPending(uid)`：同一 UID 操作未完成时禁用开关，避免连点导致的竞态与丢更新。
- `fetchData` 中多余的 `thread{}.join()`（已在 IO 线程却又开线程并等待）删除，改为内联执行。

### 6. 其它小问题

- 电池温度乱码（`閹虹煰`）改为正确的 `°C`。
- 三点菜单 `DropdownMenu` 显式设置 `containerColor = surfaceContainer`、`tonalElevation = 0.dp`，消除与中间区域的色差。
- 设置页分组的 `divider()` 在首个条目之前会画一条多余分隔线，改为跳过首项。
- 设置页列表项 `supportingContent` 文字颜色由 `outline` 改为 `onSurfaceVariant`，提升可读性。
- 关于页版本号不再显示 `versionCode (versionName)` 拼接，只显示 `NorPatch版本：3.0.x`。
- 关于页文案重写（说明为 APatch 分支、底层 KernelPatch 与上游一致、三套布局来源）。

---

## 二、新增功能

### 1. 全局卡片自定义体系

所有卡片（首页三卡、设置页 `SectionCard`、包列表、Gridu、Dashboard）共用一套外观配置，存于 `BackgroundConfig`：

| 配置 | 默认值 | 范围 |
|---|---|---|
| `cardGlassAlpha` 毛玻璃透明度 | 0.35 | 0.15 ~ 0.95 |
| `cardOpacity` 整体透明度 | 1.0 | 0.1 ~ 1.0 |
| `cardCornerRadius` 圆角 | 28dp | 0 ~ 40dp |
| `cardBorderEnabled` 边框 | 关 | 布尔 |
| `cardBorderColor` 边框色 | `0xFFFFFFFF`（自动） | ARGB |
| `cardShadowEnabled` / `cardShadowElevation` 阴影 | 关 / 0dp | 0 ~ 24dp |
| `cardPaddingScale` 内边距倍率 | 1.0 | 0.5 ~ 1.5 |

### 2. 全局动画预设

`BackgroundConfig.animationPreset`（默认 4 = 弹性），共 5 档：`0 无动画`、`1 淡入`、`2 缩放`、`3 上滑`、`4 弹性`。`GlassCard.kt` 内 `AnimationPreset` 提供 `pressScaleSpec()` / `enterTransition()` / `exitTransition()`，卡片、列表项与按压反馈共用同一套动效语言。

### 3. 首页外观改为「一级菜单 + 6 个子页面」

此前所有外观选项堆在单页，现改为索引页 + 独立子页，每个子页统一使用 `AppearanceSubPage`（顶栏 + 滚动骨架）：

1. `BlockConfigScreen` — 区块显隐与社区入口
2. `GlobalBackgroundScreen` — 壁纸、透明度、模糊、变暗
3. `CardAppearanceScreen` — 毛玻璃、圆角、边框、阴影、内边距 + **一键重置所有外观**
4. `AnimationScreen` — 动画预设
5. `GriduCardScreen` — Gridu 工作卡壁纸与显示项
6. `DashboardCardScreen` — Dashboard 英雄卡壁纸

配套新增 `AppearanceMenuRow`（菜单行）、6 条菜单摘要字符串（中英各一份）与 `home_appearance_reset_all`。

### 4. 设置页拆分为「外观」与「系统」两个分组

`settings_section_appearance` / `settings_section_system` 新增，原「通用」组按语义拆分。

### 5. 版本号机制

`build.gradle.kts` 的 `getVersionName()` 改为读取 `version.properties` 的 `managerVersionPatch`，返回 `3.0.$patch`；`version.properties` 补充注释说明递增规则。

### 6. 其它

- 「系统颜色主题」更名为「莫奈取色」，摘要改为「Material You 动态取色，从壁纸提取主题色」。
- 包列表 `AppItem` 由 `ListItem` 改为 `GlassCard` + `Row` 布局（44dp 圆角图标、包名次级文本、右侧开关），与全局玻璃质感统一。
- 新增中英文案：卡片外观各滑块的标题/摘要、动画预设 5 个名称、菜单摘要、重置按钮。

---

## 三、删除 / 移除

- 删除 `GlassCard` 的 `fillAlpha` 参数（及全部传值调用）。
- 删除 v3.0.8 的临时诊断横幅与红色调试文本。
- 删除 `NavigationBars.kt` 中 `NavigationBarDefaults` 的实色回退分支与对应 import。
- 删除 `SuperUser.kt` 中 `ListItem` / `ListItemDefaults` / `clickable` 等已被 `GlassCard` 替代的用法与 import。
- 删除 `HomeGridu.kt` 中 `RoundedCornerShape` import（改用 `globalCardShape()`）。
- 删除 `SuperUserViewModel` 中 `thread {}` / `thread{}.join()` 旧实现。
- 删除 `AboutScreen` 中 `versionCode (versionName)` 拼接逻辑。
- 删除 `SettingsSections` 中 `supportingContent = {}` 空占位。

---

## 四、已知问题 / 待办

- `app/build.gradle.kts` 的 `signingConfigs norpatch` 仍指向本地调试占位证书 `D:\NorPatch\norpatch.jks`（别名与口令均为 `norpatch`），**正式发布前必须替换为真实签名**，且该文件不应提交到仓库。
- 冷启动卡片实色问题自 3.0.12 起未再收到复现反馈，但尚未得到用户最终确认。
- 仓库内不应包含构建产物（`*.apk`）与构建日志，已在 `.gitignore` 中排除。

---

## 五、版本时间线

| 版本 | 内容 |
|---|---|
| 3.0.1 | 首版 UI 重构（三套布局、玻璃卡片） |
| 3.0.2 | 新增全局 `cardGlassAlpha`，解耦壁纸状态 |
| 3.0.3 | 删除 `GlassCard.fillAlpha` 硬编码 |
| 3.0.4 / 3.0.5 | 继续下调默认透明度（无效，后续定位到时序问题） |
| 3.0.6 | 修正 `isNavBarGlassEnabled` 默认值写反 |
| 3.0.7 | pref key 升为 `_v2`，强制新默认值生效 |
| 3.0.8 | 诊断版（红色调试文本，用于像素比对） |
| 3.0.9 | 冷启动同步加载 + 图片背景强制毛玻璃 + 一键重置 |
| 3.0.10 | 删除全部诊断代码 + 首页描边配置打通 |
| 3.0.11 | Gridu / Dashboard 接入全局圆角、描边、阴影 |
| 3.0.12 | 描边色「自动」逻辑，修复白线在浅色卡片上隐形 |
| 3.0.13 | 首页外观重构为一级菜单 + 6 个子页面 |
