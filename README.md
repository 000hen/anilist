# Anilist

本專案是一款可依年份與季度瀏覽動畫播出時間表的 Android 應用程式。

[English version](README.en.md)

應用程式啟動時會顯示當季番表；點選應用程式列中的季度即可瀏覽 2015 年至明年的季度。
每一季都會以今天開始的一週順序列出作品與播出時間。點選作品即可查看圖片、簡介、
收看平台與官方連結；下拉可重新載入，載入失敗時也能重試並查看錯誤詳細資訊。

向左或向右滑動項目可將作品加入收藏；按下應用程式列中的星號，則可隱藏所有未收藏的作品。
瀏覽當季時，今天已播出的項目會套用不同色彩，尚未播出的項目也會在播出時間到達時自動變色。

> 播出時間表資料擷取自 [YourAnimes](https://youranimes.tw) 的公開頁面。該網站並未提供
> 官方 API，因此網站改版可能導致時間表在未預警的情況下無法載入。所有節目資訊、圖片與
> 內容簡介均屬各自權利人所有。本應用程式僅供便利查閱；需要確認資料正確性時，請以原始來源為準。

## 安裝

請從[最新版本](https://github.com/000hen/anilist/releases/latest)下載適合裝置的 APK，
開啟檔案後，依 Android 提示允許瀏覽器或檔案管理員安裝應用程式。裝置需執行 Android 8.0 或更新版本。

- `anilist-arm64-v8a.apk` — 多數現行 Android 手機與平板
- `anilist-armeabi-v7a.apk` — 較舊的 32 位元 ARM 裝置
- `anilist-x86_64.apk` 或 `anilist-x86.apk` — Intel 裝置與模擬器

每個版本也會附上 `SHA256SUMS.txt`。請在下載檔案所在的目錄執行以下指令以驗證檔案：

```sh
sha256sum --check SHA256SUMS.txt
```

你也可以自行建置，方式請見下方說明。

## 開發

你需要 Android SDK、Android NDK、JDK 21、Rust 與 `cargo-ndk`。請先初始化 Rust submodule，
再安裝四種 Android Rust targets：

```sh
git submodule update --init --recursive
cargo install cargo-ndk --locked
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
```

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

應用程式需要網路連線才能載入內容；未連上網路時會顯示載入失敗畫面，並提供重試與錯誤詳細資訊。

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

## Rust 與 Android 的分工

`app` 負責 UI、季度選擇和本地時區的時間表投影；`source` 模組透過 UniFFI 使用 Rust
產生的資料模型與 `NativeAnimeSource`。Rust 負責來源網址、請求組合、解析與平台資料對應，
Android 則以 `OkHttpTransport` 執行來源回傳的通用 `HttpRequest`，因此切換季度時可取消
進行中的請求。Rust 端需要自行取得資料時，則可透過共用的 `HttpClient` trait 使用 reqwest。

Android 使用 `--no-default-features --features all-sources`，不包含 Rust HTTP、Tokio
或時區資料庫。建置前需初始化 `rust/anilist-rs` submodule，安裝 Android NDK、
`cargo-ndk` 及四種 Android Rust targets。FFI 介面變更後，請依
[開發說明](README.en.md#development) 重新產生 Kotlin bindings。
