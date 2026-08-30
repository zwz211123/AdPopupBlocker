# AdPopupBlocker

一个基于 Modern Xposed API 102 的可扩展 LSPosed 模块，用于按应用规则拦截未经用户主动触发的插屏、开屏等广告，并在必要时恢复应用运行状态。

本项目不会以“广告 SDK 一锅端”的方式工作。每个目标应用都有独立规则，只处理已经确认属于主动强插屏/开屏链路的调用，尽量保留激励广告、支付、排行榜上传及其他正常业务功能。

## 当前支持

| 应用 | 包名 | 状态 |
| --- | --- | --- |
| 管道梦工厂 1.00 | `com.bzdjl.lyxn.tg` | 已加入规则 |

具体样本分析和 Hook 点见 [`docs/injection-analysis/管道梦工厂-com.bzdjl.lyxn.tg.md`](./docs/injection-analysis/%E7%AE%A1%E9%81%93%E6%A2%A6%E5%B7%A5%E5%8E%82-com.bzdjl.lyxn.tg.md)。

## 设计原则

- 只拦截明确识别出的主动弹窗广告入口。
- 不全局屏蔽广告 SDK 的通用 `show()`。
- 不处理激励广告链路。
- 不处理支付、排行榜、成绩上传及其他正常业务功能。
- 若目标应用在展示广告前主动暂停游戏，可在规则中配置对应恢复逻辑。
- 推荐作用域通过 `META-INF/xposed/scope.list` 提供，但 `staticScope=false`，不会把模块锁死在固定应用列表中。

## 扩展结构

目标与 Hook 逻辑已经拆分为：

- `TargetRegistry`
- `TargetSpec`
- `HookRule`
- `HookEngine`

新增应用时，优先增加新的 `TargetSpec` 和 `HookRule`，而不是把包名、类名和方法名继续堆进模块入口。

注入点分析统一存放在：

```text
docs/injection-analysis/
```

文件命名规则：

```text
APP名-包名.md
```

例如：

```text
管道梦工厂-com.bzdjl.lyxn.tg.md
```

## 构建

当前工程使用：

- Android Gradle Plugin 9.2.1
- Gradle 9.4.1
- Android SDK 37
- JDK 21
- Modern Xposed API 102

仓库没有依赖本地 Android Studio。GitHub Actions 会自动安装构建环境，并同时构建 Debug APK 与未签名 Release APK。

本地已有 Gradle 9.4.1 和 Android SDK 时可以执行：

```bash
gradle :app:assembleDebug :app:assembleRelease
```

产物位于：

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

其中 Debug APK 带默认调试签名，可直接用于测试；Release APK 默认未配置发布签名。

## LSPosed 推荐作用域

当前 `META-INF/xposed/scope.list` 包含：

```text
com.bzdjl.lyxn.tg
```

后续支持新应用时继续追加包名即可。

## License

MIT License. See [`LICENSE`](./LICENSE).
