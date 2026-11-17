# 架构

## 目标

把车机当成手机的第二块屏：CarLife 负责通道，CabinCanvas 负责车规级桌面、应用窗口和系统能力。

## 分层

```
┌─────────────────────────────────────────────────────┐
│ UI (Compose + Material 3)                           │
│ Home / Apps / Media / Phone / Notifications / ...   │
├─────────────────────────────────────────────────────┤
│ DesktopCore                                         │
│ CarWindowHost / AppCompatProfile / InputRouter      │
├─────────────────────────────────────────────────────┤
│ TransportAdapter                                    │
│ EngineTransport (C)  |  HeadswapTransport (B)       │
├─────────────────────────────────────────────────────┤
│ SystemBridge                                        │
│ MediaSession / Notification / Telecom / Location    │
└─────────────────────────────────────────────────────┘
```

传输层只提供四件事：一个 Surface、触摸/按键事件、PCM 音频、连接状态。桌面层不依赖具体传输实现，M0 选 C 还是 B 都不影响 UI。

## 路线 C（M0 当前）

```
CarDeck (app)
  ├─ CastService (Binder.onTransact)
  │    prepareCast(CastConfig)
  │    onCastPrepared(displayId)
  ├─ VirtualDisplay + Presentation
  └─ ProbeView (M0) / 车机桌面 (M1+)
        ↕ Binder
com.baidu.carlife.oppo
  AOA/USB / Wi-Fi / H.264 / 触摸 / 音频 / 鉴权
        ↕ USB
869 车机
```

`probe/c` 不依赖 AIDL 生成：它按 transaction code 手工收发，先把真实 descriptor/code 记录下来，构建期再用 JADX 复核。

## 路线 B（保底）

headswap 的 `CarLifeSession`/`StreamTransport`/`DesktopHost` 提供 CarLife 2.0、虚拟屏、uinput、AudioMix。GPL-3.0。M0 通过后再决定是否 fork。

## 版本

- `versionName 0.01` 起，`versionCode = major × 100 + minor`。
- 每个版本一个 Git tag + GitHub Release。
