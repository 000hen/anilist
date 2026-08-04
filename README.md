# Anilist

An Android app for this season's anime broadcast schedule.

It opens on today and reads forward through the rest of the week: every title airing, in the order
it airs, with the time it starts. Tap a title for its artwork, a synopsis, where to watch it and its
official links. Swipe a row either way to star it, and press the star in the app bar to hide
everything you have not. A row that has already gone out today is tinted, and one still to come
flips over on its own while you are looking at the list.

> Schedule data is scraped from the public pages of [YourAnimes](https://youranimes.tw). There is no
> official API, so changes to the website may cause the schedule to stop working without notice. All
> listings, images, and descriptions belong to their respective owners. Please use this app as a
> convenient viewer and refer to the original source when accuracy matters.

## Installation

Download `app-release.apk` from the [latest release](/releases/latest) and open it on the
device, allowing installation from your browser or file manager when Android asks. Android 8.0 or
newer is required.

Building it yourself works too — see below.

## Development

You will need the Android SDK and a JDK 11 or newer; installing Android Studio gets you both.

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

The app needs a network connection to load anything, so an emulator or device without one shows an
empty week.

`./gradlew assembleRelease` produces an unsigned APK under `app/build/outputs/apk/release/`. Signing
it is left to Android Studio's *Build → Generate Signed App Bundle / APK*.

## License

Released under the [MIT License](LICENSE).
