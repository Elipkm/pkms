## Why

The first product milestone is an end-to-end local-first capture loop: a note created while the Android device is offline must be persisted locally and later synced into the Obsidian Inbox when the laptop backend is reachable.

## What

- Add a Spring Boot capture API that accepts mobile notes and writes each note as a Markdown file in a configured Obsidian Inbox directory.
- Add duplicate protection so repeated sync attempts for the same capture ID do not create multiple files.
- Replace the Android placeholder screen with a Compose capture surface backed by Room.
- Store notes locally as pending captures and use WorkManager to retry sync when connectivity is available.
- Show basic sync status in the Android UI.

## Non-Goals

- AI classification, tagging, semantic search, RAG, and wiki generation.
- Cloud sync, authentication, or multi-user support.
- Rich Obsidian browsing from mobile.
- Attachment/image capture.

## Success Criteria

- A note can be saved on Android without network access.
- Pending notes remain stored locally until synced.
- When the backend is reachable, Android sends pending notes automatically.
- The backend writes a valid Markdown file under the configured Inbox.
- Retrying a previously synced capture ID is idempotent.
