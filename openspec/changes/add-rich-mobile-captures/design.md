## Overview

This change upgrades the current plain-note capture path into a richer local-first capture model. Android remains the offline queue. The Spring Boot backend remains a thin sync and Markdown-writing layer with no AI logic.

The design keeps user-visible Obsidian metadata minimal: Markdown frontmatter includes only `created_at` and `synced_at`. Sync IDs, duplicate indexes, attachment bookkeeping, and local capture status remain implementation details in Room and backend sync metadata.

## Capture Model

Android should model a capture as:

- a stable local/internal UUID for sync idempotency
- optional text content
- zero or more links
- zero or more image attachments
- zero or more optional categories selected from configured suggestions
- local status such as pending or synced
- `createdAt` and nullable `syncedAt`

A capture is valid when it has at least one of text, link, or attachment.

Categories are user-selected suggestions, not AI classifications. To respect the metadata requirement, selected categories should be rendered into the Markdown body as Obsidian-compatible tags or a small body line, not as extra YAML frontmatter fields.

## Android

Extend the Room schema with attachment and link storage. Image binaries should be stored in app-private file storage, with Room rows storing local file URI/path, MIME type, display name, size when available, and capture ID. Links can be stored as normalized strings associated with the capture.

The Compose capture screen should include:

- text input for the note body
- link input and/or shared URL ingestion via Android share intent
- image picker for existing images
- camera capture for new photos
- `Save note` button
- optional category chips directly below `Save note`
- recent captures list with delete actions
- pending capture delete actions for captures that have not synced yet

Deleting a pending capture removes the Room capture row, link rows, attachment rows, and local attachment files. Deleting a synced recent capture removes only local app history/cache and must not call the backend to delete Vault content.

The initial category suggestions are `Work`, `Business`, `Leben`, `TODO`, and `Social`. They should live in Android resources or another simple app-level configuration point so they can be changed without touching sync logic.

## Backend

Add a rich capture upload path that can receive capture JSON plus attachment files. A multipart request is the preferred shape because it avoids base64 overhead and keeps image upload straightforward:

- JSON part: internal capture ID, text, links, selected categories, `createdAt`
- file parts: image attachments with stable client-side attachment IDs or filenames

The existing plain-note endpoint can remain for compatibility, but rich capture sync should use the new upload path once Android supports attachments.

For each new capture ID, the backend should:

- validate that the capture has text, at least one link, or at least one attachment
- copy images into a configured Vault attachment location, preferably under the Inbox such as `Inbox/_attachments/<capture-id>/`
- write one Markdown file in the configured Inbox
- embed or link copied images from the note using relative Markdown links
- render links as a simple Markdown list when present
- render selected categories as body tags or a body line, outside frontmatter
- record internal sync metadata in the existing backend index or an evolved equivalent

Duplicate capture IDs should return the previously written result without creating duplicate Markdown files or duplicate attachments.

## Markdown Format

Generated Markdown should use this visible shape:

```markdown
---
created_at: "2026-07-19T10:15:30Z"
synced_at: "2026-07-19T10:16:02Z"
---

Capture text goes here.

Tags: #Work #TODO

Links:
- https://example.com

Images:
![](_attachments/<internal-folder>/image-1.jpg)
```

The folder name may include the internal capture ID to avoid collisions, but the ID should not appear as a frontmatter property.

## App Icon

The icon concept is stored as `assets/pkms-app-icon-concept.svg` in this change. It uses a rounded square, a note/card shape, and a capture spark to represent fast local capture into knowledge. During implementation, convert it into Android adaptive icon resources and replace the current launcher assets.

## Error Handling

Android should keep failed uploads pending for WorkManager retry. Attachments must stay available locally while a capture is pending. If an attachment file is missing before sync, Android should surface the capture as failed/local-problem rather than uploading an incomplete capture silently.

Backend validation should reject empty captures, malformed timestamps, unsupported file types, and unsafe filenames. Backend file writes should be atomic enough that a failed attachment copy does not leave a completed Markdown note pointing at missing files.

## Migration

The existing Room database version must be incremented. Existing plain text captures should migrate into the new capture schema with no attachments, no links, and no selected categories.

Existing synced Markdown files are not rewritten.
