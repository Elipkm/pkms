## Why

The MVP captures plain text notes, but day-to-day mobile capture also needs images, camera photos, links, lightweight categories, and local cleanup controls. These features should keep the product local-first and offline-capable while preserving Obsidian as the readable source of truth.

## What

- Extend Android capture so a note can contain text, one or more image/camera attachments, and links.
- Synchronize attachments to the laptop backend and write Markdown notes that link/embed the copied files in the Obsidian Vault.
- Add delete actions for offline/pending captures and for local recent capture history in the Android app.
- Add optional category/tag suggestions under the `Save note` button with the initial categories `Work`, `Business`, `Leben`, `TODO`, and `Social`.
- Keep visible Obsidian Markdown frontmatter limited to `created_at` and `synced_at`; internal IDs may remain in app/backend sync storage but should not be shown in `.md` files when reasonably possible.
- Add a new Android app icon concept and integrate it as launcher/adaptive icon assets during implementation.

## Non-Goals

- AI classification, automatic tag inference, OCR, image analysis, RAG, and LLM-Wiki workflows.
- Deleting already synced Obsidian Markdown files or Vault attachments from the Android app.
- Cloud storage, multi-device conflict resolution beyond the existing local sync model, or multi-user permissions.
- A full Obsidian mobile browser.

## Success Criteria

- A capture can be saved offline with text plus images/photos, links, or both.
- When the backend is reachable, Android uploads pending captures and the backend writes a Markdown note plus linked attachment files into the Vault.
- The generated Markdown frontmatter contains only `created_at` and `synced_at`.
- Optional categories can be selected below `Save note`, are not required, and are configurable from app resources/properties.
- Pending/offline captures can be deleted before sync, including their local attachment files.
- Recent captures can be removed from the Android app history without deleting Obsidian files.
- The Android launcher uses the new PKMS icon assets after implementation.
