# APK 静态分析摘要

样本：`管道梦工厂_1.00.apk`

SHA-256：`335397b0898b6f4053357043e49a4efe1f88bf0b2db349cfd1f52fdc779bf2af`

## Manifest / 包结构

- package: `com.bzdjl.lyxn.tg`
- application: `com.secneo.apkwrapper.AW`
- appComponentFactory: `com.secneo.apkwrapper.AP`
- launcher: `com.bzdjl.lyxn.tg.StartAct`
- game activity: `com.bzdjl.lyxn.tg.GameAct`
- permission: `com.bzdjl.lyxn.tg.openadsdk.permission.TT_PANGOLIN`

表层 `classes.dex`：301 strings / 86 types / 186 methods / 5 classes，说明真实业务 DEX 被壳保护。

## Unity / Native

- `lib/arm64-v8a/libil2cpp.so` ~124.9 MB
- `lib/arm64-v8a/libunity.so` ~20.8 MB
- `assets/bin/Data/Managed/Metadata/global-metadata.dat` ~22.97 MB，metadata 内容被处理/混淆，无法直接依靠标准字符串堆还原 C# 名称。

## 广告组件证据

APK 内 `assets/app_info.txt` 明确写有：

- A聚合
- 穿山甲 7.2.3.3
- 广点通 4.660.1530
- 快手 4.6.30.1
- beizi 5.2.2.11
- sigmob 4.24.6
- 点星
- API广告(V5.80)
- jh_sdk_7.59

同时存在 `libpanglearmor.so`、`libPglbizssdk_ml.so` 等。

## 关键总控类与日志字符串

保护 DEX 内仍可恢复出原始字符串/类描述符：

`Lcom/adv/core/AdsManager;`

并出现：

- `AdsManager  TimeShowHomeInterstitial`
- `AdsManager  fixTimerInterTask isResume :`
- `AdsManager  loadFixShowInters isOpenGameFixInter`
- `AdsManager  showEventInterstitial`
- `AdsManager  showGameTimeInterstitial`
- `AdsManager  showInterstitalView`
- `AdsManager  showHotSplash`
- `AdsManager  showSplash`
- `AdsManager  openHotSplash`
- `AdsManager StarActPause`
- `AdsManager StarActResume`

这组字符串足以证明主动插屏/热开屏/启动开屏与游戏暂停恢复都集中在该 AdsManager 链路。

## 为什么不 Hook SDK 底层 show()

样本同时包含大量聚合 SDK 的 Reward / Interstitial / Splash 实现。如果直接 Hook 穿山甲、广点通、快手等 SDK 的通用 show/load 接口，容易把用户主动点的激励广告一起干掉，也会扩大兼容风险。

当前方案优先 Hook 游戏自己的 `com.adv.core.AdsManager` 强插屏入口，边界最窄。
