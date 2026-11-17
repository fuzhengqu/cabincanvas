# 安全与隐私

## 不在仓库中出现的内容

- `local.properties`
- `keystore.properties`
- `*.jks` / `*.keystore`
- `secrets.properties` / `.env`
- API Key、Token、账号密码
- 百度/OPPO 的 APK、提取资产、用户日志
- 带手机号、地址、通知内容的截图/日志

## 本地密钥

Release 签名使用本机 `keystore.properties`（已 gitignore），keystore 放在仓库外：

```
C:\Users\<user>\.android-keys\cabincanvas-release.jks
```

不要把这个文件上传到任何地方；需要备份时使用加密存储。

## GitHub Actions

签名密钥和 API Key 只放在 **Actions Secrets**，不要写进 workflow 文件。构建日志不得打印密钥内容。

## 日志

- 默认只保存在本机 `files/probe.log`。
- 分享前请检查是否包含个人信息。
- 代码中不得记录密码、Token、完整手机号。

## 漏洞报告

请通过 GitHub Security Advisory 私下报告，不要在公开 Issue 中贴密钥或个人信息。

## 支持范围

仅支持本仓库最新 release。旧版本（如 v0.01 预发布）可能包含未修复的问题。
