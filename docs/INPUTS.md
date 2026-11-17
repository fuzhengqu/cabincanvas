# 输入 APK 指纹

所有逆向结论都以这三个文件为准。构建/测试前先核对 SHA-256，避免版本记错。

| 文件 | 包名 | versionName | versionCode | SHA-256 |
|---|---|---|---:|---|
| `Car+ 车联_v17.32.4.apk` | `com.oplus.ocar` | 17.32.4 | 17032004 | `A348435F181B5B4523CA3DF0677271D2A67837973ED3C6828D48ACDBE05B42A3` |
| `智能车载百度CarLife+组件_v8.8.4(273).apk` | `com.baidu.carlife.oppo` | 8.8.4 | 273 | `BE7E77D09840F68DD8C040410DB2FBE5644153963BB95BA1B96190D84E621CF4` |
| `CarLink_v17.3.0(1703000).apk` | `com.heytap.opluscarlink` | 17.3.0 | 1703000 | `669FD9864FACE99A0D9BAEA193A0A8D2579AD1D17E9AD48E0B6D3C33934221D4` |

## 结论边界

- `com.oplus.ocar`：ColorOS 特权应用，不可移植；只做 UI/交互参考。
- `com.baidu.carlife.oppo`：M0 路线 C 的实际投屏引擎；`CarlifeConnectService` 导出且无 permission。
- `com.heytap.opluscarlink`：ICCOA/数字钥匙，当前不纳入。

## 复核命令

```powershell
Get-FileHash -Algorithm SHA256 "Car+ 车联_v17.32.4.apk"
& "D:\Android-SDK\build-tools\37.0.0\aapt2.exe" dump badging "Car+ 车联_v17.32.4.apk"
```
