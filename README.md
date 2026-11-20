# CabinCanvas（座舱画布）

把 Android 手机变成车机的一块“第二桌面”，通过百度 CarLife 通道投到只有 CarLife 的车机上。

> 目标车型：上汽大众朗逸 Plus 2021（869 8 英寸车机）
> 目标手机：Google Pixel 5（Android 14，未 root）
> 系统范围：Android 10–17（API 29–37）
> 当前版本：**v0.05（设置持久化 + 通知回复验证）**

## 这是什么

CabinCanvas 不是 CarLife+ 的换皮，而是一个车机桌面层：

- 手机负责渲染车规级桌面（卡片、Dock、应用网格、媒体/电话/通知/语音）。
- 传输层负责把桌面编码成 H.264，通过 CarLife 的 USB/无线通道送到车机，并把车机触摸回传。
- 应用投屏使用虚拟显示 + 子屏；Android 13+ 可获得完整多窗口，Android 10–12 降级为单屏桌面/手机镜像。

当前为 **v0.02（完整 UI 预览）**：车机首页、应用中心、投屏容器、媒体/电话/通知/语音/设置等页面已经可交互；M0 传输验证仍待真车。

- 路线 C：百度 CarLife+ 组件（`com.baidu.carlife.oppo`）+ 自研桌面。
- 路线 B：headswap 的 CarLife 传输 + 自研桌面（GPL-3.0）。

## 模块

| 模块 | 说明 |
|---|---|
| `:app` | 产品壳、手机端首页、`CarPreviewActivity` 预览宿主 |
| `app/.../core` | `CarViewport` / `CarLayoutPolicy` 分辨率无关布局 |
| `app/.../designsystem` | 主题、颜色、字号、玻璃卡片、图标、通用组件 |
| `app/.../carui` | 车机首页、应用中心、投屏、媒体、电话、通知、语音、设置、校准、诊断、引导 |
| `:probe:c` | M0 探针 C：绑定百度引擎，验证 `prepareCast` / 虚拟屏 / 触摸 / 音频 |

## 构建

要求：

- JDK 21+（推荐 Android Studio 自带的 JBR；只在当前终端会话设置 `JAVA_HOME`，不要改系统环境变量）
- Android SDK 37.2 / build-tools 37.0.0
- `local.properties` 指向本机 SDK（不要提交）

```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :probe:c:assembleDebug
```

本机没有把 SDK 加进 PATH 时，`local.properties` 写：

```properties
sdk.dir=D\:\\Android-SDK
```

Release 签名需要仓库外的 keystore 和本地 `keystore.properties`（已 gitignore），详见 `docs/RELEASE.md`。

## M0 探针怎么用

1. 安装百度 CarLife+ 组件 `com.baidu.carlife.oppo`（8.8.4）。
2. 安装 `probe/c` 的 APK。
3. 打开 “CabinCanvas Probe C”，点“启动引擎”，再点“绑定引擎”。
4. 手机插车机 USB，车机点 CarLife。
5. 观察车机是否出现色条 + 5 个按钮；点按钮，手机端日志会记录触摸。
6. 点“分享日志”，把日志发回。

详细说明见 `docs/M0-PROBE.md`。

## 版本与发布

- 版本从 `v0.01` 开始；普通功能/修复 +0.01；重大功能允许跳号。
- `versionCode = major × 100 + minor`（0.01 → 1，0.10 → 10，1.00 → 100）。
- 每个版本创建 GitHub Release，附 APK + 中文 changelog。

## 安全与隐私

- 不提交 `local.properties`、keystore、`keystore.properties`、`secrets.properties`、`.env`、API Key、Token。
- 不打包/不再分发百度 CarLife+、OPPO Car+、CarLink 的 APK 或资产。
- 日志默认本地保存，导出前请自行检查是否包含个人信息。

## License

GPL-3.0。详见 `LICENSE`。

## 免责声明

非官方项目，与 Google、Baidu、OPPO、vivo、上汽大众无关。CarLife、Android 等商标归其各自所有者。使用风险自负；驾驶中请勿操作屏幕。
