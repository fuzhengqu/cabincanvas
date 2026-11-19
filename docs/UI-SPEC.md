# UI 规格（v0.02）

## 设计语言

- 深空（默认）/ 晨雾 / 自动三套主题。
- 玻璃卡片：`surface` 78% 叠加、1px 高光边、24dp 圆角、无实时 blur。
- 主色 `#2F6BFF`，成功 `#19A974`，提醒 `#FFB020`，危险 `#E32E27`。
- 字体：系统 sans / Noto Sans SC；标题 24–36sp，正文 14sp，辅助 11–12sp，行高 1.28。
- 动效：220ms cubic-bezier(0.2,0,0,1)，卡片按压 0.98，页面 Crossfade。

## 页面

| 路由 | 内容 |
|---|---|
| `HOME` | 状态栏、Dock、地图卡、媒体卡、建议卡、时钟天气、通知卡 |
| `APPS` | 搜索、分类、应用网格、长按菜单 |
| `PROJECTION` | 全屏/兼容/手机比例/分屏/沉浸 |
| `MEDIA` | 封面、歌词、进度、队列 |
| `PHONE` | 最近、联系人、拨号、来电/通话中 |
| `NOTIFICATIONS` | 列表、清空、快捷回复、模拟 |
| `SETTINGS` | 连接/显示/声音/应用/语音/通知/桌面/校准/关于 |
| `THEME` | 主题卡 + 壁纸网格 |
| `CARDS` | 卡片显示/尺寸/添加 |
| `CALIBRATION` | 四边安全区/overscan |
| `DIAGNOSTICS` | 状态、日志、打开 M0 探针 |
| `ONBOARDING` | 5 步引导 |

## 自适应矩阵

| 宽度 | 布局 | 列 | Dock | 字号倍率 |
|---|---|---:|---|---:|
| 800dp | COMPACT | 3 | LEFT | 0.82 |
| 1024dp | MEDIUM | 4 | LEFT | 0.82 |
| 1280dp | MEDIUM | 4 | LEFT | 1.00 |
| 1707dp | EXPANDED | 6 | 底部胶囊 | 1.33 |
| 1920dp | EXPANDED / ULTRA | 6–8 | LEFT / 胶囊 | 1.50 |
| 3008dp | ULTRA | 8 | 胶囊 | 2.00 |
| 3840dp | ULTRA | 8–10 | 胶囊 | 2.00 |

宽高比：`<1.0` 竖屏（底部栏，2 列）；`1.0–1.5` 4:3/5:3（底部胶囊）；`1.5–2.2` 16:9/16:10（左 Dock）；`≥2.2` 超宽（底部胶囊）。

## 组件

`CarScaffold`（状态栏/Dock/内容）、`GlassCard`、`CarPrimaryButton`、`CarGhostButton`、`CarIconButton`、`CarChip`、`CarStatusBar`、`CarDock`、`PageDots`、`SettingRow`、`Avatar`、`CarProgress`、`EmptyState`、`CarBadge`、`CarIcon`（Material Rounded 映射）。

## 预览

`CarPreviewActivity`：分辨率 × DPI × 主题 × 壁纸 × 布局；`--es screen <ROUTE>` 可直接打开页面。
