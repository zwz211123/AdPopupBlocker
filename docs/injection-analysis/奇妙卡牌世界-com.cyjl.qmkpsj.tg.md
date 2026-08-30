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

并出现以下插屏 / 开屏关键方法及日志：

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

重新检查 1.03 样本后，还能在同一个 `AdsManager` 链路中确认到底部横幅广告相关入口：

- `AdsManager  initBanner`
- `AdsManager  requestBanner`
- `AdsManager  showBannerView isHighMemorySDK :`
- `AdsManager  tryShowBannerView isShowBanner:`
- `AdsManager  banner show`
- `AdsManager  banner success`
- `AdsManager  banner click`
- `AdsManager  banner close`

同时存在 `hiddenBannerView`、`tryHiddenBannerView`、`tryNativeHiddenBannerView`、`setBannerDstY` 等隐藏、恢复布局或位置调整路径，说明 Banner 并不是单纯的 SDK 自带 UI，而是由游戏自己的 `AdsManager` 管理其初始化、请求与显示状态。

## Hook 方案

与当前已支持的《管道梦工厂》使用同一套广告总控链路，因此插屏 / 开屏继续复用以下窄 Hook 入口：

- `showGameTimeInterstitial`
- `showEventInterstitial`
- `showInterstitalView`
- `TimeShowHomeInterstitial`
- `fixTimerInterTask`
- `showHotSplash`
- `openHotSplash`
- `showSplash`

《奇妙卡牌世界》额外加入专用 Banner Rule，拦截：

- `initBanner`
- `requestBanner`
- `showBannerView`
- `tryShowBannerView`

Banner Rule 只直接返回方法对应类型的默认值，不触发 `GameRecovery`。原因是横幅广告不会像强制插屏 / 开屏那样把游戏状态切到暂停链路，强行调用恢复逻辑反而可能制造额外生命周期干扰。

隐藏与清理路径不 Hook。即 `hiddenBannerView`、`tryHiddenBannerView`、`tryNativeHiddenBannerView` 等仍允许正常执行，这样即使广告对象已经在更早阶段创建，游戏自己的清理逻辑仍能把残留 View 移除。

实现层面需要允许同一个 `AdsManager` 同时安装多条 Hook Rule，因此 Hook 去重粒度从“Class 级”改为“Method 级”。否则插屏规则先命中 `AdsManager` 后，后续 Banner Rule 会因为类已被标记为已 Hook 而被直接跳过。

## 为什么继续 Hook AdsManager 而不是广告 SDK 底层

样本同时包含大量 Reward / Interstitial / Splash / Banner SDK 实现。直接拦截穿山甲、广点通、快手等 SDK 的通用 `show()` / `load()` 容易误伤玩家主动触发的激励广告，也会把其它正常广告生命周期一起砍掉。

当前方案仍只拦截游戏自己的 `com.adv.core.AdsManager` 强插屏、定时插屏、热开屏与《奇妙卡牌世界》的自动 Banner 入口，影响边界更小。
