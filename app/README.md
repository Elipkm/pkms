# PKMS Android App

Native Android capture app for local-first notes, links, images, camera photos, and optional categories.

![PKMS Android app mockup](../docs/assets/android-app-mockup.png)

## Open In Android Studio

Open this `app/` folder as the Android Studio project. The repository includes the Gradle wrapper, version catalog, module build files, Android resources, and source code needed to sync and build the app.

The first sync or command-line build downloads Gradle, Android Gradle Plugin, Kotlin, AndroidX, Room, WorkManager, Compose, and OkHttp dependencies.

## Prerequisites

- Android Studio with the Android SDK installed
- Android SDK platform 36
- A network connection for the first dependency download
- The backend running on the emulator host or on the same network as the phone

## Build APK

For the Android emulator:

```powershell
.\gradlew.bat assembleDebug
```

For a real phone, pass the laptop backend URL:

```powershell
.\gradlew.bat assembleDebug -PpkmsBackendBaseUrl=http://192.168.0.218:8080
```

Replace `192.168.0.218` with the laptop's current IPv4 address. `10.0.2.2` only works from the Android emulator.

APK output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

When this README is viewed from the repository root, the same APK path is `app\app\build\outputs\apk\debug\app-debug.apk`.

## Full Verification

```powershell
.\gradlew.bat build
```

This runs the Android build, unit tests, and lint.

## Runtime Notes

- The app stores pending captures locally in Room.
- Selected gallery images and camera captures are copied into app-private storage until sync or local deletion.
- WorkManager uploads pending captures when network is available.
- Text-only captures can fall back to the older `/api/captures/notes` endpoint.
- Rich captures use `/api/captures/rich` and require the updated backend.

## Configuration

The backend URL is compiled into `BuildConfig.PKMS_BACKEND_BASE_URL` from:

```powershell
-PpkmsBackendBaseUrl=http://<laptop-ip>:8080
```

Category chips are configured in:

```text
app\src\main\res\values\strings.xml
```

Current defaults:

```text
Work, Business, Leben, TODO, Social
```
