# Lido

**Save it now, read it later — offline, private, and yours.** Lido is an open-source read-it-later app for Android and iOS. Save articles from any app, read them in a clean distraction-free view without internet, highlight and take notes, and keep everything in files you own. No account and no server.

> 🇧🇷 *App de "ler depois" open source, alternativa ao Pocket: salva artigos, lê offline, destaques e notas, sem conta e sem servidor.*

> **Status:** early development. See [ROADMAP.md](ROADMAP.md).

## Why

Pocket, the most popular read-it-later service, was shut down by Mozilla in 2025. The good remaining options are paid subscriptions or require running your own server. Many people just want something simple:

- Share a link from the browser or any app → it's saved.
- Read it later on the subway, offline, without ads and pop-ups.
- Highlight what matters.
- Not lose everything again when a company changes its plans.

Lido stores your library **on your device** and can optionally sync through **a folder you choose** (iCloud Drive, Google Drive, Dropbox, Syncthing…) using plain, documented files. If Lido disappears tomorrow, your articles and highlights are still readable files.

## Features (planned)

- **Save from anywhere** — share sheet / share intent, plus paste a URL
- **Clean reader view** — article extraction, adjustable font, size, line height, themes (light, sepia, dark)
- **Fully offline** — text and images stored locally
- **Highlights & notes** — export to Markdown
- **Tags, archive, favorites, search** (full-text, on-device)
- **Reading progress** and estimated reading time
- **Listen** — text-to-speech using the platform's built-in voices
- **Import** — Pocket CSV export, Instapaper CSV, browser bookmarks HTML, plain URL lists
- **Export** — JSON + Markdown, any time
- **Optional folder sync** — no Lido server, no account

## Who it's for

Former Pocket users, heavy readers of newsletters and long-form articles, and anyone who prefers local-first, open-source tools.

## Built with

- **Kotlin Multiplatform** + **Compose Multiplatform** (Android and iOS)
- **Swift / SwiftUI** for the iOS Share Extension (system requirement)
- **Ktor** — fetching pages
- **Klead** (KMP, Apache 2.0) — article content extraction; Readability4J as JVM/Android fallback under evaluation
- **SQLDelight** with FTS — local library and full-text search
- **kotlinx.serialization** — sync files, import/export
- **Koin** — dependency injection
- Platform TTS (AVSpeechSynthesizer / Android TextToSpeech)
- CI: GitHub Actions; extraction quality tested against a corpus of locally generated sample HTML pages

## Architecture

```
androidApp/          Android application (thin host for the shared UI)
composeApp/          Compose Multiplatform UI (library, reader, settings)
iosApp/              Xcode host + ShareExtension (Swift)
shared/              Non-UI code; the areas below start as packages
  capture/           URL intake, fetching, extraction pipeline
  reader/            Reader model, typography, progress
  library/           SQLDelight schema, tags, search (FTS)
  highlights/        Highlight anchoring and notes
  sync/              Folder-based sync: one JSON file per item + images, merge rules
  importexport/      Pocket/Instapaper/bookmarks importers, JSON/Markdown exporters
```

### Sync format (draft)

```
Lido/
  items/<id>.json        metadata, tags, progress, highlights, updatedAt
  content/<id>.html      extracted, sanitized article
  images/<id>/...        downloaded images
```

Conflicts are resolved per field with last-writer-wins timestamps; highlights merge as sets. The format is documented in `docs/` so other tools can read it.

## Getting started

Requirements: JDK 17+, Android Studio with the Android 37 SDK platform, Xcode 16+ (iOS, macOS only).

```bash
git clone https://github.com/isaquefontinele/lido.git
cd lido
./gradlew :androidApp:installDebug   # Android
open iosApp/iosApp.xcodeproj         # iOS
./gradlew :shared:allTests
```

## Privacy

- No accounts, analytics or trackers.
- Lido only contacts the websites you save, to download their content.
- Saved content is for your personal offline reading and is never redistributed by the app.

## Contributing

Importers for other services and extraction fixes for specific sites are great contributions. See [ROADMAP.md](ROADMAP.md) and [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md).

## License

[MIT](LICENSE) © 2026 Isaque Sales Domingos Fontinele. Lido is not affiliated with Mozilla or Pocket.
