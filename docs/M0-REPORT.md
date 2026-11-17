# M0 报告（模板）

> 真车测试后填写。日志请先脱敏（手机号、地址、账号）。

## 环境

| 项 | 值 |
|---|---|
| 手机 | Pixel 5 / Android 14 |
| 引擎 | `com.baidu.carlife.oppo` 8.8.4 (273) |
| 车机 | 869 8 英寸 / 固件： |
| 分辨率 | 800×480 / 1024×600 / 1280×720 / 其他： |
| 连接 | USB / 无线（若支持） |

## 路线 C：百度引擎

- [ ] `CastService.onBind` 出现
- [ ] `manager.setCastCallback` 出现
- [ ] `callback code=1 ok=true`
- [ ] `manager.onTransact code=1`（prepareCast）
- [ ] VirtualDisplay 创建成功，flags：full / public / private
- [ ] 车机出现探针画面
- [ ] `view touch` 命中
- [ ] `remote touch` 命中
- [ ] `remote audio` 有 PCM

失败点与日志摘录：

```

```

## 路线 B：headswap DESKTOP

- [ ] 无线 ADB 激活成功
- [ ] DESKTOP 模式出画面
- [ ] 触摸命中
- [ ] 音频出声
- [ ] 拔插 10 次恢复次数：__/10

失败点与日志摘录：

```

```

## 结论

- 选择：C / B / 混合
- 理由：
- 下一步：
