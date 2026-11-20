# Changelog

所有 notable changes 记录在这里。版本号规则见 README。

## v0.05 — 2026-11-20

- DataStore 持久化主题、壁纸、布局、overscan 校准值。
- 新增诊断页“测试通知回复”，验证 RemoteInput 快捷回复链路。
- 设置/主题/校准修改立即落盘。

## v0.04 — 2026-11-19

- 接入真实媒体会话（MediaSessionManager）：封面、标题、艺术家、时长、进度、播放控制。
- 接入真实通知监听（NotificationListenerService）：通知列表、快捷回复、清空。
- 接入真实联系人、最近通话、通话状态（TelephonyCallback / PhoneStateListener）与接听/挂断。
- 新增设置 → 数据源：通知使用权、电话/联系人/通话记录权限、刷新媒体会话。
- 真实媒体没有歌词/队列时不再显示假内容；无权限时保留示例数据降级。
- 版本基线：`versionName 0.04`，`versionCode 4`。

## v0.03 — 2026-11-19

- 修复手机端首页标题/卡片标题在深色主题下对比度不足的问题。
- 为自定义深色卡片显式设置 `contentColor`，避免 Material3 默认内容色失效。
- 车机 UI 与自适应布局保持 v0.02 的内容。

## v0.02 — 2026-11-19

- 完整车机 UI：首页、应用中心、投屏容器、媒体、电话、通知、语音、设置、卡片管理、校准、诊断、引导、主题壁纸。
- 新增 `CarViewport` / `CarLayoutPolicy`：800×480 → 8K、横竖屏、超宽屏、不同 DPI 自动布局。
- 新增 `CarPreviewActivity`：手机/模拟器直接预览车机 UI，支持分辨率、DPI、主题、壁纸、布局切换。
- 首页：状态栏、左 Dock / 底部胶囊、地图卡（Canvas 手绘）、媒体卡、建议卡、时钟天气、通知卡。
- 应用中心：真实已安装应用、分类、搜索、长按菜单、应用详情、3/4/6/8 列自适应。
- 投屏容器：全屏 / 兼容 / 手机比例 / 分屏 / 沉浸模式。
- 媒体/电话/通知/语音/设置/校准/诊断/引导全部可交互。
- 版本基线：`versionName 0.02`，`versionCode 2`。

## v0.01 — 2026-11-17

- 初始化 CabinCanvas 工程（Kotlin + Compose + Material 3）。
- 加入 M0 探针 C：绑定百度 CarLife+ 组件，验证 `prepareCast` / 虚拟屏 / 触摸 / 音频。
- 加入 GitHub Actions 构建工作流（待 workflow scope 启用）。
- 版本基线：`versionName 0.01`，`versionCode 1`。
