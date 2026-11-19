# 数据源（M1）

CabinCanvas 的 M1 数据层直接读取 Android 系统数据；无权限时自动降级为示例数据，界面不会空白。

## 媒体

- `MediaSessionManager.getActiveSessions()` 读取当前媒体会话。
- 支持：标题、艺术家、专辑、时长、专辑封面、播放状态、进度。
- 控制：播放/暂停、上一首/下一首、拖动进度。
- 依赖：通知使用权（`NotificationListenerService` 组件）。
- 没有活动会话时：显示内置示例曲目，控制只影响示例。

## 通知

- `CarNotificationListenerService` 监听 `onNotificationPosted/Removed`。
- 解析标题、正文、大文本、应用名、颜色、时间、是否支持快捷回复。
- 快捷回复：`RemoteInput` + `Notification.Action.actionIntent.send()`。
- 过滤：ongoing、媒体传输类通知；最多保留 30 条。
- 依赖：通知使用权。
- 未授权时：通知中心为空，可在设置 → 数据源打开授权。

## 电话与联系人

- 联系人：`ContactsContract.CommonDataKinds.Phone`（READ_CONTACTS）。
- 最近通话：`CallLog.Calls`（READ_CALL_LOG）。
- 通话状态：`TelephonyCallback`（Android 12+）/ `PhoneStateListener`（Android 10–11），需要 READ_PHONE_STATE。
- 接听/挂断：`TelecomManager.acceptRingingCall()` / `endCall()`，需要 ANSWER_PHONE_CALLS。
- 拨号：`ACTION_DIAL`（不需要权限）或 `ACTION_CALL`（CALL_PHONE）。
- 未授权时：使用内置示例联系人，点击呼叫只弹示例来电界面。

## 应用

- `PackageManager.queryIntentActivities(MAIN/LAUNCHER)` 读取真实已安装应用。
- 图标转换为 96px `ImageBitmap`，按分类/名称排序。
- 依赖：`<queries>` 中的 MAIN/LAUNCHER intent；不需要 QUERY_ALL_PACKAGES。

## 权限入口

车机 UI：设置 → 数据源：

- 通知使用权 → `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`
- 电话与联系人 → 运行时权限请求
- 刷新媒体会话

`CarPreviewActivity` 在 `onResume` 重新检查权限，授权返回后自动刷新。

## 降级策略

| 数据 | 有权限 | 无权限 |
|---|---|---|
| 媒体 | 真实会话 | 示例曲目 |
| 通知 | 真实通知 | 空状态 + 模拟按钮 |
| 联系人 | 真实联系人 | 示例联系人 |
| 通话状态 | 真实 Telephony | 示例来电 |
| 应用 | 真实应用 | 内置示例应用 |
