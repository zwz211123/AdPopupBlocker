# APK 静态分析摘要

样本：`奇妙卡牌世界_1.03.apk`

SHA-256：`f94b3402187eca875392ba0e50370c7786c8115c448587a2ebd4f35c813c9cc6`

## Manifest / 包结构

- package: `com.cyjl.qmkpsj.tg`
- versionName: `1.03`
- application: `com.secneo.apkwrapper.AW`
- appComponentFactory: `com.secneo.apkwrapper.AP`
- launcher: `com.cyjl.qmkpsj.tg.StartAct`
- game activity: `com.cyjl.qmkpsj.tg.GameAct`
- permission: `com.cyjl.qmkpsj.tg.openadsdk.permission.TT_PANGOLIN`

表层 DEX 由 SecNeo 壳承载，但文件中仍能恢复出真实业务 DEX 的字符串与类描述符，因此可以直接确认广告总控链路。

## Unity / Native

- `lib/arm64-v8a/libil2cpp.so`：54,776,976 bytes
- `assets/bin/Data/Managed/Metadata/global-metadata.dat`：9,285,212 bytes
- 同时存在 `libunity.so`、`libpanglearmor.so`、`libPglbizssdk_ml.so` 等组件。

## 广告组件证据

APK 内 `assets/app_info.txt` 明确写有：

- A聚合
- 穿山甲 7.6.1.1
- 广点通 4.690.1560
- 快手 5.4.10.1
- beizi 5.5.0.2
- sigmob 4.25.21
- 点星 33051315
- API广告(V5.80)
- jh_sdk_7.64

Manifest 中还能看到穿山甲、广点通、快手、BeiZi、Sigmob 等广告 SDK 对应 Activity / Provider。

## 关键总控类与日志字符串

样本中存在：

`Lcom/adv/core/AdsManager;`

并出现以下关键方法及日志：

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

与当前已支持的《管道梦工厂》使用同一套广告总控链路，因此可以复用同一组窄 Hook 入口：

- `showGameTimeInterstitial`
- `showEventInterstitial`
- `showInterstitalView`
- `TimeShowHomeInterstitial`
- `fixTimerInterTask`
- `showHotSplash`
- `openHotSplash`
- `showSplash`

## 为什么继续 Hook AdsManager 而不是广告 SDK 底层

样本同时包含大量 Reward / Interstitial / Splash SDK 实现。直接拦截穿山甲、广点通、快手等 SDK 的通用 `show()` / `load()` 容易误伤玩家主动触发的激励广告。

当前方案仍只拦截游戏自己的 `com.adv.core.AdsManager` 强插屏、定时插屏与热开屏入口，并在必要时尝试恢复游戏状态，影响边界更小。
