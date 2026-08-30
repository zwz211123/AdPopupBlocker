# 管道梦工厂强插屏净化 (LSPosed Modern API 102)

目标 APK：`管道梦工厂 1.00`

- 包名：`com.bzdjl.lyxn.tg`
- SHA-256：`335397b0898b6f4053357043e49a4efe1f88bf0b2db349cfd1f52fdc779bf2af`
- 壳：`com.secneo.apkwrapper.*`，表层 DEX 仅 5 个类
- 游戏：Unity IL2CPP
- 广告聚合：A聚合，APK 自带信息列出穿山甲 7.2.3.3、广点通 4.660.1530、快手 4.6.30.1、BeiZi 5.2.2.11、Sigmob 4.24.6、点星等
- 已定位总控类：`com.adv.core.AdsManager`

## 当前阻断点

模块只针对 `com.adv.core.AdsManager` 中明确属于主动插屏/开屏链路的方法：

- `showGameTimeInterstitial`
- `showEventInterstitial`
- `showInterstitalView`（原包拼写如此）
- `TimeShowHomeInterstitial`
- `fixTimerInterTask`
- `showHotSplash`
- `openHotSplash`
- `showSplash`

所有重载都会被拦截，并按返回类型返回安全默认值。

## 明确保留

没有 Hook 下列链路，因此不会主动破坏：

- 激励视频：`showVideo` / `requestVideo` / `onVideoRewarded` / `onInsertVideoRewarded` 等
- 支付
- 排行榜 / 成绩上传
- 普通业务网络接口
- Banner / Feed / Native 广告（当前版本不处理，因为需求只要求不经同意直接怼脸的主动弹窗）

## 暂停恢复

样本字符串明确出现：

- `AdsManager StarActPause`
- `AdsManager StarActResume`

因此每次阻断强插屏后，模块会优先反射调用同一 `AdsManager` 的 `StarActResume()`。如果壳后版本里该桥接失效，还会尝试从 `AdsManager` 持有的 `Activity` 找到 `UnityPlayer` 并调用 `resume()` 作为兜底。

模块不会全局 Hook `Activity.onPause/onResume`，避免把正常切后台、支付页、系统弹窗等生命周期行为搅烂。

## 作用域

`META-INF/xposed/scope.list` 给出推荐作用域：

`com.bzdjl.lyxn.tg`

但 `module.prop` 使用：

`staticScope=false`

所以 LSPosed 不会把作用域锁死。代码结构也把目标与规则拆成 `TargetSpec / TargetRegistry / HookRule / HookEngine`，以后扩展别的包或新增规则只需要加 TargetSpec/HookRule，不用重写 Hook 引擎。

## 构建

需要 Android SDK 37、JDK 21，依赖 Modern Xposed API 102：

```bash
./gradlew :app:assembleRelease
```

产物通常在：

`app/build/outputs/apk/release/app-release.apk`

## 运行时验证

建议先在 LSPosed 日志中过滤：

`PipeAdBlocker`

正常情况下会看到：

- `target ready`
- `installed ... forced-ad hook(s)`
- 触发广告点时 `blocked forced ad: com.adv.core.AdsManager#...`

如果 SecNeo 延迟加载真实 DEX，会先看到 `installed deferred class-loader watcher for protected DEX`，随后在 `AdsManager` 真正装载时自动补 Hook。
