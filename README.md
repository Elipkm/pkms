# PKMS Obsi

Local-first personal knowledge capture for Android and Obsidian.

The project has two main parts:

- `app/`: Android app built with Kotlin, Jetpack Compose, Room, WorkManager, and OkHttp.
- `backend/PkmsBackend/`: Spring Boot backend that writes synced captures into an Obsidian Inbox.

## Common Workflow

Start the backend:

```powershell
cd backend\PkmsBackend
.\mvnw.cmd spring-boot:run
```

Build a debug APK for the Android emulator:

```powershell
cd app
.\gradlew.bat assembleDebug
```

Build a debug APK for a real phone on the same Wi-Fi as the laptop:

```powershell
cd app
.\gradlew.bat assembleDebug -PpkmsBackendBaseUrl=http://192.168.0.218:8080
```

Replace `192.168.0.218` with the laptop's current IPv4 address. On Windows, `ipconfig` shows it under the active Wi-Fi/Ethernet adapter.

The debug APK is written to:

```text
app\app\build\outputs\apk\debug\app-debug.apk
```

## Verification

Run backend tests:

```powershell
cd backend\PkmsBackend
.\mvnw.cmd test
```

Run the Android build, unit tests, and lint:

```powershell
cd app
.\gradlew.bat build
```

## Important Configuration

Android backend URL:

```powershell
-PpkmsBackendBaseUrl=http://<laptop-ip>:8080
```

Backend Vault properties:

```properties
pkms.vault.inbox-path=./vault/Inbox
pkms.vault.attachment-directory=_attachments
pkms.sync.index-path=./data/synced-captures.properties
```

Production backend overrides live in:

```text
backend\PkmsBackend\src\main\resources\application-prod.properties
```

## Capture Behavior

The Android app can save captures offline with text, links, images, camera photos, and optional categories. Pending captures sync automatically through WorkManager when the backend is reachable.

Synced Obsidian Markdown frontmatter intentionally contains only:

```yaml
created_at: "..."
synced_at: "..."
```

Internal capture IDs are still used for duplicate protection, but they are not written as visible Markdown frontmatter.
