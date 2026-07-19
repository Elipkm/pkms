## 1. Backend

- [x] 1.1 Add configurable Vault attachment path behavior for rich captures.
- [x] 1.2 Add rich capture request models for text, links, selected categories, timestamps, and attachment metadata.
- [x] 1.3 Add multipart upload endpoint for rich captures while keeping the existing plain-note endpoint compatible.
- [x] 1.4 Copy uploaded images into the Vault attachment folder and generate relative Markdown links.
- [x] 1.5 Update Markdown generation so visible frontmatter contains only `created_at` and `synced_at`.
- [x] 1.6 Keep internal duplicate protection without exposing capture IDs in Markdown frontmatter.
- [x] 1.7 Add backend tests for text+image captures, link captures, category rendering, duplicate upload idempotency, and minimal frontmatter.

## 2. Android Data And Sync

- [x] 2.1 Add Room migration for rich captures, links, attachments, optional categories, and local deletion support.
- [x] 2.2 Store selected/gallery images and camera photos in app-private storage until sync completes or the capture is deleted.
- [x] 2.3 Extend repository methods to save captures containing text, links, images, and optional categories.
- [x] 2.4 Extend WorkManager sync to upload multipart rich captures and mark successful captures as synced.
- [x] 2.5 Add delete operations for pending captures and synced recent-history entries with correct local file cleanup.
- [x] 2.6 Add handling for missing local attachment files before sync.

## 3. Android UI

- [x] 3.1 Add image picker and camera capture actions to the Compose capture screen.
- [x] 3.2 Add link input and Android share-intent handling for incoming URLs.
- [x] 3.3 Add optional category chips under the `Save note` button using configurable suggestions: `Work`, `Business`, `Leben`, `TODO`, `Social`.
- [x] 3.4 Show rich recent captures with basic indicators for text, links, images, selected categories, and sync status.
- [x] 3.5 Add delete controls for pending captures and local recent-history entries.
- [x] 3.6 Keep capture saving ergonomic when only a link, only an image, only text, or a combination is present.

## 4. App Icon

- [x] 4.1 Review `assets/pkms-app-icon-concept.svg`.
- [x] 4.2 Convert the concept into Android adaptive icon foreground/background resources.
- [x] 4.3 Replace current launcher icons and verify the icon renders correctly on modern Android launchers.

## 5. Verification

- [x] 5.1 Run backend tests.
- [x] 5.2 Run Android unit tests or build.
- [ ] 5.3 Manually verify offline rich capture, sync retry, Markdown output, pending delete, recent-history delete, and launcher icon rendering.
