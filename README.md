# Anilist

本專案是一款提供當季動畫播出時間表的 Android 應用程式。

[English version](README.en.md)

應用程式會從今天開始，依播出順序列出本週剩餘時間即將播出的所有作品與播出時間。
點選作品即可查看圖片、簡介、收看平台與官方連結。向左或向右滑動項目可將作品加入收藏；
按下應用程式列中的星號，則可隱藏所有未收藏的作品。今天已播出的項目會套用不同色彩，
尚未播出的項目也會在播出時間到達時自動變色。

> 播出時間表資料擷取自 [YourAnimes](https://youranimes.tw) 的公開頁面。該網站並未提供
> 官方 API，因此網站改版可能導致時間表在未預警的情況下無法載入。所有節目資訊、圖片與
> 內容簡介均屬各自權利人所有。本應用程式僅供便利查閱；需要確認資料正確性時，請以原始來源為準。

## 安裝

請從[最新版本](/releases/latest)下載適合裝置的 APK，開啟檔案後，依 Android 提示允許瀏覽器
或檔案管理員安裝應用程式。裝置需執行 Android 8.0 或更新版本。

- `anilist-arm64-v8a.apk` — 多數現行 Android 手機與平板
- `anilist-armeabi-v7a.apk` — 較舊的 32 位元 ARM 裝置
- `anilist-x86_64.apk` 或 `anilist-x86.apk` — Intel 裝置與模擬器

每個版本也會附上 `SHA256SUMS.txt`。請在下載檔案所在的目錄執行以下指令以驗證檔案：

```sh
sha256sum --check SHA256SUMS.txt
```

你也可以自行建置，方式請見下方說明。

## 開發

你需要 Android SDK 與 JDK 21；安裝 Android Studio 即可取得兩者。

複製儲存庫後，在專案根目錄建立 `local.properties`，指定 Android SDK 的位置：

```properties
sdk.dir=/path/to/android/sdk
```

你可以在 Android Studio 中開啟並執行專案，也可以使用命令列建置。在 POSIX shell 中使用
`./gradlew`，在 PowerShell 中則使用 `gradlew.bat`：

```sh
./gradlew assembleDebug              # 建置偵錯版 APK
./gradlew installDebug               # 建置並安裝至已連線的裝置
./gradlew lint                       # 執行靜態檢查
./gradlew testDebugUnitTest          # 執行單元測試
./gradlew connectedDebugAndroidTest  # 在已連線的裝置上執行儀器化測試
```

應用程式需要網路連線才能載入內容，因此未連上網路的模擬器或裝置只會顯示空白的一週。

執行 `./gradlew assembleRelease -PreleasePerAbi=true` 會在
`app/build/outputs/apk/release/` 下產生四個未簽署、分別適用於不同架構的 APK；若省略該
屬性，本機建置則維持慣例，產生單一 APK。若要簽署輸出檔案，請提供下列四個環境變數，並加上
`--no-configuration-cache` 執行建置：

- `RELEASE_KEYSTORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

發布 GitHub Release 時，`.github/workflows/release.yml` 會簽署四個 APK、驗證簽章、建立
`SHA256SUMS.txt`，並將這五個檔案附加至該版本。請先設定下列儲存庫密鑰：

- `RELEASE_KEYSTORE_BASE64` — 以 Base64 編碼的完整金鑰庫
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

在 PowerShell 中可用以下指令建立 Base64 值：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

每個版本都必須使用相同的簽署憑證；若更新版 APK 使用不同憑證簽署，Android 將拒絕把它安裝為
既有應用程式的更新。

## 授權條款

本專案採用 [MIT License](LICENSE) 授權。
