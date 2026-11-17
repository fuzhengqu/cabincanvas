# M0 探针 C 使用说明

探针 C 用来回答一个核心问题：**百度 CarLife+ 组件是否接受第三方 launcher，并把桌面 Surface、触摸和音频交给我们？**

它只做链路验证，不做产品 UI。

## 前置条件

1. Pixel 5 已安装 `com.baidu.carlife.oppo` 8.8.4。
2. 手机开启 USB 调试；`adb devices` 能看到设备。
3. 车机是朗逸 Plus 2021 的 869 主机（或同平台 CarLife 车机）。

## 安装

```powershell
adb install -r probe/c/build/outputs/apk/debug/c-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

如果 `probe/c` 的 APK 名字不是 `c-debug.apk`，以 `probe/c/build/outputs/apk/debug/` 下的实际文件为准。

## 真车步骤

1. 打开 **CabinCanvas Probe C**。
2. 点 **启动引擎（startForegroundService）**；如果车机/引擎没反应，再点 **启动引擎（startService 备用）**。
3. 点 **绑定引擎（bindService）**。
4. 手机插车机 USB；车机点 CarLife。
5. 观察车机是否出现深色画面、`CabinCanvas · Probe C`、5 个大按钮 A–E。
6. 点按钮，手机端应出现 `view touch ...`；如果引擎走回调，还会出现 `remote touch ...`。
7. 播放一段音乐，观察 `audio=... bytes` 是否增长。
8. 点 **分享日志**，把 `probe.log` 发回。

## 日志判读

| 日志 | 含义 | 下一步 |
|---|---|---|
| `CastService.onBind` | 引擎愿意绑定我们的服务 | 继续看 `setCastCallback` |
| `manager.setCastCallback` | 引擎把回调给了我们 | 看 `callback code=1` |
| `callback code=1 ok=true` | 我们成功调用 `onServiceReady` | 等 `prepareCast` |
| `manager.onTransact code=1` | 引擎调用 `prepareCast` | 看 CastConfig 解析 |
| `createVirtualDisplay ok flags=full` | 系统允许完整 flags | 最理想 |
| `createVirtualDisplay full failed ...` | 缺 `ADD_TRUSTED_DISPLAY`/`CAPTURE_VIDEO_OUTPUT` | 看是否回退 `public`/`private` |
| `Presentation shown` | 桌面已显示在 CastConfig 的 Surface 上 | 看车机画面 |
| `view touch ...` | 触摸进了我们的 View | 坐标/缩放正确即可 |
| `remote touch ...` | 引擎通过 `IRemoteTouchListener` 回传 | 说明注入不一定可用 |
| `remote audio packet=...` | 引擎回传 PCM | 音频链路可用 |
| 只有 `onBind` 没有 `prepareCast` | 引擎在等车机握手/权限/鉴权 | 看车机是否进入 CarLife、引擎通知/权限页 |

## 失败时的最小排查

- 车机没有画面：先确认车机端 CarLife 是否真的进入投屏页；再确认 `prepareCast` 是否到达。
- `prepareCast` 的 `Surface` 无效：CastConfig 字段顺序可能和 v1 草案不同；把日志里的 `size`/`token` 发回，构建期用 JADX 复核。
- 触摸没有反应：看是 `view touch` 还是 `remote touch`；都没有，说明引擎没有把触摸交给我们。
- 音频为 0：先切蓝牙 A2DP 验证车机功放；音频不是 M0 的硬门槛。

## 输出

把以下内容写入 `docs/M0-REPORT.md`：

- 手机型号/系统、引擎版本、车机型号/固件/分辨率。
- 探针日志（脱敏）。
- 车机画面照片（可选）。
- 结论：C 通过 / C 部分通过 / C 失败；失败原因。
- 对应下一步：继续 C、切 B、或混合。
