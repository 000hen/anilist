# Anilist

An Android app for browsing anime broadcast schedules by year and season.

It opens on the current season. Press the season in the app bar to browse seasons from 2015 through
next year; each schedule is ordered as a week starting from today. Tap a title for its artwork, a
synopsis, where to watch it, and its official links. Pull to refresh, or retry a failed load and
open
its diagnostic details.

Swipe a row either way to star it, and press the star in the app bar to hide everything you have
not. While viewing the current season, a title that has already aired today is tinted, and one still
to come changes automatically when its airtime arrives.

> Schedule data is scraped from the public pages of [YourAnimes](https://youranimes.tw). There is no
> official API, so changes to the website may cause the schedule to stop working without notice. All
> listings, images, and descriptions belong to their respective owners. Please use this app as a
> convenient viewer and refer to the original source when accuracy matters.

## Installation

Download the APK for your device from the
[latest release](https://github.com/000hen/anilist/releases/latest), then open it and allow
installation
from your browser or file manager when Android asks. Android 8.0 or newer is required.

- `anilist-arm64-v8a.apk` — most current Android phones and tablets
- `anilist-armeabi-v7a.apk` — older 32-bit ARM devices
- `anilist-x86_64.apk` or `anilist-x86.apk` — Intel devices and emulators

Each release also includes `SHA256SUMS.txt`. From the directory containing the downloaded files,
verify them with `sha256sum --check SHA256SUMS.txt`.

Building it yourself works too — see below.

## Development

You will need the Android SDK/NDK, JDK 21, Rust, and `cargo-ndk`. Initialize the Rust
submodule with `git submodule update --init --recursive`, then install `cargo-ndk` and the
Rust targets for `aarch64-linux-android`, `armv7-linux-androideabi`, `i686-linux-android`,
and `x86_64-linux-android`.

`app` owns the UI, season selection, and local-time schedule projection. The `source` module uses
Rust's generated models and `NativeAnimeSource` through UniFFI. Rust owns source URLs, request
construction, parsing, and vendor mapping; Android executes the source-independent `HttpRequest`
with `OkHttpTransport`, so changing seasons can cancel an in-flight call. Rust-side fetchers can
instead use the shared `HttpClient` trait with reqwest. Android builds use `--no-default-features
--features all-sources`, excluding Rust HTTP, Tokio, and timezone databases.

After changing the Rust FFI API, regenerate the checked-in Kotlin bindings from a matching
parser-only native library (run from `rust/anilist-rs`; use `.so`/`.dylib` on Linux/macOS):

```sh
cargo build -p anilist-ffi --no-default-features --features all-sources
cargo run -p uniffi-bindgen -- generate --library target/debug/anilist.dll --language kotlin --config ../../source/uniffi.toml --out-dir ../../source/src/main/java
```

Clone the repository, then tell the build where your SDK lives by creating `local.properties` in the
project root:

```properties
sdk.dir=/path/to/android/sdk
```

Open the project in Android Studio and run it, or drive the build from the command line —
`./gradlew` from a POSIX shell, `gradlew.bat` from PowerShell:

```sh
./gradlew assembleDebug              # build a debug APK
./gradlew installDebug               # build and install it on a connected device
./gradlew lint                       # static checks
./gradlew testDebugUnitTest          # unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests, on a connected device
```

The app needs a network connection to load content. An offline emulator or device shows the load
failure screen with retry and diagnostic-detail actions.

`./gradlew assembleRelease -PreleasePerAbi=true` produces four unsigned, architecture-specific APKs
under `app/build/outputs/apk/release/`; without that property, local builds retain the conventional
single APK. To sign the output, provide all four environment variables below and run the build with
`--no-configuration-cache`:

- `RELEASE_KEYSTORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Publishing a GitHub Release runs `.github/workflows/release.yml`, signs the four APKs, verifies
their
signatures, creates `SHA256SUMS.txt`, and attaches all five files to that release. Configure these
repository secrets first:

- `RELEASE_KEYSTORE_BASE64` — the complete keystore encoded as Base64
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

On PowerShell, create the Base64 value with:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

Keep using the same signing certificate for every release; Android will reject an APK signed with a
different certificate as an update to an installed copy.

## License

Released under the [MIT License](LICENSE).
