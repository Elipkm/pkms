# PKMS Backend

Spring Boot backend for receiving Android captures and writing Markdown files into the configured Obsidian Inbox.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

With the production profile:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

## Test

```powershell
.\mvnw.cmd test
```

## Properties

Default properties live in:

```text
src\main\resources\application.properties
```

Important values:

```properties
pkms.vault.inbox-path=./vault/Inbox
pkms.vault.attachment-directory=_attachments
pkms.sync.index-path=./data/synced-captures.properties
```

Production overrides live in:

```text
src\main\resources\application-prod.properties
```

## Endpoints

Plain text capture endpoint:

```text
POST /api/captures/notes
Content-Type: application/json
```

Rich capture endpoint for text, links, categories, and image attachments:

```text
POST /api/captures/rich
Content-Type: multipart/form-data
```

The multipart request contains:

- `capture`: JSON part with `id`, `content`, `links`, `categories`, and `createdAt`
- `files`: optional image file parts

## Markdown Output

Markdown files are written into `pkms.vault.inbox-path`. Image attachments are copied below:

```text
<Inbox>\_attachments\<capture-id>\
```

Visible Markdown frontmatter is intentionally minimal:

```yaml
created_at: "..."
synced_at: "..."
```

The backend still keeps an internal sync index at `pkms.sync.index-path` for duplicate protection.
