# 发布流程

## 版本规则

- `versionName`：`0.01` → `0.02` → … → `0.99` → `1.00`
- `versionCode = major × 100 + minor`
  - 0.01 → 1
  - 0.10 → 10
  - 0.99 → 99
  - 1.00 → 100
- 普通功能/修复/优化 +0.01；重大功能/架构/视觉/破坏性变更才允许跳号。
- `versionCode` 必须单调递增，不能复用。

## 发版前

1. 改 `app/build.gradle.kts`、`probe/c/build.gradle.kts`、`docs/releases/vX.XX.md` 中的版本号。
2. 跑：
   ```powershell
   .\gradlew.bat :app:test :app:lintDebug :app:assembleRelease :probe:c:assembleRelease
   ```
3. 检查 `git status`，确认没有 `local.properties`、keystore、APK、日志。
4. 检查 APK：
   ```powershell
   & "D:\Android-SDK\build-tools\37.0.0\aapt2.exe" dump badging app\build\outputs\apk\release\app-release.apk
   & "D:\Android-SDK\build-tools\37.0.0\apksigner.bat" verify --print-certs app\build\outputs\apk\release\app-release.apk
   ```

## 提交与 tag

```powershell
git add .
git commit -m "chore(release): vX.XX"
git tag vX.XX
git push origin main
git push origin vX.XX
```

## GitHub Release

```powershell
gh release create vX.XX `
  --title "vX.XX" `
  --notes-file docs/releases/vX.XX.md `
  app\build\outputs\apk\release\app-release.apk `
  probe\c\build\outputs\apk\release\c-release.apk
```

预发布加 `--prerelease`。

## 签名

- 本地：`keystore.properties`（gitignore）指向仓库外的 keystore。
- CI：使用 Actions Secrets，禁止把 keystore 写入仓库。
- 首次发布后不要更换签名；否则无法覆盖升级。
