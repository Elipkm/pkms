Product Summary

## Product Vision

A personal, local-first knowledge system with two main capabilities:

1. **Fast mobile capture**
2. **AI-supported knowledge management in Obsidian**

The goal is to capture notes quickly on Android, synchronize them into a central Obsidian Vault and later organize, connect and query the knowledge using tools such as Codex, LLM-Wiki workflows and optional RAG.

## Architecture

### Android app

Native Android app built with:

* Kotlin
* Jetpack Compose
* Room database
* WorkManager
* HTTP REST API

Responsibilities:

* capture notes, tasks, links and ideas
* work fully offline
* store pending captures locally
* synchronize automatically when the laptop is reachable
* show synchronization status
* optionally display selected knowledge views

The Android app contains no classification or AI business logic.

### Laptop

The laptop is the local server and central knowledge system.

Components:

* Spring Boot backend
* Obsidian
* central Obsidian Vault
* optional local database for sync metadata
* Codex and other AI tools

Spring Boot handles:

* HTTP communication
* duplicate protection
* synchronization
* Markdown file creation
* selected mobile read views

## Knowledge Flow

```text
Android App
    ↓ offline queue
Spring Boot Sync
    ↓
Obsidian Vault / Inbox
    ↓
Codex, LLM-Wiki or automation
    ↓
Structured and connected knowledge
```

## Obsidian Vault

Obsidian remains the source of truth.

Mobile notes initially enter an `Inbox` folder with stable metadata such as:

* unique ID
* creation date
* source
* synchronization status
* original content

AI tools can later:

* classify notes
* add tags and metadata
* move notes
* create links
* consolidate information
* generate canonical wiki pages
* provide semantic search or RAG

## Deployment

Everything initially runs locally:

* Android app on the phone
* Spring Boot on the laptop
* Obsidian Vault on the laptop
* synchronization over HTTP in the home network

No VPS, Raspberry Pi or cloud hosting is required for the first version.

## First MVP

The first milestone is complete when:

> A note created offline on Android is automatically written as a valid Markdown file into the Obsidian Inbox when the laptop becomes reachable.

AI classification, LLM-Wiki workflows and RAG are separate later extensions.